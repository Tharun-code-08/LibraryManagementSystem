package com.university.lms.dto.response;

import java.time.LocalDateTime;

public record AuditLogEntryDTO(Long id, String actorUsername, String action, String entityType, Long entityId,
                               LocalDateTime createdAt) {
}
