package com.university.lms.repository.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.dto.request.AuditLogSearchCriteria;
import com.university.lms.entity.AuditLog;
import com.university.lms.entity.User;
import com.university.lms.repository.AuditLogRepository;

public final class HibernateAuditLogRepository implements AuditLogRepository {

    private final SessionFactory sessionFactory;

    public HibernateAuditLogRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(Long actorUserId, String action, String entityType, Long entityId) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            User actorReference = actorUserId != null ? session.getReference(User.class, actorUserId) : null;
            AuditLog entry = new AuditLog(actorReference, action, entityType, entityId, null, null, null);
            session.persist(entry);
            session.getTransaction().commit();
        }
    }

    @Override
    public List<AuditLog> findRecent(int limit) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<AuditLog> query = session.createQuery(
                    "from AuditLog a order by a.createdAt desc", AuditLog.class);
            query.setMaxResults(limit);
            return query.list();
        }
    }

    @Override
    public List<AuditLog> search(AuditLogSearchCriteria criteria) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<AuditLog> query = session.createQuery(
                    buildSearchHql(criteria, "select a") + " order by a.createdAt desc", AuditLog.class);
            bindSearchParameters(query, criteria);
            query.setFirstResult(criteria.getPageNumber() * criteria.getPageSize());
            query.setMaxResults(criteria.getPageSize());
            return query.list();
        }
    }

    @Override
    public long countSearchResults(AuditLogSearchCriteria criteria) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(buildSearchHql(criteria, "select count(a)"), Long.class);
            bindSearchParameters(query, criteria);
            return query.uniqueResult();
        }
    }

    private String buildSearchHql(AuditLogSearchCriteria criteria, String selectClause) {
        StringBuilder hql = new StringBuilder(selectClause).append(" from AuditLog a where 1 = 1");
        if (criteria.getActorUserId() != null) {
            hql.append(" and a.user.id = :actorUserId");
        }
        if (criteria.getEntityType() != null && !criteria.getEntityType().isBlank()) {
            hql.append(" and a.entityType = :entityType");
        }
        if (criteria.getFromDate() != null) {
            hql.append(" and a.createdAt >= :fromDate");
        }
        if (criteria.getToDate() != null) {
            hql.append(" and a.createdAt <= :toDate");
        }
        return hql.toString();
    }

    private void bindSearchParameters(Query<?> query, AuditLogSearchCriteria criteria) {
        if (criteria.getActorUserId() != null) {
            query.setParameter("actorUserId", criteria.getActorUserId());
        }
        if (criteria.getEntityType() != null && !criteria.getEntityType().isBlank()) {
            query.setParameter("entityType", criteria.getEntityType());
        }
        if (criteria.getFromDate() != null) {
            query.setParameter("fromDate", criteria.getFromDate());
        }
        if (criteria.getToDate() != null) {
            query.setParameter("toDate", criteria.getToDate());
        }
    }
}
