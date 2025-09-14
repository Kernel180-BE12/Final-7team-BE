package com.softlabs.aicontents.domain.scheduler.mapper;

import com.softlabs.aicontents.domain.scheduler.vo.request.SchedulerRequestVO;
import com.softlabs.aicontents.domain.scheduler.vo.response.ScheduleResponseVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScheduleEngineMapper {

    int insertSchedule( SchedulerRequestVO schedulerRequestVO);

    ScheduleResponseVO selectScheduleEngines();
}
