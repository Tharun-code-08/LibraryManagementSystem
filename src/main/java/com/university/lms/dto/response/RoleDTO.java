package com.university.lms.dto.response;

import java.util.Set;

public record RoleDTO(Long id, String name, String description, Set<Long> permissionIds, Set<String> permissionCodes) {
}
