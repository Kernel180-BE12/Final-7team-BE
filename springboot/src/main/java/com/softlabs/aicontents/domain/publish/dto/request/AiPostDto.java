package com.softlabs.aicontents.domain.publish.dto.request;

import java.time.OffsetDateTime;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPostDto {
  private Long postId; // POST_ID
  private Long genId; // GEN_ID
  private String title; // TITLE
  private String metaDescription; // META_DESCRIPTION
  private String bodyMarkdown; // BODY_MARKDOWN (CLOB -> String)
  private String hashtagsCsv; // HASHTAGS_CSV
  private String evidenceCsv; // EVIDENCE_CSV
  private String schemaVersion; // SCHEMA_VERSION
  private OffsetDateTime createdAt; // CREATED_AT (TIMESTAMP WITH TIME ZONE)
}
