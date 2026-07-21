package com.university.lms.dto.response;

import java.math.BigDecimal;

public record MembershipTypeDTO(Long id, String name, int maxBorrowLimit, int loanPeriodDays,
                                 BigDecimal finePerDay, int gracePeriodDays, int renewalLimit) {
}
