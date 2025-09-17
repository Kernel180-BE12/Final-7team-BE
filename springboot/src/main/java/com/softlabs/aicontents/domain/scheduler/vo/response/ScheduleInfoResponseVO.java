package com.softlabs.aicontents.domain.scheduler.vo.response;

import lombok.Data;

@Data
public class ScheduleInfoResponseVO {
  private int scheduleId;
  private String executionCycle; // 실행 주기
  private String executionTime; // 실행 시간
  private int keywordCount; // 키워드 개수
  private int publishCount; // 발행 개수
  private String aiModel; // AI 모델
  private boolean isActive; // 활성화 여부
  private String createdAt; // 생성 시간
  private String nextExecutionAt; // 다음 실행 시간
}
