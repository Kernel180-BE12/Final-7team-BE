package com.softlabs.aicontents.domain.scheduler.dto.pipeLineDTO;

import lombok.Getter;

@Getter
public class StepExecutionResultDTO {

  private boolean success;
  private String resultData;
  private String errorMessage;

  //    // 생성자를 private으로 막아서 외부에서 new로 생성하는 것을 방지
  //    private StepExecutionResultDTO(boolean success, String resultData, String errorMessage) {
  //        this.success = success;
  //        this.resultData = resultData;
  //        this.errorMessage = errorMessage;
  //    }
  //
  //    // 성공 결과를 생성하는 정적 메서드
  //    public static StepExecutionResultDTO success(String resultData) {
  //        return new StepExecutionResultDTO(true, resultData, null);
  //    }
  //
  //    // 실패 결과를 생성하는 정적 메서드
  //    public static StepExecutionResultDTO failure(String errorMessage) {
  //        return new StepExecutionResultDTO(false, null, errorMessage);
  //    }
}
