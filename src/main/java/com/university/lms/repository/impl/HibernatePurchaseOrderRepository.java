package com.university.lms.repository.impl;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.dto.request.PurchaseOrderSearchCriteria;
import com.university.lms.entity.PurchaseOrder;
import com.university.lms.entity.PurchaseOrderStatus;
import com.university.lms.repository.PurchaseOrderRepository;

public final class HibernatePurchaseOrderRepository implements PurchaseOrderRepository {

    private final SessionFactory sessionFactory;

    public HibernatePurchaseOrderRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<PurchaseOrder> findById(Long id) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(PurchaseOrder.class, id));
        }
    }

    @Override
    public List<PurchaseOrder> search(PurchaseOrderSearchCriteria criteria) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<PurchaseOrder> query = session.createQuery(
                    buildSearchHql(criteria, "select po") + " order by po.orderDate desc", PurchaseOrder.class);
            bindSearchParameters(query, criteria);
            query.setFirstResult(criteria.getPageNumber() * criteria.getPageSize());
            query.setMaxResults(criteria.getPageSize());
            return query.list();
        }
    }

    @Override
    public long countSearchResults(PurchaseOrderSearchCriteria criteria) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(buildSearchHql(criteria, "select count(po)"), Long.class);
            bindSearchParameters(query, criteria);
            return query.uniqueResult();
        }
    }

    private String buildSearchHql(PurchaseOrderSearchCriteria criteria, String selectClause) {
        StringBuilder hql = new StringBuilder(selectClause).append(" from PurchaseOrder po where 1 = 1");
        if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
            hql.append(" and po.status = :status");
        }
        return hql.toString();
    }

    private void bindSearchParameters(Query<?> query, PurchaseOrderSearchCriteria criteria) {
        if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
            query.setParameter("status", PurchaseOrderStatus.valueOf(criteria.getStatus()));
        }
    }

    @Override
    public PurchaseOrder save(PurchaseOrder purchaseOrder) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            PurchaseOrder merged = session.merge(purchaseOrder);
            session.getTransaction().commit();
            return merged;
        }
    }
}
