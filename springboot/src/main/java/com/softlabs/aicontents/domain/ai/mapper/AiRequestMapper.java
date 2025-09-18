package com.softlabs.aicontents.domain.ai.mapper;

import com.softlabs.aicontents.domain.ai.entity.AiRequestEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiRequestMapper {
  AiRequestEntity findByHash(@Param("requestHash") String requestHash);

  int insertRequest(AiRequestEntity entity);
}
