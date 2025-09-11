package com.softlabs.aicontents.common.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ErrorCodeTest {

  @Test
  void testErrorCodeProperties() {
    ErrorCode errorCode = ErrorCode.INVALID_INPUT;

    assertThat(errorCode.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(errorCode.getCode()).isEqualTo("E002");
    assertThat(errorCode.getMessage()).isEqualTo("입력값이 올바르지 않습니다.");
    assertThat(errorCode.getStatus()).isEqualTo(400);
  }

  @Test
  void testAllErrorCodesHaveValidProperties() {
    for (ErrorCode errorCode : ErrorCode.values()) {
      assertThat(errorCode.getHttpStatus()).isNotNull();
      assertThat(errorCode.getCode()).isNotEmpty();
      assertThat(errorCode.getMessage()).isNotEmpty();
      assertThat(errorCode.getStatus()).isGreaterThan(0);
    }
  }

  @Test
  void testClientErrorCodes() {
    assertThat(ErrorCode.BAD_REQUEST.getStatus()).isEqualTo(400);
    assertThat(ErrorCode.UNAUTHORIZED.getStatus()).isEqualTo(401);
    assertThat(ErrorCode.FORBIDDEN.getStatus()).isEqualTo(403);
    assertThat(ErrorCode.NOT_FOUND.getStatus()).isEqualTo(404);
    assertThat(ErrorCode.METHOD_NOT_ALLOWED.getStatus()).isEqualTo(405);
    assertThat(ErrorCode.CONFLICT.getStatus()).isEqualTo(409);
  }

  @Test
  void testServerErrorCodes() {
    assertThat(ErrorCode.INTERNAL_SERVER_ERROR.getStatus()).isEqualTo(500);
    assertThat(ErrorCode.SERVICE_UNAVAILABLE.getStatus()).isEqualTo(503);
  }

  @Test
  void testErrorCodeUniqueness() {
    ErrorCode[] errorCodes = ErrorCode.values();

    for (int i = 0; i < errorCodes.length; i++) {
      for (int j = i + 1; j < errorCodes.length; j++) {
        assertThat(errorCodes[i].getCode())
            .as("Error codes should be unique: %s vs %s", errorCodes[i], errorCodes[j])
            .isNotEqualTo(errorCodes[j].getCode());
      }
    }
  }

  @Test
  void testSpecificErrorCodes() {
    ErrorCode notFound = ErrorCode.NOT_FOUND;
    assertThat(notFound.getCode()).isEqualTo("E301");
    assertThat(notFound.getMessage()).isEqualTo("요청한 리소스를 찾을 수 없습니다.");
    assertThat(notFound.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);

    ErrorCode internalError = ErrorCode.INTERNAL_SERVER_ERROR;
    assertThat(internalError.getCode()).isEqualTo("E901");
    assertThat(internalError.getMessage()).isEqualTo("내부 서버 오류가 발생했습니다.");
    assertThat(internalError.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
