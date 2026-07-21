package com.university.lms.dto.response;

import java.time.LocalDateTime;

public record ReservationDTO(Long id, String bookTitle, String memberName, LocalDateTime requestedAt,
                              int queuePosition, LocalDateTime expiresAt, String status) {
}
