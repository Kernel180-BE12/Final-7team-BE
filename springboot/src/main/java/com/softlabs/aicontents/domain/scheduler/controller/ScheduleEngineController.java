package com.softlabs.aicontents.domain.scheduler.controller;

import com.softlabs.aicontents.common.dto.request.ScheduleTasksRequestDTO;
import com.softlabs.aicontents.common.dto.response.ApiResponseDTO;
import com.softlabs.aicontents.common.dto.response.PageResponseDTO;
import com.softlabs.aicontents.common.dto.response.ScheduleTaskResponseDTO;
import com.softlabs.aicontents.domain.orchestration.PipelineService;
import com.softlabs.aicontents.domain.orchestration.dto.ExecuteApiResponseDTO;
import com.softlabs.aicontents.domain.orchestration.dto.PipeExecuteData;
import com.softlabs.aicontents.domain.orchestration.dto.PipeStatusExcIdReqDTO;
import com.softlabs.aicontents.domain.orchestration.mapper.LogMapper;
import com.softlabs.aicontents.domain.orchestration.mapper.PipelineMapper;
import com.softlabs.aicontents.domain.scheduler.dto.ScheduleInfoResquestDTO;
import com.softlabs.aicontents.domain.scheduler.dto.StatusApiResponseDTO;
import com.softlabs.aicontents.domain.scheduler.dto.resultDTO.*;
import java.util.ArrayList;
import java.util.List;
import com.softlabs.aicontents.domain.scheduler.mapper.ScheduleEngineMapper;
import com.softlabs.aicontents.domain.scheduler.service.ScheduleEngineService;
import com.softlabs.aicontents.domain.scheduler.vo.response.ScheduleResponseVO;
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
  @Autowired private PipelineMapper pipelineMapper;
  @Autowired private ScheduleEngineMapper scheduleEngineMapper;
  @Autowired private LogMapper logMapper;

  /// 08. 스케줄 생성
  @Operation(summary = "스케줄 생성 API", description = "생성할 스케줄의 상세 정보입니다.")
  @PostMapping("/schedule")
  public ApiResponseDTO<ScheduleTaskResponseDTO> setSchedule(
      @RequestBody ScheduleTasksRequestDTO scheduleTasksRequestDTO) {

    try {
      ScheduleTaskResponseDTO scheduleTaskResponseDTO = scheduleEngineService.scheduleEngine(scheduleTasksRequestDTO);

      return ApiResponseDTO.success(scheduleTaskResponseDTO,"새로운 스케줄 저장 완료");
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

  /** 파이프라인 */
  //
//   10. 파이프라인 실행
//  @PostMapping("/pipeline/execute")
//  public ExecuteApiResponseDTO executePipline() {
//   ExecuteApiResponseDTO executeApiResponseDTO = new ExecuteApiResponseDTO();
//    try {
//      // 수동실행일 경우,
//
//      return pipelineService.executionPipline();
//    } catch (Exception e) {
//      executeApiResponseDTO.setSuccess(false);
//      executeApiResponseDTO.setMessage(e.getMessage());
//      return executeApiResponseDTO;
//    }
//  }

  /// 11. 파이프라인 상태 조회
  @GetMapping("/pipeline/status/{executionId}")
  public ApiResponseDTO<StatusApiResponseDTO> executePipline(@PathVariable int executionId) {

    try {
      PipeStatusExcIdReqDTO reqDTO = new PipeStatusExcIdReqDTO();
      reqDTO.setExecutionId(executionId);

      StatusApiResponseDTO statusApiResponseDTO = pipelineService.executionPipline(reqDTO);

      // NULL 체크 추가
      if (statusApiResponseDTO == null) {
        StatusApiResponseDTO fallbackResponse = createFallbackResponse(executionId, "FAILED", "Internal Server Error");
        return ApiResponseDTO.success(fallbackResponse, "Pipeline status retrieved with fallback");
      }
      System.out.print("\n\n\n\n\n\n"+statusApiResponseDTO+"\n\n\n\n\n\n");
      String successMesg = "파이프라인 상태 데이터를 pipeResultDataDTO에 저장 완료";


      return ApiResponseDTO.success(statusApiResponseDTO, successMesg);

    }catch (Exception e) {
      log.error("파이프라인 상태 조회 중 예외 발생: executionId={}, error={}", executionId, e.getMessage(), e);
      StatusApiResponseDTO fallbackResponse = createFallbackResponse(executionId, "FAILED", "Pipeline status query failed: " + e.getMessage());
      return ApiResponseDTO.success(fallbackResponse, "Pipeline status retrieved with error fallback");
    }
  }

  /**
   * NULL이나 예외 발생 시 기본 객체 생성
   */
  private StatusApiResponseDTO createFallbackResponse(int executionId, String status, String errorMessage) {
    StatusApiResponseDTO fallbackResponse = new StatusApiResponseDTO();

    // 필수 정보 설정
    fallbackResponse.setExecutionId(executionId);
    fallbackResponse.setOverallStatus(status);
    fallbackResponse.setCurrentStage("ERROR");
    fallbackResponse.setCompletedAt(java.time.LocalDateTime.now().toString());

    // Progress 객체 안전 초기화
    ProgressResult progressResult = new ProgressResult();
    progressResult.setKeywordExtraction(new KeywordExtraction());
    progressResult.setProductCrawling(new ProductCrawling());
    progressResult.setContentGeneration(new ContentGeneration());
    progressResult.setContentPublishing(new ContentPublishing());

    // 모든 진행상태를 실패로 설정
    progressResult.getKeywordExtraction().setStatus("FAILED");
    progressResult.getKeywordExtraction().setProgress(0);
    progressResult.getProductCrawling().setStatus("FAILED");
    progressResult.getProductCrawling().setProgress(0);
    progressResult.getContentGeneration().setStatus("FAILED");
    progressResult.getContentGeneration().setProgress(0);
    progressResult.getContentPublishing().setStatus("FAILED");
    progressResult.getContentPublishing().setProgress(0);

    fallbackResponse.setProgress(progressResult);

    // Stage 객체 안전 초기화
    StageResults stageResults = new StageResults();
    stageResults.setKeywords(new ArrayList<>());
    stageResults.setProducts(new ArrayList<>());
    stageResults.setContent(new Content());
    stageResults.setPublishingStatus(new PublishingStatus());
    fallbackResponse.setStage(stageResults);

    // 로그 정보
    List<Logs> logs = new ArrayList<>();
    Logs errorLog = new Logs();
    errorLog.setStage("ERROR");
    errorLog.setMessage(errorMessage);
    errorLog.setTimestamp(java.time.LocalDateTime.now().toString());
    logs.add(errorLog);
    fallbackResponse.setLogs(logs);

    return fallbackResponse;
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
