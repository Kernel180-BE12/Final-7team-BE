package com.softlabs.aicontents.domain.health.service;

import com.softlabs.aicontents.domain.health.mapper.HealthCheckMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleHealthCheckService {
  private final HealthCheckMapper healthCheckMapper;

  public boolean isUp() {
    try {
      String isActive = healthCheckMapper.selectScheduledStatus();
      return isActive.equals("Y");
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
