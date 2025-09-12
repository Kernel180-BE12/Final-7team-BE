package com.softlabs.aicontents.domain.health.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HealthCheckMapper {
  // 상태 insert
  void insertHealthCheck(@Param("status") String status);

  // 최신 checked_at 조회
  String selectLatestCheckedAt();
}
