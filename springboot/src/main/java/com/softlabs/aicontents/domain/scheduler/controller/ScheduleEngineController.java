package com.softlabs.aicontents.domain.scheduler.controller;

import com.softlabs.aicontents.common.dto.response.ApiResponseDTO;
import com.softlabs.aicontents.domain.orchestration.PipelineService;
import com.softlabs.aicontents.domain.scheduler.dto.PipeResultDataDTO;
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
@RequestMapping("/v1/pipeline")
public class ScheduleEngineController {

  private int executionCount = 0;
  private final int MAX_executionCount = 3;
  private boolean isCompleted = false;

  @Autowired
  private PipelineService pipelineService;

  // 초 분 시 일 월 요일
  // @Scheduled(cron = "0 58 21 * * *")

  @Scheduled(cron = "1/30 * * * * *")
  /// 12시 18분 정적 실행

  /// todo : 1. 파이프라인 3회 실행 후 종료(for문)
  /// todo : 2. 파이프라인 3회 재시도 /예외처리

  /// 10. 파이프라인 실행
//  @PostMapping("/execute")
//  public void executePipline() {
//    /// 파이프라인 실행 메서드 호출
//    pipelineService.executionPipline();
//  }

  /// 11. 파이프라인 상태 조회
  @GetMapping("/status/{executionId}")
  public ApiResponseDTO<PipeResultDataDTO> checkStatus() {

    try {

      PipeResultDataDTO pipeResultDataDTO = pipelineService.executionPipline();
      String successMesg = "파이프라인 상태 데이터를 pipeResultDataDTO에 저장 완료";

      return ApiResponseDTO.success(pipeResultDataDTO, successMesg);

    }catch (Exception e){
      return  ApiResponseDTO.error("파이프라인 상태 조회 실패");
    }
    /// todo : 상태 조회 로직
    /// 파이프 라인이 종료되면, 각 기능들을 지나오면서
    //DB에서 조회한 상태, 키워드, 등등이 VO로 저장되어 있을 것이고
    //이것은 인터페이스에 저장되게 할 것이다.
    //그래서 하나의 파이프라인이 끝나면, 이 VO들이 저장된 상태가 되게 한다.
    // VO 는 대시보드에서 요청하는 DTO
    //결과적으로 VO를 DTO로 보내줘야 함.
  }

  /// 12. 파이프라인 제어
  @PostMapping("/control/{executionId}")
  public void controlPipeline() {
    /// todo : 파이프라인 제어 로직
  }
}
