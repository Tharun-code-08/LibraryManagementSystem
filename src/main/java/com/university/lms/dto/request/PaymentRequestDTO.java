package com.university.lms.dto.request;

import java.math.BigDecimal;

/** Input to {@code PaymentService.collectPayment}. {@code method} is one of CASH/CARD/ONLINE. */
public record PaymentRequestDTO(Long fineId, BigDecimal amount, String method) {
}
