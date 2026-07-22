package com.university.lms.service.people;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.university.lms.dto.request.MembershipTypeRequestDTO;
import com.university.lms.dto.response.MembershipTypeDTO;
import com.university.lms.entity.MembershipType;
import com.university.lms.exception.DuplicateResourceException;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.MembershipTypeRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.people.impl.MembershipTypeServiceImpl;

@ExtendWith(MockitoExtension.class)
class MembershipTypeServiceImplTest {

    @Mock
    private MembershipTypeRepository membershipTypeRepository;

    @Mock
    private AuditLogService auditLogService;

    private MembershipTypeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MembershipTypeServiceImpl(membershipTypeRepository, auditLogService, new AuthContext());
        lenient().when(membershipTypeRepository.save(any(MembershipType.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsNewMembershipType() {
        when(membershipTypeRepository.findByName("STUDENT_STANDARD")).thenReturn(Optional.empty());

        MembershipTypeDTO result = service.save(
                new MembershipTypeRequestDTO(null, "STUDENT_STANDARD", 3, 14, BigDecimal.valueOf(5), 1, 2));

        assertEquals("STUDENT_STANDARD", result.name());
    }

    @Test
    void rejectsDuplicateNameOnCreate() {
        when(membershipTypeRepository.findByName("STUDENT_STANDARD")).thenReturn(
                Optional.of(new MembershipType("STUDENT_STANDARD", 3, 14, BigDecimal.valueOf(5), 1, 2)));

        assertThrows(DuplicateResourceException.class, () -> service.save(
                new MembershipTypeRequestDTO(null, "STUDENT_STANDARD", 3, 14, BigDecimal.valueOf(5), 1, 2)));
    }

    @Test
    void rejectsNegativeBorrowLimit() {
        assertThrows(IllegalArgumentException.class, () -> service.save(
                new MembershipTypeRequestDTO(null, "X", -1, 14, BigDecimal.valueOf(5), 1, 2)));
    }

    @Test
    void updatesExistingMembershipType() {
        MembershipType existing = new MembershipType("OLD", 1, 7, BigDecimal.ONE, 0, 1);
        when(membershipTypeRepository.findById(5L)).thenReturn(Optional.of(existing));

        MembershipTypeDTO result = service.save(
                new MembershipTypeRequestDTO(5L, "NEW", 5, 21, BigDecimal.valueOf(10), 2, 3));

        assertEquals("NEW", result.name());
        assertEquals(5, result.maxBorrowLimit());
    }

    @Test
    void updateThrowsWhenMissing() {
        when(membershipTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.save(
                new MembershipTypeRequestDTO(99L, "X", 1, 1, BigDecimal.ONE, 0, 1)));
    }

    @Test
    void listsAllMembershipTypes() {
        when(membershipTypeRepository.findAll()).thenReturn(
                List.of(new MembershipType("A", 1, 1, BigDecimal.ONE, 0, 1)));

        assertEquals(1, service.listAll().size());
    }
}
