package com.university.lms.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentDTO(Long id, Long fineId, BigDecimal amount, String method, LocalDateTime paidAt,
                          String receiptNumber, String receivedByName, String receiptFilePath) {
}
