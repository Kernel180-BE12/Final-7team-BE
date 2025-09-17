package com.softlabs.aicontents.domain.sample.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SampleResponseDTO {

  private String message;
  private String processedData;
  private Long timestamp;

  public static SampleResponseDTO success(String processedData) {
    return new SampleResponseDTO("처리 성공", processedData, System.currentTimeMillis());
  }

  public static SampleResponseDTO of(String message, String data) {
    return new SampleResponseDTO(message, data, System.currentTimeMillis());
  }
}
