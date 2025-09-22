package com.softlabs.aicontents.domain.scheduler.dto.resultDTO;

import com.softlabs.aicontents.domain.scheduler.vo.response.ScheduleInfoResponseVO;
import lombok.Data;

@Data
public class ScheduleResponseDTO {

  private int scheduleId;
  private ExecutionCycle executionCycle; // 실행 주기
  private String executionTime; // 실행 시간
  private int keywordCount; // 키워드 개수
  private int publishCount; // 발행 개수
  private String aiModel; // AI 모델
  private boolean isActive; // 활성화 여부
  private String createdAt; // 생성 시간
  private String nextExecutionAt; // 다음 실행 시간

  public ScheduleResponseDTO() {}

  // VO를 받는 생성자
  public ScheduleResponseDTO(ScheduleInfoResponseVO vo) {
    if (vo != null) {
      this.scheduleId = vo.getScheduleId();
      this.executionCycle = ExecutionCycle.fromDisplayName(vo.getExecutionCycle());
      this.executionTime = vo.getExecutionTime();
      this.keywordCount = vo.getKeywordCount();
      this.publishCount = vo.getPublishCount();
      this.aiModel = vo.getAiModel();
      this.isActive = vo.isActive();
      this.createdAt = vo.getCreatedAt();
      this.nextExecutionAt = vo.getNextExecutionAt();
    }
  }
}
