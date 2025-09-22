package com.softlabs.aicontents.domain.health.dto.response;

import java.util.Map;

public record SystemHealthDTO(
    String status, // 전체 상태(healthy / degraded / unhealthy)
    Map<String, String> services, // 각 서비스 상태(database, llm 등)
    String version, // 애플리케이션 버전
    String lastChecked // 마지막 헬스체크 실행시각
    ) {}
