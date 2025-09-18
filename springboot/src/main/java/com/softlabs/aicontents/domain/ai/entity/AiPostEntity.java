package com.softlabs.aicontents.domain.ai.entity;

import lombok.Data;

@Data
public class AiPostEntity {
  private Long postId;
  private Long genId;

  private String title;
  private String metaDescription;
  private String bodyMarkdown;

  private String hashtagsCsv; // 최소 설계
  private String evidenceCsv;
  private String schemaVersion;
}
