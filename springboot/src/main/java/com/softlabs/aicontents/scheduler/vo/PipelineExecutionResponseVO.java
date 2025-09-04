package com.softlabs.aicontents.scheduler.vo;


//PIPELINE_EXECUTIONS 테이블 조회용

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PipelineExecutionResponseVO(

        Long executionId,
        Long taskId,
        String executionStatus,        // PENDING, RUNNING, COMPLETED, FAILED
        String executionMode,          // SCHEDULED, MANUAL
        String currentStepCode,        // STEP_01, STEP_02, STEP_03, STEP_04
        Integer totalSteps,
        Integer completedSteps,
        BigDecimal progressPercentage,
        LocalDateTime scheduledTime,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Long durationMs,
        Integer retryCount,
        Integer maxRetries,
        String errorStep,
        String errorMessage,
        String errorCode,
        String executionResult,
        String performanceMetrics


) {
}
