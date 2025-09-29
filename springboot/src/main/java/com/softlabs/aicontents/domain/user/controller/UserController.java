package com.softlabs.aicontents.domain.user.controller;

import com.softlabs.aicontents.common.dto.response.ApiResponseDTO;
import com.softlabs.aicontents.domain.email.service.EmailService;
import com.softlabs.aicontents.domain.email.service.VerificationCodeService;
import com.softlabs.aicontents.domain.user.dto.UserSignupDto;
import com.softlabs.aicontents.domain.user.service.SignupValidationService;
import com.softlabs.aicontents.domain.user.service.UserService;
import com.softlabs.aicontents.domain.user.vo.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Slf4j
@Tag(name = "User", description = "사용자 관리 API")
public class UserController {

  private final UserService userService;
  private final EmailService emailService;
  private final VerificationCodeService verificationCodeService;
  private final SignupValidationService signupValidationService;

  @Autowired
  public UserController(
      UserService userService,
      EmailService emailService,
      VerificationCodeService verificationCodeService,
      SignupValidationService signupValidationService) {
    this.userService = userService;
    this.emailService = emailService;
    this.verificationCodeService = verificationCodeService;
    this.signupValidationService = signupValidationService;
  }

  @Operation(summary = "로그인 ID 중복 확인", description = "회원가입 시 로그인 ID가 중복되는지 확인합니다.")
  @SecurityRequirements({})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "중복 확인 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
      })
  @GetMapping("/check-login-id")
  public ResponseEntity<ApiResponseDTO<Boolean>> checkLoginIdDuplicate(
      @Parameter(description = "확인할 로그인 ID", required = true, example = "testuser123") @RequestParam
          String loginId) {
    boolean isDuplicate = userService.isLoginIdDuplicate(loginId);

    if (isDuplicate) {
      return ResponseEntity.ok(ApiResponseDTO.success(true, "중복된 아이디입니다."));
    } else {
      return ResponseEntity.ok(ApiResponseDTO.success(false, "사용 가능한 아이디입니다."));
    }
  }

  @Operation(summary = "이메일 중복 확인", description = "회원가입 시 이메일이 중복되는지 확인합니다.")
  @SecurityRequirements({})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "중복 확인 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
      })
  @GetMapping("/check-email")
  public ResponseEntity<ApiResponseDTO<Boolean>> checkEmailDuplicate(
      @Parameter(description = "확인할 이메일 주소", required = true, example = "test@example.com")
          @RequestParam
          String email) {
    boolean isDuplicate = userService.isEmailDuplicate(email);

    if (isDuplicate) {
      return ResponseEntity.ok(ApiResponseDTO.success(true, "중복된 이메일입니다."));
    } else {
      return ResponseEntity.ok(ApiResponseDTO.success(false, "사용 가능한 이메일입니다."));
    }
  }

  @Operation(summary = "인증코드 발송", description = "이메일로 인증코드를 발송합니다. 인증코드는 5분간 유효합니다.")
  @SecurityRequirements({})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "인증코드 발송 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "인증코드 발송 실패")
      })
  @PostMapping("/send-verification-code")
  public ResponseEntity<ApiResponseDTO<Void>> sendVerificationCode(
      @Parameter(description = "인증코드를 받을 이메일 주소", required = true, example = "test@example.com")
          @RequestParam
          String email) {
    try {
      String verificationCode = verificationCodeService.generateVerificationCode(email);
      emailService.sendVerificationEmail(email, verificationCode);

      return ResponseEntity.ok(ApiResponseDTO.success(null, "인증코드가 메일로 발송되었습니다. 인증코드는 5분간 유효합니다."));
    } catch (Exception e) {
      log.error("인증코드 발송 실패: {}", e.getMessage());
      return ResponseEntity.internalServerError().body(ApiResponseDTO.error("인증코드 발송에 실패했습니다."));
    }
  }

  @Operation(summary = "인증코드 확인", description = "이메일로 발송된 인증코드를 확인합니다.")
  @SecurityRequirements({})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "인증코드 확인 완료"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
      })
  @PostMapping("/verify-code")
  public ResponseEntity<ApiResponseDTO<Boolean>> verifyCode(
      @Parameter(description = "인증코드를 받은 이메일 주소", required = true, example = "test@example.com")
          @RequestParam
          String email,
      @Parameter(description = "인증코드", required = true, example = "123456") @RequestParam
          String code) {
    boolean isValid = verificationCodeService.verifyCode(email, code);

    if (isValid) {
      return ResponseEntity.ok(ApiResponseDTO.success(true, "인증이 완료되었습니다."));
    } else {
      return ResponseEntity.ok(ApiResponseDTO.success(false, "인증코드가 올바르지 않습니다."));
    }
  }

  @Operation(summary = "회원가입", description = "새로운 사용자 계정을 생성합니다. 이메일 인증이 완료된 상태여야 합니다.")
  @SecurityRequirements({})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 유효성 검사 실패"),
        @ApiResponse(responseCode = "409", description = "중복된 아이디 또는 이메일"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
      })
  @PostMapping("/signup")
  public ResponseEntity<ApiResponseDTO<Boolean>> signup(
      @Parameter(description = "회원가입 정보", required = true) @Valid @RequestBody
          UserSignupDto signupDto,
      @Parameter(description = "이메일 인증코드", required = true, example = "123456") @RequestParam
          String verificationCode) {

    signupValidationService.validateSignupConditions(
        signupDto.getLoginId(), signupDto.getEmail(), verificationCode);

    User user = userService.signupUser(signupDto);
    log.info("회원가입 완료: loginId={}, email={}", user.getLoginId(), user.getEmail());

    return ResponseEntity.ok(ApiResponseDTO.success(true, "회원가입이 완료되었습니다."));
  }
}
