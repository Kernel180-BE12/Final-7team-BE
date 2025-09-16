package com.softlabs.aicontents.domain.datacollector.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softlabs.aicontents.common.util.TraceIdUtil;
import com.softlabs.aicontents.domain.datacollector.config.DataCollectorConfig;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PapagoTranslationService {

  private final DataCollectorConfig config;
  private final CloseableHttpClient httpClient;

  private static final String API_URL = "https://papago.apigw.ntruss.com/nmt/v1/translation";
  private static final Map<String, String> translationCache = new HashMap<>();
  private static final int MAX_CACHE_SIZE = 1000;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public String koreanToEnglish(String koreanText) {
    return translate(koreanText, "ko", "en");
  }

  public String englishToKorean(String englishText) {
    return translate(englishText, "en", "ko");
  }

  public String translate(String text, String sourceLang, String targetLang) {
    if (text == null || text.trim().isEmpty()) {
      return null;
    }

    text = text.trim();

    String cacheKey = sourceLang + ":" + targetLang + ":" + text;
    if (translationCache.containsKey(cacheKey)) {
      log.debug("캐시에서 번역 결과 반환: {}", text);
      return translationCache.get(cacheKey);
    }

    try {
      HttpPost httpPost = new HttpPost(API_URL);

      httpPost.setHeader("x-ncp-apigw-api-key-id", config.getPapagoClientId());
      httpPost.setHeader("x-ncp-apigw-api-key", config.getPapagoClientSecret());
      httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

      String requestBody =
          String.format(
              "source=%s&target=%s&text=%s",
              sourceLang, targetLang, URLEncoder.encode(text, StandardCharsets.UTF_8));

      httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

      try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
        String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

        if (response.getStatusLine().getStatusCode() == 200) {
          JsonNode jsonResponse = objectMapper.readTree(responseBody);
          String translatedText =
              jsonResponse.path("message").path("result").path("translatedText").asText();

          if (!translatedText.isEmpty()) {
            addToCache(cacheKey, translatedText);
            log.info("파파고 번역 성공: {} → {}", text, translatedText);
            return translatedText;
          }
        } else {
          log.error("파파고 API 호출 실패: {}, 응답: {}", response.getStatusLine().getStatusCode(), responseBody);
        }
      }

    } catch (Exception e) {
      log.error("파파고 API 호출 중 오류 발생: {}, 더미 번역으로 대체", e.getMessage());
      return getDummyTranslation(text, sourceLang, targetLang);
    }

    return null;
  }

  public String autoTranslate(String text) {
    if (text == null || text.trim().isEmpty()) {
      return null;
    }

    if (containsKorean(text)) {
      return koreanToEnglish(text);
    } else if (isEnglishOnly(text)) {
      return englishToKorean(text);
    }

    return null;
  }

  private void addToCache(String key, String value) {
    if (translationCache.size() >= MAX_CACHE_SIZE) {
      String firstKey = translationCache.keySet().iterator().next();
      translationCache.remove(firstKey);
    }
    translationCache.put(key, value);
  }

  private String getDummyTranslation(String text, String sourceLang, String targetLang) {
    Map<String, String> dummyMappings = new HashMap<>();

    if ("ko".equals(sourceLang) && "en".equals(targetLang)) {
      dummyMappings.put("애플워치", "Apple Watch");
      dummyMappings.put("베드민턴", "Badminton");
      dummyMappings.put("대마종자유", "Hemp Seed Oil");
      dummyMappings.put("이어플러그", "Earplug");
    } else if ("en".equals(sourceLang) && "ko".equals(targetLang)) {
      dummyMappings.put("Apple Watch", "애플워치");
      dummyMappings.put("Badminton", "베드민턴");
      dummyMappings.put("Hemp Seed Oil", "대마종자유");
      dummyMappings.put("Earplug", "이어플러그");
    }

    return dummyMappings.get(text);
  }

  private boolean containsKorean(String text) {
    return text.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣].*");
  }

  private boolean isEnglishOnly(String text) {
    return text.matches("^[a-zA-Z\\s]+$");
  }

  public int getCacheSize() {
    return translationCache.size();
  }

  public void clearCache() {
    translationCache.clear();
    log.info("번역 캐시 초기화 완료");
  }
}