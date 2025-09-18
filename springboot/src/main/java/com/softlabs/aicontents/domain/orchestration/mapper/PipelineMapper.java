package com.softlabs.aicontents.domain.orchestration.mapper;

import com.softlabs.aicontents.domain.orchestration.vo.StepExecutionResultVO;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.KeywordResult;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PipelineMapper {

  KeywordResult selectKeywordStatuscode();

  StepExecutionResultVO selectProductInfoStatuscode();

  StepExecutionResultVO selectAiContentStatuscode();

  StepExecutionResultVO selectPublishStatuscode();
}
