package com.university.lms.repository.impl;

import org.hibernate.SessionFactory;

import com.university.lms.entity.AuditLog;
import com.university.lms.repository.AuditLogRepository;

public final class HibernateAuditLogRepository implements AuditLogRepository {

    private final SessionFactory sessionFactory;

    public HibernateAuditLogRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public AuditLog save(AuditLog auditLog) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(auditLog);
            session.getTransaction().commit();
            return auditLog;
        }
    }
}
