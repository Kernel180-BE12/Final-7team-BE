package com.softlabs.aicontents.domain.monitoring.vo.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data // VO: 조회 결과 전달용
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LogEntryVO {
  private Long logId;
  private Long executionId;
  private String stepCode;
  private String sourceTable;
  private String sourceId;
  private String businessKey;
  private String logCategory;
  private String logLevel;
  private String statusCode;
  private String logMessage;
  private LocalDateTime createdAt;
}
