package com.university.lms.service.admin.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.university.lms.dto.response.UserSummaryDTO;
import com.university.lms.entity.Role;
import com.university.lms.entity.User;
import com.university.lms.entity.UserStatus;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.RoleRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.service.admin.UserManagementService;
import com.university.lms.service.auth.AuditLogService;

public final class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditLogService auditLogService;

    public UserManagementServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                                      AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public List<UserSummaryDTO> listUsers() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public UserSummaryDTO setStatus(Long userId, UserStatus status, Long actorUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setStatus(status);
        User saved = userRepository.save(user);
        auditLogService.log(actorUserId, "USER_STATUS_CHANGED", "User", userId);
        return toDto(saved);
    }

    @Override
    public UserSummaryDTO assignRoles(Long userId, Set<Long> roleIds, Long actorUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Set<Role> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            roles.add(roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", roleId)));
        }
        user.setRoles(roles);
        User saved = userRepository.save(user);
        auditLogService.log(actorUserId, "USER_ROLES_CHANGED", "User", userId);
        return toDto(saved);
    }

    private UserSummaryDTO toDto(User user) {
        Set<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        return new UserSummaryDTO(user.getId(), user.getUsername(), user.getEmail(),
                user.getStatus().name(), roleNames);
    }
}
