package com.softlabs.aicontents.domain.f_001.controller;

import com.softlabs.aicontents.common.util.TraceIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestController {

  @GetMapping("/logging")
  public String testLogging() {
    TraceIdUtil.setNewTraceId();

    log.debug("DEBUG 레벨 로그 테스트");
    log.info("INFO 레벨 로그 테스트");
    log.warn("WARN 레벨 로그 테스트");
    log.error("ERROR 레벨 로그 테스트");

    String traceId = TraceIdUtil.getTraceId();

    TraceIdUtil.clearTraceId();

    return "로깅 테스트 완료. TraceId: " + traceId;
  }
}
