package com.softlabs.aicontents.domain.orchestration.mapper;

import com.softlabs.aicontents.domain.orchestration.dto.PipeExecuteData;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.AIContentsResult;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.BlogPublishResult;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.KeywordResult;
import com.softlabs.aicontents.domain.orchestration.vo.pipelineObject.ProductCrawlingResult;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PipelineMapper {

  KeywordResult selectKeywordStatuscode();

  ProductCrawlingResult selctproductCrawlingStatuscode();

  AIContentsResult selectAiContentStatuscode();

  BlogPublishResult selectPublishStatuscode();

  void insertNewExecutionId();

  PipeExecuteData selectNewExecutionId();
}
