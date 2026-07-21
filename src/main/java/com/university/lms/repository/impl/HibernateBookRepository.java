package com.university.lms.repository.impl;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.dto.request.BookSearchCriteria;
import com.university.lms.entity.Book;
import com.university.lms.entity.BookCopyStatus;
import com.university.lms.repository.BookRepository;

public final class HibernateBookRepository implements BookRepository {

    private final SessionFactory sessionFactory;

    public HibernateBookRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Book> findById(Long id) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Book.class, id));
        }
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Book> query = session.createQuery("from Book b where b.isbn = :isbn", Book.class);
            query.setParameter("isbn", isbn);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public List<Book> search(BookSearchCriteria criteria) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Book> query = session.createQuery(
                    buildSearchHql(criteria, "select distinct b") + " order by b.title", Book.class);
            bindSearchParameters(query, criteria);
            query.setFirstResult(criteria.getPageNumber() * criteria.getPageSize());
            query.setMaxResults(criteria.getPageSize());
            return query.list();
        }
    }

    @Override
    public long countSearchResults(BookSearchCriteria criteria) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(
                    buildSearchHql(criteria, "select count(distinct b)"), Long.class);
            bindSearchParameters(query, criteria);
            return query.uniqueResult();
        }
    }

    private String buildSearchHql(BookSearchCriteria criteria, String selectClause) {
        StringBuilder hql = new StringBuilder(selectClause)
                .append(" from Book b left join b.authors a where (b.deleted = false or :includeDeleted = true)");
        if (criteria.getCategoryId() != null) {
            hql.append(" and b.category.id = :categoryId");
        }
        if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
            hql.append(" and (lower(b.title) like :keyword or lower(b.isbn) like :keyword or lower(a.name) like :keyword)");
        }
        if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
            hql.append(" and exists (select 1 from BookCopy bc where bc.book = b and bc.status = :status)");
        }
        return hql.toString();
    }

    private void bindSearchParameters(Query<?> query, BookSearchCriteria criteria) {
        query.setParameter("includeDeleted", criteria.isIncludeDeleted());
        if (criteria.getCategoryId() != null) {
            query.setParameter("categoryId", criteria.getCategoryId());
        }
        if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
            query.setParameter("keyword", "%" + criteria.getKeyword().toLowerCase() + "%");
        }
        if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
            query.setParameter("status", BookCopyStatus.valueOf(criteria.getStatus()));
        }
    }

    @Override
    public Book save(Book book) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Book merged = session.merge(book);
            session.getTransaction().commit();
            return merged;
        }
    }
}
