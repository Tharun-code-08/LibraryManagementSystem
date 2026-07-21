package com.university.lms.repository.impl;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;

import com.university.lms.entity.Branch;
import com.university.lms.repository.BranchRepository;

public final class HibernateBranchRepository implements BranchRepository {

    private final SessionFactory sessionFactory;

    public HibernateBranchRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Branch> findById(Long id) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Branch.class, id));
        }
    }

    @Override
    public List<Branch> findAll() {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return session.createQuery("from Branch b order by b.name", Branch.class).list();
        }
    }
}
