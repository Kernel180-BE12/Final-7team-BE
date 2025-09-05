package com.softlabs.aicontents.domain.monitoring.dto.response;

public record MonitoringStatsResponse (
        Stats stats,
        java.util.List<LogEntry> logs
){
    public record Stats(
            int totalSuccess,
            int totalFail,
            int successRate
    ){}
    public record LogEntry(
            String message,
            String detail,
            Status status
    ){}
    public enum Status{
        SUCCESS, WAITING, RUNNING, FAILED
    }
}
