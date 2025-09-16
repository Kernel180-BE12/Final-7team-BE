package com.softlabs.aicontents.domain.orchestration.vo;

import java.time.LocalDateTime;

public class PipeStatusRequestVO {

  private int taskId; // 스케줄링 작업 ID
  private int executionId; // 파이프라인 ID

  private String stepCode; // 특정 단계 조회 (F-002, F-003 등)
  private String businessKey; // 비즈니스 키 (키워드명)

  private LocalDateTime fromDate; // 조회 시작일시
  private LocalDateTime toDate; // 조회 종료일시
}
