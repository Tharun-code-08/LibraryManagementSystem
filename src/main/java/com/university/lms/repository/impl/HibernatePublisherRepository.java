package com.university.lms.repository.impl;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.Publisher;
import com.university.lms.repository.PublisherRepository;

public final class HibernatePublisherRepository implements PublisherRepository {

    private final SessionFactory sessionFactory;

    public HibernatePublisherRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Publisher> findById(Long id) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Publisher.class, id));
        }
    }

    @Override
    public Optional<Publisher> findByName(String name) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Publisher> query = session.createQuery("from Publisher p where p.name = :name", Publisher.class);
            query.setParameter("name", name);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public List<Publisher> findAll() {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return session.createQuery("from Publisher p order by p.name", Publisher.class).list();
        }
    }

    @Override
    public Publisher save(Publisher publisher) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Publisher merged = session.merge(publisher);
            session.getTransaction().commit();
            return merged;
        }
    }

    @Override
    public void delete(Publisher publisher) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.remove(session.contains(publisher) ? publisher : session.merge(publisher));
            session.getTransaction().commit();
        }
    }
}
