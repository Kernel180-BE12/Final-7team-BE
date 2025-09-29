package com.softlabs.aicontents.domain.monitoring.mapper;

import com.softlabs.aicontents.domain.monitoring.vo.response.LogEntryVO;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper // mybatis 매퍼 인터페이스
public interface UnifiedLogMapper {
  // 조건에 따른 로그 목록 조회
  List<LogEntryVO> findLogsByConditions(Map<String, Object> params);

  long countLogsByConditions(Map<String, Object> params);
}
