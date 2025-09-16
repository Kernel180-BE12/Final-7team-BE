package com.softlabs.aicontents.domain.scheduler.service;

import com.softlabs.aicontents.common.dto.request.ScheduleTasksRequestDTO;
import com.softlabs.aicontents.common.dto.response.PageResponseDTO;
import com.softlabs.aicontents.common.dto.response.ScheduleTasksResponseDTO;
import com.softlabs.aicontents.domain.scheduler.dto.resultDTO.ExecutionCycle;
import com.softlabs.aicontents.domain.scheduler.dto.resultDTO.ScheduleResponseDTO;
import com.softlabs.aicontents.domain.scheduler.mapper.ScheduleEngineMapper;
import com.softlabs.aicontents.domain.scheduler.dto.ScheduleInfoResquestDTO;
import com.softlabs.aicontents.domain.scheduler.vo.request.PagingVO;
import com.softlabs.aicontents.domain.scheduler.vo.request.SchedulerRequestVO;
import com.softlabs.aicontents.domain.scheduler.vo.response.ScheduleInfoResponseVO;
import com.softlabs.aicontents.domain.scheduler.vo.response.ScheduleResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ScheduleEngineService {

    @Autowired
    private ScheduleEngineMapper scheduleEngineMapper;

    @Transactional
    public ScheduleTasksResponseDTO scheduleEngine(ScheduleTasksRequestDTO scheduleTasksRequestDTO) {

        try {
            // 스케줄 생성 및 taskId 반환
            int insertResult = createSchedule(scheduleTasksRequestDTO);
            int taskId = selectSchedule().getTaskId();
            ScheduleTasksResponseDTO resDTO = new ScheduleTasksResponseDTO();
            resDTO.setTaskId(taskId);

            return resDTO;

        } catch (Exception e) {
            throw new RuntimeException(e);
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


    // DB 저장 로직
    public int createSchedule(ScheduleTasksRequestDTO scheduleTasksRequestDTO) {

        SchedulerRequestVO schedulerRequestVO = this.convertDTOtoVO(scheduleTasksRequestDTO);

        int resultInsert = scheduleEngineMapper.insertSchedule(schedulerRequestVO);
        log.info("DB 저장 메퍼 실행 완료");

        return resultInsert;
    }


    public ScheduleResponseVO selectSchedule() {

        ScheduleResponseVO resultSelect = scheduleEngineMapper.selectScheduleEngines();

        return resultSelect;
    }


    public PageResponseDTO<ScheduleResponseDTO> getScheduleInfoList(ScheduleInfoResquestDTO scheduleInfoResquestDTO) {

        int pageNumber = Optional.ofNullable(scheduleInfoResquestDTO.getPage()).orElse(1);
        int pageSize = Optional.ofNullable(scheduleInfoResquestDTO.getLimit()).orElse(10);

        if (pageNumber <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("페이지 번호와 크기는 0보다 커야 합니다.");
        }

        PagingVO pagingVO = new PagingVO(pageNumber, pageSize);

        // 전체 개수 조회
        long totalCount = scheduleEngineMapper.selectScheduleInfoCount();

        // 페이징된 데이터 조회
        List<ScheduleInfoResponseVO> scheduleResList = scheduleEngineMapper.selectScheduleInfo(pagingVO);

        if (scheduleResList == null) {
            scheduleResList = Collections.emptyList();
        }

        // DTO 변환
        List<ScheduleResponseDTO> scheduleResponseDTOList = scheduleResList.stream()
                .filter(Objects::nonNull)
                .map(ScheduleResponseDTO::new)
                .collect(Collectors.toList());

        // 페이징 정보 포함해서 반환
        return new PageResponseDTO<>(scheduleResponseDTOList, pageNumber, pageSize, totalCount);
    }
}