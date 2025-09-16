package com.softlabs.aicontents.domain.datacollector.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.softlabs.aicontents.common.util.TraceIdUtil;
import com.softlabs.aicontents.domain.datacollector.model.ProductInfo;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SsadaguCrawlerService {

  private final Browser browser;

  public ProductInfo crawlProductInfo(String keyword) {
    TraceIdUtil.setNewTraceId();

    ProductInfo productInfo = new ProductInfo();
    productInfo.setKeyword(keyword);
    productInfo.setCrawledAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

    BrowserContext context = null;
    Page page = null;

    try {
      context = browser.newContext(new Browser.NewContextOptions()
          .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
          .setViewportSize(1920, 1080));

      page = context.newPage();

      log.info("싸다구몰에 접속 중... keyword: {}", keyword);
      page.navigate("https://ssadagu.kr");
      page.waitForLoadState(LoadState.NETWORKIDLE);
      page.waitForTimeout(3000);
      log.info("싸다구몰 접속 완료: {}", page.url());

      log.info("키워드 '{}' 검색 중...", keyword);

      Locator searchBox = findSearchBox(page);

      if (searchBox == null) {
        throw new RuntimeException("검색창을 찾을 수 없습니다.");
      }

      performSearch(page, searchBox, keyword);

      log.info("검색 결과 로딩 중...");
      page.waitForLoadState(LoadState.NETWORKIDLE);
      page.waitForTimeout(5000);

      log.info("검색 완료: {}", page.url());

      log.info("첫 번째 상품을 찾아서 클릭 중...");

      boolean productClicked = clickFirstProduct(page);

      if (!productClicked) {
        log.warn("첫 번째 상품을 클릭하지 못했습니다. 검색 결과에서 정보를 추출합니다.");
        extractFromSearchResults(page, productInfo);
        return productInfo;
      }

      log.info("상품 상세 페이지에서 정보 추출 중...");
      page.waitForLoadState(LoadState.NETWORKIDLE);
      page.waitForTimeout(3000);

      log.info("상품 상세 페이지 접속 완료: {}", page.url());
      productInfo.setProductUrl(page.url());

      extractFromDetailPage(page, productInfo);

    } catch (Exception e) {
      log.error("크롤링 중 오류 발생: keyword={}", keyword, e);

      if (productInfo.getProductName() == null || productInfo.getProductName().isEmpty()) {
        productInfo.setProductName("정보 수집 실패");
        productInfo.setDescription("크롤링 오류: " + e.getMessage());
      }
    } finally {
      if (page != null) {
        try {
          page.waitForTimeout(1000);
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

    return productInfo;
  }

  private Locator findSearchBox(Page page) {
    String[] searchSelectors = {
        "input[name='ss_tx']",
        "input[type='text'][name*='search']",
        "input[type='text'][name*='ss']",
        "input[placeholder*='검색']",
        "input[placeholder*='상품']",
        "input[placeholder*='찾기']",
        "#ss_tx",
        ".search-input",
        "#search-input",
        "input[type='text']",
        "input[type='search']"
    };

    for (String selector : searchSelectors) {
      try {
        Locator locator = page.locator(selector);
        if (locator.count() > 0) {
          Locator firstInput = locator.first();
          if (firstInput.isVisible() && firstInput.isEnabled()) {
            log.debug("검색창 발견: {}", selector);
            return firstInput;
          }
        }
      } catch (Exception e) {
        continue;
      }
    }

    log.error("검색창을 찾을 수 없습니다.");
    return null;
  }

  private void performSearch(Page page, Locator searchBox, String keyword) {
    try {
      searchBox.clear();
      page.waitForTimeout(500);
      searchBox.fill(keyword);
      page.waitForTimeout(1000);

      searchBox.press("Enter");
      page.waitForTimeout(2000);

      String currentUrl = page.url();
      if (!currentUrl.contains("search") && !currentUrl.contains(keyword)) {
        log.debug("Enter 키 검색 실패, 검색 버튼 클릭 시도...");

        String[] searchButtonSelectors = {
            "button[type='submit']", "input[type='submit']",
            ".search-button", "#search-button", ".btn-search",
            "button:contains(검색)", "input[value*='검색']"
        };

        for (String buttonSelector : searchButtonSelectors) {
          try {
            if (page.locator(buttonSelector).count() > 0) {
              page.locator(buttonSelector).first().click();
              page.waitForTimeout(2000);
              break;
            }
          } catch (Exception e) {
            continue;
          }
        }
      }
    } catch (Exception e) {
      log.warn("일반적인 검색 방법 실패, JavaScript로 시도...");

      page.evaluate("() => {" +
          "const searchInput = document.querySelector('input[name=\"ss_tx\"], input[type=\"text\"]');" +
          "if (searchInput) {" +
          "  searchInput.value = '" + keyword + "';" +
          "  const form = searchInput.closest('form');" +
          "  if (form) {" +
          "    form.submit();" +
          "  } else {" +
          "    const event = new KeyboardEvent('keydown', { key: 'Enter' });" +
          "    searchInput.dispatchEvent(event);" +
          "  }" +
          "}" +
      "}");
    }
  }

  private boolean clickFirstProduct(Page page) {
    try {
      log.debug("상품 링크를 찾는 중...");

      page.waitForTimeout(2000);

      String[] productLinkSelectors = {
          "a[href*='view.php']:not([href*='search_view.php'])",
          "a[href*='view.php'][href*='platform'][href*='num_iid']"
      };

      for (String selector : productLinkSelectors) {
        try {
          Locator products = page.locator(selector);
          int count = products.count();
          log.debug("선택자 '{}'로 {}개 요소 발견", selector, count);

          if (count > 0) {
            for (int i = 0; i < Math.min(count, 3); i++) {
              try {
                Locator product = products.nth(i);

                if (product.isVisible() && product.isEnabled()) {
                  log.debug("{}번째 상품 클릭 시도...", i+1);

                  product.scrollIntoViewIfNeeded();
                  page.waitForTimeout(1000);

                  String beforeUrl = page.url();

                  product.click();
                  page.waitForTimeout(3000);

                  String afterUrl = page.url();
                  if (!afterUrl.equals(beforeUrl)) {
                    log.info("상품 클릭 성공! 페이지 이동됨: {}", afterUrl);
                    return true;
                  } else {
                    log.debug("클릭했지만 페이지가 변경되지 않음. 다음 상품 시도...");
                  }
                }
              } catch (Exception e) {
                log.debug("{}번째 상품 클릭 실패: {}", i+1, e.getMessage());
                continue;
              }
            }
          }
        } catch (Exception e) {
          log.debug("선택자 '{}' 시도 중 오류: {}", selector, e.getMessage());
          continue;
        }
      }

      return tryJavaScriptClick(page);

    } catch (Exception e) {
      log.error("상품 클릭 중 오류", e);
    }

    log.warn("모든 방법으로 상품 클릭을 시도했지만 실패했습니다.");
    return false;
  }

  private boolean tryJavaScriptClick(Page page) {
    log.debug("JavaScript로 실제 상품 링크 클릭 시도...");
    Object result = page.evaluate("() => {" +
        "const productLinks = document.querySelectorAll('a[href*=\"view.php\"]:not([href*=\"search_view.php\"])');" +
        "console.log('실제 상품 링크 개수:', productLinks.length);" +

        "for (let i = 0; i < Math.min(productLinks.length, 5); i++) {" +
        "  const link = productLinks[i];" +
        "  const href = link.href;" +
        "  console.log('링크 ' + (i+1) + ':', href);" +

        "  if (href.includes('platform=') && href.includes('num_iid=') && link.offsetParent !== null) {" +
        "    try {" +
        "      link.scrollIntoView();" +
        "      link.click();" +
        "      console.log('실제 상품 링크 클릭 성공:', href);" +
        "      return {success: true, url: href, index: i};" +
        "    } catch(e) {" +
        "      console.log('링크 클릭 오류:', e.message);" +
        "    }" +
        "  }" +
        "}" +

        "return {success: false};" +
    "}");

    if (result instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> resultMap = (Map<String, Object>) result;
      Boolean success = (Boolean) resultMap.get("success");

      if (Boolean.TRUE.equals(success)) {
        page.waitForTimeout(3000);
        log.info("JavaScript 클릭 성공!");
        return true;
      }
    }

    return false;
  }

  private void extractFromSearchResults(Page page, ProductInfo productInfo) {
    log.info("검색 결과 페이지에서 정보 추출 중...");

    try {
      String htmlContent = page.content();
      Document doc = Jsoup.parse(htmlContent);

      Element firstProduct = findFirstProductInResults(doc);

      if (firstProduct != null) {
        log.info("검색 결과에서 첫 번째 상품 발견!");

        String productName = extractProductName(firstProduct);
        productInfo.setProductName(productName);
        log.debug("상품명: {}", productName);

        String price = extractProductPrice(firstProduct);
        productInfo.setPrice(price);
        log.debug("가격: {}", price);

        String rating = extractProductRating(firstProduct);
        productInfo.setRating(rating);
        log.debug("평점: {}", rating);

        String repurchaseRate = extractRepurchaseRate(firstProduct);
        String salesCount = extractSalesCount(firstProduct);

        productInfo.setDescription("재구매율: " + repurchaseRate + ", 판매개수: " + salesCount);

        String productUrl = extractProductUrl(firstProduct);
        if (!productUrl.equals("URL 정보 없음")) {
          productInfo.setProductUrl(productUrl);
        } else {
          productInfo.setProductUrl(page.url());
        }

      } else {
        log.warn("검색 결과에서 상품을 찾을 수 없습니다.");
        setDefaultProductInfo(productInfo, page.url());
      }

    } catch (Exception e) {
      log.error("검색 결과 추출 중 오류", e);
      setDefaultProductInfo(productInfo, page.url());
    }
  }

  private void extractFromDetailPage(Page page, ProductInfo productInfo) {
    log.info("상품 상세 페이지에서 정보 추출 중...");

    try {
      String htmlContent = page.content();
      Document doc = Jsoup.parse(htmlContent);

      extractDetailedProductInfo(doc, productInfo);

    } catch (Exception e) {
      log.error("상세 페이지 정보 추출 중 오류", e);
    }
  }

  // Helper methods for HTML parsing
  private Element findFirstProductInResults(Document doc) {
    String[] productSelectors = {
        "div.product_item",
        "div.product_info",
        "div:has(.product_title)",
        "div:has(.product_price)",
        "div:has(a[href*='view.php'])",
        ".product_item",
        ".product_info"
    };

    for (String selector : productSelectors) {
      try {
        Elements elements = doc.select(selector);
        if (!elements.isEmpty()) {
          log.debug("상품 선택자 발견: {} (개수: {})", selector, elements.size());
          return elements.first();
        }
      } catch (Exception e) {
        continue;
      }
    }

    return null;
  }

  private String extractProductName(Element productElement) {
    String[] nameSelectors = {
        ".product_title", "div.product_title", ".title", "[class*='title']",
        "a[href*='view.php']", ".product-name", ".product_name",
        ".goods-name", ".item-title", ".product-title",
        "h1", "h2", "h3", "h4",
        ".name", "[class*='name']",
        "a[title]",
        "img[alt]"
    };

    for (String selector : nameSelectors) {
      try {
        Element element = productElement.selectFirst(selector);
        if (element != null) {
          String text = element.text().trim();

          if (text.isEmpty()) {
            text = element.attr("title");
            if (text.isEmpty()) {
              text = element.attr("alt");
            }
          }

          if (!text.isEmpty() && text.length() > 3 && text.length() < 200) {
            if (!text.equals("더보기") && !text.equals("상세보기") &&
                !text.equals("이미지") && !text.matches("^[0-9]+$")) {
              return text;
            }
          }
        }
      } catch (Exception e) {
        continue;
      }
    }

    return "상품명 정보 없음";
  }

  private String extractProductPrice(Element productElement) {
    String[] priceSelectors = {
        ".product_price", "div.product_price", ".price", "[class*='price']",
        "[class*='cost']", ".amount", ".money", ".won", ".sale-price",
        ".current-price", ".final-price", ".price-current",
        "span:contains(원)", "div:contains(원)",
        "strong:contains(원)", "em:contains(원)"
    };

    for (String selector : priceSelectors) {
      try {
        Element element = productElement.selectFirst(selector);
        if (element != null && !element.text().trim().isEmpty()) {
          String priceText = element.text().trim();

          if ((priceText.matches(".*[0-9,]+.*") && priceText.contains("원")) ||
              priceText.matches(".*[0-9,]+\\s*원.*") ||
              priceText.matches(".*\\d+.*원.*")) {

            priceText = priceText.replaceAll("판매가격|가격|Price|price", "").trim();

            if (priceText.length() > 0 && priceText.length() < 50) {
              return priceText;
            }
          }
        }
      } catch (Exception e) {
        continue;
      }
    }

    try {
      String fullText = productElement.text();
      java.util.regex.Pattern pricePattern = java.util.regex.Pattern.compile("([0-9,]+\\s*원)");
      java.util.regex.Matcher matcher = pricePattern.matcher(fullText);
      if (matcher.find()) {
        return matcher.group(1);
      }
    } catch (Exception e) {
      // 무시
    }

    return "가격 정보 없음";
  }

  private String extractProductRating(Element productElement) {
    try {
      Element starContainer = productElement.selectFirst(".start");
      if (starContainer != null) {
        Elements allStars = starContainer.select("img[src*='icon_star.svg']");
        Elements fullStars = starContainer.select("img[src*='icon_star.svg']:not([src*='none'])");

        if (allStars.size() > 0) {
          return fullStars.size() + "/" + allStars.size() + "점";
        }
      }

      String[] ratingSelectors = {
          ".rating", ".star", ".score", ".grade", "[class*='rating']"
      };

      for (String selector : ratingSelectors) {
        Element element = productElement.selectFirst(selector);
        if (element != null && !element.text().trim().isEmpty()) {
          return element.text().trim();
        }
      }

    } catch (Exception e) {
      log.debug("평점 추출 오류: {}", e.getMessage());
    }

    return "평점 정보 없음";
  }

  private String extractRepurchaseRate(Element productElement) {
    try {
      Element repurchaseElement = productElement.selectFirst(".product_repurchaseRate");
      if (repurchaseElement != null) {
        String text = repurchaseElement.text().trim();
        if (text.contains("%")) {
          String[] parts = text.split("\\s+");
          for (String part : parts) {
            if (part.contains("%")) {
              return part;
            }
          }
        }
        return text;
      }
    } catch (Exception e) {
      log.debug("재구매율 추출 오류: {}", e.getMessage());
    }

    return "재구매율 정보 없음";
  }

  private String extractSalesCount(Element productElement) {
    try {
      Element salesElement = productElement.selectFirst(".product_sales");
      if (salesElement != null) {
        String text = salesElement.text().trim();
        if (text.contains("개")) {
          java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s*개");
          java.util.regex.Matcher matcher = pattern.matcher(text);
          if (matcher.find()) {
            return matcher.group(1) + "개";
          }
        }
        return text;
      }
    } catch (Exception e) {
      log.debug("판매개수 추출 오류: {}", e.getMessage());
    }

    return "판매 정보 없음";
  }

  private String extractProductUrl(Element productElement) {
    try {
      Element linkElement = productElement.selectFirst("a[href*='view.php']");
      if (linkElement != null) {
        String href = linkElement.attr("href");
        if (href.startsWith("http")) {
          return href;
        } else if (href.startsWith("/")) {
          return "https://ssadagu.kr" + href;
        } else {
          return "https://ssadagu.kr/" + href;
        }
      }
    } catch (Exception e) {
      log.debug("상품 URL 추출 오류: {}", e.getMessage());
    }

    return "URL 정보 없음";
  }

  private void extractDetailedProductInfo(Document doc, ProductInfo productInfo) {
    log.debug("상세 페이지에서 정보 추출 중...");

    String[] nameSelectors = {
        "h1", "h2", ".product-title", ".goods-name", ".title",
        ".product-name", ".item-title", ".detail-title", ".product_title",
        ".goods-title", ".item-name", "[class*='title']", "[class*='name']"
    };

    String productName = extractTextBySelectors(doc, nameSelectors);
    if (!productName.equals("정보 없음") && productName.length() > 3) {
      productInfo.setProductName(productName);
      log.debug("상세 페이지 상품명: {}", productName);
    }

    String[] priceSelectors = {
        ".price", ".current-price", ".sale-price", ".price-current",
        ".final-price", ".cost", ".won", ".product_price", ".amount",
        ".price-now", ".selling-price", "[class*='price']", "[class*='cost']",
        "span:contains(원)", "strong:contains(원)", "em:contains(원)"
    };

    String price = extractPriceBySelectors(doc, priceSelectors);
    if (!price.equals("정보 없음")) {
      productInfo.setPrice(price);
      log.debug("상세 페이지 가격: {}", price);
    }

    String detailedSpecs = extractProductSpecs(doc);
    if (!detailedSpecs.isEmpty()) {
      String existingDesc = productInfo.getDescription();
      if (existingDesc != null && !existingDesc.isEmpty()) {
        productInfo.setDescription(existingDesc + "\n\n=== 상품 상세 스펙 ===\n" + detailedSpecs);
      } else {
        productInfo.setDescription("=== 상품 상세 스펙 ===\n" + detailedSpecs);
      }
      log.debug("상품 스펙 정보를 추출했습니다.");
    }

    String[] ratingSelectors = {
        ".rating", ".star", ".score", ".grade", "[class*='rating']",
        "[class*='star']", ".review-rating"
    };

    String rating = extractTextBySelectors(doc, ratingSelectors);
    if (!rating.equals("정보 없음")) {
      productInfo.setRating(rating);
      log.debug("평점: {}", rating);
    }
  }

  private String extractTextBySelectors(Document doc, String[] selectors) {
    for (String selector : selectors) {
      try {
        Element element = doc.selectFirst(selector);
        if (element != null && !element.text().trim().isEmpty()) {
          return element.text().trim();
        }
      } catch (Exception e) {
        continue;
      }
    }
    return "정보 없음";
  }

  private String extractPriceBySelectors(Document doc, String[] selectors) {
    for (String selector : selectors) {
      try {
        Element element = doc.selectFirst(selector);
        if (element != null && !element.text().trim().isEmpty()) {
          String priceText = element.text().trim();

          if ((priceText.matches(".*[0-9,]+.*") && priceText.contains("원")) ||
              priceText.matches(".*[0-9,]+\\s*원.*") ||
              priceText.matches(".*\\d+.*원.*")) {

            priceText = priceText.replaceAll("판매가격|가격|Price|price", "").trim();

            if (priceText.length() > 0 && priceText.length() < 50) {
              return priceText;
            }
          }
        }
      } catch (Exception e) {
        continue;
      }
    }

    try {
      String fullText = doc.text();
      java.util.regex.Pattern pricePattern = java.util.regex.Pattern.compile("([0-9,]+\\s*원)");
      java.util.regex.Matcher matcher = pricePattern.matcher(fullText);
      if (matcher.find()) {
        return matcher.group(1);
      }
    } catch (Exception e) {
      // 무시
    }

    return "정보 없음";
  }

  private String extractProductSpecs(Document doc) {
    StringBuilder specs = new StringBuilder();

    try {
      Element proInfoBoxs = doc.selectFirst(".pro-info-boxs");
      if (proInfoBoxs != null) {
        Elements proInfoItems = proInfoBoxs.select(".pro-info-item");

        log.debug("상품 스펙 {}개 항목 발견", proInfoItems.size());

        for (Element item : proInfoItems) {
          Element titleElement = item.selectFirst(".pro-info-title");
          Element infoElement = item.selectFirst(".pro-info-info");

          if (titleElement != null && infoElement != null) {
            String title = titleElement.text().trim();
            String info = infoElement.text().trim();

            if (!title.isEmpty() && !info.isEmpty() && info.length() < 100) {
              specs.append(title).append(": ").append(info).append("\n");
            }
          }
        }
      }

    } catch (Exception e) {
      log.debug("스펙 추출 중 오류: {}", e.getMessage());
    }

    return specs.toString();
  }

  private void setDefaultProductInfo(ProductInfo productInfo, String currentUrl) {
    if (productInfo.getProductName() == null || productInfo.getProductName().isEmpty()) {
      productInfo.setProductName("검색 결과: " + productInfo.getKeyword());
    }

    if (productInfo.getPrice() == null || productInfo.getPrice().isEmpty()) {
      productInfo.setPrice("가격 확인 필요");
    }

    productInfo.setProductUrl(currentUrl);
    productInfo.setDescription("검색 결과 페이지에서 추출된 정보");
    productInfo.setRating("평점 정보 없음");
  }
}