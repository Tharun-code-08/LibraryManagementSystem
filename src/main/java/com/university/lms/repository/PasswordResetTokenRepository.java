package com.university.lms.repository;

import java.util.Optional;

import com.university.lms.entity.PasswordResetToken;

/** Persistence contract for {@link PasswordResetToken}. */
public interface PasswordResetTokenRepository {

    Optional<PasswordResetToken> findByToken(String token);

    PasswordResetToken save(PasswordResetToken token);
}
