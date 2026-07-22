package com.university.lms.service.admin.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.university.lms.dto.response.PermissionDTO;
import com.university.lms.dto.response.RoleDTO;
import com.university.lms.entity.Permission;
import com.university.lms.entity.Role;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.PermissionRepository;
import com.university.lms.repository.RoleRepository;
import com.university.lms.service.auth.AuditLogService;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private AuditLogService auditLogService;

    private RoleServiceImpl service;
    private Role role;
    private Permission bookView;
    private Permission bookManage;

    @BeforeEach
    void setUp() {
        service = new RoleServiceImpl(roleRepository, permissionRepository, auditLogService);
        role = new Role("LIBRARIAN", "Day-to-day library operations staff");
        bookView = new Permission("BOOK_VIEW", "View book catalog");
        bookManage = new Permission("BOOK_MANAGE", "Create/edit/delete/restore books");
        setId(bookView, 1L);
        setId(bookManage, 2L);
        lenient().when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void listRolesMapsPermissionCodes() {
        role.getPermissions().add(bookView);
        when(roleRepository.findAll()).thenReturn(List.of(role));

        List<RoleDTO> result = service.listRoles();

        assertEquals(1, result.size());
        assertTrue(result.get(0).permissionCodes().contains("BOOK_VIEW"));
    }

    @Test
    void listPermissionsMapsAllPermissions() {
        when(permissionRepository.findAll()).thenReturn(List.of(bookView, bookManage));

        List<PermissionDTO> result = service.listPermissions();

        assertEquals(2, result.size());
    }

    @Test
    void updatePermissionsReplacesRolePermissionSet() {
        when(roleRepository.findById(10L)).thenReturn(Optional.of(role));
        when(permissionRepository.findAll()).thenReturn(List.of(bookView, bookManage));

        RoleDTO result = service.updatePermissions(10L, Set.of(1L), 100L);

        assertEquals(Set.of("BOOK_VIEW"), result.permissionCodes());
        assertEquals(1, role.getPermissions().size());
    }

    @Test
    void updatePermissionsThrowsWhenRoleMissing() {
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updatePermissions(999L, Set.of(1L), 100L));
    }

    private static void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
