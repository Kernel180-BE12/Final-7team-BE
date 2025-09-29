package com.softlabs.aicontents.domain.orchestration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteApiResponseDTO {

  // @PostMapping("/execute")
  private boolean success;
  private String message;
  private PipeExecuteData data;
}
