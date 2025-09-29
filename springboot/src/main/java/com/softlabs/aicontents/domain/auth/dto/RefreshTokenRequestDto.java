package com.softlabs.aicontents.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "토큰 재발급 요청 정보")
public class RefreshTokenRequestDto {

  @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  @NotBlank(message = "리프레시 토큰은 필수입니다.")
  private String refreshToken;

  public RefreshTokenRequestDto() {}

  public RefreshTokenRequestDto(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }
}
