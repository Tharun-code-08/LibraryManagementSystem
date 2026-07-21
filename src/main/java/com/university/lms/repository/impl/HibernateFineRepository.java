package com.university.lms.repository.impl;

import org.hibernate.SessionFactory;

import com.university.lms.entity.Fine;
import com.university.lms.repository.FineRepository;

public final class HibernateFineRepository implements FineRepository {

    private final SessionFactory sessionFactory;

    public HibernateFineRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Fine save(Fine fine) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Fine merged = session.merge(fine);
            session.getTransaction().commit();
            return merged;
        }
    }
}
