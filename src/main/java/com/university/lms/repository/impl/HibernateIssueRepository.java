package com.university.lms.repository.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.Issue;
import com.university.lms.entity.IssueStatus;
import com.university.lms.repository.IssueRepository;

public final class HibernateIssueRepository implements IssueRepository {

    private final SessionFactory sessionFactory;

    public HibernateIssueRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Issue> findById(Long id) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Issue.class, id));
        }
    }

    @Override
    public Optional<Issue> findOpenByCopyId(Long copyId) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Issue> query = session.createQuery(
                    "from Issue i where i.bookCopy.id = :copyId and i.status in (:issued, :overdue)", Issue.class);
            query.setParameter("copyId", copyId);
            query.setParameter("issued", IssueStatus.ISSUED);
            query.setParameter("overdue", IssueStatus.OVERDUE);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public long countOpenByMembershipId(Long membershipId) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(
                    "select count(i) from Issue i where i.membership.id = :membershipId and i.status in (:issued, :overdue)",
                    Long.class);
            query.setParameter("membershipId", membershipId);
            query.setParameter("issued", IssueStatus.ISSUED);
            query.setParameter("overdue", IssueStatus.OVERDUE);
            return query.uniqueResult();
        }
    }

    @Override
    public List<Issue> findOverdueOpenIssues(LocalDateTime asOf) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Issue> query = session.createQuery(
                    "from Issue i where i.status = :issued and i.dueDate < :asOf", Issue.class);
            query.setParameter("issued", IssueStatus.ISSUED);
            query.setParameter("asOf", asOf);
            return query.list();
        }
    }

    @Override
    public List<Issue> findByIssueDateRange(LocalDateTime from, LocalDateTime to) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Issue> query = session.createQuery(
                    "from Issue i where i.issueDate >= :from and i.issueDate <= :to order by i.issueDate desc", Issue.class);
            query.setParameter("from", from);
            query.setParameter("to", to);
            return query.list();
        }
    }

    @Override
    public Issue save(Issue issue) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Issue merged = session.merge(issue);
            session.getTransaction().commit();
            return merged;
        }
    }
}
