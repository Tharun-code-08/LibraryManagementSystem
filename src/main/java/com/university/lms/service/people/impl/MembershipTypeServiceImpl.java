package com.university.lms.service.people.impl;

import java.util.List;

import com.university.lms.dto.request.MembershipTypeRequestDTO;
import com.university.lms.dto.response.MembershipTypeDTO;
import com.university.lms.entity.MembershipType;
import com.university.lms.exception.DuplicateResourceException;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.MembershipTypeRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.people.MembershipTypeService;

public final class MembershipTypeServiceImpl implements MembershipTypeService {

    private final MembershipTypeRepository membershipTypeRepository;
    private final AuditLogService auditLogService;
    private final AuthContext authContext;

    public MembershipTypeServiceImpl(MembershipTypeRepository membershipTypeRepository,
                                      AuditLogService auditLogService, AuthContext authContext) {
        this.membershipTypeRepository = membershipTypeRepository;
        this.auditLogService = auditLogService;
        this.authContext = authContext;
    }

    @Override
    public MembershipTypeDTO save(MembershipTypeRequestDTO request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Membership type name is required.");
        }
        if (request.maxBorrowLimit() < 0 || request.loanPeriodDays() < 0) {
            throw new IllegalArgumentException("Borrow limit and loan period must not be negative.");
        }

        MembershipType membershipType;
        if (request.id() == null) {
            membershipTypeRepository.findByName(request.name()).ifPresent(existing -> {
                throw new DuplicateResourceException("A membership type named '" + request.name() + "' already exists.");
            });
            membershipType = new MembershipType(request.name(), request.maxBorrowLimit(), request.loanPeriodDays(),
                    request.finePerDay(), request.gracePeriodDays(), request.renewalLimit());
        } else {
            membershipType = membershipTypeRepository.findById(request.id())
                    .orElseThrow(() -> new ResourceNotFoundException("MembershipType", request.id()));
            membershipType.setName(request.name());
            membershipType.setMaxBorrowLimit(request.maxBorrowLimit());
            membershipType.setLoanPeriodDays(request.loanPeriodDays());
            membershipType.setFinePerDay(request.finePerDay());
            membershipType.setGracePeriodDays(request.gracePeriodDays());
            membershipType.setRenewalLimit(request.renewalLimit());
        }

        MembershipType saved = membershipTypeRepository.save(membershipType);
        auditLogService.log(currentUserId(), request.id() == null ? "MEMBERSHIP_TYPE_CREATED" : "MEMBERSHIP_TYPE_UPDATED",
                "MembershipType", saved.getId());
        return toDto(saved);
    }

    @Override
    public List<MembershipTypeDTO> listAll() {
        return membershipTypeRepository.findAll().stream().map(this::toDto).toList();
    }

    private Long currentUserId() {
        return authContext.isAuthenticated() ? authContext.getCurrentUser().getId() : null;
    }

    private MembershipTypeDTO toDto(MembershipType type) {
        return new MembershipTypeDTO(type.getId(), type.getName(), type.getMaxBorrowLimit(), type.getLoanPeriodDays(),
                type.getFinePerDay(), type.getGracePeriodDays(), type.getRenewalLimit());
    }
}
