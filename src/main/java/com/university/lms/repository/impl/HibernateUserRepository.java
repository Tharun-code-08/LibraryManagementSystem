package com.university.lms.repository.impl;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.User;
import com.university.lms.repository.UserRepository;

public final class HibernateUserRepository implements UserRepository {

    private final SessionFactory sessionFactory;

    public HibernateUserRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<User> findById(Long id) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(User.class, id));
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery("from User u where u.username = :username", User.class);
            query.setParameter("username", username);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery("from User u where u.email = :email", User.class);
            query.setParameter("email", email);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery(
                    "from User u where u.username = :value or u.email = :value", User.class);
            query.setParameter("value", usernameOrEmail);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public List<User> findAll() {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery("from User u order by u.username", User.class);
            return query.list();
        }
    }

    @Override
    public User save(User user) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            User merged = session.merge(user);
            session.getTransaction().commit();
            return merged;
        }
    }
}
