package com.softlabs.aicontents.domain.health.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HealthCheckMapper {
  // 상태 insert
  void insertHealthCheck(@Param("status") String status);

  // 최신 checked_at 조회
  String selectLatestCheckedAt();

  // 최신 execution_id에 해당하는 키워드 상태별 카운트 조회
  // 결과 예시: [{STATUS=FAILED, CNT=3}, {STATUS=SUCCESS, CNT=1}]
  String selectKeywordStatus();

  String selectScheduledStatus();
}
