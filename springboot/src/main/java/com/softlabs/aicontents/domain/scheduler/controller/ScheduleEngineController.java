package com.softlabs.aicontents.domain.scheduler.controller;

import com.softlabs.aicontents.domain.scheduler.service.PipelineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Component
@Slf4j
@EnableScheduling
@RestController
@RequestMapping("/v1")
public class ScheduleEngineController {

  private int executionCount = 0;
  private final int MAX_executionCount = 3;
  private boolean isCompleted = false;

  @Autowired private PipelineService pipelineService;

  // 초 분 시 일 월 요일
  // @Scheduled(cron = "0 58 21 * * *")

  @Scheduled(cron = "1/5 * * * * *")
  /// 12시 18분 정적 실행

  /// todo : 1. 파이프라인 3회 실행 후 종료(for문)
  /// todo : 2. 파이프라인 3회 재시도 /예외처리

  /// 10. 파이프라인 실행
  @PostMapping("/pipeline/execute")
  public void executePipline() {
    /// 파이프라인 실행 메서드 호출
    pipelineService.executionPipline();
  }

  /// 11. 파이프라인 상태 조회
  @GetMapping("/pipeline/status/{executionId}")
  public void checkStatus() {
    /// todo : 상태 조회 로직
  }

  /// 12. 파이프라인 제어
  @PostMapping("/pipeline/control/{executionId}")
  public void controlPipeline() {
    /// todo : 파이프라인 제어 로직
  }
}
