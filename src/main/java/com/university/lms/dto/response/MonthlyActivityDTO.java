package com.university.lms.dto.response;

/** One month's worth of circulation activity. {@code yearMonth} is formatted "yyyy-MM". */
public record MonthlyActivityDTO(String yearMonth, long issuedCount, long returnedCount) {
}
