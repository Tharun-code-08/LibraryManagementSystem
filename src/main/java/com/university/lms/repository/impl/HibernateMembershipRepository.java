package com.university.lms.repository.impl;

import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.HolderType;
import com.university.lms.entity.Membership;
import com.university.lms.entity.MembershipStatus;
import com.university.lms.repository.MembershipRepository;

public final class HibernateMembershipRepository implements MembershipRepository {

    private final SessionFactory sessionFactory;

    public HibernateMembershipRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Membership> findActiveByHolder(HolderType holderType, Long holderId) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Membership> query = session.createQuery(
                    "from Membership m where m.holderType = :holderType and m.holderId = :holderId "
                            + "and m.status = :status order by m.expiryDate desc", Membership.class);
            query.setParameter("holderType", holderType);
            query.setParameter("holderId", holderId);
            query.setParameter("status", MembershipStatus.ACTIVE);
            query.setMaxResults(1);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public Membership save(Membership membership) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Membership merged = session.merge(membership);
            session.getTransaction().commit();
            return merged;
        }
    }
}
