package com.university.lms.service.admin;

import java.util.List;
import java.util.Set;

import com.university.lms.dto.response.UserSummaryDTO;
import com.university.lms.entity.UserStatus;

public interface UserManagementService {

    List<UserSummaryDTO> listUsers();

    UserSummaryDTO setStatus(Long userId, UserStatus status, Long actorUserId);

    UserSummaryDTO assignRoles(Long userId, Set<Long> roleIds, Long actorUserId);
}
