package com.softlabs.aicontents.scheduler.interfacePipe;

import com.softlabs.aicontents.scheduler.dto.pipeLineDTO.StepExecutionResultDTO;

public interface PipelineStepExecutor {
// 파이프라인 실행관련 공통 인터페이스

    StepExecutionResultDTO execute(Long executionId);

}

