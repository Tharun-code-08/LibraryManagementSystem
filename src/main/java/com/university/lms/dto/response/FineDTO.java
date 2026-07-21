package com.university.lms.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FineDTO(Long id, Long issueId, String bookTitle, String memberName, String reason,
                       BigDecimal amount, BigDecimal paidAmount, BigDecimal remainingAmount,
                       String status, LocalDateTime createdAt) {
}
