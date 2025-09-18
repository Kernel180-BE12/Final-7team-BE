package com.softlabs.aicontents.domain.monitoring.vo.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LogListResponse {
    private Long executionId; //실행 ID
    private List<LogEntryVO> logs; //로그 목록
}