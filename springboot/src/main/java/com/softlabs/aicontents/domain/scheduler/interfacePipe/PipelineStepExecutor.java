package com.softlabs.aicontents.domain.scheduler.interfacePipe;

import com.softlabs.aicontents.domain.orchestration.vo.StepExecutionResultVO;
import com.softlabs.aicontents.domain.scheduler.dto.pipeLineDTO.StepExecutionResultDTO;

public interface PipelineStepExecutor {
  // 파이프라인 실행관련 공통 인터페이스

  StepExecutionResultVO execute(int executionId);
}
