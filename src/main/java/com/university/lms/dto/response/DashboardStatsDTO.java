package com.university.lms.dto.response;

public record DashboardStatsDTO(long totalBooks, long issuedBooks, long availableBooks, long overdueBooks,
                                 long activeReservations, long totalStudents, long totalFaculty) {
}
