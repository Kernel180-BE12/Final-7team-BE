package com.softlabs.aicontents.domain.monitoring.dto.request;

import java.time.LocalDate;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data // Lombok: getter, setter, toString 등 자동 생성
public class LogSearchRequest {
  private Long executionId; // 실행 ID

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate startDate; // 조회 시작일(YYYY-MM-DD)

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate endDate; // 조회 종료일(YYYY-MM-DD)

  private String status; // 로그 상태 코드
  private String logLevel; // 로그 레벨

  //페이징 관려 파라미터 추가
  private int page = 1; //기본값 1
  private int limit = 20; //기본값 20

  public int getOffset(){
    return (page-1) * limit;
  }


}
