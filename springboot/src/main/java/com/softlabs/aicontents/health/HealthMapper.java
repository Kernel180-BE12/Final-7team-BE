package com.softlabs.aicontents.health;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HealthMapper {
  String getSysdate();
}
