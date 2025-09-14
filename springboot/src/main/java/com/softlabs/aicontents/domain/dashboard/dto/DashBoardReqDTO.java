package com.softlabs.aicontents.domain.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DashBoardReqDTO {
  @NotBlank(message = "dashboard.role.not-blank")
  String role;

  @NotBlank(message = "dashboard.username.not-blank")
  @Size(min = 2, max = 20, message = "dashboard.username.size")
  String userName;
}
