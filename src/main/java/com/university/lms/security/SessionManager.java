package com.university.lms.security;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import com.university.lms.entity.User;
import com.university.lms.entity.UserSession;
import com.university.lms.repository.SessionRepository;

/**
 * Creates and validates {@link UserSession} rows. The session token is a 256-bit random value,
 * base64url-encoded — opaque to the client, never a JWT (no need for stateless verification in
 * a single desktop client talking to one trusted database).
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
        String token = generateToken();
        LocalDateTime now = LocalDateTime.now();
        long lifetimeMinutes = extendedLifetime ? idleTimeoutMinutes * 24 * 30 : idleTimeoutMinutes;
        UserSession session = new UserSession(user, token, now, now.plusMinutes(lifetimeMinutes), ipAddress);
        return sessionRepository.save(session);
    }

    public Optional<UserSession> validate(String token) {
        return sessionRepository.findByToken(token).filter(UserSession::isActive);
    }

    public void touch(UserSession session) {
        session.setExpiresAt(LocalDateTime.now().plusMinutes(idleTimeoutMinutes));
        sessionRepository.save(session);
    }

    public void revoke(String token) {
        sessionRepository.findByToken(token).ifPresent(session -> {
            session.revoke();
            sessionRepository.save(session);
        });
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
