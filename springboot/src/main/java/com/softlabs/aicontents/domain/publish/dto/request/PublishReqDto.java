package com.softlabs.aicontents.domain.publish.dto.request;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublishReqDto {
  private Long publishId;
  private Long aiContentId;
  private Long channelId;
  private String channelName;
  private PublishStatus status;
  private String platformPostId;
  private String platformUrl;
  private String rawResponse;
  private String errorMessage;
  private String idempotencyKey;
  private int attemptCount;
  private OffsetDateTime publishedAt;

  public enum PublishStatus {
    SUCCESS,
    FAILED,
    PENDING
  }
}
