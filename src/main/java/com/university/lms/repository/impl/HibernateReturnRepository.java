package com.university.lms.repository.impl;

import org.hibernate.SessionFactory;

import com.university.lms.entity.Return;
import com.university.lms.repository.ReturnRepository;

public final class HibernateReturnRepository implements ReturnRepository {

    private final SessionFactory sessionFactory;

    public HibernateReturnRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
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
