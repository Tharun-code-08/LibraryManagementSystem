package com.university.lms.repository.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.InventoryAuditItem;
import com.university.lms.repository.InventoryAuditItemRepository;

public final class HibernateInventoryAuditItemRepository implements InventoryAuditItemRepository {

    private final SessionFactory sessionFactory;

    public HibernateInventoryAuditItemRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<InventoryAuditItem> findByAuditId(Long auditId) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<InventoryAuditItem> query = session.createQuery(
                    "from InventoryAuditItem i where i.inventoryAudit.id = :auditId", InventoryAuditItem.class);
            query.setParameter("auditId", auditId);
            return query.list();
        }
    }

    @Override
    public InventoryAuditItem save(InventoryAuditItem item) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            InventoryAuditItem merged = session.merge(item);
            session.getTransaction().commit();
            return merged;
        }
    }
}
