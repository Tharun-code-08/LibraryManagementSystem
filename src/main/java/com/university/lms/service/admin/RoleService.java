package com.university.lms.service.admin;

import java.util.List;
import java.util.Set;

import com.university.lms.dto.response.PermissionDTO;
import com.university.lms.dto.response.RoleDTO;

public interface RoleService {

    List<RoleDTO> listRoles();

    List<PermissionDTO> listPermissions();

    RoleDTO updatePermissions(Long roleId, Set<Long> permissionIds, Long actorUserId);
}
