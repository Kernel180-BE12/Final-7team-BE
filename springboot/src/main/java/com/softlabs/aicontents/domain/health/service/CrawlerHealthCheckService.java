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
      // 1. 최근 execution_id에 대한 상태 리스트 조회, 행이 여러개 일 수 있어 list임
      List<Map<String, Object>> statusList = healthCheckMapper.selectKeywordStatus();

      int failCount = 0;

      // 2. 상태별로 순회하면 판단
      for (Map<String, Object> row : statusList) {
        String status = (String) row.get("STATUS"); // 컬럼명은 Oracle 대문자 주의
        int count = ((Number) row.get("CNT")).intValue();

        System.out.println("상태: " + status + ", 횟수: " + count);

        // 상태가 실패인 카운트
        if ("FAILED".equalsIgnoreCase(status)) {
          failCount = count;
        }

        // 성공이 하나라도 있으면 바로 up
        if ("SUCCESS".equalsIgnoreCase(status)) {
          return true; // 성공 있으면 바로 up 처리
        }
      }
      return failCount < 3;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
