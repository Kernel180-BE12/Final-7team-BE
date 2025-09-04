package com.softlabs.aicontents.scheduler.dto;

public record ScheduleResponseDTO(
        Long taskId,
        String taskName,
        String taskDescription,
        String cronExpression,
        String taskType,
        boolean isActive,
        String nextExecution,
        String lastExecution,
        String status
) {}