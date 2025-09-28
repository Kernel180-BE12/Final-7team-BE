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
    // System.out.println("파라미터"+ request);

    // 검색 조건을 Map으로 구성
    Map<String, Object> param = new HashMap<>();
    param.put("executionId", request.getExecutionId()); // 실행 ID 조건
    param.put("startDate", request.getStartDate()); // 시작일 조건
    param.put("endDate", request.getEndDate()); // 종료일 조건
    param.put(
        "status",
        request.getStatus() != null ? request.getStatus().toUpperCase() : null); // 상태코드 조건
    param.put(
        "logLevel",
        request.getLogLevel() != null ? request.getLogLevel().toUpperCase() : null); // 로그 레벌 조건
    param.put("limit",request.getLimit());
    param.put("offset",request.getOffset());

    //전체 개수 조회
    long totalCount = logMapper.countLogsByConditions(param);

    // 조건에 맞는 로그 목록 조회
    List<LogEntryVO> logs = logMapper.findLogsByConditions(param);

    //totalPages 계산
    int totalPages =(int) Math.ceil((double)totalCount/request.getLimit());

    // 응답 DTO 생성 후 반환
    return new LogListResponse(
            logs,
            new LogListResponse.Pagination(
                    request.getPage(),
                    totalPages,
                    totalCount,
                    request.getLimit()
            )
    );
  }
}
