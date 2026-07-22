package com.university.lms.repository.impl;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.Payment;
import com.university.lms.repository.PaymentRepository;

public final class HibernatePaymentRepository implements PaymentRepository {

    private final SessionFactory sessionFactory;

    public HibernatePaymentRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Payment> findByFineId(Long fineId) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Payment> query = session.createQuery(
                    "from Payment p where p.fine.id = :fineId order by p.paidAt", Payment.class);
            query.setParameter("fineId", fineId);
            return query.list();
        }
    }

    @Override
    public BigDecimal sumAmountByFineId(Long fineId) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<BigDecimal> query = session.createQuery(
                    "select coalesce(sum(p.amount), 0) from Payment p where p.fine.id = :fineId", BigDecimal.class);
            query.setParameter("fineId", fineId);
            return query.uniqueResult();
        }
    }

    @Override
    public Payment save(Payment payment) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Payment merged = session.merge(payment);
            session.getTransaction().commit();
            return merged;
        }
    }
}
