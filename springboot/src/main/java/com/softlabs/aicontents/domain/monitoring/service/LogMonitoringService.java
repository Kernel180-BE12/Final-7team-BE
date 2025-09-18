package com.softlabs.aicontents.domain.monitoring.service;

import com.softlabs.aicontents.domain.monitoring.dto.request.LogSearchRequest;
import com.softlabs.aicontents.domain.monitoring.mapper.UnifiedLogMapper;
import com.softlabs.aicontents.domain.monitoring.vo.response.LogEntryVO;
import com.softlabs.aicontents.domain.monitoring.vo.response.LogListResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogMonitoringService {
  private final UnifiedLogMapper logMapper;

  // 로그 목록 조회 서비스 메서드
  public LogListResponse getLogs(LogSearchRequest request) {
    // 검색 조건을 Map으로 구성
    Map<String, Object> param = new HashMap<>();
    param.put("executionId", request.getExecutionId()); // 실행 ID 조건
    param.put("startDate", request.getStartDate()); // 시작일 조건
    param.put("endDate", request.getEndDate()); // 종료일 조건
    param.put("status", request.getStatus()); // 상태코드 조건
    param.put("logLevel", request.getLogLevel()); // 로그 레벌 조건

    // 조건에 맞는 로그 목록 조회
    List<LogEntryVO> logs = logMapper.findLogsByConditions(param);

    // 응답 DTO 생성 후 반환
    return new LogListResponse(request.getExecutionId(), logs);
  }
}
