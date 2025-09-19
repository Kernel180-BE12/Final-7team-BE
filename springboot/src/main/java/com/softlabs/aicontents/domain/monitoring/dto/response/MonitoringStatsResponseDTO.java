package com.softlabs.aicontents.domain.monitoring.dto.response;

import java.util.List;

//프론트에 전달할 응답 구조 DTO
public record MonitoringStatsResponseDTO (Stats stats, List<LogEntry> log){
    public record Stats(
            int totalSuccess, //성공횟수
            int totalFail, //실패횟수
            float successRate //성공률(%)
    ){}
    public record LogEntry(
            String message, //메서지
            String detail, //상세 설명
            String status //상태
    ){}
}
