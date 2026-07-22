package com.university.lms.service.admin.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.university.lms.dto.response.UserSummaryDTO;
import com.university.lms.entity.Role;
import com.university.lms.entity.User;
import com.university.lms.entity.UserStatus;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.RoleRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.service.auth.AuditLogService;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuditLogService auditLogService;

    private UserManagementServiceImpl service;
    private User user;
    private Role librarianRole;

    @BeforeEach
    void setUp() {
        service = new UserManagementServiceImpl(userRepository, roleRepository, auditLogService);
        user = new User("jdoe", "jdoe@university.edu", "hash", null);
        librarianRole = new Role("LIBRARIAN", "Day-to-day library operations staff");
        lenient().when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void listUsersMapsRoleNames() {
        user.setRoles(Set.of(librarianRole));
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserSummaryDTO> result = service.listUsers();

        assertEquals(1, result.size());
        assertTrue(result.get(0).roleNames().contains("LIBRARIAN"));
    }

    @Test
    void setStatusUpdatesAndAudits() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserSummaryDTO result = service.setStatus(1L, UserStatus.LOCKED, 100L);

        assertEquals("LOCKED", result.status());
        assertEquals(UserStatus.LOCKED, user.getStatus());
    }

    @Test
    void setStatusThrowsWhenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.setStatus(99L, UserStatus.LOCKED, 100L));
    }

    @Test
    void assignRolesReplacesRoleSet() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(5L)).thenReturn(Optional.of(librarianRole));

        UserSummaryDTO result = service.assignRoles(1L, Set.of(5L), 100L);

        assertTrue(result.roleNames().contains("LIBRARIAN"));
        assertEquals(1, user.getRoles().size());
    }

    @Test
    void assignRolesThrowsWhenRoleMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.assignRoles(1L, Set.of(404L), 100L));
    }
}
