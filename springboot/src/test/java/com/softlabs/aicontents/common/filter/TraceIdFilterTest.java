package com.softlabs.aicontents.common.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.softlabs.aicontents.common.util.TraceIdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;

class TraceIdFilterTest {

  private TraceIdFilter filter;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private FilterChain filterChain;

  @BeforeEach
  void setUp() {
    filter = new TraceIdFilter();
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    filterChain = mock(FilterChain.class);

    // 기본 모킹 설정
    when(request.getRequestURI()).thenReturn("/api/test");
    when(request.getMethod()).thenReturn("GET");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
  }

  @AfterEach
  void tearDown() {
    TraceIdUtil.clearAllMdc();
  }

  @Test
  @DisplayName("요청 헤더에 TraceId가 없을 때 새로 생성하고 응답 헤더에 설정")
  void doFilter_noTraceIdInHeader_generatesNew() throws ServletException, IOException {
    when(request.getHeader("X-Trace-Id")).thenReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    ArgumentCaptor<String> traceIdCaptor = ArgumentCaptor.forClass(String.class);
    verify(response).setHeader(eq("X-Trace-Id"), traceIdCaptor.capture());

    String capturedTraceId = traceIdCaptor.getValue();
    assertThat(capturedTraceId).isNotNull();
    assertThat(capturedTraceId).hasSize(16);
    assertThat(capturedTraceId).matches("[a-f0-9]{16}");

    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("요청 헤더에 TraceId가 있을 때 기존 값을 사용하고 응답 헤더에 설정")
  void doFilter_existingTraceIdInHeader_usesExisting() throws ServletException, IOException {
    String existingTraceId = "existing12345678";
    when(request.getHeader("X-Trace-Id")).thenReturn(existingTraceId);

    filter.doFilterInternal(request, response, filterChain);

    verify(response).setHeader(eq("X-Trace-Id"), eq(existingTraceId));
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("요청 헤더에 빈 문자열 TraceId가 있을 때 새로 생성")
  void doFilter_emptyTraceIdInHeader_generatesNew() throws ServletException, IOException {
    when(request.getHeader("X-Trace-Id")).thenReturn("");

    filter.doFilterInternal(request, response, filterChain);

    ArgumentCaptor<String> traceIdCaptor = ArgumentCaptor.forClass(String.class);
    verify(response).setHeader(eq("X-Trace-Id"), traceIdCaptor.capture());

    String capturedTraceId = traceIdCaptor.getValue();
    assertThat(capturedTraceId).isNotNull();
    assertThat(capturedTraceId).hasSize(16);
    assertThat(capturedTraceId).matches("[a-f0-9]{16}");

    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("요청 헤더에 공백만 있는 TraceId가 있을 때 새로 생성")
  void doFilter_whitespaceTraceIdInHeader_generatesNew() throws ServletException, IOException {
    when(request.getHeader("X-Trace-Id")).thenReturn("   ");

    filter.doFilterInternal(request, response, filterChain);

    ArgumentCaptor<String> traceIdCaptor = ArgumentCaptor.forClass(String.class);
    verify(response).setHeader(eq("X-Trace-Id"), traceIdCaptor.capture());

    String capturedTraceId = traceIdCaptor.getValue();
    assertThat(capturedTraceId).isNotNull();
    assertThat(capturedTraceId).hasSize(16);
    assertThat(capturedTraceId).matches("[a-f0-9]{16}");

    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("필터 처리 후 MDC에서 TraceId가 정리됨")
  void doFilter_clearsTraceIdAfterProcessing() throws ServletException, IOException {
    when(request.getHeader("X-Trace-Id")).thenReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    assertThat(TraceIdUtil.getTraceId()).isNull();
    assertThat(TraceIdUtil.hasTraceId()).isFalse();
  }

  @Test
  @DisplayName("필터 체인에서 예외 발생 시에도 TraceId가 정리됨")
  void doFilter_clearsTraceIdEvenOnException() throws ServletException, IOException {
    when(request.getHeader("X-Trace-Id")).thenReturn(null);
    doThrow(new RuntimeException("Test exception")).when(filterChain).doFilter(request, response);

    try {
      filter.doFilterInternal(request, response, filterChain);
    } catch (RuntimeException e) {
      // 예외는 예상됨
    }

    assertThat(TraceIdUtil.getTraceId()).isNull();
    assertThat(TraceIdUtil.hasTraceId()).isFalse();
  }

  @Test
  @DisplayName("제외 경로 패턴 테스트 - shouldNotFilter")
  void shouldNotFilter_excludesConfiguredPaths() throws ServletException {
    // /actuator/health 제외
    when(request.getRequestURI()).thenReturn("/actuator/health");
    assertThat(filter.shouldNotFilter(request)).isTrue();

    // 정적 리소스 제외
    when(request.getRequestURI()).thenReturn("/static/css/main.css");
    assertThat(filter.shouldNotFilter(request)).isTrue();

    when(request.getRequestURI()).thenReturn("/css/style.css");
    assertThat(filter.shouldNotFilter(request)).isTrue();

    when(request.getRequestURI()).thenReturn("/js/app.js");
    assertThat(filter.shouldNotFilter(request)).isTrue();

    when(request.getRequestURI()).thenReturn("/images/logo.png");
    assertThat(filter.shouldNotFilter(request)).isTrue();

    when(request.getRequestURI()).thenReturn("/favicon.ico");
    assertThat(filter.shouldNotFilter(request)).isTrue();

    // API 경로는 포함
    when(request.getRequestURI()).thenReturn("/api/users");
    assertThat(filter.shouldNotFilter(request)).isFalse();
  }

  @Test
  @DisplayName("X-Forwarded-For 헤더로 클라이언트 IP 추출")
  void doFilter_extractsClientIpFromXForwardedFor() throws ServletException, IOException {
    when(request.getHeader("X-Trace-Id")).thenReturn(null);
    when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100, 10.0.0.1");

    doAnswer(
            invocation -> {
              // 필터 체인 실행 중에 MDC 값 검증
              assertThat(MDC.get("clientIp")).isEqualTo("192.168.1.100");
              return null;
            })
        .when(filterChain)
        .doFilter(request, response);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("X-Real-IP 헤더로 클라이언트 IP 추출")
  void doFilter_extractsClientIpFromXRealIp() throws ServletException, IOException {
    when(request.getHeader("X-Trace-Id")).thenReturn(null);
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);
    when(request.getHeader("X-Real-IP")).thenReturn("203.0.113.10");

    doAnswer(
            invocation -> {
              // 필터 체인 실행 중에 MDC 값 검증
              assertThat(MDC.get("clientIp")).isEqualTo("203.0.113.10");
              return null;
            })
        .when(filterChain)
        .doFilter(request, response);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("MDC에 요청 정보가 올바르게 설정됨")
  void doFilter_setsMdcRequestInfo() throws ServletException, IOException {
    when(request.getHeader("X-Trace-Id")).thenReturn(null);
    when(request.getRequestURI()).thenReturn("/api/users");
    when(request.getMethod()).thenReturn("POST");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");

    doAnswer(
            invocation -> {
              // 필터 체인 실행 중에 MDC 값 검증
              assertThat(MDC.get("requestUri")).isEqualTo("/api/users");
              assertThat(MDC.get("httpMethod")).isEqualTo("POST");
              assertThat(MDC.get("clientIp")).isEqualTo("127.0.0.1");
              return null;
            })
        .when(filterChain)
        .doFilter(request, response);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("필터 처리 후 모든 MDC 값이 정리됨")
  void doFilter_clearsAllMdcAfterProcessing() throws ServletException, IOException {
    when(request.getHeader("X-Trace-Id")).thenReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    assertThat(MDC.get("traceId")).isNull();
    assertThat(MDC.get("requestUri")).isNull();
    assertThat(MDC.get("httpMethod")).isNull();
    assertThat(MDC.get("clientIp")).isNull();
  }
}
