package com.softlabs.aicontents.domain.datacollector.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softlabs.aicontents.common.util.TraceIdUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaverStoreApiService {

  private final CloseableHttpClient httpClient;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public List<String> getBestKeywords() {
    try {
      log.info("네이버 쇼핑 베스트 API에서 1-20위 키워드 수집 중...");

      List<String> keywords = fetchKeywordsFromAPI();

      if (keywords.isEmpty()) {
        log.error("API에서 키워드 추출 실패");
        return new ArrayList<>();
      }

      log.info("API에서 {}개 키워드 추출 성공", keywords.size());
      return keywords;

    } catch (Exception e) {
      log.error("[네이버 쇼핑 베스트 API 호출 실패] 예외 발생", e);
      return new ArrayList<>();
    }
  }

  private List<String> fetchKeywordsFromAPI() {
    try {
      String apiUrl = "https://snxbest.naver.com/api/v1/snxbest/keyword/rank"
          + "?ageType=ALL"
          + "&categoryId=A"
          + "&sortType=KEYWORD_POPULAR"
          + "&periodType=DAILY";

      HttpGet httpGet = new HttpGet(apiUrl);

      httpGet.setHeader("User-Agent",
          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
      httpGet.setHeader("Referer", "https://snxbest.naver.com/keyword/best");
      httpGet.setHeader("Accept", "*/*");
      httpGet.setHeader("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
      httpGet.setHeader("Accept-Encoding", "gzip, deflate, br, zstd");

      log.debug("API 요청 URL: {}", apiUrl);

      try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 200) {
          String responseBody = EntityUtils.toString(response.getEntity());
          log.info("API 응답 성공 (200 OK)");

          return parseKeywordsFromResponse(responseBody);
        } else {
          log.error("API 요청 실패 - HTTP 상태 코드: {}", statusCode);
          return new ArrayList<>();
        }
      }

    } catch (Exception e) {
      log.error("API 요청 중 오류 발생", e);
      return new ArrayList<>();
    }
  }

  private List<String> parseKeywordsFromResponse(String responseBody) {
    List<String> keywords = new ArrayList<>();

    try {
      JsonNode rootNode = objectMapper.readTree(responseBody);

      if (rootNode.isArray()) {
        log.debug("JSON 배열에서 키워드 추출 시작...");

        for (JsonNode keywordNode : rootNode) {
          JsonNode titleNode = keywordNode.get("title");
          JsonNode rankNode = keywordNode.get("rank");

          if (titleNode != null && !titleNode.isNull()) {
            String keyword = titleNode.asText().trim();
            int rank = rankNode != null ? rankNode.asInt() : keywords.size() + 1;

            keywords.add(keyword);
            log.debug("{}위: {}", rank, keyword);
          }
        }

        log.info("총 {}개 키워드 추출 완료", keywords.size());
      } else {
        log.error("JSON 응답이 배열 형식이 아닙니다");
      }

    } catch (Exception e) {
      log.error("JSON 파싱 중 오류 발생", e);
    }

    return keywords;
  }

  public boolean isValidKeyword(String keyword) {
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
}