package com.softlabs.aicontents.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청 정보")
public class LoginRequestDto {

  @NotBlank(message = "로그인 아이디는 필수입니다.")
  @Schema(description = "로그인 아이디", example = "user123")
  private String loginId;

  @NotBlank(message = "비밀번호는 필수입니다.")
  @Schema(description = "비밀번호", example = "password123")
  private String password;

  public LoginRequestDto() {}

  public LoginRequestDto(String loginId, String password) {
    this.loginId = loginId;
    this.password = password;
  }

  public String getLoginId() {
    return loginId;
  }

  public void setLoginId(String loginId) {
    this.loginId = loginId;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}