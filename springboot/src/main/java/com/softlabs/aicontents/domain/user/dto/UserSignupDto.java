package com.softlabs.aicontents.domain.user.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserSignupDto {

  @NotBlank(message = "{common.login-id.not-blank}")
  @Size(min = 4, max = 20, message = "{common.login-id.size}")
  private String loginId;

  @Email(message = "{common.email.invalid}")
  @NotBlank(message = "{common.email.not-blank}")
  private String email;

  @NotBlank(message = "{common.password.not-blank}")
  @Size(min = 8, max = 20, message = "{common.password.size}")
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "{common.password.pattern}")
  private String password;
}
