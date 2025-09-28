package com.softlabs.aicontents.domain.monitoring.vo.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LogListResponse {
  // private Long executionId; // 실행 ID
  private List<LogEntryVO> logs; // 로그 목록
  private Pagination pagination;

  @Data
  @AllArgsConstructor
  public static class Pagination {
    private int currentPage;
    private int totalPages; //
    private long totalCount;
    private int pageSize;
  }
}
