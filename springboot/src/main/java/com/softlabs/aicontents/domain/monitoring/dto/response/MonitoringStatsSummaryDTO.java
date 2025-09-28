package com.softlabs.aicontents.domain.monitoring.dto.response;

import java.util.List;

public record MonitoringStatsSummaryDTO(
    int successCount,
    int failureCount,
    float successRate,
    int totalExecutions,
    int activeExecutions,
    List<ActivityEntry> recentActivities) {}
