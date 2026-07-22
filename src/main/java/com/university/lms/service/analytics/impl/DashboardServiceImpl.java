package com.university.lms.service.analytics.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.university.lms.dto.response.CategoryDistributionDTO;
import com.university.lms.dto.response.DashboardStatsDTO;
import com.university.lms.dto.response.MonthlyActivityDTO;
import com.university.lms.dto.response.PopularBookDTO;
import com.university.lms.dto.response.RecentActivityDTO;
import com.university.lms.entity.AuditLog;
import com.university.lms.entity.BookCopyStatus;
import com.university.lms.repository.AuditLogRepository;
import com.university.lms.repository.DashboardRepository;
import com.university.lms.service.analytics.DashboardService;

public final class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;
    private final AuditLogRepository auditLogRepository;

    public DashboardServiceImpl(DashboardRepository dashboardRepository, AuditLogRepository auditLogRepository) {
        this.dashboardRepository = dashboardRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public DashboardStatsDTO getStats() {
        return new DashboardStatsDTO(
                dashboardRepository.countActiveBooks(),
                dashboardRepository.countCopiesByStatus(BookCopyStatus.ISSUED),
                dashboardRepository.countCopiesByStatus(BookCopyStatus.AVAILABLE),
                dashboardRepository.countOpenOverdueIssues(LocalDateTime.now()),
                dashboardRepository.countActiveReservations(),
                dashboardRepository.countStudents(),
                dashboardRepository.countFaculty());
    }

    @Override
    public List<MonthlyActivityDTO> getMonthlyActivity(int monthsBack) {
        LocalDate since = LocalDate.now().minusMonths(Math.max(monthsBack - 1, 0)).withDayOfMonth(1);
        return dashboardRepository.getMonthlyIssuesAndReturns(since);
    }

    @Override
    public List<CategoryDistributionDTO> getCategoryDistribution() {
        return dashboardRepository.getCategoryDistribution();
    }

    @Override
    public List<PopularBookDTO> getPopularBooks(int limit) {
        return dashboardRepository.getPopularBooks(limit);
    }

    @Override
    public List<RecentActivityDTO> getRecentActivity(int limit) {
        return auditLogRepository.findRecent(limit).stream().map(this::toDto).toList();
    }

    private RecentActivityDTO toDto(AuditLog log) {
        String actorUsername = log.getUser() != null ? log.getUser().getUsername() : "system";
        return new RecentActivityDTO(actorUsername, log.getAction(), log.getEntityType(), log.getEntityId(), log.getCreatedAt());
    }
}
