package com.softlabs.aicontents.health;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class HealthService {

    private final HealthMapper healthMapper;

    public HealthService(HealthMapper healthMapper) {
        this.healthMapper = healthMapper;
    }

    public Map<String, Object> checkHealth() {
        Map<String, Object> healthInfo = new HashMap<>();
        
        try {
            // RDS 연결 확인을 위해 현재 시간 조회
            String currentTime = healthMapper.getSysdate();
            
            healthInfo.put("status", "UP");
            healthInfo.put("database", "Oracle RDS");
            healthInfo.put("connection", "SUCCESS");
            healthInfo.put("currentTime", currentTime);
            healthInfo.put("message", "RDS 연결이 정상적으로 작동중입니다.");
            
        } catch (Exception e) {
            healthInfo.put("status", "DOWN");
            healthInfo.put("database", "Oracle RDS");
            healthInfo.put("connection", "FAILED");
            healthInfo.put("error", e.getMessage());
            healthInfo.put("message", "RDS 연결에 실패했습니다.");
        }
        
        return healthInfo;
    }
}
