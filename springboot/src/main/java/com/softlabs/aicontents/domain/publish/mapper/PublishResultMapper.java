package com.softlabs.aicontents.domain.publish.mapper;

import com.softlabs.aicontents.domain.publish.dto.response.PublishResDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PublishResultMapper {
  void insertPublishResult(PublishResDto publishResultDto);
}
