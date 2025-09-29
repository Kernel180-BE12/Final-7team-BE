package com.softlabs.aicontents.common.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

  // 400번대 클라이언트 에러
  BAD_REQUEST(HttpStatus.BAD_REQUEST, "E001", "잘못된 요청입니다."),
  INVALID_INPUT(HttpStatus.BAD_REQUEST, "E002", "입력값이 올바르지 않습니다."),
  MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "E003", "필수 필드가 누락되었습니다."),
  INVALID_FORMAT(HttpStatus.BAD_REQUEST, "E004", "올바르지 않은 형식입니다."),

  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "E101", "인증이 필요합니다."),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "E102", "유효하지 않은 토큰입니다."),
  TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "E103", "토큰이 만료되었습니다."),

  FORBIDDEN(HttpStatus.FORBIDDEN, "E201", "접근 권한이 없습니다."),
  INSUFFICIENT_PERMISSIONS(HttpStatus.FORBIDDEN, "E202", "권한이 부족합니다."),

  NOT_FOUND(HttpStatus.NOT_FOUND, "E301", "요청한 리소스를 찾을 수 없습니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "E302", "사용자를 찾을 수 없습니다."),
  DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "E303", "데이터를 찾을 수 없습니다."),

  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "E401", "허용되지 않은 HTTP 메서드입니다."),

  CONFLICT(HttpStatus.CONFLICT, "E501", "데이터 충돌이 발생했습니다."),
  DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "E502", "이미 존재하는 리소스입니다."),

  // 회원가입 관련 에러
  LOGIN_ID_ALREADY_EXISTS(HttpStatus.CONFLICT, "E503", "이미 존재하는 아이디입니다."),
  EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "E504", "이미 존재하는 이메일입니다."),
  EMAIL_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "E505", "이메일 인증코드 검증에 실패했습니다."),

  // 로그인 관련 에러
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "E506", "로그인 아이디 또는 비밀번호가 잘못되었습니다."),
  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "E507", "유효하지 않은 리프레시 토큰입니다."),

  // 500번대 서버 에러
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E901", "내부 서버 오류가 발생했습니다."),
  DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E902", "데이터베이스 오류가 발생했습니다."),
  EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E903", "외부 API 호출 중 오류가 발생했습니다."),
  SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "E904", "서비스를 사용할 수 없습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  ErrorCode(HttpStatus httpStatus, String code, String message) {
    this.httpStatus = httpStatus;
    this.code = code;
    this.message = message;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public int getStatus() {
    return httpStatus.value();
  }
}
