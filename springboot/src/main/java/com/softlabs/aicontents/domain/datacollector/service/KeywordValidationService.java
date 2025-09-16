package com.softlabs.aicontents.domain.datacollector.service;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.softlabs.aicontents.common.util.TraceIdUtil;
import com.softlabs.aicontents.domain.datacollector.util.PapagoTranslationService;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeywordValidationService {

  private final PapagoTranslationService translationService;

  public boolean isBasicValidKeyword(String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
      return false;
    }

    keyword = keyword.trim();

    if (keyword.length() < 2 || keyword.length() > 20) {
      return false;
    }

    if (keyword.matches("^[0-9]+$")) {
      return false;
    }

    if (keyword.contains("<") || keyword.contains(">") || keyword.contains("&") ||
        keyword.contains("javascript") || keyword.contains("function")) {
      return false;
    }

    String[] invalidKeywords = {
        "더보기", "검색", "베스트", "인기", "순위", "전체", "카테고리",
        "필터", "정렬", "페이지", "이동", "클릭", "선택", "버튼",
        "레이어", "열기", "닫기", "메뉴", "네비게이션"
    };

    for (String invalid : invalidKeywords) {
      if (keyword.toLowerCase().contains(invalid.toLowerCase())) {
        return false;
      }
    }

    return true;
  }

  public boolean validateKeyword(Page page, String keyword) {
    return validateKeyword(page, keyword, null);
  }

  public boolean validateKeyword(Page page, String searchKeyword, String originalKeyword) {
    if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
      log.warn("빈 키워드는 검증할 수 없습니다.");
      return false;
    }

    log.info("키워드 '{}' 유효성 검증 시작...", searchKeyword);
    if (originalKeyword != null && !originalKeyword.equals(searchKeyword)) {
      log.info("원본 키워드: '{}' 와 함께 유사도 검증", originalKeyword);
    }

    try {
      page.navigate("https://ssadagu.kr");
      page.waitForLoadState();

      String[] searchSelectors = {
          "input[name='ss_tx']",
          "input[type='text'][placeholder*='검색']",
          ".search-input",
          "input.form-control"
      };

      Locator searchBox = null;
      for (String selector : searchSelectors) {
        if (page.locator(selector).count() > 0) {
          searchBox = page.locator(selector).first();
          break;
        }
      }

      if (searchBox == null) {
        log.error("검색창을 찾을 수 없어 키워드 검증 실패");
        return false;
      }

      searchBox.clear();
      searchBox.fill(searchKeyword);
      searchBox.press("Enter");
      page.waitForLoadState();
      page.waitForTimeout(2000);

      boolean clickSuccess = clickFirstProduct(page);
      if (!clickSuccess) {
        log.error("키워드 '{}': 첫 번째 상품 클릭 실패", searchKeyword);
        return false;
      }

      page.waitForLoadState();
      page.waitForTimeout(2000);

      String productTitle = extractProductPageTitle(page);

      if (productTitle == null || productTitle.trim().isEmpty()) {
        log.error("키워드 '{}': 상품 페이지에서 제목을 찾을 수 없음", searchKeyword);
        return false;
      }

      log.info("상품 페이지 제목 추출 완료: {}", productTitle);

      boolean isRelated = isKeywordRelatedToProduct(searchKeyword, originalKeyword, productTitle);

      if (isRelated) {
        log.info("키워드 '{}' 검증 성공 - 매칭된 상품: {}", searchKeyword, productTitle);
        return true;
      } else {
        log.warn("키워드 '{}' 검증 실패: 상품 제목과 연관성 부족 - 찾은 상품: {}", searchKeyword, productTitle);
        return false;
      }

    } catch (Exception e) {
      log.error("키워드 검증 중 오류 발생: keyword={}", searchKeyword, e);
      return false;
    }
  }

  public String validateWithAlternativeLanguage(Page page, String originalKeyword) {
    String alternativeKeyword = getAlternativeLanguage(originalKeyword);

    if (alternativeKeyword == null) {
      log.debug("키워드 '{}'의 대체 언어 매핑이 없습니다.", originalKeyword);
      return null;
    }

    log.info("대체 언어로 검증 시도: {} → {}", originalKeyword, alternativeKeyword);

    if (validateKeyword(page, alternativeKeyword, originalKeyword)) {
      return alternativeKeyword;
    }

    return null;
  }

  private String getAlternativeLanguage(String keyword) {
    if (containsKorean(keyword)) {
      return translationService.koreanToEnglish(keyword);
    } else if (isEnglishOnly(keyword)) {
      return translationService.englishToKorean(keyword);
    }
    return null;
  }

  private boolean containsKorean(String text) {
    return text.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣].*");
  }

  private boolean isEnglishOnly(String text) {
    return text.matches("^[a-zA-Z\\s]+$");
  }

  private boolean clickFirstProduct(Page page) {
    String[] productSelectors = {
        "div.product_item a[href*='view.php']",
        "div.product_item a",
        ".product_item a",
        ".product-item a",
        "a[href*='view.php']"
    };

    for (String selector : productSelectors) {
      try {
        Locator elements = page.locator(selector);
        if (elements.count() > 0) {
          log.debug("첫 번째 상품 클릭 시도: {}", selector);
          elements.first().click();
          return true;
        }
      } catch (Exception e) {
        log.debug("선택자 실패: {} - {}", selector, e.getMessage());
        continue;
      }
    }

    return false;
  }

  private String extractProductPageTitle(Page page) {
    String[] titleSelectors = {
        ".product_title",
        ".goods_name",
        ".item_name",
        ".product-title",
        ".goods-title",
        "h1",
        "h2",
        ".title"
    };

    for (String selector : titleSelectors) {
      try {
        Locator elements = page.locator(selector);
        if (elements.count() > 0) {
          String title = elements.first().textContent();
          if (title != null && !title.trim().isEmpty()) {
            log.debug("상품 제목 선택자 성공: {}", selector);
            return title.trim();
          }
        }
      } catch (Exception e) {
        continue;
      }
    }

    return null;
  }

  private boolean isKeywordRelatedToProduct(String searchKeyword, String originalKeyword, String productTitle) {
    log.debug("키워드 연관성 검증 시작");
    log.debug("검색 키워드: {}", searchKeyword);
    if (originalKeyword != null && !originalKeyword.equals(searchKeyword)) {
      log.debug("원본 키워드: {}", originalKeyword);
    }
    log.debug("상품명: {}", productTitle);

    Set<String> keywordsToValidate = new LinkedHashSet<>();
    keywordsToValidate.add(searchKeyword);
    if (originalKeyword != null && !originalKeyword.equals(searchKeyword)) {
      keywordsToValidate.add(originalKeyword);
    }

    for (String keyword : keywordsToValidate) {
      log.debug("검증 대상: '{}'", keyword);

      if (isRelevantBySimpleMatching(keyword, productTitle)) {
        log.info("키워드 '{}' 유사도 검증 성공", keyword);
        return true;
      }
      log.debug("키워드 '{}' 유사도 검증 실패", keyword);
    }

    log.warn("모든 키워드 검증 실패");
    return false;
  }

  private boolean isRelevantBySimpleMatching(String keyword, String productTitle) {
    if (keyword == null || productTitle == null) {
      return false;
    }

    String lowerKeyword = keyword.toLowerCase().trim();
    String lowerTitle = productTitle.toLowerCase().trim();

    // 직접 포함 검사
    if (lowerTitle.contains(lowerKeyword)) {
      log.debug("직접 매칭 성공: '{}' in '{}'", lowerKeyword, lowerTitle);
      return true;
    }

    // 키워드 길이별 세부 검증
    if (lowerKeyword.length() == 1) {
      return validateOneCharKeyword(lowerKeyword, lowerTitle);
    } else if (lowerKeyword.length() == 2) {
      return validateTwoCharKeyword(lowerKeyword, lowerTitle);
    } else {
      return validateLongKeyword(lowerKeyword, lowerTitle);
    }
  }

  private boolean validateOneCharKeyword(String keyword, String productTitle) {
    String[] titleWords = productTitle.split("[^가-힣a-zA-Z0-9]+");

    long count = Arrays.stream(titleWords)
        .filter(word -> word.equals(keyword))
        .count();

    log.debug("단어 '{}' 포함 횟수: {}회", keyword, count);
    return count >= 2;
  }

  private boolean validateTwoCharKeyword(String keyword, String productTitle) {
    String char1 = String.valueOf(keyword.charAt(0));
    String char2 = String.valueOf(keyword.charAt(1));

    String[] titleWords = productTitle.split("[^가-힣a-zA-Z0-9]+");

    int passedCharCount = 0;

    long count1 = Arrays.stream(titleWords)
        .filter(word -> word.equals(char1))
        .count();
    log.debug("글자 '{}' 포함 횟수: {}회", char1, count1);
    if (count1 >= 2) {
      passedCharCount++;
    }

    long count2 = Arrays.stream(titleWords)
        .filter(word -> word.equals(char2))
        .count();
    log.debug("글자 '{}' 포함 횟수: {}회", char2, count2);
    if (count2 >= 2) {
      passedCharCount++;
    }

    log.debug("2회 이상 포함된 글자 종류 수: {}개", passedCharCount);
    return passedCharCount >= 1;
  }

  private boolean validateLongKeyword(String keyword, String productTitle) {
    log.debug("3글자 이상 키워드 종합 검증 시작");

    // 부분 문자열 매칭 검사
    if (isRelevantBySubstringAnalysis(keyword, productTitle)) {
      log.debug("부분 문자열 분석 통과");
      return true;
    }

    // 개별 글자 빈도수 검증
    if (validateLongKeywordByCharFrequency(keyword, productTitle)) {
      log.debug("개별 글자 빈도수 검증 통과");
      return true;
    }

    log.debug("모든 단계 검증 실패");
    return false;
  }

  private boolean validateLongKeywordByCharFrequency(String keyword, String productTitle) {
    String[] titleWords = productTitle.split("[^가-힣a-zA-Z0-9]+");

    int passedCharCount = 0;
    int totalCharCount = keyword.length();

    for (int i = 0; i < keyword.length(); i++) {
      String singleChar = String.valueOf(keyword.charAt(i));
      long count = Arrays.stream(titleWords)
          .filter(word -> word.equals(singleChar))
          .count();

      log.debug("글자 '{}' 포함 횟수: {}회", singleChar, count);

      if (count >= 2) {
        passedCharCount++;
      }
    }

    log.debug("2회 이상 포함된 글자: {}/{}", passedCharCount, totalCharCount);

    double passRate = (double) passedCharCount / totalCharCount;
    boolean isPassed = passRate >= 0.3;

    log.debug("통과율: {:.1f}% (기준: 30% 이상) → {}", passRate * 100, isPassed ? "통과" : "실패");

    return isPassed;
  }

  private boolean isRelevantBySubstringAnalysis(String keyword, String productTitle) {
    log.debug("부분 문자열 조합 분석 시작...");

    List<String> substrings = new ArrayList<>();
    for (int i = 0; i < keyword.length(); i++) {
      for (int j = i + 1; j <= keyword.length(); j++) {
        substrings.add(keyword.substring(i, j));
      }
    }

    substrings.sort((s1, s2) -> s2.length() - s1.length());

    Set<String> matchedSubstrings = new HashSet<>();

    for (String sub : substrings) {
      if (sub.length() < 2 || sub.equals(keyword)) {
        continue;
      }

      log.debug("부분 문자열 '{}' 검증 중...", sub);
      if (productTitle.toLowerCase().contains(sub)) {
        boolean isRedundant = false;
        for (String matched : matchedSubstrings) {
          if (matched.contains(sub)) {
            isRedundant = true;
            break;
          }
        }

        if (!isRedundant) {
          log.debug("부분 문자열 매칭: {}", sub);
          matchedSubstrings.add(sub);
          log.info("부분 문자열 '{}'에서 연관성 발견!", sub);
          return true;
        }
      }
    }

    log.debug("모든 부분 문자열 조합에서 연관성 찾지 못함.");
    return false;
  }
}