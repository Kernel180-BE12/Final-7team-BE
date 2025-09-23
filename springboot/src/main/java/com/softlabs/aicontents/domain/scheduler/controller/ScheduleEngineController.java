package com.softlabs.aicontents.domain.scheduler.controller;

import com.softlabs.aicontents.common.dto.request.ScheduleTasksRequestDTO;
import com.softlabs.aicontents.common.dto.response.ApiResponseDTO;
import com.softlabs.aicontents.common.dto.response.PageResponseDTO;
import com.softlabs.aicontents.domain.orchestration.PipelineService;
import com.softlabs.aicontents.domain.orchestration.dto.ExecuteApiResponseDTO;
import com.softlabs.aicontents.domain.orchestration.mapper.PipelineMapper;
import com.softlabs.aicontents.domain.scheduler.dto.ScheduleInfoResquestDTO;
import com.softlabs.aicontents.domain.scheduler.dto.StatusApiResponseDTO;
import com.softlabs.aicontents.domain.scheduler.dto.resultDTO.ScheduleResponseDTO;
import com.softlabs.aicontents.domain.scheduler.mapper.ScheduleEngineMapper;
import com.softlabs.aicontents.domain.scheduler.service.ScheduleEngineService;
import com.softlabs.aicontents.domain.scheduler.vo.request.SchedulerRequestVO;
import com.softlabs.aicontents.domain.scheduler.vo.response.ScheduleResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
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
  @Autowired private PipelineMapper pipelineMapper;
  @Autowired private ScheduleEngineMapper scheduleEngineMapper;

  /// 08. 스케줄 생성
  @Operation(summary = "스케줄 생성 API", description = "생성할 스케줄의 상세 정보입니다.")
  @PostMapping("/schedule")
  public ApiResponseDTO<String> setSchedule(
      @RequestBody ScheduleTasksRequestDTO scheduleTasksRequestDTO) {

    System.out.println("요청 들어온 executeImmediately 값: " + scheduleTasksRequestDTO.isExecuteImmediately());
    System.out.println("전체 DTO 내용: " + scheduleTasksRequestDTO);

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

  //  @Scheduled(cron = "0 23 14 * * *")

  /** 파이프라인 */
  //
  //  / 10. 파이프라인 실행
  @PostMapping("/pipeline/execute")
  public ExecuteApiResponseDTO executePipline() {

    //taskId불러오는 매퍼
    ScheduleResponseVO scheduleResponseVO = pipelineMapper.selectScheduleResponseVO();
    int taskId = scheduleResponseVO.getTaskId();
    String executeImmediately = scheduleResponseVO.getExecuteImmediately();

    System.out.println("여기야여기여기라고 여기! "+taskId + executeImmediately);

    //executeImmediately 조회 메퍼
// 만일 수동이라면,
    //수동중 가장 최신에 생성된 taskid를 출력하라
    //만일 자동이라면,
    //자동 중 가장 최신에 생성된 takid를 출력하라.

    //어떤 경우든, 파이프라인 execution Id를 생성한 후, 파라메터로 읽어온 takid도 함께 저장해라.



    try {
//      if ("true".equals(executeImmediately)) {

        //수동 실행일 경우,
      ScheduleTasksRequestDTO scheduleTasksRequestDTO = new ScheduleTasksRequestDTO();

      System.out.println("pipeline/execute에서 생성된 빈 ScheduleTasksRequestDTO:");
      System.out.println("executeImmediately 값: " + scheduleTasksRequestDTO.isExecuteImmediately());
      System.out.println("전체 DTO 내용: " + scheduleTasksRequestDTO);
//
      scheduleEngineService.scheduleEngine(scheduleTasksRequestDTO);
      ExecuteApiResponseDTO executeApiResponseDTO = pipelineService.executionPipline();

        return executeApiResponseDTO;

//      } else if ("false".equals(executeImmediately)) {
//        //자동실행일 경우
///       ExecuteApiResponseDTO executeApiResponseDTO = registerDynamicSchedule(schedulerRequestVO);
////        return executeApiResponseDTO;
//        return null;
//      }
    } catch (Exception e) {
    }
    return null;
  }


  /// 11. 파이프라인 상태 조회
  @GetMapping("/pipeline/status/{executionId}")
  public ApiResponseDTO<StatusApiResponseDTO> statusPipeline(@PathVariable int executionId) {

    try {
      StatusApiResponseDTO statusApiResponseDTO = pipelineService.getStatusPipline(executionId);
      String successMesg = "파이프라인 상태 데이터를 pipeResultDataDTO에 저장 완료";

      return ApiResponseDTO.success(statusApiResponseDTO, successMesg);

    } catch (Exception e) {
      return ApiResponseDTO.error("파이프라인 상태 조회 실패");
    }
  }

  //  // GET 요청으로 바로 실행
  //  @GetMapping("/create-execution")
  //  private int testCreateExecution() {
  //
  //    int executionId= pipelineService.createNewExecution();
  //
  //    System.out.println(executionId);
  //    return executionId;

}
