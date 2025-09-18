package com.softlabs.aicontents.domain.scheduler.controller;

import com.softlabs.aicontents.common.dto.request.ScheduleTasksRequestDTO;
import com.softlabs.aicontents.common.dto.response.ApiResponseDTO;
import com.softlabs.aicontents.common.dto.response.PageResponseDTO;
import com.softlabs.aicontents.domain.orchestration.PipelineService;
import com.softlabs.aicontents.domain.scheduler.dto.PipeResultDataDTO;
import com.softlabs.aicontents.domain.scheduler.dto.ScheduleInfoResquestDTO;
import com.softlabs.aicontents.domain.scheduler.dto.resultDTO.ScheduleResponseDTO;
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
  public ApiResponseDTO<String> setSchedule(
      @RequestBody ScheduleTasksRequestDTO scheduleTasksRequestDTO) {

    try {
      scheduleEngineService.scheduleEngine(scheduleTasksRequestDTO);

      return ApiResponseDTO.success("새로운 스케줄 저장 완료");
    } catch (Exception e) {

      return ApiResponseDTO.error("스케줄 저장 실패" + e.getMessage());
    }
  }

  @Operation(summary = "스케줄 정보 출력 API", description = "스케줄 default 정보 출력값")
  @GetMapping("/schedule/list")
  public ApiResponseDTO<PageResponseDTO<ScheduleResponseDTO>> getScheduleList(
      ScheduleInfoResquestDTO scheduleInfoResquestDTO) {
    try {
      // 입력 파라미터 null 체크
      if (scheduleInfoResquestDTO == null) {
        return ApiResponseDTO.error("요청 파라미터가 필요합니다.");
      }

      PageResponseDTO<ScheduleResponseDTO> pageResponse =
          scheduleEngineService.getScheduleInfoList(scheduleInfoResquestDTO);

      // 빈 결과 체크
      if (pageResponse.getContent() == null || pageResponse.getContent().isEmpty()) {
        return ApiResponseDTO.success(pageResponse, "조회된 스케줄이 없습니다.");
      }

      return ApiResponseDTO.success(pageResponse, "스케줄 정보 출력 완료");

    } catch (Exception e) {
      System.out.println("예외 발생: " + e.getMessage());
      e.printStackTrace();
      return ApiResponseDTO.error("스케줄 정보 출력 실패: " + e.getMessage());
    }
  }


  /**
   * 파이프라인
   **/
//
//  / 10. 파이프라인 실행
  @PostMapping("/execute")
  public void executePipline() {
    /// 파이프라인 실행 메서드 호출
    pipelineService.executionPipline();
  }

  /// 11. 파이프라인 상태 조회
  @GetMapping("/pipeline/status/{executionId}")
  public ApiResponseDTO<PipeResultDataDTO> checkStatus() {

    try {
      PipeResultDataDTO pipeResultDataDTO = pipelineService.executionPipline();
      String successMesg = "파이프라인 상태 데이터를 pipeResultDataDTO에 저장 완료";

      return ApiResponseDTO.success(pipeResultDataDTO, successMesg);

    } catch (Exception e) {
      return ApiResponseDTO.error("파이프라인 상태 조회 실패");
    }
    /// todo : 상태 조회 로직
    /// 파이프 라인이 종료되면, 각 기능들을 지나오면서
    //DB에서 조회한 상태, 키워드, 등등이 VO로 저장되어 있을 것이고
    //이것은 인터페이스에 저장되게 할 것이다.
    //그래서 하나의 파이프라인이 끝나면, 이 VO들이 저장된 상태가 되게 한다.
    // VO 는 대시보드에서 요청하는 DTO
    //결과적으로 VO를 DTO로 보내줘야 함.

//
//  /// 12. 파이프라인 제어
//  @PostMapping("/pipeline/control/{executionId}")
//  public void controlPipeline() {
//    /// todo : 파이프라인 제어 로직
//  }

  }


  }
