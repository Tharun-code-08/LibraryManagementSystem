package com.university.lms.repository.impl;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.BookCopy;
import com.university.lms.entity.BookCopyStatus;
import com.university.lms.repository.BookCopyRepository;

public final class HibernateBookCopyRepository implements BookCopyRepository {

    private final SessionFactory sessionFactory;

    public HibernateBookCopyRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<BookCopy> findById(Long id) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(BookCopy.class, id));
        }
    }

    @Override
    public Optional<BookCopy> findByBarcode(String barcode) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<BookCopy> query = session.createQuery("from BookCopy c where c.barcode = :barcode", BookCopy.class);
            query.setParameter("barcode", barcode);
            return query.uniqueResultOptional();
        }
    }

    @Override
    public List<BookCopy> findByBookId(Long bookId) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<BookCopy> query = session.createQuery("from BookCopy c where c.book.id = :bookId", BookCopy.class);
            query.setParameter("bookId", bookId);
            return query.list();
        }
    }

    @Override
    public long countByBookId(Long bookId) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(
                    "select count(c) from BookCopy c where c.book.id = :bookId", Long.class);
            query.setParameter("bookId", bookId);
            return query.uniqueResult();
        }
    }

    @Override
    public long countByBookIdAndStatus(Long bookId, BookCopyStatus status) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(
                    "select count(c) from BookCopy c where c.book.id = :bookId and c.status = :status", Long.class);
            query.setParameter("bookId", bookId);
            query.setParameter("status", status);
            return query.uniqueResult();
        }
    }

    @Override
    public BookCopy save(BookCopy bookCopy) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            BookCopy merged = session.merge(bookCopy);
            session.getTransaction().commit();
            return merged;
        }
    }
}
