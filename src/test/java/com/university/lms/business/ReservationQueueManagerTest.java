package com.university.lms.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.university.lms.entity.Book;
import com.university.lms.entity.HolderType;
import com.university.lms.entity.Membership;
import com.university.lms.entity.MembershipType;
import com.university.lms.entity.Reservation;
import com.university.lms.entity.ReservationStatus;
import com.university.lms.repository.ReservationRepository;

@ExtendWith(MockitoExtension.class)
class ReservationQueueManagerTest {

    @Mock
    private ReservationRepository reservationRepository;

    private ReservationQueueManager queueManager;

    @BeforeEach
    void setUp() {
        queueManager = new ReservationQueueManager(reservationRepository, 3);
    }

    @Test
    void nextQueuePositionIsOneMoreThanCurrentWaitingCount() {
        when(reservationRepository.countWaitingByBookId(5L)).thenReturn(2L);
        assertEquals(3, queueManager.nextQueuePosition(5L));
    }

    @Test
    void promotesLongestWaitingReservationToReadyWithHoldExpiry() {
        MembershipType type = new MembershipType("STUDENT_STANDARD", 3, 14, BigDecimal.valueOf(5), 1, 2);
        Membership membership = new Membership(type, HolderType.STUDENT, 1L, LocalDate.now(), LocalDate.now().plusDays(300));
        Book book = new Book("978-1", "Test Book", null, null, null, null, null, null, BigDecimal.ZERO, null, null);
        Reservation waiting = new Reservation(book, membership, LocalDateTime.now().minusDays(1), 1);

        when(reservationRepository.findWaitingByBookId(10L)).thenReturn(List.of(waiting));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Reservation> promoted = queueManager.promoteNextWaiting(10L);

        assertTrue(promoted.isPresent());
        assertEquals(ReservationStatus.READY, promoted.get().getStatus());
    }

    @Test
    void returnsEmptyWhenNoOneIsWaiting() {
        when(reservationRepository.findWaitingByBookId(10L)).thenReturn(List.of());
        assertTrue(queueManager.promoteNextWaiting(10L).isEmpty());
    }
}
