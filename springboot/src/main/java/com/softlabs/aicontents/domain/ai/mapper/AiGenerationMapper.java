package com.softlabs.aicontents.domain.ai.mapper;

import com.softlabs.aicontents.domain.ai.entity.AiGenerationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiGenerationMapper {
    int insertGeneration(AiGenerationEntity entity);
    int markSuccess(AiGenerationEntity entity);
    int markError(@Param("genId") Long genId, @Param("errorMsg") String errorMsg);
}
