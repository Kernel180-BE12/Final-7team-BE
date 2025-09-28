package com.softlabs.aicontents.domain.monitoring.controller;

import com.softlabs.aicontents.common.dto.response.ApiResponseDTO;
import com.softlabs.aicontents.domain.monitoring.dto.request.LogSearchRequest;
import com.softlabs.aicontents.domain.monitoring.service.LogMonitoringService;
import com.softlabs.aicontents.domain.monitoring.vo.response.LogListResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/monitoring")
@RequiredArgsConstructor
public class LogMonitoringController {
  private final LogMonitoringService logMonitoringService;

  @GetMapping("/logs")
  @Operation(summary = "작업 로그 조회 API", description = "해당하는 작업 ID의 로그를 조회합니다")
  public ResponseEntity<ApiResponseDTO<LogListResponse>> getLogs(LogSearchRequest request) {
    LogListResponse response = logMonitoringService.getLogs(request);
    return ResponseEntity.ok(ApiResponseDTO.success(response));
  }
}
