package com.softlabs.aicontents.domain.datacollector.controller;

import com.softlabs.aicontents.domain.datacollector.model.CrawlJobType;
import com.softlabs.aicontents.domain.datacollector.model.ProductInfo;
import com.softlabs.aicontents.domain.datacollector.service.CrawlingTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 데이터 수집기 컨트롤러
 * 스케줄러 및 내부 시스템에서 크롤링 기능을 호출할 수 있는 인터페이스 제공
 */
@RestController
@RequestMapping("/internal/datacollector")
@RequiredArgsConstructor
@Slf4j
public class DataCollectorController {

    private final CrawlingTriggerService crawlingTriggerService;

    /**
     * 스케줄러에서 호출하는 크롤링 시작점
     *
     * @param crawlType 크롤링 타입 (AUTOMATIC 또는 MANUAL)
     * @param keyword 수동 크롤링 시 사용할 키워드 (자동 크롤링에서는 무시됨)
     * @return 크롤링 결과
     */
    @PostMapping("/crawl/{crawlType}")
    public ResponseEntity<ProductInfo> executeCrawling(
            @PathVariable("crawlType") String crawlType,
            @RequestParam(value = "keyword", required = false) String keyword) {

        log.info("🔔 스케줄러로부터 크롤링 요청 수신 - type: {}, keyword: {}", crawlType, keyword);

        try {
            // 문자열을 ENUM으로 변환
            CrawlJobType jobType = CrawlJobType.valueOf(crawlType.toUpperCase());

            // 크롤링 실행
            ProductInfo result = crawlingTriggerService.executeCrawling(jobType, keyword);

            if (result != null && !result.getProductName().equals("정보 수집 실패")) {
                log.info("✅ 스케줄러 요청 크롤링 성공 - type: {}, product: {}",
                         crawlType, result.getProductName());
                return ResponseEntity.ok(result);
            } else {
                log.warn("⚠️ 스케줄러 요청 크롤링 부분 실패 - type: {}", crawlType);
                return ResponseEntity.ok(result); // 부분 실패도 200으로 반환
            }

        } catch (IllegalArgumentException e) {
            log.error("❌ 잘못된 크롤링 타입: {}", crawlType, e);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("❌ 스케줄러 요청 크롤링 중 오류 발생 - type: {}", crawlType, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 자동 크롤링 실행 (편의 메서드)
     * 스케줄러에서 자동 크롤링만 실행할 때 사용
     *
     * @return 크롤링 결과
     */
    @PostMapping("/crawl/auto")
    public ResponseEntity<ProductInfo> executeAutomaticCrawling() {
        log.info("🤖 스케줄러로부터 자동 크롤링 요청 수신");

        try {
            ProductInfo result = crawlingTriggerService.executeAutomaticCrawling();

            if (result != null && !result.getProductName().equals("정보 수집 실패")) {
                log.info("✅ 자동 크롤링 성공 - product: {}", result.getProductName());
                return ResponseEntity.ok(result);
            } else {
                log.warn("⚠️ 자동 크롤링 부분 실패");
                return ResponseEntity.ok(result);
            }

        } catch (Exception e) {
            log.error("❌ 자동 크롤링 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 수동 크롤링 실행 (편의 메서드)
     * 스케줄러에서 특정 키워드로 크롤링할 때 사용
     *
     * @param keyword 크롤링할 키워드
     * @return 크롤링 결과
     */
    @PostMapping("/crawl/manual")
    public ResponseEntity<ProductInfo> executeManualCrawling(
            @RequestParam("keyword") String keyword) {

        log.info("👤 스케줄러로부터 수동 크롤링 요청 수신 - keyword: {}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            log.error("❌ 수동 크롤링에 키워드가 필요합니다");
            return ResponseEntity.badRequest().build();
        }

        try {
            ProductInfo result = crawlingTriggerService.executeManualCrawling(keyword);

            if (result != null && !result.getProductName().equals("정보 수집 실패")) {
                log.info("✅ 수동 크롤링 성공 - keyword: {}, product: {}",
                         keyword, result.getProductName());
                return ResponseEntity.ok(result);
            } else {
                log.warn("⚠️ 수동 크롤링 부분 실패 - keyword: {}", keyword);
                return ResponseEntity.ok(result);
            }

        } catch (Exception e) {
            log.error("❌ 수동 크롤링 중 오류 발생 - keyword: {}", keyword, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 헬스 체크 엔드포인트
     * 스케줄러에서 크롤링 서비스 상태 확인용
     *
     * @return 서비스 상태
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.debug("헬스 체크 요청 수신");
        return ResponseEntity.ok("Data Collector Service is running");
    }
}