package com.softlabs.aicontents.domain.orchestration.dto;

import java.util.List;
import lombok.Data;

@Data
public class PipeExecuteData {

  private int executionId;
  private int taskId;
  private String status;
  private String estimatedDuration;
  private List<String> stages;
}