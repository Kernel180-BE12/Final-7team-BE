package com.softlabs.aicontents.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그아웃 요청 정보")
public class LogoutRequestDto {

  @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  @NotBlank(message = "액세스 토큰은 필수입니다.")
  private String accessToken;

  public LogoutRequestDto() {}

  public LogoutRequestDto(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }
}
