package com.university.lms.repository.impl;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.MembershipType;
import com.university.lms.repository.MembershipTypeRepository;

public final class HibernateMembershipTypeRepository implements MembershipTypeRepository {

    private final SessionFactory sessionFactory;

    public HibernateMembershipTypeRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<MembershipType> findById(Long id) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(MembershipType.class, id));
        }
    }

    @Override
    public Optional<MembershipType> findByName(String name) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<MembershipType> query = session.createQuery(
                    "from MembershipType t where t.name = :name", MembershipType.class);
            query.setParameter("name", name);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public List<MembershipType> findAll() {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return session.createQuery("from MembershipType t order by t.name", MembershipType.class).list();
        }
    }

    @Override
    public MembershipType save(MembershipType membershipType) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            MembershipType merged = session.merge(membershipType);
            session.getTransaction().commit();
            return merged;
        }
    }
}
