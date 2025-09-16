package com.softlabs.aicontents.domain.datacollector.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class KeywordInfo {
  private Long keywordId;
  private Long productId;
  private String keyword;
  private String source;
  private LocalDateTime createdAt;
  private boolean isUsedRecently;

  public KeywordInfo() {}

  public KeywordInfo(String keyword, String source) {
    this.keyword = keyword;
    this.source = source;
    this.createdAt = LocalDateTime.now();
  }
}