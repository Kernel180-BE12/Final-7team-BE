// package com.softlabs.aicontents.domain.auth.controller;
//
// import com.softlabs.aicontents.common.dto.response.ApiResponseDTO;
// import com.softlabs.aicontents.domain.auth.dto.LoginRequestDto;
// import com.softlabs.aicontents.domain.auth.dto.LoginResponseDto;
// import com.softlabs.aicontents.domain.auth.service.AuthService;
// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.responses.ApiResponse;
// import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.tags.Tag;
// import jakarta.validation.Valid;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
//
// @RestController
// @RequestMapping("/auth")
// @Tag(name = "Authentication", description = "인증 관련 API")
// public class AuthController {
//
//  private final AuthService authService;
//
//  public AuthController(AuthService authService) {
//    this.authService = authService;
//  }
//
//  @PostMapping("/login")
//  @Operation(summary = "로그인", description = "사용자 로그인을 처리합니다.")
//  @ApiResponses({
//    @ApiResponse(responseCode = "200", description = "로그인 성공"),
//    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
//    @ApiResponse(responseCode = "401", description = "인증 실패")
//  })
//  public ResponseEntity<ApiResponseDTO<LoginResponseDto>> login(
//      @Valid @RequestBody LoginRequestDto loginRequestDto) {
//    LoginResponseDto response = authService.login(loginRequestDto);
//    return ResponseEntity.ok(ApiResponseDTO.success(response, "로그인이 완료되었습니다."));
//  }
// }
