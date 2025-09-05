package com.softlabs.aicontents.scheduler.controller;

import com.softlabs.aicontents.scheduler.mapper.SchedulerMapper;

public class ExecutionContext {

    // 실행 식별자
    private Long executionId;
    private Long taskId;
    private String currentStepName;

    // DB 접근용
    private SchedulerMapper scheduledMapper;

    // 생성자
    public ExecutionContext(SchedulerMapper scheduledMapper) {
        this.scheduledMapper = scheduledMapper;
    }

    /**
     * 실행 시작시 호출 - DB에 실행 기록 생성
     */
    public void initialize(Long taskId) {
        this.taskId = taskId;

        // TODO: PIPELINE_EXECUTIONS 테이블에 새 실행 기록 생성
        // TODO: executionId 설정

        System.out.println("ExecutionContext 초기화 완료 - TaskID: " + taskId);
    }

    /**
     * 스케줄 작업 설정값 조회
     */
    public String getTaskSetting(String settingKey) {
        // TODO: SCHEDULED_TASKS 테이블에서 설정 조회

        switch(settingKey) {
            case "target_keywords":
                return "기본키워드1,기본키워드2"; // 임시 하드코딩
            case "pipeline_config":
                return "{}"; // 임시 빈 JSON
            default:
                return null;
        }
    }

    /**
     * 이전 단계 결과 조회
     */
    public String getPreviousStepResult(String stepName) {
        // TODO: STEP_RESULTS 테이블에서 해당 단계의 output_data 조회

        System.out.println("이전 단계 결과 조회: " + stepName);
        return "임시_이전단계_결과_데이터"; // 임시 하드코딩
    }

    /**
     * 현재 단계 결과 저장
     */
    public void saveCurrentStepResult(String stepName, String result) {
        this.currentStepName = stepName;

        // TODO: STEP_RESULTS 테이블에 결과 저장
        // TODO: PIPELINE_EXECUTIONS 상태 업데이트

        System.out.println("단계 결과 저장 - " + stepName + ": " + result.substring(0, Math.min(50, result.length())));
    }

    /**
     * 실행 완료 처리
     */
    public void markCompleted() {
        // TODO: PIPELINE_EXECUTIONS 상태를 'COMPLETED'로 변경
        // TODO: end_time 설정

        System.out.println("실행 완료 처리 - ExecutionID: " + executionId);
    }

    /**
     * 실행 실패 처리
     */
    public void markFailed(String errorMessage) {
        // TODO: PIPELINE_EXECUTIONS 상태를 'FAILED'로 변경
        // TODO: error_message 저장

        System.out.println("실행 실패 처리 - " + errorMessage);
    }

    // Getters
    public Long getExecutionId() {
        return executionId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public String getCurrentStepName() {
        return currentStepName;
    }
}