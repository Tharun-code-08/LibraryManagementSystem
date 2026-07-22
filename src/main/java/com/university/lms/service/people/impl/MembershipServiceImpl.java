package com.university.lms.service.people.impl;

import java.time.LocalDate;
import java.util.Optional;

import com.university.lms.dto.response.MembershipDTO;
import com.university.lms.entity.HolderType;
import com.university.lms.entity.Membership;
import com.university.lms.entity.MembershipStatus;
import com.university.lms.entity.MembershipType;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.MembershipRepository;
import com.university.lms.repository.MembershipTypeRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.security.PermissionEvaluator;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.people.MembershipService;

public final class MembershipServiceImpl implements MembershipService {

    private final MembershipRepository membershipRepository;
    private final MembershipTypeRepository membershipTypeRepository;
    private final AuditLogService auditLogService;
    private final AuthContext authContext;
    private final PermissionEvaluator permissionEvaluator;

    public MembershipServiceImpl(MembershipRepository membershipRepository,
                                  MembershipTypeRepository membershipTypeRepository,
                                  AuditLogService auditLogService, AuthContext authContext,
                                  PermissionEvaluator permissionEvaluator) {
        this.membershipRepository = membershipRepository;
        this.membershipTypeRepository = membershipTypeRepository;
        this.auditLogService = auditLogService;
        this.authContext = authContext;
        this.permissionEvaluator = permissionEvaluator;
    }

    @Override
    public MembershipDTO assignOrRenew(HolderType holderType, Long holderId, Long membershipTypeId, int validityDays) {
        permissionEvaluator.requirePermission("PEOPLE_MANAGE");
        MembershipType membershipType = membershipTypeRepository.findById(membershipTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("MembershipType", membershipTypeId));

        Optional<Membership> existing = membershipRepository.findActiveByHolder(holderType, holderId);
        Membership membership;
        String action;
        if (existing.isPresent()) {
            membership = existing.get();
            LocalDate base = membership.getExpiryDate().isAfter(LocalDate.now()) ? membership.getExpiryDate() : LocalDate.now();
            membership.setExpiryDate(base.plusDays(validityDays));
            membership.setMembershipType(membershipType);
            action = "MEMBERSHIP_RENEWED";
        } else {
            membership = new Membership(membershipType, holderType, holderId, LocalDate.now(), LocalDate.now().plusDays(validityDays));
            action = "MEMBERSHIP_ASSIGNED";
        }

        Membership saved = membershipRepository.save(membership);
        auditLogService.log(currentUserId(), action, "Membership", saved.getId());
        return toDto(saved);
    }

    @Override
    public Optional<MembershipDTO> getActiveMembership(HolderType holderType, Long holderId) {
        return membershipRepository.findActiveByHolder(holderType, holderId).map(this::toDto);
    }

    private Long currentUserId() {
        return authContext.isAuthenticated() ? authContext.getCurrentUser().getId() : null;
    }

    private MembershipDTO toDto(Membership membership) {
        return new MembershipDTO(membership.getId(), membership.getHolderType().name(), membership.getHolderId(),
                membership.getMembershipType().getName(), membership.getStartDate(), membership.getExpiryDate(),
                membership.getStatus().name());
    }
}
