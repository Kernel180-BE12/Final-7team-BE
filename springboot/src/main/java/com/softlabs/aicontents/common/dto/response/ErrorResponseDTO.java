package com.softlabs.aicontents.common.dto.response;

import com.softlabs.aicontents.common.enums.ErrorCode;
import com.softlabs.aicontents.common.util.TraceIdUtil;
import java.time.LocalDateTime;

public record ErrorResponseDTO(
    LocalDateTime timestamp, String code, String message, String traceId, String path, int status) {

  public static ErrorResponseDTO of(ErrorCode errorCode, String path) {
    return new ErrorResponseDTO(
        LocalDateTime.now(),
        errorCode.getCode(),
        errorCode.getMessage(),
        TraceIdUtil.getOrCreateTraceId(),
        path,
        errorCode.getStatus());
  }

  public static ErrorResponseDTO of(ErrorCode errorCode, String path, String customMessage) {
    return new ErrorResponseDTO(
        LocalDateTime.now(),
        errorCode.getCode(),
        customMessage,
        TraceIdUtil.getOrCreateTraceId(),
        path,
        errorCode.getStatus());
  }

  public static ErrorResponseDTO ofWithTraceId(ErrorCode errorCode, String path, String traceId) {
    return new ErrorResponseDTO(
        LocalDateTime.now(),
        errorCode.getCode(),
        errorCode.getMessage(),
        traceId,
        path,
        errorCode.getStatus());
  }
}
