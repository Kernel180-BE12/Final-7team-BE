package com.softlabs.aicontents.domain.user.controller;

import com.softlabs.aicontents.common.dto.response.ApiResponseDTO;
import com.softlabs.aicontents.domain.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/check-login-id")
  public ResponseEntity<ApiResponseDTO<Boolean>> checkLoginIdDuplicate(
      @RequestParam String loginId) {
    boolean isDuplicate = userService.isLoginIdDuplicate(loginId);

    if (isDuplicate) {
      return ResponseEntity.ok(ApiResponseDTO.success(true, "중복된 아이디입니다."));
    } else {
      return ResponseEntity.ok(ApiResponseDTO.success(false, "사용 가능한 아이디입니다."));
    }
  }
}
