package com.softlabs.aicontents.domain.monitoring.controller;


import com.softlabs.aicontents.domain.monitoring.dto.response.MonitoringStatsResponseDTO;
import com.softlabs.aicontents.domain.monitoring.service.MonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController //JSON형식으로 응답
@RequestMapping("/v1/monitoring")
@RequiredArgsConstructor
public class MonitoringController {
    private final MonitoringService monitoringService;

    //통계 + 로그 리스트 반환
    @GetMapping("/stats")
    @Operation(summary = "결과 모니터링",description = "전체 성공/실패/성공률과 요약 로그를 반환")
    public MonitoringStatsResponseDTO getStats(){
        return monitoringService.getStats();
    }
}
