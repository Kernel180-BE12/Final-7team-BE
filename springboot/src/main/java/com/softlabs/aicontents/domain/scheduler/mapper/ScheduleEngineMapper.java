package com.softlabs.aicontents.domain.scheduler.mapper;

import com.softlabs.aicontents.domain.scheduler.vo.request.PagingVO;
import com.softlabs.aicontents.domain.scheduler.vo.request.SchedulerRequestVO;
import com.softlabs.aicontents.domain.scheduler.vo.response.ScheduleInfoResponseVO;
import com.softlabs.aicontents.domain.scheduler.vo.response.ScheduleResponseVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ScheduleEngineMapper {

    int insertSchedule(SchedulerRequestVO schedulerRequestVO);

    List<ScheduleInfoResponseVO> selectScheduleInfo(PagingVO pagingVO);

    int selectScheduleInfoCount();
}
