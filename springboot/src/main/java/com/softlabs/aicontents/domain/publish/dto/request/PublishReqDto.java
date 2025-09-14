package com.softlabs.aicontents.domain.publish.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublishReqDto {

  @NotNull
  private  Long aiContentId;
  private  String title;
  private  String metaDescription;
  private  String markdown;
  private  String hashtag;
}