package com.softlabs.aicontents.domain.monitoring.service;

import com.softlabs.aicontents.domain.monitoring.dto.response.MonitoringStatsResponse;

public interface MonitoringService {
    MonitoringStatsResponse getStats();
}
