package com.university.lms.service.circulation.impl;

import java.time.LocalDateTime;

import com.university.lms.business.HolderRef;
import com.university.lms.business.MembershipHolderResolver;
import com.university.lms.business.ReservationQueueManager;
import com.university.lms.dto.request.ReservationRequestDTO;
import com.university.lms.dto.response.ReservationDTO;
import com.university.lms.entity.Book;
import com.university.lms.entity.Membership;
import com.university.lms.entity.Reservation;
import com.university.lms.entity.ReservationStatus;
import com.university.lms.exception.NoActiveMembershipException;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.BookRepository;
import com.university.lms.repository.MembershipRepository;
import com.university.lms.repository.ReservationRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.security.PermissionEvaluator;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.circulation.ReservationService;

public final class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final MembershipRepository membershipRepository;
    private final MembershipHolderResolver membershipHolderResolver;
    private final ReservationQueueManager reservationQueueManager;
    private final AuditLogService auditLogService;
    private final AuthContext authContext;
    private final PermissionEvaluator permissionEvaluator;

    public ReservationServiceImpl(ReservationRepository reservationRepository, BookRepository bookRepository,
                                   MembershipRepository membershipRepository,
                                   MembershipHolderResolver membershipHolderResolver,
                                   ReservationQueueManager reservationQueueManager,
                                   AuditLogService auditLogService, AuthContext authContext,
                                   PermissionEvaluator permissionEvaluator) {
        this.reservationRepository = reservationRepository;
        this.bookRepository = bookRepository;
        this.membershipRepository = membershipRepository;
        this.membershipHolderResolver = membershipHolderResolver;
        this.reservationQueueManager = reservationQueueManager;
        this.auditLogService = auditLogService;
        this.authContext = authContext;
        this.permissionEvaluator = permissionEvaluator;
    }

    @Override
    public ReservationDTO reserve(ReservationRequestDTO request) {
        permissionEvaluator.requirePermission("CIRCULATION_MANAGE");
        HolderRef holder = membershipHolderResolver.resolveByIdentifier(request.memberIdentifier())
                .orElseThrow(() -> new NoActiveMembershipException(request.memberIdentifier()));
        Membership membership = membershipRepository.findActiveByHolder(holder.holderType(), holder.holderId())
                .orElseThrow(() -> new NoActiveMembershipException(request.memberIdentifier()));
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", request.bookId()));

        int queuePosition = reservationQueueManager.nextQueuePosition(book.getId());
        Reservation reservation = new Reservation(book, membership, LocalDateTime.now(), queuePosition);
        Reservation saved = reservationRepository.save(reservation);

        auditLogService.log(currentUserId(), "RESERVATION_CREATED", "Reservation", saved.getId());
        return toDto(saved, book.getTitle(), holder.displayName());
    }

    @Override
    public void cancelReservation(Long reservationId) {
        permissionEvaluator.requirePermission("CIRCULATION_MANAGE");
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", reservationId));
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        auditLogService.log(currentUserId(), "RESERVATION_CANCELLED", "Reservation", reservationId);
    }

    @Override
    public int expireStaleReservations() {
        int expiredCount = reservationQueueManager.expireStaleReservations();
        if (expiredCount > 0) {
            auditLogService.log(currentUserId(), "RESERVATION_EXPIRY_SWEEP", "Reservation", null);
        }
        return expiredCount;
    }

    private Long currentUserId() {
        return authContext.isAuthenticated() ? authContext.getCurrentUser().getId() : null;
    }

    private ReservationDTO toDto(Reservation reservation, String bookTitle, String memberName) {
        return new ReservationDTO(reservation.getId(), bookTitle, memberName, reservation.getRequestedAt(),
                reservation.getQueuePosition(), reservation.getExpiresAt(), reservation.getStatus().name());
    }
}
