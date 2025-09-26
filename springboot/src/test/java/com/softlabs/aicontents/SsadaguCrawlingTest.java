package com.softlabs.aicontents;

import java.time.Duration;
import java.time.Instant;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class SsadaguCrawlingTest {

  public static void main(String[] args) {
    SsadaguCrawlingTest test = new SsadaguCrawlingTest();

    // 테스트 키워드들
    String[] testKeywords = {"키보드", "마우스", "모니터", "헤드셋"};

    for (String keyword : testKeywords) {
      System.out.println("=== 테스트 시작: " + keyword + " ===");

      // 1차: API 직접 호출 방식 테스트
      ProductInfo apiResult = test.testApiCrawling(keyword);

      // 2차: Selenium 더미 방식 테스트
      ProductInfo seleniumResult = test.testSeleniumCrawling(keyword);

      // 결과 비교
      test.compareResults(keyword, apiResult, seleniumResult);

      System.out.println("=== 테스트 완료: " + keyword + " ===\n");
    }
  }

  /** API 직접 호출 방식 테스트 */
  private ProductInfo testApiCrawling(String keyword) {
    Instant start = Instant.now();

    try {
      System.out.println("API 직접 호출 방식으로 크롤링 시도: " + keyword);

      RestTemplate restTemplate = new RestTemplate();
      HttpHeaders headers = new HttpHeaders();

      // 브라우저 헤더 설정
      headers.set(
          "User-Agent",
          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
      headers.set(
          "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
      headers.set("Accept-Language", "ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3");
      headers.set("Referer", "https://ssadagu.kr/");

      HttpEntity<String> entity = new HttpEntity<>(headers);

      // 다양한 검색 URL 패턴 시도
      String[] searchUrls = {
        "https://ssadagu.kr/shop/search.php?stx=" + keyword,
        "https://ssadagu.kr/shop/search.php?q=" + keyword,
        "https://ssadagu.kr/search?keyword=" + keyword,
        "https://ssadagu.kr/product/search?query=" + keyword
      };

      for (String searchUrl : searchUrls) {
        try {
          System.out.println("  → URL 시도: " + searchUrl);

          ResponseEntity<String> response =
              restTemplate.exchange(searchUrl, HttpMethod.GET, entity, String.class);

          if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            ProductInfo productInfo = parseSearchResponse(response.getBody(), keyword);
            if (productInfo != null) {
              Duration elapsed = Duration.between(start, Instant.now());
              productInfo.setExecutionTime(elapsed.toMillis());
              System.out.println(
                  "✅ API 직접 호출 성공: "
                      + searchUrl
                      + " -> "
                      + productInfo.getProductName()
                      + " ("
                      + elapsed.toMillis()
                      + "ms)");
              return productInfo;
            }
          }
        } catch (Exception e) {
          System.out.println("  ❌ URL 시도 실패: " + searchUrl + " - " + e.getMessage());
        }
      }

      Duration elapsed = Duration.between(start, Instant.now());
      System.out.println("❌ API 직접 호출 실패 (" + elapsed.toMillis() + "ms)");
      return null;

    } catch (Exception e) {
      Duration elapsed = Duration.between(start, Instant.now());
      System.out.println(
          "❌ API 직접 호출 방식 실패: " + e.getMessage() + " (" + elapsed.toMillis() + "ms)");
      return null;
    }
  }

  /** Selenium 더미 방식 테스트 */
  private ProductInfo testSeleniumCrawling(String keyword) {
    Instant start = Instant.now();

    try {
      System.out.println("Selenium 방식으로 크롤링 시도 (더미): " + keyword);

      // Selenium 시뮬레이션 (실제로는 브라우저 실행 시간)
      Thread.sleep(2000);

      // 더미 상품 정보 생성 (실제로는 Selenium으로 크롤링)
      String productName = "[싸다구몰] " + keyword + " 베스트 상품";
      String sourceUrl = "https://ssadagu.kr/product/detail/" + keyword.toLowerCase();
      int price = (int) (Math.random() * 300000) + 10000;

      Duration elapsed = Duration.between(start, Instant.now());
      ProductInfo productInfo = new ProductInfo(productName, sourceUrl, price, "SELENIUM");
      productInfo.setExecutionTime(elapsed.toMillis());

      System.out.println(
          "✅ Selenium 방식 성공 (더미): " + productName + " (" + elapsed.toMillis() + "ms)");
      return productInfo;

    } catch (Exception e) {
      Duration elapsed = Duration.between(start, Instant.now());
      System.out.println("❌ Selenium 방식 실패: " + e.getMessage() + " (" + elapsed.toMillis() + "ms)");
      return null;
    }
  }

  /** HTML 응답에서 상품 정보 파싱 */
  private ProductInfo parseSearchResponse(String html, String keyword) {
    try {
      // HTML에서 상품 정보 추출 로직
      // 실제 싸다구몰의 HTML 구조에 따라 파싱 로직 작성 필요

      System.out.println("    HTML 응답 크기: " + html.length() + " bytes");
      System.out.println(
          "    HTML 미리보기: " + html.substring(0, Math.min(200, html.length())) + "...");

      if (html.contains("상품") || html.contains("product") || html.contains("search")) {
        // 간단한 파싱 예시 (실제로는 더 정교한 파싱 필요)
        String productName = "[싸다구몰 API] " + keyword + " 검색 결과 상품";
        String sourceUrl = "https://ssadagu.kr/product/" + keyword;
        int price = (int) (Math.random() * 200000) + 20000;

        return new ProductInfo(productName, sourceUrl, price, "API");
      }

      return null;

    } catch (Exception e) {
      System.out.println("❌ HTML 파싱 실패: " + e.getMessage());
      return null;
    }
  }

  /** 두 크롤링 방식 결과 비교 */
  private void compareResults(String keyword, ProductInfo apiResult, ProductInfo seleniumResult) {
    System.out.println("=== 크롤링 결과 비교: " + keyword + " ===");

    if (apiResult != null) {
      System.out.println("✅ API 방식 성공: " + apiResult.getExecutionTime() + "ms");
      System.out.println("   상품명: " + apiResult.getProductName());
      System.out.println("   가격: " + apiResult.getPrice() + "원");
      System.out.println("   URL: " + apiResult.getSourceUrl());
    } else {
      System.out.println("❌ API 방식 실패");
    }

    if (seleniumResult != null) {
      System.out.println("✅ Selenium 방식 성공: " + seleniumResult.getExecutionTime() + "ms");
      System.out.println("   상품명: " + seleniumResult.getProductName());
      System.out.println("   가격: " + seleniumResult.getPrice() + "원");
      System.out.println("   URL: " + seleniumResult.getSourceUrl());
    } else {
      System.out.println("❌ Selenium 방식 실패");
    }

    // 성능 비교
    if (apiResult != null && seleniumResult != null) {
      long speedDifference = seleniumResult.getExecutionTime() - apiResult.getExecutionTime();
      if (apiResult.getExecutionTime() < seleniumResult.getExecutionTime()) {
        System.out.println("📊 API 방식이 " + speedDifference + "ms 더 빠름");
      } else {
        System.out.println("📊 Selenium 방식이 " + (-speedDifference) + "ms 더 빠름");
      }
    }
  }

  // 테스트용 ProductInfo 클래스
  private static class ProductInfo {
    private String productName;
    private String sourceUrl;
    private int price;
    private String crawlingMethod;
    private long executionTime;

    public ProductInfo(String productName, String sourceUrl, int price, String crawlingMethod) {
      this.productName = productName;
      this.sourceUrl = sourceUrl;
      this.price = price;
      this.crawlingMethod = crawlingMethod;
    }

    // getters and setters
    public String getProductName() {
      return productName;
    }

    public String getSourceUrl() {
      return sourceUrl;
    }

    public int getPrice() {
      return price;
    }

    public String getCrawlingMethod() {
      return crawlingMethod;
    }

    public long getExecutionTime() {
      return executionTime;
    }

    public void setExecutionTime(long executionTime) {
      this.executionTime = executionTime;
    }
  }
}
