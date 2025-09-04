package com.softlabs.aicontents.common.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;

@RestController
public class SwaggerTestController {
    
    @GetMapping("/swagger-test")
    @Operation(summary = "Swagger 테스트 API", description = "Swagger UI에서 동작을 확인하기 위한 테스트용 API입니다.")
    public String swaggerTest() {
        return "Swagger 연동이 정상적으로 작동 중입니다!";
    }
}
