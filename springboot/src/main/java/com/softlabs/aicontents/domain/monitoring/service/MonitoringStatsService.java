package com.softlabs.aicontents.domain.monitoring.service;

import com.softlabs.aicontents.domain.monitoring.dto.response.MonitoringStatsResponseDTO;

public interface MonitoringStatsService {
  MonitoringStatsResponseDTO getStats();
}
