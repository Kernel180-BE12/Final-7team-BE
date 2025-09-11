package com.softlabs.aicontents.domain.health.service;

import com.softlabs.aicontents.domain.health.dto.response.SystemHealthDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemHealthService {
    private final DatabaseHealthCheckService dbService; //DB 헬스체크 서비스
    private final LlmHealthCheckService llmservice; //LLM(FastAPI) 헬스체크 서비스

    public SystemHealthDTO getSystemHealth() {
        //1. 개별 서비스 상태 수집
        Map<String, String> services = new HashMap<>();

        services.put("database",dbService.isUp() ? "up" : "down");
        services.put("llm", llmservice.isUp() ? "up" : "down");

        //2.통합 상태 집계
        String status=aggregateStatus(services);

        //3.DTO 생성 후 반환
        return new SystemHealthDTO(
                status, //전체 상태
                services, //개별 서비스 상태 맵
                getVersion(), //버전 정보
                Instant.now().toString()//현재 시간
        );
    }
    //서비스 상태들을 모아 전체 상태를 결정하는 메서드
    private String aggregateStatus(Map<String, String> services) {
        boolean allUp = services.values().stream().allMatch("up"::equals);
        //서비스의 상태가 모두 up
        boolean allDown = services.values().stream().allMatch("down"::equals);
        //서비스의 상태가 모두 down

        if (allUp) return "healthy"; //전체 정상
        if (allDown) return "unhealthy"; //전체 실패
        return "degraded"; //일부 정상
    }

    private long getLastCheckTime() {
        // TODO: 실제 애플리케이션 시작 시각 기준 계산
        return 3600L;
    }

    private String getVersion() {
        return "1.0.0"; //
    }
}
