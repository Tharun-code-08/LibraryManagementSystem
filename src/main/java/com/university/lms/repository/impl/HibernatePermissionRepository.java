package com.university.lms.repository.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.Permission;
import com.university.lms.repository.PermissionRepository;

public final class HibernatePermissionRepository implements PermissionRepository {

    private final SessionFactory sessionFactory;

    public HibernatePermissionRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Permission> findAll() {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Permission> query = session.createQuery("from Permission p order by p.code", Permission.class);
            return query.list();
        }
    }
}
