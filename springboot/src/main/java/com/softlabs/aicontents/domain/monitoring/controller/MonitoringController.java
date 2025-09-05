package com.softlabs.aicontents.domain.monitoring.controller;


import com.softlabs.aicontents.domain.monitoring.dto.response.MonitoringStatsResponse;
import com.softlabs.aicontents.domain.monitoring.service.MonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoringController {
    private final MonitoringService monitoringService;
    @GetMapping("/stats")
    @Operation(summary = "결과 모니터링",description = "전체 성공/실패/성공률과 요약 로그를 반환")
    public MonitoringStatsResponse getStats(){
        return monitoringService.getStats();
    }
}
