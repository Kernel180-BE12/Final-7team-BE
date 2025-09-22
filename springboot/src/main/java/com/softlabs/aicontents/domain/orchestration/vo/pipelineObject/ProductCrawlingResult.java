package com.softlabs.aicontents.domain.orchestration.vo.pipelineObject;

import lombok.Data;

@Data
public class ProductCrawlingResult {

  // 공통
  private int executionId;
  private boolean success;
  private String resultData;
  private String errorMessage;
  private String stepCode;

  // 상품 수집 크롤링 실행 객체
  private String keyword;

  // LLM 생성에 필요한 응답 객체
  private String productName;
  private String sourceUrl;
  private Integer price;
  private String productStatusCode;
}
