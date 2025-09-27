package com.softlabs.aicontents.domain.monitoring.mapper;

import com.softlabs.aicontents.domain.monitoring.vo.response.MonitoringStatsResponseVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MonitoringStatsMapper {
  List<MonitoringStatsResponseVO> findRecentLogs(); // 최근 로그 N개 조회

  int successCount();

  int failedCount();

  float allRows();

  int activeExecutions();
  int totalExecutions();
}
