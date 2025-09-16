package com.softlabs.aicontents.domain.datacollector.vo.response;

import lombok.Data;

@Data
public class KeywordHistoryVo {
  private Long keywordId;
  private Long productId;
  private String keyword;
  private String source;
  private String createdAt;
}