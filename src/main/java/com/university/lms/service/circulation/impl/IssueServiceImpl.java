package com.university.lms.service.circulation.impl;

import java.time.LocalDateTime;

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
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.circulation.IssueService;

public final class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepository;
    private final BookCopyRepository bookCopyRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final MembershipHolderResolver membershipHolderResolver;
    private final BorrowLimitValidator borrowLimitValidator;
    private final AuditLogService auditLogService;

    public IssueServiceImpl(IssueRepository issueRepository, BookCopyRepository bookCopyRepository,
                             MembershipRepository membershipRepository, UserRepository userRepository,
                             MembershipHolderResolver membershipHolderResolver,
                             BorrowLimitValidator borrowLimitValidator, AuditLogService auditLogService) {
        this.issueRepository = issueRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.membershipHolderResolver = membershipHolderResolver;
        this.borrowLimitValidator = borrowLimitValidator;
        this.auditLogService = auditLogService;
    }

    @Override
    public IssueResultDTO issueBook(IssueRequestDTO request, Long issuedByUserId) {
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
        Issue saved = issueRepository.save(issue);

        copy.setStatus(BookCopyStatus.ISSUED);
        bookCopyRepository.save(copy);

        auditLogService.log(issuedByUserId, "BOOK_ISSUED", "Issue", saved.getId());

        return new IssueResultDTO(saved.getId(), copy.getBook().getTitle(), copy.getBarcode(),
                holder.displayName(), request.memberIdentifier(), issueDate, dueDate);
    }
}
