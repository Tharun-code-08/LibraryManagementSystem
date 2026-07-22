package com.university.lms.repository.impl;

import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.Tag;
import com.university.lms.repository.TagRepository;

public final class HibernateTagRepository implements TagRepository {

    private final SessionFactory sessionFactory;

    public HibernateTagRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Tag> findByName(String name) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Tag> query = session.createQuery("from Tag t where t.name = :name", Tag.class);
            query.setParameter("name", name);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public Tag save(Tag tag) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Tag merged = session.merge(tag);
            session.getTransaction().commit();
            return merged;
        }
    }
}
