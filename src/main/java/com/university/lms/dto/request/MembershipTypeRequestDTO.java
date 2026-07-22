package com.university.lms.dto.request;

import java.math.BigDecimal;

public record MembershipTypeRequestDTO(Long id, String name, int maxBorrowLimit, int loanPeriodDays,
                                        BigDecimal finePerDay, int gracePeriodDays, int renewalLimit) {
}
