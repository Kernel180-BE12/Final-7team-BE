package com.softlabs.aicontents.domain.ai.mapper;

import com.softlabs.aicontents.domain.ai.entity.AiPostEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiPostMapper {
    int insertPost(AiPostEntity entity);
    AiPostEntity selectByGenId(@Param("genId") Long genId);
}
