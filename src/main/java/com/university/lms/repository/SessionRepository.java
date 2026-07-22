package com.university.lms.repository;

import java.util.Optional;

import com.university.lms.entity.UserSession;

/** Persistence contract for {@link UserSession}. */
public interface SessionRepository {

    Optional<UserSession> findByToken(String token);

    UserSession save(UserSession session);
}
