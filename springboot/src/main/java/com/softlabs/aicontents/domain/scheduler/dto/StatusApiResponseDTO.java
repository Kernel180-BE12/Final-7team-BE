package com.softlabs.aicontents.domain.scheduler.dto;

import com.softlabs.aicontents.domain.scheduler.dto.resultDTO.Logs;
import com.softlabs.aicontents.domain.scheduler.dto.resultDTO.ProgressResult;
import com.softlabs.aicontents.domain.scheduler.dto.resultDTO.StageResults;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
@Schema
public class StatusApiResponseDTO {

  // @GetMapping("/pipeline/status/{executionId}")

  // 실행정보
  int executionId;
  String overallStatus;
  String startedAt;
  String completedAt;
  String currentStage;

  // 각 단계별 진행 상황
  ProgressResult progress;

  // 단계별 결과 데이터
  StageResults stage;

  // 로그 정보
  List<Logs> logs;
}
