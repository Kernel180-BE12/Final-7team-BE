package com.softlabs.aicontents.domain.monitoring.vo.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data // VO: 조회 결과 전달용
public class LogEntryVO {
  private String stepCode; // 파이프라인 단계 코드(ex. f-002)
  private String logLevel; // 로그 레벨(ex. INFO, ERROR)
  private String statusCode; // 상태 코드(ex. SUCCESS)
  private String message; // 로그 메서지 본문
  private LocalDateTime timestamp; // 로그 생성 시각
}
