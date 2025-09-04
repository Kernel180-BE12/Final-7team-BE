package com.softlabs.aicontents.scheduler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ScheduleRequestDTO(
        @JsonProperty("taskId") Long taskId,
        @JsonProperty("taskName") String taskName,
        @JsonProperty("taskDescription") String taskDescription,
        @JsonProperty("cronExpression") String cronExpression,
        @JsonProperty("taskType") String taskType,
        @JsonProperty("isActive") Boolean isActive,
        @JsonProperty("nextExecution") String nextExecution,
        @JsonProperty("lastExecution") String lastExecution,
        @JsonProperty("status") String status
) {

}