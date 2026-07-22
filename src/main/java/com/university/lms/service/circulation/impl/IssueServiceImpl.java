package com.university.lms.service.circulation.impl;

import java.time.LocalDateTime;

import org.hibernate.exception.ConstraintViolationException;

import com.university.lms.business.BorrowLimitValidator;
import com.university.lms.business.HolderRef;
import com.university.lms.business.MembershipHolderResolver;
import com.university.lms.dto.request.IssueRequestDTO;
import com.university.lms.dto.response.IssueResultDTO;
import com.university.lms.entity.BookCopy;
import com.university.lms.entity.BookCopyStatus;
import com.university.lms.entity.Issue;
import com.university.lms.entity.Membership;
import com.university.lms.entity.User;
import com.university.lms.exception.BookNotAvailableException;
import com.university.lms.exception.NoActiveMembershipException;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.BookCopyRepository;
import com.university.lms.repository.IssueRepository;
import com.university.lms.repository.MembershipRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.security.PermissionEvaluator;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.circulation.IssueService;
import com.university.lms.service.notification.NotificationService;

public final class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepository;
    private final BookCopyRepository bookCopyRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final MembershipHolderResolver membershipHolderResolver;
    private final BorrowLimitValidator borrowLimitValidator;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final PermissionEvaluator permissionEvaluator;

    public IssueServiceImpl(IssueRepository issueRepository, BookCopyRepository bookCopyRepository,
                             MembershipRepository membershipRepository, UserRepository userRepository,
                             MembershipHolderResolver membershipHolderResolver,
                             BorrowLimitValidator borrowLimitValidator, AuditLogService auditLogService,
                             NotificationService notificationService, PermissionEvaluator permissionEvaluator) {
        this.issueRepository = issueRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.membershipHolderResolver = membershipHolderResolver;
        this.borrowLimitValidator = borrowLimitValidator;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
        this.permissionEvaluator = permissionEvaluator;
    }

    @Override
    public IssueResultDTO issueBook(IssueRequestDTO request, Long issuedByUserId) {
        permissionEvaluator.requirePermission("CIRCULATION_MANAGE");
        HolderRef holder = membershipHolderResolver.resolveByIdentifier(request.memberIdentifier())
                .orElseThrow(() -> new NoActiveMembershipException(request.memberIdentifier()));
        Membership membership = membershipRepository.findActiveByHolder(holder.holderType(), holder.holderId())
                .orElseThrow(() -> new NoActiveMembershipException(request.memberIdentifier()));

        BookCopy copy = bookCopyRepository.findByBarcode(request.copyBarcode())
                .orElseThrow(() -> new BookNotAvailableException(request.copyBarcode()));
        if (copy.getStatus() != BookCopyStatus.AVAILABLE) {
            throw new BookNotAvailableException(copy.getBarcode());
        }

        long currentOpenCount = issueRepository.countOpenByMembershipId(membership.getId());
        borrowLimitValidator.validate(membership.getMembershipType(), currentOpenCount);

        User issuedBy = userRepository.findById(issuedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", issuedByUserId));

        LocalDateTime issueDate = LocalDateTime.now();
        LocalDateTime dueDate = issueDate.plusDays(membership.getMembershipType().getLoanPeriodDays());
        Issue issue = new Issue(copy, membership, issuedBy, issueDate, dueDate);
        Issue saved;
        try {
            saved = issueRepository.save(issue);
        } catch (ConstraintViolationException e) {
            // A concurrent issueBook() call for the same copy won the race between our
            // AVAILABLE check above and this insert — the DB's uk_issues_open_copy unique
            // index (see V4__circulation_finance.sql) is the real guard; this just turns the
            // loser's raw Hibernate exception into the same error a stale/already-issued
            // barcode scan would produce.
            throw new BookNotAvailableException(copy.getBarcode());
        }

        copy.setStatus(BookCopyStatus.ISSUED);
        bookCopyRepository.save(copy);

        auditLogService.log(issuedByUserId, "BOOK_ISSUED", "Issue", saved.getId());
        notificationService.sendIssueReceipt(saved);

        return new IssueResultDTO(saved.getId(), copy.getBook().getTitle(), copy.getBarcode(),
                holder.displayName(), request.memberIdentifier(), issueDate, dueDate);
    }
}
