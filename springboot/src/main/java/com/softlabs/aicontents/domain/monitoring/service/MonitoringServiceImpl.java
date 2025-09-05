package com.softlabs.aicontents.domain.monitoring.service;

import com.softlabs.aicontents.domain.monitoring.dto.response.MonitoringStatsResponse;
import com.softlabs.aicontents.domain.monitoring.mapper.UnifiedLogMapper;
import com.softlabs.aicontents.domain.monitoring.vo.response.UnifiedLogResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MonitoringServiceImpl implements MonitoringService{

    private final UnifiedLogMapper unifiedLogMapper;

    @Override
    public MonitoringStatsResponse getStats(){
        //1)DB에서 로그 최근 3건 조회
        List<UnifiedLogResponseVO> logVOs=unifiedLogMapper.findRecentLogs();
//        System.out.println("rows" + logVOs.size());

        //2) VO -> DTO 매핑
        List<MonitoringStatsResponse.LogEntry> logs=logVOs.stream()
                .map(vo->new MonitoringStatsResponse.LogEntry(
                        vo.logMessage(),
                        vo.logDetail(),
                        vo.statusCode()
                )).toList();

        //3) 성공 횟수, 실패 횟수, 성공률 통계
        int successCount= unifiedLogMapper.successCount();
        int failedCount= unifiedLogMapper.failedCount();
        float allRows= unifiedLogMapper.allRows();
        float successRate= (successCount/allRows)*100; //성공률 =(성공횟수/총시도횟수(모든행))*100

        var stats=new MonitoringStatsResponse.Stats(successCount,failedCount,successRate);

        //4)DTO 조립 후 반환
        return new MonitoringStatsResponse(stats,logs);
    }
}
