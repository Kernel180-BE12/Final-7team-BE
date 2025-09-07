package com.softlabs.aicontents.domain.monitoring.mapper;

import com.softlabs.aicontents.domain.monitoring.vo.response.UnifiedLogResponseVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
//UnifiedLog 테이블 접근용 MyBatis Mapper

@Mapper
public interface UnifiedLogMapper {
    List<UnifiedLogResponseVO> findRecentLogs(); //최근 로그 N개 조회
    int successCount(); //성공 횟수
    int failedCount(); //실패 횟수
    float allRows(); //모든 행의 수(성공률 계산용)
}
