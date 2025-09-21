package com.softlabs.aicontents.domain.health.service;

import com.softlabs.aicontents.domain.health.mapper.HealthCheckMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrawlerHealthCheckService {
  private final HealthCheckMapper healthCheckMapper;

  public boolean isUp() {
    try {
      String isActive = healthCheckMapper.selectKeywordStatus();

      return isActive.equals("Y");
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
