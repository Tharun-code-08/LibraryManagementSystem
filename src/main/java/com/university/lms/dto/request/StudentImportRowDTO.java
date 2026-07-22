package com.university.lms.dto.request;

/** One row of a bulk student-registration Excel import. */
public record StudentImportRowDTO(int rowNumber, String username, String email, String studentId,
                                   String rollNumber, String department, Integer year,
                                   Integer semester, String phone) {
}
