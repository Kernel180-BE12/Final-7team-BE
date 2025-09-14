package com.softlabs.aicontents.domain.health.service;

import com.softlabs.aicontents.domain.health.mapper.HealthCheckMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleHealthCheckService {
    private final HealthCheckMapper healthCheckMapper;

    public boolean isUp(){
        try{
            int cnt=healthCheckMapper.selectScheduledStatus();
            return cnt<3;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
