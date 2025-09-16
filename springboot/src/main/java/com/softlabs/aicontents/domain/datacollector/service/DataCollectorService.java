package com.softlabs.aicontents.domain.datacollector.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.softlabs.aicontents.common.util.TraceIdUtil;
import com.softlabs.aicontents.domain.datacollector.model.CrawlJobType;
import com.softlabs.aicontents.domain.datacollector.model.ProductInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCollectorService {

  private final Browser browser;
  private final NaverStoreApiService naverStoreApiService;
  private final KeywordValidationService keywordValidationService;
  private final SsadaguCrawlerService ssadaguCrawlerService;
  private final DatabaseService databaseService;

  public ProductInfo executeManualCrawling(String keyword) {
    TraceIdUtil.setNewTraceId();
    log.info("수동 크롤링 시작 - keyword: {}", keyword);

    try {
      if (!keywordValidationService.isBasicValidKeyword(keyword)) {
        log.warn("기본 유효성 검증 실패 - keyword: {}", keyword);
        return createFailedProductInfo(keyword, "기본 유효성 검증 실패", CrawlJobType.MANUAL);
      }

      BrowserContext context = browser.newContext();
      Page page = context.newPage();

      try {
        boolean isValid = keywordValidationService.validateKeyword(page, keyword);
        if (!isValid) {
          log.warn("키워드 유효성 검증 실패 - keyword: {}", keyword);
          return createFailedProductInfo(keyword, "키워드 유효성 검증 실패", CrawlJobType.MANUAL);
        }

        ProductInfo productInfo = ssadaguCrawlerService.crawlProductInfo(keyword);
        productInfo.setCrawlType(CrawlJobType.MANUAL.name());

        // 데이터베이스에 저장
        databaseService.saveProductInfo(productInfo);

        log.info("수동 크롤링 완료 - keyword: {}, product: {}", keyword, productInfo.getProductName());
        return productInfo;

      } finally {
        if (page != null) {
          try {
            page.close();
          } catch (Exception e) {
            log.debug("페이지 정리 중 오류 무시: {}", e.getMessage());
          }
        }
        if (context != null) {
          try {
            context.close();
          } catch (Exception e) {
            log.debug("컨텍스트 정리 중 오류 무시: {}", e.getMessage());
          }
        }
      }

    } catch (Exception e) {
      log.error("수동 크롤링 중 오류 발생 - keyword: {}", keyword, e);
      return createFailedProductInfo(keyword, "크롤링 오류: " + e.getMessage(), CrawlJobType.MANUAL);
    } finally {
      TraceIdUtil.clearTraceId();
    }
  }

  public ProductInfo executeAutoCrawling() {
    TraceIdUtil.setNewTraceId();
    log.info("자동 크롤링 시작");

    try {
      log.info("네이버 쇼핑 베스트에서 1-20위 키워드 수집 중...");
      List<String> bestKeywords = naverStoreApiService.getBestKeywords();

      if (bestKeywords.isEmpty()) {
        log.error("베스트 키워드를 가져오지 못했습니다. 프로그램을 종료합니다.");
        return createFailedProductInfo("", "네이버 쇼핑 베스트 키워드 수집 실패", CrawlJobType.AUTOMATIC);
      }

      log.info("베스트 키워드 {}개를 수집했습니다", bestKeywords.size());

      // 키워드 목록 출력
      log.info("=== 수집된 네이버 쇼핑 베스트 키워드 ===");
      for (int i = 0; i < bestKeywords.size(); i++) {
        log.info("{}위: {}", i + 1, bestKeywords.get(i));
      }

      log.info("키워드 순차 검증 시작...");
      String selectedKeyword = null;

      BrowserContext context = browser.newContext();
      Page page = context.newPage();

      try {
        for (int i = 0; i < bestKeywords.size(); i++) {
          String keyword = bestKeywords.get(i);
          int rank = i + 1;

          log.info("{}위 키워드 '{}' 검증 시작...", rank, keyword);

          try {
            if (!keywordValidationService.isBasicValidKeyword(keyword)) {
              log.warn("{}위 키워드 '{}' 기본 유효성 검증 실패 - 다음 키워드 확인", rank, keyword);
              continue;
            }

            // 30일 중복 검사
            boolean isUsedRecently = databaseService.isKeywordUsedRecently(keyword);
            if (isUsedRecently) {
              log.warn("{}위 키워드 '{}'는 최근 30일 내 사용됨 - 다음 키워드 확인", rank, keyword);
              continue;
            }
            log.info("{}위 키워드 '{}' - 30일 중복 검사 통과", rank, keyword);

            log.info("{}위 키워드 '{}' 실제 상품 연관성 검사 중...", rank, keyword);
            if (keywordValidationService.validateKeyword(page, keyword)) {
              log.info("최종 선택된 키워드: {} (순위: {}위, 모든 검증 통과)", keyword, rank);
              selectedKeyword = keyword;
              break;
            } else {
              log.warn("키워드 '{}' 실제 상품 연관성 검사 실패 - 대체 언어 시도", keyword);

              String alternativeKeyword = keywordValidationService.validateWithAlternativeLanguage(page, keyword);
              if (alternativeKeyword != null) {
                log.info("대체 언어 키워드로 최종 선택: {} (원본: {}, 순위: {}위)", alternativeKeyword, keyword, rank);
                selectedKeyword = alternativeKeyword;
                break;
              }

              log.warn("키워드 '{}' 대체 언어 검증도 실패 - 다음 키워드 확인", keyword);
            }

          } catch (Exception e) {
            log.error("{}위 키워드 '{}' 검증 중 오류 발생", rank, keyword, e);
            continue;
          }
        }

      } finally {
        if (page != null) {
          try {
            page.close();
          } catch (Exception e) {
            log.debug("페이지 정리 중 오류 무시: {}", e.getMessage());
          }
        }
        if (context != null) {
          try {
            context.close();
          } catch (Exception e) {
            log.debug("컨텍스트 정리 중 오류 무시: {}", e.getMessage());
          }
        }
      }

      if (selectedKeyword == null) {
        log.error("1-20위 키워드 중 모든 검증을 통과한 키워드가 없습니다.");
        return createFailedProductInfo("", "모든 키워드 검증 실패", CrawlJobType.AUTOMATIC);
      }

      log.info("선택된 키워드 확인 완료: '{}'", selectedKeyword);

      log.info("싸다구몰에서 '{}' 상품 정보 수집 중...", selectedKeyword);
      ProductInfo productInfo = ssadaguCrawlerService.crawlProductInfo(selectedKeyword);
      productInfo.setCrawlType(CrawlJobType.AUTOMATIC.name());

      // 데이터베이스에 저장
      databaseService.saveProductInfo(productInfo);

      validateAndDisplayResults(productInfo);

      log.info("=== 크롤링 작업이 완료되었습니다! ===");
      log.info("키워드: {}", selectedKeyword);
      log.info("상품명: {}", productInfo.getProductName());
      log.info("가격: {}", productInfo.getPrice());

      return productInfo;

    } catch (Exception e) {
      log.error("자동 크롤링 중 오류 발생", e);
      return createFailedProductInfo("오류 발생", "크롤링 실패: " + e.getMessage(), CrawlJobType.AUTOMATIC);
    } finally {
      TraceIdUtil.clearTraceId();
    }
  }

  private ProductInfo createFailedProductInfo(String keyword, String errorMessage, CrawlJobType crawlType) {
    ProductInfo productInfo = new ProductInfo();
    productInfo.setKeyword(keyword);
    productInfo.setProductName("정보 수집 실패");
    productInfo.setPrice("정보 없음");
    productInfo.setDescription(errorMessage);
    productInfo.setRating("정보 없음");
    productInfo.setProductUrl("정보 없음");
    productInfo.setCrawlType(crawlType.name());
    productInfo.setCurrentTime();

    return productInfo;
  }

  private void validateAndDisplayResults(ProductInfo productInfo) {
    log.info("=".repeat(60));
    log.info("수집된 상품 정보");
    log.info("=".repeat(60));

    log.info("검색 키워드: {}", productInfo.getKeyword());

    String productName = productInfo.getProductName();
    if (productName != null && !productName.equals("정보 수집 실패") && !productName.equals("정보 없음")) {
      log.info("✅ 상품명: {}", productName);
    } else {
      log.warn("❌ 상품명: {}", productName != null ? productName : "정보 없음");
    }

    String price = productInfo.getPrice();
    if (price != null && !price.equals("가격 정보 없음") && !price.equals("정보 없음")) {
      log.info("✅ 가격: {}", price);
    } else {
      log.warn("❌ 가격: {}", price != null ? price : "정보 없음");
    }

    String rating = productInfo.getRating();
    if (rating != null && !rating.equals("평점 정보 없음") && !rating.equals("정보 없음")) {
      log.info("✅ 평점: {}", rating);
    } else {
      log.warn("❌ 평점: {}", rating != null ? rating : "정보 없음");
    }

    String description = productInfo.getDescription();
    if (description != null && description.contains("재구매율")) {
      log.info("✅ 재구매율: {}", description);
    }

    String url = productInfo.getProductUrl();
    if (url != null && !url.equals("URL 정보 없음") && !url.equals("정보 없음")) {
      log.info("✅ 상품 URL: {}", url);
    }

    log.info("⏰ 수집 시간: {}", productInfo.getCrawledAt());
    log.info("=".repeat(60));

    boolean isSuccess = (productName != null && !productName.equals("정보 수집 실패") && !productName.equals("정보 없음")) &&
                       (price != null && !price.equals("가격 정보 없음") && !price.equals("정보 없음"));

    if (isSuccess) {
      log.info("🎉 크롤링 성공! 주요 정보가 정상적으로 수집되었습니다.");
    } else {
      log.warn("⚠️ 크롤링 부분 성공! 일부 정보만 수집되었습니다.");
      log.info("💡 다음 사항을 확인해보세요:");
      log.info("   - 싸다구몰 사이트의 구조가 변경되었을 수 있습니다.");
      log.info("   - 검색 결과에 상품이 없을 수 있습니다.");
      log.info("   - 네트워크 연결 상태를 확인하세요.");
    }
  }
}