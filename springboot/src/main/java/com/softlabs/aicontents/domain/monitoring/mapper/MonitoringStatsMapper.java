package com.softlabs.aicontents.domain.monitoring.mapper;

import com.softlabs.aicontents.domain.monitoring.vo.response.MonitoringStatsResponseVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MonitoringStatsMapper {
    List<MonitoringStatsResponseVO> findRecentLogs(); //최근 로그 N개 조회
    int successCount();
    int failedCount();
    float allRows();
}
