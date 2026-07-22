package com.university.lms.dto.response;

import java.time.LocalDateTime;

public record SettingDTO(Long id, String key, String value, String category, LocalDateTime updatedAt) {
}
