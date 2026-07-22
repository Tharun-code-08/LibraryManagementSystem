package com.university.lms.repository.impl;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.Role;
import com.university.lms.repository.RoleRepository;

public final class HibernateRoleRepository implements RoleRepository {

    private final SessionFactory sessionFactory;

    public HibernateRoleRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Role> findByName(String name) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Role> query = session.createQuery("from Role r where r.name = :name", Role.class);
            query.setParameter("name", name);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public Optional<Role> findById(Long id) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Role.class, id));
        }
    }

    @Override
    public List<Role> findAll() {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Role> query = session.createQuery("from Role r order by r.name", Role.class);
            return query.list();
        }
    }

    @Override
    public Role save(Role role) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Role merged = session.merge(role);
            session.getTransaction().commit();
            return merged;
        }
    }
}
