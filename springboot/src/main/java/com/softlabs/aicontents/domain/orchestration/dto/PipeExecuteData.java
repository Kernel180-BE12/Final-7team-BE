package com.softlabs.aicontents.domain.orchestration.dto;

import lombok.Data;

import java.util.List;

@Data
public class PipeExecuteData {

    private int executionId;
    private String status;
    private String estimatedDuration;
    private List<String> stages;


}
