package com.softlabs.aicontents.domain.auth.controller;

import com.softlabs.aicontents.common.dto.response.ApiResponseDTO;
import com.softlabs.aicontents.domain.auth.dto.LoginRequestDto;
import com.softlabs.aicontents.domain.auth.dto.LoginResponseDto;
import com.softlabs.aicontents.domain.auth.dto.LogoutRequestDto;
import com.softlabs.aicontents.domain.auth.dto.RefreshTokenRequestDto;
import com.softlabs.aicontents.domain.auth.dto.RefreshTokenResponseDto;
import com.softlabs.aicontents.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "인증 관련 API")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  @Operation(summary = "로그인", description = "사용자 로그인을 처리합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "로그인 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
    @ApiResponse(responseCode = "401", description = "인증 실패")
  })
  public ResponseEntity<ApiResponseDTO<LoginResponseDto>> login(
      @Valid @RequestBody LoginRequestDto loginRequestDto) {
    LoginResponseDto response = authService.login(loginRequestDto);
    return ResponseEntity.ok(ApiResponseDTO.success(response, "로그인이 완료되었습니다."));
  }

  @PostMapping("/logout")
  @Operation(summary = "로그아웃", description = "사용자 로그아웃을 처리합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
    @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
  })
  public ResponseEntity<ApiResponseDTO<Void>> logout(
      @Valid @RequestBody LogoutRequestDto logoutRequestDto) {
    authService.logout(logoutRequestDto);
    return ResponseEntity.ok(ApiResponseDTO.success(null, "로그아웃이 완료되었습니다."));
  }

  @PostMapping("/refresh")
  @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
    @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
  })
  public ResponseEntity<ApiResponseDTO<RefreshTokenResponseDto>> refreshToken(
      @Valid @RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
    RefreshTokenResponseDto response = authService.refreshToken(refreshTokenRequestDto);
    return ResponseEntity.ok(ApiResponseDTO.success(response, "토큰이 재발급되었습니다."));
  }
}
