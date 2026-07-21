package com.university.lms.repository.impl;

import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.PasswordResetToken;
import com.university.lms.repository.PasswordResetTokenRepository;

public final class HibernatePasswordResetTokenRepository implements PasswordResetTokenRepository {

    private final SessionFactory sessionFactory;

    public HibernatePasswordResetTokenRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<PasswordResetToken> query = session.createQuery(
                    "from PasswordResetToken t where t.token = :token", PasswordResetToken.class);
            query.setParameter("token", token);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            PasswordResetToken merged = session.merge(token);
            session.getTransaction().commit();
            return merged;
        }
    }
}
