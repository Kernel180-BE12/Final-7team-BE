package com.softlabs.aicontents.domain.testDomainService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softlabs.aicontents.domain.testDomain.TestDomainMapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KeywordService {

  @Autowired private TestDomainMapper testDomainMapper;

  @Value("${naver.shopping.popular.count:50}")
  private int defaultPopularCount;

  @Value(
      "${naver.shopping.popular.url:https://snxbest.naver.com/keyword/best?categoryId=A&sortType=KEYWORD_POPULAR&periodType=DAILY&ageType=ALL&activeRankId=1601977169&syncDate=20250922}")
  private String popularUrl;

  @Value(
      "${naver.shopping.popular.altUrl1:https://snxbest.naver.com/keyword/issue?categoryId=A&sortType=KEYWORD_ISSUE&periodType=WEEKLY&ageType=ALL&activeRankId=1600477074&syncDate=20250922}")
  private String altPopularUrl1;

  @Value(
      "${naver.shopping.popular.altUrl2:https://snxbest.naver.com/keyword/new?categoryId=A&sortType=KEYWORD_NEW&periodType=WEEKLY&ageType=ALL&activeRankId=1600401941&syncDate=20250922}")
  private String altPopularUrl2;

  @Value(
      "${naver.shopping.popular.altUrl3:https://snxbest.naver.com/product/best/click?categoryId=A&sortType=PRODUCT_CLICK&periodType=DAILY&ageType=ALL}")
  private String altPopularUrl3;

  @Value(
      "${naver.shopping.popular.altUrl4:https://snxbest.naver.com/product/best/buy?categoryId=A&sortType=PRODUCT_BUY&periodType=DAILY}")
  private String altPopularUrl4;

  @Value("${naver.shopping.popular.retries:3}")
  private int maxRetries;

  @Value("${naver.shopping.cookie:}")
  private String cookieHeader;

  @Value("${crawler.python.enabled:false}")
  private boolean pythonCrawlerEnabled;

  @Value("${crawler.python.command:python}")
  private String pythonCommand;

  @Value("${crawler.python.script:fastapi/app/keyword_crawler.py}")
  private String pythonScriptPath;

  @Value("${keyword.filter.productOnly:false}")
  private boolean productOnlyFilter;

  @Value("${keyword.filter.categoryId:}")
  private String targetCategoryId;

  private final ObjectMapper objectMapper = new ObjectMapper();

  // 상품 관련 키워드 패턴들
  private final Set<String> productCategories =
      Set.of(
          "휴대폰", "케이스", "지도", "텀블러", "가방", "신발", "옷", "화장품", "책", "노트북", "마우스", "키보드", "이어폰", "헤드폰",
          "충전기", "스마트워치", "태블릿", "카메라", "렌즈", "삼각대", "모니터", "의자", "책상", "침대", "소파", "냉장고", "세탁기",
          "에어컨", "청소기", "밥솥", "전자레인지", "커피머신", "블렌더", "토스터", "샴푸", "린스", "바디워시", "치약", "칫솔", "수건",
          "베개", "이불", "매트리스", "커튼", "조명", "선풍기", "히터", "가습기", "향수", "립스틱", "파운데이션", "마스카라", "아이섀도",
          "블러셔", "운동화", "구두", "샌들", "부츠", "양말", "속옷", "티셔츠", "바지", "원피스", "자켓", "코트", "모자", "장갑",
          "머플러", "벨트", "지갑", "시계", "반지", "목걸이", "귀걸이", "팔찌", "선글라스", "안경");

  // 브랜드나 모델명 패턴
  private final Set<String> brandKeywords =
      Set.of(
          "아이폰", "갤럭시", "삼성", "LG", "애플", "구글", "픽셀", "화웨이", "나이키", "아디다스", "뉴발란스", "컨버스", "반스",
          "퓨마", "루이비통", "구찌", "프라다", "샤넬", "에르메스", "디올", "스타벅스", "투썸", "이디야", "메가커피", "빽다방");

  /** 프로토타입용 키워드 수집 서비스 executionId를 받아서 샘플 키워드를 DB에 저장 */
  public void collectKeywordAndSave(int executionId) {
    try {
      log.info("키워드 수집 시작 - executionId: {}", executionId);

      String selectedKeyword = null;

      if (pythonCrawlerEnabled) {
        try {
          selectedKeyword = executePythonCrawler();
          log.info("파이썬 크롤러 키워드: {}", selectedKeyword);
        } catch (Exception pyEx) {
          log.warn("파이썬 크롤러 실패, 자바 크롤러로 대체: {}", pyEx.toString());
        }
      }

      if (selectedKeyword == null || selectedKeyword.isEmpty()) {
        List<String> popularKeywords = fetchPopularKeywordsFromNaver(defaultPopularCount);
        selectedKeyword = pickRandomKeyword(popularKeywords);

        // 수집된 키워드 50개 콘솔 출력
        log.info("=== 수집된 키워드 목록 (총 {}개) ===", popularKeywords.size());
        for (int i = 0; i < popularKeywords.size(); i++) {
          log.info("{}. {}", i + 1, popularKeywords.get(i));
        }
        log.info("=== 키워드 목록 출력 완료 ===");
      }

      saveKeywordResult(executionId, selectedKeyword, "SUCCESS");

      log.info("키워드 수집 완료 - executionId: {}, keyword: {}", executionId, selectedKeyword);

    } catch (Exception e) {
      log.error("키워드 수집 중 오류 발생 - executionId: {}", executionId, e);
      // 실패 시에도 DB에 기록
      saveKeywordResult(executionId, null, "FAILED");
      throw new RuntimeException("키워드 수집 실패", e);
    }
  }

  /** 외부 파이썬 크롤러 실행 후 JSON stdout을 파싱하여 키워드 1개를 반환 */
  private String executePythonCrawler() throws Exception {
    ProcessBuilder pb = new ProcessBuilder(pythonCommand, pythonScriptPath);
    pb.redirectErrorStream(true);
    Process p = pb.start();
    try (java.io.BufferedReader br =
        new java.io.BufferedReader(
            new java.io.InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
      int exit = p.waitFor();
      if (exit != 0) {
        throw new IllegalStateException("python exit=" + exit);
      }
      String out = sb.toString();
      JsonNode node = objectMapper.readTree(out);
      if (node == null || node.get("keyword") == null) {
        throw new IllegalStateException("python output invalid: " + out);
      }
      String kw = node.get("keyword").asText("").trim();
      if (kw.isEmpty()) {
        throw new IllegalStateException("python keyword empty");
      }
      return kw;
    }
  }

  /** 네이버 쇼핑 인기검색어 수집 (최대 limit개), 실패 시 예외 발생 */
  private List<String> fetchPopularKeywordsFromNaver(int limit) {
    try {
      // 1) 주 URL 시도 + 2) 보조 URL들 시도 (필요 시)
      List<String> urls = new ArrayList<>();
      urls.add(popularUrl);
      if (altPopularUrl1 != null && !altPopularUrl1.isEmpty()) urls.add(altPopularUrl1);
      if (altPopularUrl2 != null && !altPopularUrl2.isEmpty()) urls.add(altPopularUrl2);
      if (altPopularUrl3 != null && !altPopularUrl3.isEmpty()) urls.add(altPopularUrl3);
      if (altPopularUrl4 != null && !altPopularUrl4.isEmpty()) urls.add(altPopularUrl4);

      log.info("총 {}개의 크롤링 URL 시도: {}", urls.size(), urls);

      Set<String> dedupFinal = new HashSet<>();
      List<String> resultsFinal = new ArrayList<>();

      HttpClient client =
          HttpClient.newBuilder()
              .followRedirects(HttpClient.Redirect.NORMAL)
              .connectTimeout(Duration.ofSeconds(10))
              .build();

      // 각 URL에서 전체 페이지 스캔을 완료한 후 다음 URL로 이동
      for (String url : urls) {
        log.info("사이트 전체 페이지 스캔 시작: {}", url);

        List<String> siteKeywords =
            scanFullPageForKeywords(url, client, limit - resultsFinal.size());

        // 수집된 키워드를 결과에 추가
        for (String keyword : siteKeywords) {
          if (dedupFinal.add(keyword)) {
            resultsFinal.add(keyword);
            if (resultsFinal.size() >= limit) break;
          }
        }

        log.info("사이트에서 수집된 키워드 수: {} (누적: {})", siteKeywords.size(), resultsFinal.size());

        // 목표 개수에 도달했으면 다음 사이트로 이동하지 않음
        if (resultsFinal.size() >= limit) {
          log.info("목표 키워드 개수({})에 도달하여 수집 완료", limit);
          break;
        }
      }

      // 내부 탐색: 결과가 부족하면 네이버 쇼핑 내 관련 페이지를 소규모 탐색하여 보완
      if (resultsFinal.size() < Math.max(1, limit)) {
        try {
          List<String> expanded =
              crawlWithinShopping(urls.get(0), Math.max(1, limit) - resultsFinal.size(), client);
          for (String k : expanded) {
            if (k == null || k.isEmpty()) continue;
            if (dedupFinal.add(k)) {
              resultsFinal.add(k);
              if (resultsFinal.size() >= Math.max(1, limit)) break;
            }
          }
        } catch (Exception exp) {
          log.warn("내부 탐색 보완 실패: {}", exp.toString());
        }
      }

      // 필터링 전 키워드 수 확인
      log.info("필터링 전 수집된 총 키워드 수: {}", resultsFinal.size());
      log.info("필터링 전 키워드 목록: {}", resultsFinal);

      // 상품 관련 키워드만 필터링 (옵션)
      if (productOnlyFilter) {
        resultsFinal = filterProductKeywords(resultsFinal);
        log.info("상품 필터링 적용 후 키워드 수: {}", resultsFinal.size());
      }

      if (resultsFinal.isEmpty()) {
        throw new IllegalStateException("네이버 인기검색어 파싱 결과가 비어 있습니다");
      }

      return resultsFinal;

    } catch (Exception e) {
      log.error("네이버 쇼핑 인기검색어 수집 실패: {}", e.toString());
      throw new RuntimeException("실시간 크롤링 실패", e);
    }
  }

  /**
   * 전체 페이지 스캔으로 키워드를 수집하는 메서드 (스크롤 시뮬레이션 포함)
   *
   * @param url 크롤링할 URL
   * @param client HTTP 클라이언트
   * @param needed 필요한 키워드 개수
   * @return 수집된 키워드 리스트
   */
  private List<String> scanFullPageForKeywords(String url, HttpClient client, int needed) {
    List<String> keywords = new ArrayList<>();
    Set<String> dedup = new HashSet<>();

    try {
      // 1. 기본 페이지 로드 (초기 화면)
      String initialContent = loadPage(url, client);
      keywords.addAll(extractKeywordsFromContent(initialContent, dedup, needed));
      log.info("초기 페이지 로드 완료 - 수집 키워드: {}", keywords.size());

      if (keywords.size() >= needed) return keywords;

      // 2. 스크롤 시뮬레이션을 위한 추가 요청 (AJAX/API 호출 시뮬레이션)
      keywords.addAll(simulateScrollAndLoadMore(url, client, dedup, needed - keywords.size()));
      log.info("스크롤 시뮬레이션 완료 - 총 수집 키워드: {}", keywords.size());

      if (keywords.size() >= needed) return keywords;

      // 3. 페이지 내 링크 탐색으로 추가 키워드 수집
      keywords.addAll(
          explorePageLinks(url, client, initialContent, dedup, needed - keywords.size()));
      log.info("페이지 링크 탐색 완료 - 최종 수집 키워드: {}", keywords.size());

    } catch (Exception e) {
      log.warn("전체 페이지 스캔 중 오류 발생 - url: {}, error: {}", url, e.toString());
    }

    return keywords;
  }

  /** 페이지를 로드하고 HTML 콘텐츠를 반환 */
  private String loadPage(String url, HttpClient client) throws Exception {
    for (int attempt = 1; attempt <= maxRetries; attempt++) {
      try {
        HttpRequest.Builder rb =
            HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .header(
                    "Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,application/json;q=0.8,*/*;q=0.7")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Accept-Encoding", "gzip")
                .header("Cache-Control", "no-cache")
                .header("Pragma", "no-cache")
                .GET();

        if (cookieHeader != null && !cookieHeader.isEmpty()) {
          rb.header("Cookie", cookieHeader);
        }

        HttpRequest request = rb.build();
        HttpResponse<byte[]> response =
            client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() >= 400) {
          throw new IllegalStateException("HTTP status=" + response.statusCode());
        }

        String content = decodeBody(response);
        log.debug(
            "페이지 로드 완료 - url: {}, status: {}, size: {}KB",
            url,
            response.statusCode(),
            content.length() / 1024);

        return content;

      } catch (Exception ex) {
        log.warn(
            "페이지 로드 실패 - url: {}, attempt: {}/{}, error: {}",
            url,
            attempt,
            maxRetries,
            ex.toString());
        if (attempt < maxRetries) {
          try {
            Thread.sleep(500L * attempt);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
          }
        }
      }
    }
    throw new RuntimeException("페이지 로드 최종 실패: " + url);
  }

  /** 스크롤을 시뮬레이션하여 동적 콘텐츠 로드 */
  private List<String> simulateScrollAndLoadMore(
      String url, HttpClient client, Set<String> dedup, int needed) {
    List<String> keywords = new ArrayList<>();

    try {
      // 다양한 페이지네이션/스크롤 파라미터 시도
      List<String> scrollUrls = generateScrollUrls(url);

      for (String scrollUrl : scrollUrls) {
        if (keywords.size() >= needed) break;

        try {
          String content = loadPage(scrollUrl, client);
          List<String> newKeywords =
              extractKeywordsFromContent(content, dedup, needed - keywords.size());
          keywords.addAll(newKeywords);
          log.debug("스크롤 URL에서 키워드 추출: {} → {} 개", scrollUrl, newKeywords.size());

          // 동적 로딩 시뮬레이션을 위한 지연
          Thread.sleep(200);

        } catch (Exception ex) {
          log.debug("스크롤 URL 실패: {}, error: {}", scrollUrl, ex.toString());
        }
      }

    } catch (Exception e) {
      log.warn("스크롤 시뮬레이션 실패: {}", e.toString());
    }

    return keywords;
  }

  /** 스크롤/페이지네이션 URL 생성 */
  private List<String> generateScrollUrls(String baseUrl) {
    List<String> urls = new ArrayList<>();

    try {
      // 페이지 번호 추가 (2~5페이지)
      for (int page = 2; page <= 5; page++) {
        if (baseUrl.contains("?")) {
          urls.add(baseUrl + "&page=" + page);
          urls.add(baseUrl + "&offset=" + ((page - 1) * 20));
        } else {
          urls.add(baseUrl + "?page=" + page);
          urls.add(baseUrl + "?offset=" + ((page - 1) * 20));
        }
      }

      // 다양한 정렬/필터 옵션 추가
      String[] sortOptions = {"popular", "recent", "trending", "hot"};
      for (String sort : sortOptions) {
        if (baseUrl.contains("?")) {
          urls.add(baseUrl + "&sort=" + sort);
        } else {
          urls.add(baseUrl + "?sort=" + sort);
        }
      }

    } catch (Exception e) {
      log.debug("스크롤 URL 생성 실패: {}", e.toString());
    }

    return urls.subList(0, Math.min(urls.size(), 10)); // 최대 10개로 제한
  }

  /** 페이지 내 링크를 탐색하여 추가 키워드 수집 */
  private List<String> explorePageLinks(
      String baseUrl, HttpClient client, String content, Set<String> dedup, int needed) {
    List<String> keywords = new ArrayList<>();

    try {
      // 페이지 내 관련 링크 추출
      List<String> relatedLinks = extractRelatedLinks(baseUrl, content);

      for (String link : relatedLinks) {
        if (keywords.size() >= needed) break;

        try {
          String linkContent = loadPage(link, client);
          List<String> linkKeywords =
              extractKeywordsFromContent(linkContent, dedup, needed - keywords.size());
          keywords.addAll(linkKeywords);
          log.debug("관련 링크에서 키워드 추출: {} → {} 개", link, linkKeywords.size());

          Thread.sleep(100); // 요청 간 지연

        } catch (Exception ex) {
          log.debug("관련 링크 탐색 실패: {}, error: {}", link, ex.toString());
        }
      }

    } catch (Exception e) {
      log.warn("페이지 링크 탐색 실패: {}", e.toString());
    }

    return keywords;
  }

  /** HTML 콘텐츠에서 관련 링크 추출 */
  private List<String> extractRelatedLinks(String baseUrl, String content) {
    List<String> links = new ArrayList<>();

    try {
      // 같은 도메인의 관련 링크만 추출
      Pattern linkPattern = Pattern.compile("href=[\"'](.*?)[\"']");
      Matcher matcher = linkPattern.matcher(content);

      while (matcher.find() && links.size() < 5) { // 최대 5개 링크
        String href = matcher.group(1);
        if (href == null || href.isEmpty()) continue;

        String absoluteUrl = toAbsoluteUrl(baseUrl, href);
        if (absoluteUrl != null
            && absoluteUrl.contains("snxbest.naver.com")
            && (href.contains("keyword") || href.contains("best") || href.contains("popular"))) {
          links.add(absoluteUrl);
        }
      }

    } catch (Exception e) {
      log.debug("관련 링크 추출 실패: {}", e.toString());
    }

    return links;
  }

  /** 상대 URL을 절대 URL로 변환 */
  private String toAbsoluteUrl(String base, String href) {
    try {
      if (href.startsWith("http")) return href;
      if (href.startsWith("//")) return "https:" + href;
      if (href.startsWith("/")) {
        URI baseUri = URI.create(base);
        return baseUri.getScheme() + "://" + baseUri.getHost() + href;
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }

  /** HTML 콘텐츠에서 키워드 추출 (기존 로직 활용) */
  private List<String> extractKeywordsFromContent(String content, Set<String> dedup, int needed) {
    List<String> keywords = new ArrayList<>();

    try {
      // 1. JSON 키워드 추출
      Pattern jsonKeywordPattern =
          Pattern.compile("\"(keyword|rankKeyword)\"\\s*:\\s*\"(.*?)\"", Pattern.CASE_INSENSITIVE);
      Matcher matcher = jsonKeywordPattern.matcher(content);
      while (matcher.find() && keywords.size() < needed) {
        String keyword = matcher.group(2).trim();
        if (isValidKeyword(keyword) && dedup.add(keyword)) {
          keywords.add(keyword);
        }
      }

      // 2. HTML 구조에서 키워드 추출 (기존 로직 활용)
      if (keywords.size() < needed) {
        int idx1 = indexOfAny(content, new String[] {"> 1 <", ">1<", "> 1<", "rank", "순위", "키워드"});
        int start = (idx1 >= 0) ? Math.max(0, idx1 - 1000) : 0;
        int end = Math.min(content.length(), start + 50000); // 범위를 더 넓게
        String section = content.substring(start, end);

        List<Pattern> patterns =
            List.of(
                Pattern.compile(
                    "(?:(?:>\\s*|\\n|\\r)([1-9][0-9]{0,2})(?:\\s*<[^>]*>){0,6}\\s*)"
                        + "([가-힣a-zA-Z0-9+#&()\\-\\s]{2,40})\\s*(?=<)"),
                Pattern.compile(">\\s*([가-힣a-zA-Z0-9+#&()\\-\\s]{2,20})\\s*<"),
                Pattern.compile("data-[^=]*=\"([가-힣a-zA-Z0-9+#&()\\-\\s]{2,20})\""),
                Pattern.compile("(?:alt|title)=\"([가-힣a-zA-Z0-9+#&()\\-\\s]{2,20})\""));

        for (Pattern pattern : patterns) {
          if (keywords.size() >= needed) break;

          Matcher patternMatcher = pattern.matcher(section);
          while (patternMatcher.find() && keywords.size() < needed) {
            String label = patternMatcher.group(patternMatcher.groupCount()).trim();
            if (isValidKeyword(label) && dedup.add(label)) {
              keywords.add(label);
            }
          }
        }
      }

    } catch (Exception e) {
      log.warn("키워드 추출 실패: {}", e.toString());
    }

    return keywords;
  }

  /** 키워드 유효성 검사 (기존 필터 로직 통합) */
  private boolean isValidKeyword(String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) return false;
    if (keyword.length() < 2) return false;
    if (keyword.matches("^\\d+$")) return false; // 숫자만
    if (isUIOrTechnicalKeyword(keyword)) return false; // UI/기술 키워드
    if (keyword.contains("<") || keyword.contains(">") || keyword.contains("&"))
      return false; // HTML 태그

    return true;
  }

  private String decodeBody(HttpResponse<byte[]> response) throws Exception {
    String contentEncoding = response.headers().firstValue("Content-Encoding").orElse("");
    if (contentEncoding != null && contentEncoding.toLowerCase().contains("gzip")) {
      try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(response.body()));
          InputStreamReader isr = new InputStreamReader(gis, StandardCharsets.UTF_8);
          BufferedReader br = new BufferedReader(isr)) {
        return br.lines().collect(Collectors.joining("\n"));
      }
    }
    return new String(response.body(), StandardCharsets.UTF_8);
  }

  // 네이버 쇼핑 내 링크를 한정적으로 따라가 추가 키워드 수집 (최대 depth 2, 최대 6페이지)
  private List<String> crawlWithinShopping(String seedUrl, int needed, HttpClient client)
      throws Exception {
    List<String> collected = new ArrayList<>();
    if (needed <= 0) return collected;

    Set<String> visited = new HashSet<>();
    List<String> queue = new ArrayList<>();
    queue.add(seedUrl);
    int pagesVisited = 0;
    int maxPages = 6;
    int depth = 0;

    while (!queue.isEmpty() && pagesVisited < maxPages && collected.size() < needed && depth <= 2) {
      String current = queue.remove(0);
      if (current == null || visited.contains(current)) continue;
      visited.add(current);
      pagesVisited++;

      try {
        HttpRequest.Builder rb =
            HttpRequest.newBuilder(URI.create(current))
                .timeout(Duration.ofSeconds(12))
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
                        + " AppleWebKit/537.36 (KHTML, like Gecko)"
                        + " Chrome/122.0.0.0 Safari/537.36")
                .header(
                    "Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,application/json;q=0.8,*/*;q=0.7")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Referer", "https://snxbest.naver.com")
                .header("Accept-Encoding", "gzip")
                .GET();
        if (cookieHeader != null && !cookieHeader.isEmpty()) {
          rb.header("Cookie", cookieHeader);
        }
        HttpResponse<byte[]> resp =
            client.send(rb.build(), HttpResponse.BodyHandlers.ofByteArray());
        String body = decodeBody(resp);

        // 키워드 추출: productTitle, title-like, query 파라미터(q|query)
        Set<String> dedupLocal = new HashSet<>();
        extractInto(
            body,
            Pattern.compile("\"productTitle\"\\s*:\\s*\"(.*?)\""),
            1,
            collected,
            dedupLocal,
            needed);
        extractInto(
            body, Pattern.compile("\"title\"\\s*:\\s*\"(.*?)\""), 1, collected, dedupLocal, needed);

        // href 내 쿼리 키워드 수집
        Matcher hrefMatcher = Pattern.compile("href=\\\"(.*?)\\\"").matcher(body);
        List<String> nextLinks = new ArrayList<>();
        while (hrefMatcher.find()) {
          String href = hrefMatcher.group(1);
          if (href == null) continue;
          String abs = toAbsoluteShoppingUrl(seedUrl, href);
          if (abs == null) continue;
          if (!abs.contains("search.shopping.naver.com")) continue;

          // 쿼리 파라미터에서 query 추출
          Matcher qm = Pattern.compile("[?&](q|query)=([^&]+)").matcher(abs);
          if (qm.find()) {
            String qv = urlDecode(qm.group(2));
            if (qv != null && qv.length() >= 2 && collected.size() < needed) {
              if (dedupLocal.add(qv)) collected.add(qv);
            }
          }

          // 다음 링크 후보 제한적으로 수집
          if ((href.contains("/best")
                  || href.contains("/all")
                  || href.contains("/popular")
                  || href.contains("/catalog"))
              && nextLinks.size() < 10) {
            nextLinks.add(abs);
          }
          if (collected.size() >= needed) break;
        }

        // 다음 depth로 큐에 추가
        if (depth < 2) {
          queue.addAll(nextLinks);
        }
        depth++;

      } catch (Exception ex) {
        log.warn("내부 페이지 수집 실패 url={} cause={}", current, ex.toString());
      }
    }

    return collected;
  }

  private void extractInto(
      String body, Pattern pattern, int groupIdx, List<String> out, Set<String> dedup, int needed) {
    Matcher m = pattern.matcher(body);
    while (m.find() && out.size() < needed) {
      String s = m.group(groupIdx);
      if (s == null) continue;
      s = s.trim();
      if (s.isEmpty()) continue;
      // 숫자만 있는 키워드 제외 (내부 탐색에서도)
      if (s.matches("^\\d+$")) {
        log.debug("내부탐색에서 숫자 키워드 제외: {}", s);
        continue;
      }
      // UI/기술 키워드 제외 (내부 탐색에서도)
      if (isUIOrTechnicalKeyword(s)) {
        log.debug("내부탐색에서 UI/기술 키워드 제외: {}", s);
        continue;
      }
      if (dedup.add(s)) out.add(s);
    }
  }

  private String toAbsoluteShoppingUrl(String base, String href) {
    try {
      if (href.startsWith("http")) return href;
      if (href.startsWith("//")) return "https:" + href;
      if (href.startsWith("/")) return "https://search.shopping.naver.com" + href;
    } catch (Exception ignored) {
    }
    return null;
  }

  private String urlDecode(String s) {
    try {
      return java.net.URLDecoder.decode(s, java.nio.charset.StandardCharsets.UTF_8);
    } catch (Exception e) {
      return s;
    }
  }

  private String pickRandomKeyword(List<String> candidates) {
    if (candidates == null || candidates.isEmpty()) return "";
    Random random = new Random();
    int index = random.nextInt(candidates.size());
    return candidates.get(index);
  }

  // 유틸: 본문에서 여러 패턴 중 첫 번째로 발견되는 인덱스
  private int indexOfAny(String text, String[] needles) {
    int min = -1;
    for (String n : needles) {
      int idx = text.indexOf(n);
      if (idx >= 0) {
        if (min == -1 || idx < min) min = idx;
      }
    }
    return min;
  }

  /**
   * 상품 관련 키워드만 필터링하는 함수
   *
   * @param allKeywords 전체 키워드 리스트
   * @return 상품 관련 키워드만 포함하는 리스트
   */
  private List<String> filterProductKeywords(List<String> allKeywords) {
    List<String> productKeywords = new ArrayList<>();

    for (String keyword : allKeywords) {
      if (isProductRelated(keyword)) {
        productKeywords.add(keyword);
        log.debug("상품 키워드 인식: {}", keyword);
      } else {
        log.debug("상품 키워드 제외: {}", keyword);
      }
    }

    return productKeywords;
  }

  /**
   * UI/기술 키워드인지 판단하는 함수 (네이버 쇼핑 사이트 내부 키워드 제외)
   *
   * @param keyword 검사할 키워드
   * @return UI/기술 키워드 여부
   */
  private boolean isUIOrTechnicalKeyword(String keyword) {
    String cleanKeyword = keyword.toLowerCase().trim();

    // 1. 네이버 쇼핑 UI 키워드들
    Set<String> uiKeywords =
        Set.of(
            "베스트홈",
            "베스트브랜드",
            "베스트키워드",
            "베스트상품",
            "선택됨",
            "인기키워드",
            "이슈키워드",
            "신규키워드",
            "쇼핑",
            "best",
            "내 또래를 위한",
            "레이어 열기",
            "best keyword",
            "전체",
            "패션의류",
            "패션잡화",
            "식품",
            "일간",
            "주간",
            "월간",
            "랭킹 유지",
            "접기",
            "원가",
            "할인율",
            "배송비",
            "별점",
            "더보기",
            "상세보기",
            "리뷰",
            "인기",
            "랭킹",
            "검색",
            "클릭",
            "조회",
            "이동",
            "바로가기",
            "자세히",
            "상세",
            "보기");

    // 2. 기술적/프로그래밍 키워드들
    Set<String> technicalKeywords =
        Set.of(
            "navi",
            "shopping",
            "bkeypop",
            "slot",
            "demo",
            "filter",
            "cate",
            "period",
            "daily",
            "weekly",
            "monthly",
            "rank",
            "pd",
            "prod",
            "adinfo",
            "data",
            "api",
            "url",
            "http",
            "html",
            "css",
            "js",
            "json",
            "xml",
            "div",
            "span",
            "class",
            "style",
            "script",
            "link",
            "meta",
            "title",
            "header",
            "footer",
            "nav",
            "section",
            "article");

    // 3. 네이버 특화 키워드들
    Set<String> naverSpecificKeywords =
        Set.of(
            "네이버", "naver", "snx", "snxbest", "쇼핑몰", "쇼핑하우", "스마트스토어", "브랜드스토어", "카테고리", "상품상세",
            "상품정보", "상품후기", "구매후기", "사용후기");

    // 키워드 매칭 검사
    for (String ui : uiKeywords) {
      if (cleanKeyword.equals(ui) || cleanKeyword.contains(ui)) {
        return true;
      }
    }

    for (String tech : technicalKeywords) {
      if (cleanKeyword.equals(tech) || cleanKeyword.contains(tech)) {
        return true;
      }
    }

    for (String naver : naverSpecificKeywords) {
      if (cleanKeyword.equals(naver) || cleanKeyword.contains(naver)) {
        return true;
      }
    }

    return false;
  }

  /**
   * 키워드가 상품 관련인지 판단하는 함수
   *
   * @param keyword 검사할 키워드
   * @return 상품 관련 여부
   */
  private boolean isProductRelated(String keyword) {
    String cleanKeyword = keyword.toLowerCase().trim();

    // 0. 숫자만 있는 키워드 제외
    if (cleanKeyword.matches("^\\d+$")) {
      log.debug("숫자만 있는 키워드 제외: {}", keyword);
      return false;
    }

    // 1. 명확한 비상품 키워드만 제외 (기준 완화)
    Set<String> excludeKeywords = Set.of("뉴스", "정치", "경제", "연예", "주식", "금융");

    for (String exclude : excludeKeywords) {
      if (cleanKeyword.contains(exclude)) {
        log.debug("제외 키워드 매칭으로 필터링: {} (매칭된 단어: {})", keyword, exclude);
        return false;
      }
    }

    // 1. 길이 조건 - 2글자 이상이면 기본적으로 허용
    if (cleanKeyword.length() >= 2) {
      log.debug("기본 키워드로 인식: {}", keyword);
      return true;
    }

    log.debug("상품 관련성 없음: {}", keyword);
    return false;
  }

  /** 상품명 패턴을 포함하는지 검사 */
  private boolean containsProductPattern(String keyword) {
    // 상품 관련 접미사/접두사
    String[] productSuffixes = {"케이스", "커버", "스탠드", "거치대", "악세서리", "용품", "세트", "키트"};
    String[] productPrefixes = {"무선", "블루투스", "스마트", "디지털", "전자", "휴대용", "미니"};

    for (String suffix : productSuffixes) {
      if (keyword.contains(suffix)) return true;
    }

    for (String prefix : productPrefixes) {
      if (keyword.contains(prefix)) return true;
    }

    return false;
  }

  /**
   * 특정 카테고리 영역의 키워드만 추출 (향후 확장용)
   *
   * @param allKeywords 전체 키워드 리스트
   * @param categoryFilter 카테고리 필터 (예: "전자제품", "패션", "화장품")
   * @return 해당 카테고리의 키워드만 포함하는 리스트
   */
  private List<String> filterByCategory(List<String> allKeywords, String categoryFilter) {
    if (categoryFilter == null || categoryFilter.isEmpty()) {
      return allKeywords;
    }

    List<String> filteredKeywords = new ArrayList<>();

    // 카테고리별 키워드 매핑
    Map<String, Set<String>> categoryMap =
        Map.of(
            "전자제품", Set.of("휴대폰", "노트북", "태블릿", "이어폰", "충전기", "모니터", "키보드", "마우스"),
            "패션", Set.of("옷", "신발", "가방", "시계", "모자", "벨트", "선글라스", "운동화"),
            "화장품", Set.of("립스틱", "파운데이션", "마스카라", "향수", "샴푸", "린스", "바디워시"),
            "생활용품", Set.of("텀블러", "베개", "이불", "수건", "조명", "청소기", "세탁기", "냉장고"));

    Set<String> targetCategories = categoryMap.getOrDefault(categoryFilter, Set.of());

    for (String keyword : allKeywords) {
      for (String category : targetCategories) {
        if (keyword.toLowerCase().contains(category.toLowerCase())) {
          filteredKeywords.add(keyword);
          break;
        }
      }
    }

    return filteredKeywords;
  }

  /** 키워드 결과를 DB에 저장 */
  private void saveKeywordResult(int executionId, String keyword, String statusCode) {
    try {
      String safeKeyword = (keyword == null) ? "" : keyword;

      // 키워드에서 띄어쓰기 제거
      if (!safeKeyword.isEmpty()) {
        safeKeyword = safeKeyword.replaceAll("\\s+", ""); // 모든 공백 제거
        log.info("키워드 공백 제거: {} -> {}", keyword, safeKeyword);
      }

      testDomainMapper.insertKeywordData(executionId, safeKeyword, statusCode);
      String safeKeyword = (keyword == null) ? "" : keyword;
      testDomainMapper.insertKeywordData(executionId, safeKeyword, statusCode);
      log.debug(
          "키워드 데이터 저장 완료 - executionId: {}, keyword: {}, status: {}",
          executionId,
          safeKeyword,
          statusCode);
    } catch (Exception e) {
      log.error("키워드 데이터 저장 실패 - executionId: {}", executionId, e);
      throw e;
    }
  }
}
