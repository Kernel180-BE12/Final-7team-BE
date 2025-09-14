package com.softlabs.aicontents.domain.scheduler.vo.request;

import lombok.Data;

//서비스 -> DB로 보내는 객체 보관용 클래스
// 하나의 VO가 모든 데이터를 관리

@Data
public class SchedulerRequestVO {

    /// 스케줄 관련 데이터
    private String executionCycle;  // "매일(A)/ 주간 실행(B)/ 월간 실행(C)"
    private String executionTime;   // "HH:MM" 자동 실행 시간
    private int keywordCount;  // 추출 키워드 수량
    private int publishCount;  // 블로그 발행 수량
    private String aiModel;    // AI 모델명 (예: "OpenAI GPT-4")


    /// 파이프라인 관련 데이터

    int taskId;
    String taskName;
    String taskDescription;
    String cronExpression;
    String taskType;
    boolean isActive;
    int maxRetryCount;
    int timeoutMinutes;
    String nextExecution;
    String lastExecution;
    String pipelineConfig;
    String createdBy;
    String updatedBy;
    String createdAt;
    String updatedAt;

}
