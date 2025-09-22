package com.softlabs.aicontents.domain.scheduler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softlabs.aicontents.common.dto.request.ScheduleTasksRequestDTO;
import com.softlabs.aicontents.common.dto.response.PageResponseDTO;
import com.softlabs.aicontents.domain.orchestration.PipelineService;
import com.softlabs.aicontents.domain.scheduler.dto.ScheduleInfoResquestDTO;
import com.softlabs.aicontents.domain.scheduler.dto.resultDTO.ScheduleResponseDTO;
import com.softlabs.aicontents.domain.scheduler.mapper.ScheduleEngineMapper;
import com.softlabs.aicontents.domain.scheduler.vo.request.PagingVO;
import com.softlabs.aicontents.domain.scheduler.vo.request.SchedulerRequestVO;
import com.softlabs.aicontents.domain.scheduler.vo.response.ScheduleInfoResponseVO;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ScheduleEngineService {

  @Autowired
  private ScheduleEngineMapper scheduleEngineMapper;
  @Autowired
  private TaskScheduler taskScheduler; //동적 스케줄 시간

  @Autowired private PipelineService pipelineService;

  @Transactional
  public void scheduleEngine(ScheduleTasksRequestDTO scheduleTasksRequestDTO) {

    try {
      // 1. DTO -> VO 변환
      SchedulerRequestVO schedulerRequestVO = this.convertDTOtoVO(scheduleTasksRequestDTO);

      // 2. PIPELINE_CONFIG는 그냥 전체 객체를 JSON으로
      ObjectMapper objectMapper = new ObjectMapper();
      String pipelineConfigJson = objectMapper.writeValueAsString(scheduleTasksRequestDTO);
      schedulerRequestVO.setPipelineConfig(pipelineConfigJson);

      // 3. NEXT_EXCCUTION, LAST_EXCCUTION
      String scheduleType = schedulerRequestVO.getScheduleType();
      LocalDateTime nextExecution =
              calculateNextExecution(scheduleType, schedulerRequestVO.getExecutionTime());
      LocalDateTime lastExecution = calculateLastExecution(scheduleType, nextExecution);
      schedulerRequestVO.setNextExecution(nextExecution);
      schedulerRequestVO.setLastExecution(lastExecution);

      // 4. DB 저장
      int resultInsert = scheduleEngineMapper.insertSchedule(schedulerRequestVO);
      if (resultInsert <= 0) {
        throw new RuntimeException("스케줄 저장 실패");
      }

      // 5. 동적 스케줄 등록 추가
      registerDynamicSchedule(schedulerRequestVO);

      // 6. executeImmediately 플래그 확인 후 즉시 실행
      if (scheduleTasksRequestDTO.isExecuteImmediately()) {
        log.info("ExecuteImmediately=true, 즉시 실행");
        pipelineService.executionPipline();
        log.info(" 즉시 실행 완료");
      }

    } catch (Exception e) {
      throw new RuntimeException("스케줄 저장 중 오류 발생: " + e.getMessage(), e);
    }
  }

  // DTO -> VO로 변환
  private SchedulerRequestVO convertDTOtoVO(ScheduleTasksRequestDTO scheduleTasksRequestDTO) {

    SchedulerRequestVO schedulerRequestVO = new SchedulerRequestVO();

    schedulerRequestVO.setTaskName(scheduleTasksRequestDTO.getTaskName());
    schedulerRequestVO.setCronExpression(scheduleTasksRequestDTO.getCronExpression());
    schedulerRequestVO.setScheduleType(scheduleTasksRequestDTO.getScheduleType());
    schedulerRequestVO.setExecutionTime(scheduleTasksRequestDTO.getExecutionTime());
    schedulerRequestVO.setKeywordCount(scheduleTasksRequestDTO.getKeywordCount());
    schedulerRequestVO.setContentCount(scheduleTasksRequestDTO.getContentCount());
    schedulerRequestVO.setAiModel(scheduleTasksRequestDTO.getAiModel());

    return schedulerRequestVO;
  }

  public PageResponseDTO<ScheduleResponseDTO> getScheduleInfoList(
          ScheduleInfoResquestDTO scheduleInfoResquestDTO) {

    int pageNumber = Optional.ofNullable(scheduleInfoResquestDTO.getPage()).orElse(1);
    int pageSize = Optional.ofNullable(scheduleInfoResquestDTO.getLimit()).orElse(10);

    if (pageNumber <= 0 || pageSize <= 0) {
      throw new IllegalArgumentException("페이지 번호와 크기는 0보다 커야 합니다.");
    }

    PagingVO pagingVO = new PagingVO(pageNumber, pageSize);

    // 전체 개수 조회
    long totalCount = scheduleEngineMapper.selectScheduleInfoCount();

    // 페이징된 데이터 조회
    List<ScheduleInfoResponseVO> scheduleResList =
            scheduleEngineMapper.selectScheduleInfo(pagingVO);

    if (scheduleResList == null) {
      scheduleResList = Collections.emptyList();
    }

    // DTO 변환
    List<ScheduleResponseDTO> scheduleResponseDTOList =
            scheduleResList.stream()
                    .filter(Objects::nonNull)
                    .map(ScheduleResponseDTO::new)
                    .collect(Collectors.toList());

    // 페이징 정보 포함해서 반환
    return new PageResponseDTO<>(scheduleResponseDTOList, pageNumber, pageSize, totalCount);
  }

  private LocalDateTime calculateNextExecution(String executionCycle, String executionTime) {
    LocalDateTime now = LocalDateTime.now();
    LocalTime time = LocalTime.parse(executionTime); // "08:00" -> LocalTime

    switch (executionCycle) {
      case "매일 실행":
        LocalDateTime todayExecution = now.toLocalDate().atTime(time);
        return now.isAfter(todayExecution) ? todayExecution.plusDays(1) : todayExecution;

      case "주간 실행":
        // 다음 주 같은 요일 같은 시간
        return now.toLocalDate().atTime(time).plusWeeks(1);

      case "월간 실행":
        // 다음 달 같은 날 같은 시간
        return now.toLocalDate().atTime(time).plusMonths(1);

      default:
        throw new IllegalArgumentException("지원하지 않는 스케줄 타입: " + executionCycle);
    }
  }

  private LocalDateTime calculateLastExecution(String executionCycle, LocalDateTime nextExecution) {
    switch (executionCycle) {
      case "주간 실행":
        return nextExecution.minusWeeks(1);
      case "월간 실행":
        return nextExecution.minusMonths(1);
      default:
        return nextExecution.minusDays(1); // default는 "매일 실행"으로 간주
    }
  }

  public String createCronExpression(String executionTime, String scheduleType) {

    String[] timeparts = executionTime.split(":");
    int hour = Integer.parseInt(timeparts[0]);
    int minute = Integer.parseInt(timeparts[1]);

    switch (scheduleType) {
      case "매일 실행":
        return String.format("0 %d %d * * *", minute, hour);
      case "주간 실행":
        return String.format("0 %d %d * * MON", hour, minute); //(임시) 매주 월요일 고정
      case "월간 실행":
        return String.format("0 %d %d 1 * *", hour, minute); // (임시)매월 1일
      default:
        throw new IllegalArgumentException("지원하지 않는 스케줄 타입입니다 : " + scheduleType);
    }
  }


  private void registerDynamicSchedule(SchedulerRequestVO schedulerRequestVO) {
    try {
      //크론식 생성
      String cronExpression = createCronExpression(schedulerRequestVO.getExecutionTime()
              , schedulerRequestVO.getScheduleType());

      CronTrigger cronTrigger = new CronTrigger(cronExpression);

      Runnable task = () -> {
        try {
          pipelineService.executionPipline();
          log.info("스케줄 실행 완료: {}", schedulerRequestVO.getTaskName());
        } catch (Exception e) {
          log.error("스케줄 실행 실패: {}", e.getMessage());
        }
      };
      taskScheduler.schedule(task, cronTrigger);
      log.info("동적 스케줄 등록 완료: {}", schedulerRequestVO.getTaskName());
    } catch (Exception e) {
      log.error("동적 스케줄 등록 실패: {}", e.getMessage());
      throw new RuntimeException("스케줄 등록 실패", e);

    }
  }
}

