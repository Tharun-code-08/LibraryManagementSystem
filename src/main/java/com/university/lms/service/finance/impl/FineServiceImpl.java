package com.university.lms.service.finance.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.university.lms.business.MembershipHolderResolver;
import com.university.lms.dto.request.FineSearchCriteria;
import com.university.lms.dto.request.ManualFineRequestDTO;
import com.university.lms.dto.response.FineDTO;
import com.university.lms.entity.Fine;
import com.university.lms.entity.FineReason;
import com.university.lms.entity.FineStatus;
import com.university.lms.entity.Issue;
import com.university.lms.exception.FineAlreadySettledException;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.model.Page;
import com.university.lms.repository.FineRepository;
import com.university.lms.repository.IssueRepository;
import com.university.lms.repository.PaymentRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.finance.FineService;

public final class FineServiceImpl implements FineService {

    private final FineRepository fineRepository;
    private final IssueRepository issueRepository;
    private final PaymentRepository paymentRepository;
    private final MembershipHolderResolver membershipHolderResolver;
    private final AuditLogService auditLogService;
    private final AuthContext authContext;

    public FineServiceImpl(FineRepository fineRepository, IssueRepository issueRepository,
                            PaymentRepository paymentRepository, MembershipHolderResolver membershipHolderResolver,
                            AuditLogService auditLogService, AuthContext authContext) {
        this.fineRepository = fineRepository;
        this.issueRepository = issueRepository;
        this.paymentRepository = paymentRepository;
        this.membershipHolderResolver = membershipHolderResolver;
        this.auditLogService = auditLogService;
        this.authContext = authContext;
    }

    @Override
    public Page<FineDTO> search(FineSearchCriteria criteria) {
        List<FineDTO> content = fineRepository.search(criteria).stream().map(this::toDto).toList();
        long total = fineRepository.countSearchResults(criteria);
        return new Page<>(content, criteria.getPageNumber(), criteria.getPageSize(), total);
    }

    @Override
    public Optional<FineDTO> getById(Long id) {
        return fineRepository.findById(id).map(this::toDto);
    }

    @Override
    public FineDTO createManualFine(ManualFineRequestDTO request) {
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Manual fine amount must be positive.");
        }
        Issue issue = issueRepository.findById(request.issueId())
                .orElseThrow(() -> new ResourceNotFoundException("Issue", request.issueId()));

        Fine fine = new Fine(issue, request.amount(), FineReason.MANUAL);
        Fine saved = fineRepository.save(fine);
        auditLogService.log(currentUserId(), "FINE_MANUAL_CREATED", "Fine", saved.getId());
        return toDto(saved);
    }

    @Override
    public FineDTO waive(Long fineId, Long waivedByUserId) {
        Fine fine = fineRepository.findById(fineId).orElseThrow(() -> new ResourceNotFoundException("Fine", fineId));
        if (fine.getStatus() == FineStatus.PAID || fine.getStatus() == FineStatus.WAIVED) {
            throw new FineAlreadySettledException();
        }
        fine.setStatus(FineStatus.WAIVED);
        Fine saved = fineRepository.save(fine);
        auditLogService.log(waivedByUserId != null ? waivedByUserId : currentUserId(), "FINE_WAIVED", "Fine", saved.getId());
        return toDto(saved);
    }

    private Long currentUserId() {
        return authContext.isAuthenticated() ? authContext.getCurrentUser().getId() : null;
    }

    private FineDTO toDto(Fine fine) {
        BigDecimal paidAmount = paymentRepository.sumAmountByFineId(fine.getId());
        BigDecimal remaining = fine.getAmount().subtract(paidAmount).max(BigDecimal.ZERO);
        String memberName = membershipHolderResolver.resolveDisplayName(
                fine.getIssue().getMembership().getHolderType(), fine.getIssue().getMembership().getHolderId());

        return new FineDTO(fine.getId(), fine.getIssue().getId(), fine.getIssue().getBookCopy().getBook().getTitle(),
                memberName, fine.getReason().name(), fine.getAmount(), paidAmount, remaining,
                fine.getStatus().name(), fine.getCreatedAt());
    }
}
