package com.university.lms.service.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.university.lms.dto.response.CategoryDistributionDTO;
import com.university.lms.dto.response.DashboardStatsDTO;
import com.university.lms.dto.response.MonthlyActivityDTO;
import com.university.lms.dto.response.RecentActivityDTO;
import com.university.lms.entity.AuditLog;
import com.university.lms.entity.BookCopyStatus;
import com.university.lms.entity.User;
import com.university.lms.repository.AuditLogRepository;
import com.university.lms.repository.DashboardRepository;
import com.university.lms.service.analytics.impl.DashboardServiceImpl;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private DashboardRepository dashboardRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardServiceImpl(dashboardRepository, auditLogRepository);
    }

    @Test
    void aggregatesAllCountsIntoOneStatsObject() {
        when(dashboardRepository.countActiveBooks()).thenReturn(120L);
        when(dashboardRepository.countCopiesByStatus(BookCopyStatus.ISSUED)).thenReturn(30L);
        when(dashboardRepository.countCopiesByStatus(BookCopyStatus.AVAILABLE)).thenReturn(90L);
        when(dashboardRepository.countOpenOverdueIssues(any())).thenReturn(4L);
        when(dashboardRepository.countActiveReservations()).thenReturn(6L);
        when(dashboardRepository.countStudents()).thenReturn(500L);
        when(dashboardRepository.countFaculty()).thenReturn(40L);

        DashboardStatsDTO stats = dashboardService.getStats();

        assertEquals(new DashboardStatsDTO(120, 30, 90, 4, 6, 500, 40), stats);
    }

    @Test
    void delegatesMonthlyActivityToRepositoryWithComputedStartMonth() {
        when(dashboardRepository.getMonthlyIssuesAndReturns(any()))
                .thenReturn(List.of(new MonthlyActivityDTO("2026-07", 10, 8)));

        List<MonthlyActivityDTO> result = dashboardService.getMonthlyActivity(6);

        assertEquals(1, result.size());
        assertEquals("2026-07", result.get(0).yearMonth());
    }

    @Test
    void mapsAuditLogEntriesToRecentActivityDtos() {
        User actor = new User("librarian", "lib@library.local", "hash", null);
        AuditLog log = new AuditLog(actor, "BOOK_ISSUED", "Issue", 42L, null, null, null);
        when(auditLogRepository.findRecent(15)).thenReturn(List.of(log));

        List<RecentActivityDTO> result = dashboardService.getRecentActivity(15);

        assertEquals(1, result.size());
        assertEquals("librarian", result.get(0).actorUsername());
        assertEquals("BOOK_ISSUED", result.get(0).action());
    }

    @Test
    void categoryDistributionPassesThroughUnchanged() {
        when(dashboardRepository.getCategoryDistribution())
                .thenReturn(List.of(new CategoryDistributionDTO("Programming", 25)));

        List<CategoryDistributionDTO> result = dashboardService.getCategoryDistribution();

        assertEquals(1, result.size());
        assertEquals("Programming", result.get(0).categoryName());
    }
}
