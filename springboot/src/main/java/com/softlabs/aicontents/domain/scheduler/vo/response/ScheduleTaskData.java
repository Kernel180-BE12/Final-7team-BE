package com.softlabs.aicontents.domain.scheduler.vo.response;

import lombok.Data;

@Data
public class ScheduleTaskData {

  private int taskId;
  private String executeImmediately;
}
