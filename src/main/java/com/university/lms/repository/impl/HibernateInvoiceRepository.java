package com.university.lms.repository.impl;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.Invoice;
import com.university.lms.repository.InvoiceRepository;

public final class HibernateInvoiceRepository implements InvoiceRepository {

    private final SessionFactory sessionFactory;

    public HibernateInvoiceRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Invoice> findByPurchaseOrderId(Long purchaseOrderId) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Invoice> query = session.createQuery(
                    "from Invoice i where i.purchaseOrder.id = :poId order by i.invoiceDate", Invoice.class);
            query.setParameter("poId", purchaseOrderId);
            return query.list();
        }
    }

    @Override
    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Invoice> query = session.createQuery(
                    "from Invoice i where i.invoiceNumber = :number", Invoice.class);
            query.setParameter("number", invoiceNumber);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public Invoice save(Invoice invoice) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Invoice merged = session.merge(invoice);
            session.getTransaction().commit();
            return merged;
        }
    }
}
