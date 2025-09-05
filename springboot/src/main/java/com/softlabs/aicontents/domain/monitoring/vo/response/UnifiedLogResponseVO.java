package com.softlabs.aicontents.domain.monitoring.vo.response;
/*
DB에서 조회한 로그 정보 VO
DB 필드명과 유사하게 유지
Service 계층에서 DTO로 변환됨
 */
public record UnifiedLogResponseVO (
        String logMessage, //로그 메시지(CLOB)
        String logDetail, //로그 상세(CLOB)
        String statusCode //상태 코드(ex.SUCCESS, FAILED 등)
){}
