package com.university.lms.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.university.lms.dto.response.CategoryDistributionDTO;
import com.university.lms.dto.response.MonthlyActivityDTO;
import com.university.lms.dto.response.PopularBookDTO;
import com.university.lms.entity.BookCopyStatus;

/** Read-only aggregate/analytics queries backing the dashboard — never used to persist anything. */
public interface DashboardRepository {

    long countActiveBooks();

    long countCopiesByStatus(BookCopyStatus status);

    long countOpenOverdueIssues(LocalDateTime asOf);

    long countActiveReservations();

    long countStudents();

    long countFaculty();

    /** One entry per calendar month from {@code since} to now, in chronological order. */
    List<MonthlyActivityDTO> getMonthlyIssuesAndReturns(LocalDate since);

    List<CategoryDistributionDTO> getCategoryDistribution();

    List<PopularBookDTO> getPopularBooks(int limit);
}
