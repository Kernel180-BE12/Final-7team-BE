package com.softlabs.aicontents.scheduler.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.springframework.scheduling.config.ScheduledTask;

import java.util.List;

@Mapper
//이 인터페이스는 MyBatis가 관리하는 Mapper다
public interface ScheduledTaskMapper {
    //인터페이스 시작

    int countTasks();
    //countTasks라는 메서드 선언, 반환 int
    // 실제 구현은 xml에 있음.
}