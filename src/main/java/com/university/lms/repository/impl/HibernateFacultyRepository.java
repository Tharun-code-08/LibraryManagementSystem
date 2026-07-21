package com.university.lms.repository.impl;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.Faculty;
import com.university.lms.repository.FacultyRepository;

public final class HibernateFacultyRepository implements FacultyRepository {

    private final SessionFactory sessionFactory;

    public HibernateFacultyRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Faculty> findById(Long id) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Faculty.class, id));
        }
    }

    @Override
    public Optional<Faculty> findByFacultyId(String facultyId) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Faculty> query = session.createQuery("from Faculty f where f.facultyId = :facultyId", Faculty.class);
            query.setParameter("facultyId", facultyId);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public List<Faculty> findAll() {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return session.createQuery("from Faculty f order by f.user.username", Faculty.class).list();
        }
    }

    @Override
    public Faculty save(Faculty faculty) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Faculty merged = session.merge(faculty);
            session.getTransaction().commit();
            return merged;
        }
    }
}
