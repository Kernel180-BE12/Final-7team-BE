package com.softlabs.aicontents.common.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.softlabs.aicontents.common.enums.ErrorCode;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ErrorResponseDTOTest {

  @Test
  void testErrorResponseCreationWithPath() {
    String path = "/api/test";
    ErrorCode errorCode = ErrorCode.NOT_FOUND;

    ErrorResponseDTO response = ErrorResponseDTO.of(errorCode, path);

    assertThat(response.code()).isEqualTo(errorCode.getCode());
    assertThat(response.message()).isEqualTo(errorCode.getMessage());
    assertThat(response.path()).isEqualTo(path);
    assertThat(response.status()).isEqualTo(errorCode.getStatus());
    assertThat(response.timestamp()).isNotNull();
    assertThat(response.traceId()).isNotNull();
  }

  @Test
  void testErrorResponseCreationWithCustomMessage() {
    String path = "/api/test";
    String customMessage = "사용자 정의 오류 메시지";
    ErrorCode errorCode = ErrorCode.INVALID_INPUT;

    ErrorResponseDTO response = ErrorResponseDTO.of(errorCode, path, customMessage);

    assertThat(response.code()).isEqualTo(errorCode.getCode());
    assertThat(response.message()).isEqualTo(customMessage);
    assertThat(response.path()).isEqualTo(path);
    assertThat(response.status()).isEqualTo(errorCode.getStatus());
    assertThat(response.timestamp()).isNotNull();
    assertThat(response.traceId()).isNotNull();
  }

  @Test
  void testErrorResponseCreationWithCustomTraceId() {
    String path = "/api/test";
    String customTraceId = "custom-trace-123";
    ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

    ErrorResponseDTO response = ErrorResponseDTO.ofWithTraceId(errorCode, path, customTraceId);

    assertThat(response.code()).isEqualTo(errorCode.getCode());
    assertThat(response.message()).isEqualTo(errorCode.getMessage());
    assertThat(response.path()).isEqualTo(path);
    assertThat(response.status()).isEqualTo(errorCode.getStatus());
    assertThat(response.timestamp()).isNotNull();
    assertThat(response.traceId()).isEqualTo(customTraceId);
  }

  @Test
  void testErrorResponseTimestamp() {
    LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

    ErrorResponseDTO response = ErrorResponseDTO.of(ErrorCode.BAD_REQUEST, "/api/test");

    LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

    assertThat(response.timestamp()).isBetween(beforeCreation, afterCreation);
  }

  @Test
  void testErrorResponseWithDifferentErrorCodes() {
    String path = "/api/test";

    ErrorResponseDTO badRequestResponse = ErrorResponseDTO.of(ErrorCode.BAD_REQUEST, path);
    assertThat(badRequestResponse.status()).isEqualTo(400);
    assertThat(badRequestResponse.code()).isEqualTo("E001");

    ErrorResponseDTO unauthorizedResponse = ErrorResponseDTO.of(ErrorCode.UNAUTHORIZED, path);
    assertThat(unauthorizedResponse.status()).isEqualTo(401);
    assertThat(unauthorizedResponse.code()).isEqualTo("E101");

    ErrorResponseDTO notFoundResponse = ErrorResponseDTO.of(ErrorCode.NOT_FOUND, path);
    assertThat(notFoundResponse.status()).isEqualTo(404);
    assertThat(notFoundResponse.code()).isEqualTo("E301");

    ErrorResponseDTO serverErrorResponse =
        ErrorResponseDTO.of(ErrorCode.INTERNAL_SERVER_ERROR, path);
    assertThat(serverErrorResponse.status()).isEqualTo(500);
    assertThat(serverErrorResponse.code()).isEqualTo("E901");
  }

  @Test
  void testErrorResponseImmutability() {
    ErrorCode errorCode = ErrorCode.FORBIDDEN;
    String path = "/api/forbidden";

    ErrorResponseDTO response = ErrorResponseDTO.of(errorCode, path);

    assertThat(response.code()).isEqualTo(errorCode.getCode());
    assertThat(response.message()).isEqualTo(errorCode.getMessage());
    assertThat(response.path()).isEqualTo(path);
    assertThat(response.status()).isEqualTo(errorCode.getStatus());
  }
}
