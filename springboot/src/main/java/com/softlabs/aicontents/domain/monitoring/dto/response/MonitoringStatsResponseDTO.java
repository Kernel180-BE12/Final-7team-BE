package com.softlabs.aicontents.domain.monitoring.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
//프론트에 전달할 응답 구조 DTO
//통계 정보 + 로그 리스트 포함
public record MonitoringStatsResponseDTO(
        Stats stats, //성공/실패/성공률 정보
        List<LogEntry> logs //로그 리스트
){
    public record Stats(
            int totalSuccess, //성공 횟수
            int totalFail, //실패 횟수
            float successRate //성공률(%)
    ){}
    public record LogEntry(
            @JsonProperty("message") String message , //메시지
            @JsonProperty("detail") String detail, //상세 설명
            @JsonProperty("status") String status //상태
    ){}

}
