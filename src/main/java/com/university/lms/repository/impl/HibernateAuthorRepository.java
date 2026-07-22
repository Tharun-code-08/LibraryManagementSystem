package com.university.lms.repository.impl;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.Author;
import com.university.lms.repository.AuthorRepository;

public final class HibernateAuthorRepository implements AuthorRepository {

    private final SessionFactory sessionFactory;

    public HibernateAuthorRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Author> findById(Long id) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Author.class, id));
        }
    }

    @Override
    public Optional<Author> findByName(String name) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Author> query = session.createQuery("from Author a where a.name = :name", Author.class);
            query.setParameter("name", name);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public List<Author> findAll() {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return session.createQuery("from Author a order by a.name", Author.class).list();
        }
    }

    @Override
    public Author save(Author author) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Author merged = session.merge(author);
            session.getTransaction().commit();
            return merged;
        }
    }

    @Override
    public void delete(Author author) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.remove(session.contains(author) ? author : session.merge(author));
            session.getTransaction().commit();
        }
    }
}
