package com.university.lms.repository.impl;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.Backup;
import com.university.lms.repository.BackupRepository;

public final class HibernateBackupRepository implements BackupRepository {

    private final SessionFactory sessionFactory;

    public HibernateBackupRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Backup save(Backup backup) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Backup merged = session.merge(backup);
            session.getTransaction().commit();
            return merged;
        }
    }

    @Override
    public Optional<Backup> findById(Long id) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Backup.class, id));
        }
    }

    @Override
    public List<Backup> findRecent(int limit) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Backup> query = session.createQuery("from Backup b order by b.createdAt desc", Backup.class);
            query.setMaxResults(limit);
            return query.list();
        }
    }
}
