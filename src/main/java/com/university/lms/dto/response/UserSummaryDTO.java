package com.university.lms.dto.response;

import java.util.Set;

public record UserSummaryDTO(Long id, String username, String email, String status, Set<String> roleNames) {
}
