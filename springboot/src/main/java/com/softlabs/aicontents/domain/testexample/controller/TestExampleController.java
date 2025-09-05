package com.softlabs.aicontents.domain.testexample.controller;

import com.softlabs.aicontents.domain.testexample.service.TestExampleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/test-example")
public class TestExampleController {
    
    private final TestExampleService testExampleService;
    
    public TestExampleController(TestExampleService testExampleService) {
        this.testExampleService = testExampleService;
    }
    
    @PostMapping
    @Operation(summary = "테스트 데이터 삽입", description = "RDS 테이블에 테스트 데이터를 삽입합니다.")
    public ResponseEntity<String> createTestExample(@RequestParam String testData) {
        try {
            testExampleService.createTestExample(testData);
            return ResponseEntity.ok("테스트 데이터가 성공적으로 삽입되었습니다: " + testData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("데이터 삽입 실패: " + e.getMessage());
        }
    }
}