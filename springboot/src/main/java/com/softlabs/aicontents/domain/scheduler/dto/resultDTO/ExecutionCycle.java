package com.softlabs.aicontents.domain.scheduler.dto.resultDTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ExecutionCycle {
  DAILY("매일 실행"),
  WEEKLY("주간 실행"),
  MONTHLY("월간 실행");

  private final String displayName;

  ExecutionCycle(String displayName) {
    this.displayName = displayName;
  }

  @JsonValue // JSON 직렬화 시 이 값을 사용 (응답)
  public String getDisplayName() {
    return displayName;
  }

  // 안전한 switch-case로 변경
  @JsonCreator // JSON 역직렬화 시 이 메서드 사용 (요청)
  public static ExecutionCycle fromDisplayName(String displayName) {
    if (displayName == null) {
      return DAILY; // 기본값
    }

    switch (displayName) {
      case "매일 실행":
        return DAILY;
      case "주간 실행":
        return WEEKLY;
      case "월간 실행":
        return MONTHLY;
      default:
        // 로그 출력 후 기본값 반환 (예외 대신)
        //                System.out.println("알 수 없는 실행 주기: " + displayName + ", 기본값(DAILY)으로 설정");
        return DAILY;
    }
  }

  @Override
  public String toString() {
    return displayName;
  }
}
