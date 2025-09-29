package com.softlabs.aicontents.domain.scheduler.mapper;

import com.softlabs.aicontents.domain.scheduler.vo.request.PagingVO;
import com.softlabs.aicontents.domain.scheduler.vo.request.SchedulerRequestVO;
import com.softlabs.aicontents.domain.scheduler.vo.response.ScheduleInfoResponseVO;
import com.softlabs.aicontents.domain.scheduler.vo.response.ScheduleResponseVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScheduleEngineMapper {

  int insertSchedule(SchedulerRequestVO schedulerRequestVO);

  ScheduleResponseVO selectScheduleResponseVO(int taskId);

  List<ScheduleInfoResponseVO> selectScheduleInfo(PagingVO pagingVO);

  int selectScheduleInfoCount();

  List<ScheduleResponseVO> selectActiveSchedules();
}
