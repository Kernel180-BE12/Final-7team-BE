package com.softlabs.aicontents.scheduler.vo;

import java.time.LocalDateTime;

public record ScheduledResponseVO(
        Long taskId,
        String taskName,
        String taskDescription,
        String cronExpression,
        String taskType,
        String isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
          // DB에서 조회한 모든 필드 포함

    ) {}