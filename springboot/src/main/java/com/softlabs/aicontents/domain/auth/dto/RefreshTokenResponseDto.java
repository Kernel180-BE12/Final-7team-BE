package com.softlabs.aicontents.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 재발급 응답 정보")
public class RefreshTokenResponseDto {

  @Schema(description = "새로운 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  private String accessToken;

  @Schema(description = "토큰 타입", example = "Bearer")
  private String tokenType = "Bearer";

  @Schema(description = "사용자 로그인 아이디", example = "user123")
  private String loginId;

  public RefreshTokenResponseDto() {}

  public RefreshTokenResponseDto(String accessToken, String loginId) {
    this.accessToken = accessToken;
    this.loginId = loginId;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public String getLoginId() {
    return loginId;
  }

  public void setLoginId(String loginId) {
    this.loginId = loginId;
  }
}
