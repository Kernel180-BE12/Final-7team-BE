package com.softlabs.aicontents.scheduler.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;


//자바 - DB 데이터 송신 객체

public record PipelineExecutionRequestVO(

        Long executionId,
        Long taskId,
        String executionStatus,
        String executionMode,
        String currentStepCode,
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
    // 새 실행 생성용 (정적)팩토리 메서드 패턴
    // 각 칼럼들의 타입을 미리 작성해 두어 새로 생성할 때 오류 방지
    public static PipelineExecutionRequestVO createNew(Long taskId,String executionMode){
        //메서드 이름은 creatNew
        return new PipelineExecutionRequestVO(
                null,                           // executionId (DB 자동 생성)
                taskId,
                "PENDING",
                executionMode,
                "STEP_01",
                4,
                0,
                BigDecimal.ZERO,
                LocalDateTime.now(),
                null,
                null,
                null,
                0,
                3,
                null,
                null,
                null,
                null,
                null
        );


    }


}

