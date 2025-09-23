package com.softlabs.aicontents.domain.scheduler.vo.response;

import lombok.Data;

@Data
public class ScheduleResponseVO {

  // TASK 식별자
  private int taskId;
  private String executeImmediately;

}
