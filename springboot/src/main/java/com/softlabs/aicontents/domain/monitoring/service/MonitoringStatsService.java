package com.softlabs.aicontents.domain.monitoring.service;

import com.softlabs.aicontents.domain.monitoring.dto.response.MonitoringStatsSummaryDTO;

public interface MonitoringStatsService {
  MonitoringStatsSummaryDTO getStats();
}
