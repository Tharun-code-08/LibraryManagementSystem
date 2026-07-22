package com.university.lms.service.admin.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.university.lms.dto.response.PermissionDTO;
import com.university.lms.dto.response.RoleDTO;
import com.university.lms.entity.Permission;
import com.university.lms.entity.Role;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.PermissionRepository;
import com.university.lms.repository.RoleRepository;
import com.university.lms.service.admin.RoleService;
import com.university.lms.service.auth.AuditLogService;

public final class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuditLogService auditLogService;

    public RoleServiceImpl(RoleRepository roleRepository, PermissionRepository permissionRepository,
                            AuditLogService auditLogService) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public List<RoleDTO> listRoles() {
        return roleRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public List<PermissionDTO> listPermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> new PermissionDTO(p.getId(), p.getCode(), p.getDescription()))
                .toList();
    }

    @Override
    public RoleDTO updatePermissions(Long roleId, Set<Long> permissionIds, Long actorUserId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));
        List<Permission> allPermissions = permissionRepository.findAll();
        Set<Permission> selected = allPermissions.stream()
                .filter(p -> permissionIds.contains(p.getId()))
                .collect(Collectors.toCollection(HashSet::new));
        role.getPermissions().clear();
        role.getPermissions().addAll(selected);
        Role saved = roleRepository.save(role);
        auditLogService.log(actorUserId, "ROLE_PERMISSIONS_CHANGED", "Role", roleId);
        return toDto(saved);
    }

    private RoleDTO toDto(Role role) {
        Set<Long> permissionIds = role.getPermissions().stream().map(Permission::getId).collect(Collectors.toSet());
        Set<String> permissionCodes = role.getPermissions().stream().map(Permission::getCode).collect(Collectors.toSet());
        return new RoleDTO(role.getId(), role.getName(), role.getDescription(), permissionIds, permissionCodes);
    }
}
