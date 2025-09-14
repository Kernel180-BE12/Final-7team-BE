package com.softlabs.aicontents.domain.sample.controller;

import com.softlabs.aicontents.domain.sample.dto.SampleRequestDTO;
import com.softlabs.aicontents.domain.sample.dto.SampleResponseDTO;
import com.softlabs.aicontents.domain.sample.service.SampleValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/sample")
@RequiredArgsConstructor
@Tag(name = "Sample Validation API", description = "검증 및 예외 처리 테스트용 API")
public class SampleValidationController {

  private final SampleValidationService sampleValidationService;

  @Operation(summary = "Validation 테스트", description = "Bean Validation 어노테이션 동작 확인")
  @PostMapping("/validate")
  public ResponseEntity<SampleResponseDTO> validateTest(
      @Valid @RequestBody SampleRequestDTO request) {

    log.info("Validation 테스트 요청 - 이름: {}, 이메일: {}", request.getName(), request.getEmail());

    SampleResponseDTO response = sampleValidationService.processValidData(request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "비즈니스 예외 테스트", description = "비즈니스 로직 예외 처리 확인")
  @GetMapping("/business-error/{type}")
  public ResponseEntity<SampleResponseDTO> businessErrorTest(
      @Parameter(description = "에러 타입: notfound, unauthorized, forbidden, conflict") @PathVariable
          String type) {

    log.info("비즈니스 예외 테스트 요청 - 타입: {}", type);

    sampleValidationService.throwBusinessException(type);
    return ResponseEntity.ok(SampleResponseDTO.of("이 응답은 나오면 안됩니다", "error"));
  }

  @Operation(summary = "서버 예외 테스트", description = "예상치 못한 서버 에러 처리 확인")
  @GetMapping("/server-error")
  public ResponseEntity<SampleResponseDTO> serverErrorTest() {

    log.info("서버 예외 테스트 요청");

    sampleValidationService.throwServerException();
    return ResponseEntity.ok(SampleResponseDTO.of("이 응답은 나오면 안됩니다", "error"));
  }

  @Operation(summary = "NPE 테스트", description = "NullPointerException 처리 확인")
  @GetMapping("/npe-error")
  public ResponseEntity<SampleResponseDTO> npeErrorTest() {

    log.info("NPE 예외 테스트 요청");

    sampleValidationService.throwNullPointerException();
    return ResponseEntity.ok(SampleResponseDTO.of("이 응답은 나오면 안됩니다", "error"));
  }

  @Operation(summary = "정상 응답 테스트", description = "정상적인 API 응답 확인")
  @GetMapping("/success")
  public ResponseEntity<SampleResponseDTO> successTest(
      @Parameter(description = "테스트 메시지") @RequestParam(defaultValue = "테스트") String message) {

    log.info("정상 응답 테스트 요청 - 메시지: {}", message);

    SampleResponseDTO response = SampleResponseDTO.of("정상 처리 완료", message);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "파라미터 타입 오류 테스트", description = "잘못된 파라미터 타입 전달 시 처리 확인")
  @GetMapping("/type-error/{id}")
  public ResponseEntity<SampleResponseDTO> typeErrorTest(
      @Parameter(description = "숫자 ID (문자 전달하면 에러)") @PathVariable Long id) {

    log.info("타입 에러 테스트 요청 - ID: {}", id);

    SampleResponseDTO response = SampleResponseDTO.of("ID 처리 완료", id.toString());
    return ResponseEntity.ok(response);
  }
}
