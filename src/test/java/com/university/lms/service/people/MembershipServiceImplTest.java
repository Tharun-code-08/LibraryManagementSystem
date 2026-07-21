package com.university.lms.service.people;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.university.lms.dto.response.MembershipDTO;
import com.university.lms.entity.HolderType;
import com.university.lms.entity.Membership;
import com.university.lms.entity.MembershipType;
import com.university.lms.repository.MembershipRepository;
import com.university.lms.repository.MembershipTypeRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.people.impl.MembershipServiceImpl;

@ExtendWith(MockitoExtension.class)
class MembershipServiceImplTest {

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private MembershipTypeRepository membershipTypeRepository;

    @Mock
    private AuditLogService auditLogService;

    private MembershipServiceImpl membershipService;
    private MembershipType studentStandard;

    @BeforeEach
    void setUp() {
        AuthContext authContext = new AuthContext();
        membershipService = new MembershipServiceImpl(membershipRepository, membershipTypeRepository, auditLogService, authContext);
        studentStandard = new MembershipType("STUDENT_STANDARD", 3, 14, BigDecimal.valueOf(5), 1, 2);

        when(membershipTypeRepository.findById(1L)).thenReturn(Optional.of(studentStandard));
        when(membershipRepository.save(any(Membership.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void assignsNewMembershipWhenNoneActive() {
        when(membershipRepository.findActiveByHolder(HolderType.STUDENT, 10L)).thenReturn(Optional.empty());

        MembershipDTO result = membershipService.assignOrRenew(HolderType.STUDENT, 10L, 1L, 365);

        assertEquals("STUDENT_STANDARD", result.membershipTypeName());
        assertEquals(LocalDate.now().plusDays(365), result.expiryDate());
    }

    @Test
    void renewalExtendsFromCurrentExpiryWhenStillActive() {
        LocalDate currentExpiry = LocalDate.now().plusDays(30);
        Membership existing = new Membership(studentStandard, HolderType.STUDENT, 10L, LocalDate.now().minusDays(300), currentExpiry);
        when(membershipRepository.findActiveByHolder(HolderType.STUDENT, 10L)).thenReturn(Optional.of(existing));

        MembershipDTO result = membershipService.assignOrRenew(HolderType.STUDENT, 10L, 1L, 365);

        assertEquals(currentExpiry.plusDays(365), result.expiryDate());
    }
}
