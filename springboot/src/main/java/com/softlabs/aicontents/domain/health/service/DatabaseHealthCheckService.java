package com.softlabs.aicontents.domain.health.service;

import com.softlabs.aicontents.domain.health.mapper.HealthCheckMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // final 필드 생성자 자동 생성
public class DatabaseHealthCheckService {
//  private final JdbcTemplate jdbcTemplate; // DB 접근을 위한 JdbcTemplate
  private final HealthCheckMapper healthCheckMapper;

  public boolean isUp() {
    try {
//      jdbcTemplate.queryForObject("select 1 from dual", Integer.class);
      String status=healthCheckMapper.selectDbStatus();

      return status!=null;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
