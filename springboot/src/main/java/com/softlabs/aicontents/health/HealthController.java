//package com.softlabs.aicontents.health;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/health")
//public class HealthController {
//
//    private final HealthService healthService;
//
//    public HealthController(HealthService healthService) {
//        this.healthService = healthService;
//    }
//
//    @GetMapping
//    public ResponseEntity<Map<String, Object>> checkHealth() {
//        Map<String, Object> healthInfo = healthService.checkHealth();
//
//        // 연결 상태에 따라 HTTP 상태 코드 설정
//        if ("UP".equals(healthInfo.get("status"))) {
//            return ResponseEntity.ok(healthInfo);
//        } else {
//            return ResponseEntity.status(503).body(healthInfo);
//        }
//    }
//
//    @GetMapping("/db")
//    public ResponseEntity<Map<String, Object>> checkDatabase() {
//        return checkHealth();
//    }
//}
