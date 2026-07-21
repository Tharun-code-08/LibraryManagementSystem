package com.university.lms.repository.impl;

import java.util.Optional;

import org.hibernate.SessionFactory;

import com.university.lms.entity.InventoryAudit;
import com.university.lms.repository.InventoryAuditRepository;

public final class HibernateInventoryAuditRepository implements InventoryAuditRepository {

    private final SessionFactory sessionFactory;

    public HibernateInventoryAuditRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<InventoryAudit> findById(Long id) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(InventoryAudit.class, id));
        }
    }

    @Override
    public InventoryAudit save(InventoryAudit inventoryAudit) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            InventoryAudit merged = session.merge(inventoryAudit);
            session.getTransaction().commit();
            return merged;
        }
    }
}
