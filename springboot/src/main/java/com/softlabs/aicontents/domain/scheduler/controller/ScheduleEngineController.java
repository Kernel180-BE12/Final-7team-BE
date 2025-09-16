package com.softlabs.aicontents.domain.scheduler.controller;

import com.softlabs.aicontents.common.dto.request.ScheduleTasksRequestDTO;
import com.softlabs.aicontents.common.dto.response.ApiResponseDTO;
import com.softlabs.aicontents.common.dto.response.ScheduleTasksResponseDTO;
import com.softlabs.aicontents.domain.orchestration.PipelineService;
import com.softlabs.aicontents.domain.orchestration.vo.PipeStatusResponseVO;
import com.softlabs.aicontents.domain.scheduler.dto.PipeResultResponseDTO;
import com.softlabs.aicontents.domain.scheduler.service.ScheduleEngineService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@Component
@Slf4j
@EnableScheduling
@RestController
@RequestMapping("/v1")
public class ScheduleEngineController {

  private int executionCount = 0;
  private final int MAX_executionCount = 3;
  private boolean isCompleted = false;

  @Autowired private PipelineService pipelineService; // 파이프라인(=오케스트레이션)
  @Autowired private ScheduleEngineService scheduleEngineService; // 스케줄 엔진

  /// 08. 스케줄 생성
  @Operation(summary = "스케줄 생성 API", description = "생성할 스케줄의 상세 정보입니다.")
  @PostMapping("/schedule")
  public ApiResponseDTO<ScheduleTasksResponseDTO> setSchedule(
      ScheduleTasksRequestDTO scheduleTasksRequestDTO) {

    // 확인 메세지
    System.out.println("scheduleTasksRequestDTO를 전달받음.=>" + scheduleTasksRequestDTO.toString());

    try {
      ScheduleTasksResponseDTO scheduleTasksResponseDTO =
          scheduleEngineService.scheduleEngine(scheduleTasksRequestDTO);

      return ApiResponseDTO.success(scheduleTasksResponseDTO, "새로운 스케줄 저장 완료");
    } catch (Exception e) {

      return ApiResponseDTO.error("스케줄 저장 실패" + e.getMessage());
    }
  }

  // 초 분 시 일 월 요일
  //   @Scheduled(cron = "1/5 * * * * *")

  /// 10. 파이프라인 실행
  @PostMapping("/execute")
  public void executePipline() {
    /// 파이프라인 실행 메서드 호출
    pipelineService.executionPipline();
  }

  /// 11. 파이프라인 상태 조회
  @GetMapping("/pipeline/status/{executionId}")
  public ApiResponseDTO<PipeResultResponseDTO> checkStatus(
      @PathVariable String executionId, PipeStatusResponseVO pipeStatusResponseVO) {
    System.out.println("checkStatus 메서드 시작 - pipeStatusResponseVO: {}" + pipeStatusResponseVO);

    pipelineService.executionPipline();
    try {
      System.out.println("convertVOtoDTO 호출 전");
      PipeResultResponseDTO pipeResultResponseDTO = convertVOtoDTO(pipeStatusResponseVO);
      System.out.println("PipeStatusResponseVO(파이프라인 상태 조회)를 pipeResultResponseDTO 저장 완료");

      String successMesg = "PipeStatusResponseVO(파이프라인 상태 조회)를 pipeResultResponseDTO 반환 완료";
      return ApiResponseDTO.success(pipeResultResponseDTO, successMesg);

    } catch (Exception e) {
      return ApiResponseDTO.error("파이프라인 상태 조회 실패");
    }
    ///// todo : 상태 조회 로직
  }

  private PipeResultResponseDTO convertVOtoDTO(PipeStatusResponseVO pipeStatusResponseVO) {

    if (pipeStatusResponseVO == null) {
      return new PipeResultResponseDTO();
    }

    PipeResultResponseDTO dto = new PipeResultResponseDTO();

    dto.setExecutionId(pipeStatusResponseVO.getExecutionId());
    dto.setOverallStatus(pipeStatusResponseVO.getOverallStatus());
    dto.setStartedAt(pipeStatusResponseVO.getStartedAt());
    dto.setCompletedAt(pipeStatusResponseVO.getCompletedAt());
    dto.setCurrentStage(pipeStatusResponseVO.getCurrentStage());
    dto.setProgressResult(pipeStatusResponseVO.getProgressResult());
    dto.setResults(pipeStatusResponseVO.getResults());
    dto.setLogs(pipeStatusResponseVO.getLogs());

    return dto;
  }
}
//
//  /// 12. 파이프라인 제어
//  @PostMapping("/pipeline/control/{executionId}")
//  public void controlPipeline() {
//    /// todo : 파이프라인 제어 로직
//
//
//  }
//
