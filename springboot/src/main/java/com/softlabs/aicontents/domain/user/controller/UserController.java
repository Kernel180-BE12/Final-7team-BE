package com.softlabs.aicontents.domain.user.controller;

import com.softlabs.aicontents.common.dto.response.ApiResponseDTO;
import com.softlabs.aicontents.domain.email.service.EmailService;
import com.softlabs.aicontents.domain.email.service.VerificationCodeService;
import com.softlabs.aicontents.domain.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

  private final UserService userService;
  private final EmailService emailService;
  private final VerificationCodeService verificationCodeService;

  @Autowired
  public UserController(
      UserService userService,
      EmailService emailService,
      VerificationCodeService verificationCodeService) {
    this.userService = userService;
    this.emailService = emailService;
    this.verificationCodeService = verificationCodeService;
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

  @GetMapping("/check-email")
  public ResponseEntity<ApiResponseDTO<Boolean>> checkEmailDuplicate(@RequestParam String email) {
    boolean isDuplicate = userService.isEmailDuplicate(email);

    if (isDuplicate) {
      return ResponseEntity.ok(ApiResponseDTO.success(true, "중복된 이메일입니다."));
    } else {
      return ResponseEntity.ok(ApiResponseDTO.success(false, "사용 가능한 이메일입니다."));
    }
  }

  @PostMapping("/send-verification-code")
  public ResponseEntity<ApiResponseDTO<Void>> sendVerificationCode(@RequestParam String email) {
    try {
      String verificationCode = verificationCodeService.generateVerificationCode(email);
      emailService.sendVerificationEmail(email, verificationCode);

      return ResponseEntity.ok(ApiResponseDTO.success(null, "인증코드가 이메일로 발송되었습니다."));
    } catch (Exception e) {
      log.error("인증코드 발송 실패: {}", e.getMessage());
      return ResponseEntity.internalServerError()
          .body(ApiResponseDTO.error("인증코드 발송에 실패했습니다."));
    }
  }

  @PostMapping("/verify-code")
  public ResponseEntity<ApiResponseDTO<Boolean>> verifyCode(
      @RequestParam String email, @RequestParam String code) {
    boolean isValid = verificationCodeService.verifyCode(email, code);

    if (isValid) {
      return ResponseEntity.ok(ApiResponseDTO.success(true, "인증이 완료되었습니다."));
    } else {
      return ResponseEntity.ok(ApiResponseDTO.success(false, "인증코드가 올바르지 않습니다."));
    }
  }
}
