package com.softlabs.aicontents.domain.scheduler.vo.request;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SchedulerRequestVO {

  /// 스케줄 관련 데이터
  private String scheduleType; // "매일 실행(A)/ 주간 실행(B)/ 월간 실행(C)"
  private String executionTime; // "HH:MM" 자동 실행 시간
  private int keywordCount; // 추출 키워드 수량
  private int contentCount; // 블로그 발행 수량
  private String aiModel; // AI 모델명 (예: "OpenAI GPT-4")
  private LocalDateTime lastExecution; // 마지막 실행 시간
  private LocalDateTime nextExecution; // 다음 실행 시간
  private String pipelineConfig; // 파이프라인 설정 JSON
  private String executeImmediately;

  /// 파이프라인 관련 데이터

  private int taskId;
  private String taskName;
  private String taskDescription;
  private String cronExpression;
  private String taskType;
  private boolean isActive;
  private int maxRetryCount;
  private int timeoutMinutes;
  private String createdBy;
  private String updatedBy;
  private String createdAt;
  private String updatedAt;
}
