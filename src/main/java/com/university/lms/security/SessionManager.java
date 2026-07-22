package com.university.lms.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import com.university.lms.entity.User;
import com.university.lms.entity.UserSession;
import com.university.lms.repository.SessionRepository;

/**
 * Creates and validates {@link UserSession} rows. The session token is a 256-bit random value,
 * base64url-encoded — opaque to the client, never a JWT (no need for stateless verification in
 * a single desktop client talking to one trusted database).
 *
 * <p>Only a SHA-256 hash of the token is ever persisted ({@link UserSession#getToken()} despite
 * its name holds the hash, not the raw value) — a database dump alone can't be replayed as a
 * live session or remember-me credential. The raw token exists only in memory for the length of
 * one request/response and in the client's local RememberMeStore.
 */
public final class SessionManager {

    private final SessionRepository sessionRepository;
    private final long idleTimeoutMinutes;
    private final SecureRandom secureRandom = new SecureRandom();

    public SessionManager(SessionRepository sessionRepository, long idleTimeoutMinutes) {
        this.sessionRepository = sessionRepository;
        this.idleTimeoutMinutes = idleTimeoutMinutes;
    }

    public UserSession createSession(User user, String ipAddress, boolean extendedLifetime) {
        String rawToken = generateToken();
        LocalDateTime now = LocalDateTime.now();
        long lifetimeMinutes = extendedLifetime ? idleTimeoutMinutes * 24 * 30 : idleTimeoutMinutes;
        UserSession session = new UserSession(user, hash(rawToken), now, now.plusMinutes(lifetimeMinutes), ipAddress);
        UserSession saved = sessionRepository.save(session);
        // Repurpose the in-memory field to hold the raw token for the caller; the session is
        // already detached (its Hibernate session closed inside save()) so this never gets
        // flushed back — only the hash written above ever reaches the database.
        saved.setToken(rawToken);
        return saved;
    }

    public Optional<UserSession> validate(String rawToken) {
        return sessionRepository.findByToken(hash(rawToken)).filter(UserSession::isActive);
    }

    public void touch(UserSession session) {
        session.setExpiresAt(LocalDateTime.now().plusMinutes(idleTimeoutMinutes));
        sessionRepository.save(session);
    }

    public void revoke(String rawToken) {
        sessionRepository.findByToken(hash(rawToken)).ifPresent(session -> {
            session.revoke();
            sessionRepository.save(session);
        });
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
