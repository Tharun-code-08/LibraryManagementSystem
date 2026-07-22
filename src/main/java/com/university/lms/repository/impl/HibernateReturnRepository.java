package com.university.lms.repository.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.Return;
import com.university.lms.repository.ReturnRepository;

public final class HibernateReturnRepository implements ReturnRepository {

    private final SessionFactory sessionFactory;

    public HibernateReturnRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Return> findByReturnDateRange(LocalDateTime from, LocalDateTime to) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Return> query = session.createQuery(
                    "from Return r where r.returnDate >= :from and r.returnDate <= :to order by r.returnDate desc", Return.class);
            query.setParameter("from", from);
            query.setParameter("to", to);
            return query.list();
        }
    }

    @Override
    public Return save(Return returnRecord) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Return merged = session.merge(returnRecord);
            session.getTransaction().commit();
            return merged;
        }
    }
}
