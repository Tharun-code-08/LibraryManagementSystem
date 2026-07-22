package com.university.lms.dto.request;

import java.math.BigDecimal;

/** Input to {@code FineService.createManualFine} — a librarian-initiated adjustment fine. */
public record ManualFineRequestDTO(Long issueId, BigDecimal amount, String reason) {
}
