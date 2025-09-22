package com.softlabs.aicontents.domain.publish.mapper;

import com.softlabs.aicontents.domain.publish.dto.request.AiPostDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiPostMapper {
  AiPostDto selectByPostId(Long postId);
}
