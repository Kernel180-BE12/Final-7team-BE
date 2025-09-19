package com.softlabs.aicontents.domain.monitoring.vo.response;

/*
    DB에서 조회한 로그 정보 VO, DB 필드명과 유사하게 유지
    service 계층에서 DTO로 변환됨
    record가 가진 필드(데이터)를 정의, 컴파일러가 코드를 보고 자동으로 생성자, getter 등을 만들어줌
 */
public record MonitoringStatsResponseVO (
        String logMessage, //로그 메시지
        String logDetail, //로그 상세
        String statusCode //상태 코드
){ }
