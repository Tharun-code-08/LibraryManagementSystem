package com.university.lms.service.analytics;

import java.util.List;

import com.university.lms.dto.response.CategoryDistributionDTO;
import com.university.lms.dto.response.DashboardStatsDTO;
import com.university.lms.dto.response.MonthlyActivityDTO;
import com.university.lms.dto.response.PopularBookDTO;
import com.university.lms.dto.response.RecentActivityDTO;

public interface DashboardService {

    DashboardStatsDTO getStats();

    List<MonthlyActivityDTO> getMonthlyActivity(int monthsBack);

    List<CategoryDistributionDTO> getCategoryDistribution();

    List<PopularBookDTO> getPopularBooks(int limit);

    List<RecentActivityDTO> getRecentActivity(int limit);
}
