package com.softlabs.aicontents.scheduler.mapper;


import com.softlabs.aicontents.scheduler.vo.ScheduledResponseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
//이 인터페이스는 MyBatis가 관리하는 Mapper다
public interface SchedulerMapper {
    //인터페이스 시작

    int countTasks();
    //countTasks라는 메서드 선언, 반환 int
    // 실제 구현은 xml에 있음.

    ScheduledResponseVO selectByTaskId(@Param("taskId") Long taskId);
    //파라미터 이름 지정: XML에서 #{taskId}로 사용할 수 있게 이름을 지정
    //입력 파라미터: 이 메서드가 받는 데이터

}