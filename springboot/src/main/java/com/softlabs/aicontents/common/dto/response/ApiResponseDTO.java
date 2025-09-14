package com.softlabs.aicontents.common.dto.response;

// 공통 응답 DTO
public record ApiResponseDTO<T>(
        boolean success,
        T data,
        String message)

{
  // 성공 응답 생성
  public static <T> ApiResponseDTO<T> success(T data) {
    return new ApiResponseDTO<>(true, data, null);
  }

  // 성공 응답 + 메시지
  public static <T> ApiResponseDTO<T> success(T data, String message) {
    return new ApiResponseDTO<>(true, data, message);
  }

  // 실패 응답
  public static <T> ApiResponseDTO<T> error(String message) {
    return new ApiResponseDTO<>(false, null, message);
  }
}
