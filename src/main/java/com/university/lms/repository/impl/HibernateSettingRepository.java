package com.university.lms.repository.impl;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.Setting;
import com.university.lms.repository.SettingRepository;

public final class HibernateSettingRepository implements SettingRepository {

    private final SessionFactory sessionFactory;

    public HibernateSettingRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Setting> findAll() {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Setting> query = session.createQuery("from Setting s order by s.category, s.key", Setting.class);
            return query.list();
        }
    }

    @Override
    public Optional<Setting> findByKey(String key) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Setting> query = session.createQuery("from Setting s where s.key = :key", Setting.class);
            query.setParameter("key", key);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public Setting save(Setting setting) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Setting merged = session.merge(setting);
            session.getTransaction().commit();
            return merged;
        }
    }
}
