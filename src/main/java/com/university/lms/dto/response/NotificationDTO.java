package com.university.lms.dto.response;

import java.time.LocalDateTime;

public record NotificationDTO(Long id, String category, String channel, String message, boolean read,
                              LocalDateTime createdAt, LocalDateTime sentAt) {
}
