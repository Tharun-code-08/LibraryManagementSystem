package com.university.lms.business;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;

import com.university.lms.entity.Reservation;
import com.university.lms.entity.ReservationStatus;
import com.university.lms.repository.ReservationRepository;

/** FIFO reservation queueing per book, and the hold-expiry sweep. */
public final class ReservationQueueManager {

    private final ReservationRepository reservationRepository;
    private final int holdDays;

    public ReservationQueueManager(ReservationRepository reservationRepository, int holdDays) {
        this.reservationRepository = reservationRepository;
        this.holdDays = holdDays;
    }

    public int nextQueuePosition(Long bookId) {
        return (int) reservationRepository.countWaitingByBookId(bookId) + 1;
    }

    /**
     * Promotes the longest-waiting reservation for a book to READY with a hold expiry, if any.
     * If a concurrent call for the same book already promoted a reservation first, the DB's
     * {@code uk_reservations_ready_book} unique index (see V10 migration) rejects this one —
     * that's treated as "nothing to promote" rather than an error, since the copy is already
     * correctly held for whoever won the race.
     */
    public Optional<Reservation> promoteNextWaiting(Long bookId) {
        List<Reservation> waiting = reservationRepository.findWaitingByBookId(bookId);
        if (waiting.isEmpty()) {
            return Optional.empty();
        }
        Reservation next = waiting.get(0);
        next.setStatus(ReservationStatus.READY);
        next.setExpiresAt(LocalDateTime.now().plusDays(holdDays));
        try {
            return Optional.of(reservationRepository.save(next));
        } catch (ConstraintViolationException e) {
            return Optional.empty();
        }
    }

    /** Expires READY holds whose window has lapsed unclaimed. Entry point for a scheduled sweep. */
    public int expireStaleReservations() {
        List<Reservation> expired = reservationRepository.findExpiredReady(LocalDateTime.now());
        for (Reservation reservation : expired) {
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
        }
        return expired.size();
    }
}
