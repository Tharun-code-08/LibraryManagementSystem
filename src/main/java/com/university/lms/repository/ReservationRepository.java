package com.university.lms.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.university.lms.entity.Reservation;

public interface ReservationRepository {

    Optional<Reservation> findById(Long id);

    List<Reservation> findWaitingByBookId(Long bookId);

    long countWaitingByBookId(Long bookId);

    List<Reservation> findExpiredReady(LocalDateTime asOf);

    Reservation save(Reservation reservation);
}
