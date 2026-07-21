package com.university.lms.repository.impl;

import org.hibernate.SessionFactory;

import com.university.lms.entity.AuditLog;
import com.university.lms.entity.User;
import com.university.lms.repository.AuditLogRepository;

public final class HibernateAuditLogRepository implements AuditLogRepository {

    private final SessionFactory sessionFactory;

    public HibernateAuditLogRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(Long actorUserId, String action, String entityType, Long entityId) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            User actorReference = actorUserId != null ? session.getReference(User.class, actorUserId) : null;
            AuditLog entry = new AuditLog(actorReference, action, entityType, entityId, null, null, null);
            session.persist(entry);
            session.getTransaction().commit();
        }
    }
}
