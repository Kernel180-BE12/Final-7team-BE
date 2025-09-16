package com.softlabs.aicontents.common.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 공통 응답 DTO
// FE -> BE(DTO) 대시보드 중 "스케줄 관리" 카드
// 단순 객체 전달용
// 용도 및 의미만 맞추고 XML에는 alias 적용

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "ScheduleTasksRequestDTO - 스케줄 작업 요청 객체")
public class ScheduleTasksRequestDTO {

  @Schema(
      description = "스케줄러 명칭",
      example = "Untitled Schedule",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String taskName;

  @Schema(description = "크론 표현식", example = "0 8 * * *")
  private String cronExpression;

  @Schema(description = "실행 주기", example = "08:00")
  private String scheduleType; // "매일 실행/ 주간 실행/ 월간 실행"

  @Schema(description = "실행 시간", example = "08:00")
  private String executionTime; // "HH:MM" 자동 실행 시간

  @Schema(description = "키워드 추출 개수", example = "50")
  private int keywordCount; // 추출 키워드 수량

  @Schema(description = "블로그 발행 개수", example = "1")
  private int contentCount; // 블로그 발행 수량

  @Schema(description = "AI 모델명", example = "OpenAI GPT-4")
  private String aiModel; // AI 모델명 (예: "OpenAI GPT-4")
}
