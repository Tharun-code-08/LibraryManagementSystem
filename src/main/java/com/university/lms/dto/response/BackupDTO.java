package com.university.lms.dto.response;

import java.time.LocalDateTime;

public record BackupDTO(Long id, String filePath, long sizeBytes, LocalDateTime createdAt, String status) {
}
