package com.university.lms.repository.impl;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.dto.request.FineSearchCriteria;
import com.university.lms.entity.Fine;
import com.university.lms.entity.FineStatus;
import com.university.lms.repository.FineRepository;

public final class HibernateFineRepository implements FineRepository {

    private final SessionFactory sessionFactory;

    public HibernateFineRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Fine> findById(Long id) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Fine.class, id));
        }
    }

    @Override
    public List<Fine> search(FineSearchCriteria criteria) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Fine> query = session.createQuery(
                    buildSearchHql(criteria, "select f") + " order by f.createdAt desc", Fine.class);
            bindSearchParameters(query, criteria);
            query.setFirstResult(criteria.getPageNumber() * criteria.getPageSize());
            query.setMaxResults(criteria.getPageSize());
            return query.list();
        }
    }

    @Override
    public long countSearchResults(FineSearchCriteria criteria) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(buildSearchHql(criteria, "select count(f)"), Long.class);
            bindSearchParameters(query, criteria);
            return query.uniqueResult();
        }
    }

    private String buildSearchHql(FineSearchCriteria criteria, String selectClause) {
        StringBuilder hql = new StringBuilder(selectClause).append(" from Fine f where 1 = 1");
        if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
            hql.append(" and f.status = :status");
        }
        return hql.toString();
    }

    private void bindSearchParameters(Query<?> query, FineSearchCriteria criteria) {
        if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
            query.setParameter("status", FineStatus.valueOf(criteria.getStatus()));
        }
    }

    @Override
    public Fine save(Fine fine) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Fine merged = session.merge(fine);
            session.getTransaction().commit();
            return merged;
        }
    }
}
