package com.university.lms.service.circulation.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import com.university.lms.business.FineCalculationStrategy;
import com.university.lms.business.MembershipHolderResolver;
import com.university.lms.business.ReservationQueueManager;
import com.university.lms.dto.request.ReturnRequestDTO;
import com.university.lms.dto.response.ReturnResultDTO;
import com.university.lms.entity.BookCopy;
import com.university.lms.entity.BookCopyStatus;
import com.university.lms.entity.Fine;
import com.university.lms.entity.FineReason;
import com.university.lms.entity.Issue;
import com.university.lms.entity.IssueStatus;
import com.university.lms.entity.Return;
import com.university.lms.entity.Reservation;
import com.university.lms.entity.ReturnCondition;
import com.university.lms.entity.User;
import com.university.lms.exception.NoOpenIssueException;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.BookCopyRepository;
import com.university.lms.repository.FineRepository;
import com.university.lms.repository.IssueRepository;
import com.university.lms.repository.ReturnRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.security.PermissionEvaluator;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.circulation.ReturnService;
import com.university.lms.service.notification.NotificationService;

public final class ReturnServiceImpl implements ReturnService {

    private final IssueRepository issueRepository;
    private final BookCopyRepository bookCopyRepository;
    private final ReturnRepository returnRepository;
    private final FineRepository fineRepository;
    private final UserRepository userRepository;
    private final FineCalculationStrategy fineCalculationStrategy;
    private final ReservationQueueManager reservationQueueManager;
    private final MembershipHolderResolver membershipHolderResolver;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final PermissionEvaluator permissionEvaluator;

    public ReturnServiceImpl(IssueRepository issueRepository, BookCopyRepository bookCopyRepository,
                              ReturnRepository returnRepository, FineRepository fineRepository,
                              UserRepository userRepository, FineCalculationStrategy fineCalculationStrategy,
                              ReservationQueueManager reservationQueueManager,
                              MembershipHolderResolver membershipHolderResolver, AuditLogService auditLogService,
                              NotificationService notificationService, PermissionEvaluator permissionEvaluator) {
        this.issueRepository = issueRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.returnRepository = returnRepository;
        this.fineRepository = fineRepository;
        this.userRepository = userRepository;
        this.fineCalculationStrategy = fineCalculationStrategy;
        this.reservationQueueManager = reservationQueueManager;
        this.membershipHolderResolver = membershipHolderResolver;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
        this.permissionEvaluator = permissionEvaluator;
    }

    @Override
    public ReturnResultDTO returnBook(ReturnRequestDTO request, Long receivedByUserId) {
        permissionEvaluator.requirePermission("CIRCULATION_MANAGE");
        BookCopy copy = bookCopyRepository.findByBarcode(request.copyBarcode())
                .orElseThrow(() -> new NoOpenIssueException(request.copyBarcode()));
        Issue issue = issueRepository.findOpenByCopyId(copy.getId())
                .orElseThrow(() -> new NoOpenIssueException(request.copyBarcode()));

        ReturnCondition condition = ReturnCondition.valueOf(request.condition());
        User receivedBy = userRepository.findById(receivedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", receivedByUserId));

        LocalDateTime returnDate = LocalDateTime.now();
        Return returnRecord = new Return(issue, receivedBy, returnDate, condition, request.notes());
        Return savedReturn = returnRepository.save(returnRecord);

        issue.setStatus(IssueStatus.RETURNED);
        issueRepository.save(issue);

        copy.setStatus(resolveCopyStatusAfterReturn(condition));
        bookCopyRepository.save(copy);

        BigDecimal fineAmount = fineCalculationStrategy.calculate(issue, returnDate, condition);
        if (fineAmount.compareTo(BigDecimal.ZERO) > 0) {
            FineReason reason = condition == ReturnCondition.LOST ? FineReason.LOST
                    : condition == ReturnCondition.DAMAGED ? FineReason.DAMAGE : FineReason.OVERDUE;
            fineRepository.save(new Fine(issue, fineAmount, reason));
        }

        boolean reservationPromoted = false;
        if (copy.getStatus() == BookCopyStatus.AVAILABLE) {
            Optional<Reservation> promoted = reservationQueueManager.promoteNextWaiting(copy.getBook().getId());
            reservationPromoted = promoted.isPresent();
            if (reservationPromoted) {
                copy.setStatus(BookCopyStatus.RESERVED);
                bookCopyRepository.save(copy);
                notificationService.sendReservationReady(promoted.get());
            }
        }

        String memberName = membershipHolderResolver.resolveDisplayName(
                issue.getMembership().getHolderType(), issue.getMembership().getHolderId());
        auditLogService.log(receivedByUserId, "BOOK_RETURNED", "Issue", issue.getId());
        notificationService.sendReturnReceipt(savedReturn, fineAmount);

        return new ReturnResultDTO(savedReturn.getId(), copy.getBook().getTitle(), memberName,
                returnDate, fineAmount, reservationPromoted);
    }

    private BookCopyStatus resolveCopyStatusAfterReturn(ReturnCondition condition) {
        return switch (condition) {
            case LOST -> BookCopyStatus.LOST;
            case DAMAGED -> BookCopyStatus.MAINTENANCE;
            case GOOD -> BookCopyStatus.AVAILABLE;
        };
    }
}
