package com.softlabs.aicontents.domain.datacollector.service;

import com.softlabs.aicontents.common.util.TraceIdUtil;
import com.softlabs.aicontents.domain.datacollector.model.CrawlJobType;
import com.softlabs.aicontents.domain.datacollector.model.ProductInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 외부 스케줄러로부터 신호를 받아 크롤링을 실행하는 서비스
 * 다른 사람이 구현한 스케줄러가 AUTOMATIC/MANUAL 신호를 보내면 해당 크롤링을 실행
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlingTriggerService {

  private final DataCollectorService dataCollectorService;

  /**
   * 외부 스케줄러로부터 크롤링 실행 신호를 받아 처리
   *
   * @param crawlType 크롤링 타입 (AUTOMATIC 또는 MANUAL)
   * @param keyword MANUAL 타입일 때 사용할 키워드 (AUTOMATIC일 때는 무시됨)
   * @return 크롤링 결과
   */
  public ProductInfo executeCrawling(CrawlJobType crawlType, String keyword) {
    TraceIdUtil.setNewTraceId();

    log.info("🔔 외부 스케줄러로부터 크롤링 신호 수신 - type: {}", crawlType.name());

    try {
      ProductInfo result;

      switch (crawlType) {
        case AUTOMATIC:
          log.info("🤖 자동 크롤링 실행 시작");
          result = dataCollectorService.executeAutoCrawling();
          break;

        case MANUAL:
          log.info("👤 수동 크롤링 실행 시작 - keyword: {}", keyword);
          if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("MANUAL 크롤링에는 키워드가 필요합니다.");
          }
          result = dataCollectorService.executeManualCrawling(keyword);
          break;

        default:
          throw new IllegalArgumentException("지원하지 않는 크롤링 타입: " + crawlType);
      }

      if (result != null && !result.getProductName().equals("정보 수집 실패")) {
        log.info("✅ 외부 스케줄러 트리거 크롤링 성공 - type: {}, keyword: {}, product: {}",
                 crawlType.name(), result.getKeyword(), result.getProductName());
      } else {
        log.warn("⚠️ 외부 스케줄러 트리거 크롤링 부분 실패 - type: {}, result: {}",
                 crawlType.name(), result != null ? result.getDescription() : "알 수 없는 오류");
      }

      return result;

    } catch (Exception e) {
      log.error("❌ 외부 스케줄러 트리거 크롤링 중 오류 발생 - type: {}", crawlType.name(), e);
      throw e;
    } finally {
      TraceIdUtil.clearTraceId();
    }
  }

  /**
   * AUTOMATIC 크롤링 실행 (키워드 불필요)
   */
  public ProductInfo executeAutomaticCrawling() {
    return executeCrawling(CrawlJobType.AUTOMATIC, null);
  }

  /**
   * MANUAL 크롤링 실행 (키워드 필수)
   */
  public ProductInfo executeManualCrawling(String keyword) {
    return executeCrawling(CrawlJobType.MANUAL, keyword);
  }
}