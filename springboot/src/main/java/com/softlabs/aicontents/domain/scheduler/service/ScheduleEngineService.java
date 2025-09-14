package com.softlabs.aicontents.domain.scheduler.service;

import com.softlabs.aicontents.common.dto.request.ScheduleTasksRequestDTO;
import com.softlabs.aicontents.common.dto.response.ScheduleTasksResponseDTO;
import com.softlabs.aicontents.domain.scheduler.mapper.ScheduleEngineMapper;
import com.softlabs.aicontents.domain.scheduler.vo.request.SchedulerRequestVO;
import com.softlabs.aicontents.domain.scheduler.vo.response.ScheduleResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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

}