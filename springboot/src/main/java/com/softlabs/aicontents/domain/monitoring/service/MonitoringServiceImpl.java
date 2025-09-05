package com.softlabs.aicontents.domain.monitoring.service;

import com.softlabs.aicontents.domain.monitoring.dto.response.MonitoringStatsResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonitoringServiceImpl implements MonitoringService{
    @Override
    public MonitoringStatsResponse getStats(){
        return new MonitoringStatsResponse(
                new MonitoringStatsResponse.Stats(23,2,92),
                List.of(
                        new MonitoringStatsResponse.LogEntry(
                                "발행 완료",
                                "겨울 패딩 관련 블로그 글 게시 성공",
                                MonitoringStatsResponse.Status.SUCCESS
                        ),
                        new MonitoringStatsResponse.LogEntry(
                                "다음 스케줄 대기",
                                "내일 8시 자동 실행 예정",
                                MonitoringStatsResponse.Status.WAITING
                        )
                )
        );
    }
}
