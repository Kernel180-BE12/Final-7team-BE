package com.softlabs.aicontents.scheduler.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@EnableScheduling
public class ScheduleEngine {

  private int executionCount = 0;
  private final int MAX_executionCount = 3;
  private boolean isCompleted = false;

  @Autowired private PipelineService pipelineService;

  // 시간에 따른 자동실행
  // 테스트시에만 사용
  // 초 분 시 일 월 요일
  // 0  7  21 *  *  *
  //    @Scheduled(cron = "0 58 21 * * *")
  @Scheduled(cron = "1/5 * * * * *")
  /// 12시 18분 정적 실행

  /// todo : 1. 파이프라인 3회 실행 후 종료(for문)
  /// todo : 2. 파이프라인 3회 재시도 /예외처리

  public void executePipline() {
    /// 파이프라인 실행 메서드 호출
    pipelineService.executionPipline();
  }
}
