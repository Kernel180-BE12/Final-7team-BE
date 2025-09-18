package com.softlabs.aicontents.domain.monitoring.mapper;

import com.softlabs.aicontents.domain.monitoring.dto.request.LogSearchRequest;
import com.softlabs.aicontents.domain.monitoring.vo.response.LogEntryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper //mybatis 매퍼 인터페이스
public interface UnifiedLogMapper {
    //조건에 따른 로그 목록 조회
    List<LogEntryVO> findLogsByConditions(Map<String, Object> params);
}
