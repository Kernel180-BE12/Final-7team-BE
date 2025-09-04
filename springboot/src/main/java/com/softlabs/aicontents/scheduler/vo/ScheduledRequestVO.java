package com.softlabs.aicontents.scheduler.vo;

public record ScheduledRequestVO(
        String taskName,
        String taskDescription,
        String cronExpression,
        String taskType,
        String isActive,
        Integer maxRetryCount,
        Integer timeoutMinutes
        // ID, 생성일시 등 DB 자동생성 필드 제외
) {}

