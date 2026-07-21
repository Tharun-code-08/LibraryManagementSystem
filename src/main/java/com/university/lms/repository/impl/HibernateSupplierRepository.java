package com.university.lms.repository.impl;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.Supplier;
import com.university.lms.repository.SupplierRepository;

public final class HibernateSupplierRepository implements SupplierRepository {

    private final SessionFactory sessionFactory;

    public HibernateSupplierRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Supplier> findById(Long id) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Supplier.class, id));
        }
    }

    @Override
    public Optional<Supplier> findByName(String name) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Supplier> query = session.createQuery("from Supplier s where s.name = :name", Supplier.class);
            query.setParameter("name", name);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public List<Supplier> findAll() {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return session.createQuery("from Supplier s order by s.name", Supplier.class).list();
        }
    }

    @Override
    public Supplier save(Supplier supplier) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Supplier merged = session.merge(supplier);
            session.getTransaction().commit();
            return merged;
        }
    }

    @Override
    public void delete(Supplier supplier) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.remove(session.contains(supplier) ? supplier : session.merge(supplier));
            session.getTransaction().commit();
        }
    }
}
