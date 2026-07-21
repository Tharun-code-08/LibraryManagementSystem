package com.university.lms.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReturnResultDTO(Long returnId, String bookTitle, String memberName, LocalDateTime returnDate,
                               BigDecimal fineAmount, boolean reservationPromoted) {
}
