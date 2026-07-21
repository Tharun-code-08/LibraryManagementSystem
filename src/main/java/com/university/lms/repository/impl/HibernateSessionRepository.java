package com.university.lms.repository.impl;

import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.UserSession;
import com.university.lms.repository.SessionRepository;

public final class HibernateSessionRepository implements SessionRepository {

    private final SessionFactory sessionFactory;

    public HibernateSessionRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<UserSession> findByToken(String token) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<UserSession> query = session.createQuery(
                    "from UserSession s where s.token = :token", UserSession.class);
            query.setParameter("token", token);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public UserSession save(UserSession userSession) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            UserSession merged = session.merge(userSession);
            session.getTransaction().commit();
            return merged;
        }
    }
}
