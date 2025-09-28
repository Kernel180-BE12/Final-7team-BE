package com.softlabs.aicontents.domain.orchestration.vo;

// import com.softlabs.aicontents.domain.scheduler.dto.resultDTO.ExecutionResults;
import com.softlabs.aicontents.domain.scheduler.dto.resultDTO.Logs;
import com.softlabs.aicontents.domain.scheduler.dto.resultDTO.ProgressResult;
import com.softlabs.aicontents.domain.scheduler.dto.resultDTO.StageResults;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.Data;

@Data
public class PipeStatusResponseVO {

  int executionId;
  int taskId;
  String overallStatus;
  String startedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  String completedAt;
  String currentStage;

  // 각 단계별 진행 상황
  ProgressResult progressResult;

  // 단계별 결과 데이터
  StageResults stageResults;

  // 로그 정보
  List<Logs> logs;
}
