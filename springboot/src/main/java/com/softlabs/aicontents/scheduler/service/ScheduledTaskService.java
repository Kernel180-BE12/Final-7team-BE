package com.softlabs.aicontents.scheduler.service;


import com.softlabs.aicontents.scheduler.mapper.ScheduledTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
//Lombok 어노테이션
public class ScheduledTaskService {

    private final ScheduledTaskMapper scheduledTaskMapper;

    public int getTaskCount() {
        //반환은 int , 메서드 선언
        int res = scheduledTaskMapper.countTasks();
        return res;
    }
}
