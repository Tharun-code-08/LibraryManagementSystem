package com.university.lms.repository.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.university.lms.entity.Reservation;
import com.university.lms.entity.ReservationStatus;
import com.university.lms.repository.ReservationRepository;

public final class HibernateReservationRepository implements ReservationRepository {

    private final SessionFactory sessionFactory;

    public HibernateReservationRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Reservation.class, id));
        }
    }

    @Override
    public List<Reservation> findWaitingByBookId(Long bookId) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Reservation> query = session.createQuery(
                    "from Reservation r where r.book.id = :bookId and r.status = :waiting order by r.queuePosition asc",
                    Reservation.class);
            query.setParameter("bookId", bookId);
            query.setParameter("waiting", ReservationStatus.WAITING);
            return query.list();
        }
    }

    @Override
    public long countWaitingByBookId(Long bookId) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(
                    "select count(r) from Reservation r where r.book.id = :bookId and r.status = :waiting", Long.class);
            query.setParameter("bookId", bookId);
            query.setParameter("waiting", ReservationStatus.WAITING);
            return query.uniqueResult();
        }
    }

    @Override
    public List<Reservation> findExpiredReady(LocalDateTime asOf) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            Query<Reservation> query = session.createQuery(
                    "from Reservation r where r.status = :ready and r.expiresAt < :asOf", Reservation.class);
            query.setParameter("ready", ReservationStatus.READY);
            query.setParameter("asOf", asOf);
            return query.list();
        }
    }

    @Override
    public Reservation save(Reservation reservation) {
        try (org.hibernate.Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Reservation merged = session.merge(reservation);
            session.getTransaction().commit();
            return merged;
        }
    }
}
