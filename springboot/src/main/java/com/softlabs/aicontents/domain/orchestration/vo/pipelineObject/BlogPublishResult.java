package com.softlabs.aicontents.domain.orchestration.vo.pipelineObject;

import lombok.Data;

@Data
public class BlogPublishResult {

  // 공통
  private int executionId;
  private boolean success;
  private String resultData;
  private String errorMessage;
  private String stepCode;

  // 발행
  private String blogPlatform;
  private String blogPostId;
  private String blogUrl;
  private String publishStatusCode;
}
