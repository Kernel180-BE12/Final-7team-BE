package com.softlabs.aicontents.common.filter;

import static org.assertj.core.api.Assertions.assertThat;
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
  }

  @AfterEach
  void tearDown() {
    TraceIdUtil.clearAllMdc();
  }

  @Test
  @DisplayName("요청 헤더에 TraceId가 없을 때 새로 생성하고 응답 헤더에 설정")
  void doFilter_noTraceIdInHeader_generatesNew() throws ServletException, IOException {
    when(request.getHeader("X-Trace-Id")).thenReturn(null);

    filter.doFilter(request, response, filterChain);

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

    filter.doFilter(request, response, filterChain);

    verify(response).setHeader(eq("X-Trace-Id"), eq(existingTraceId));
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("요청 헤더에 빈 문자열 TraceId가 있을 때 새로 생성")
  void doFilter_emptyTraceIdInHeader_generatesNew() throws ServletException, IOException {
    when(request.getHeader("X-Trace-Id")).thenReturn("");

    filter.doFilter(request, response, filterChain);

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

    filter.doFilter(request, response, filterChain);

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

    filter.doFilter(request, response, filterChain);

    assertThat(TraceIdUtil.getTraceId()).isNull();
    assertThat(TraceIdUtil.hasTraceId()).isFalse();
  }

  @Test
  @DisplayName("필터 체인에서 예외 발생 시에도 TraceId가 정리됨")
  void doFilter_clearsTraceIdEvenOnException() throws ServletException, IOException {
    when(request.getHeader("X-Trace-Id")).thenReturn(null);
    doThrow(new RuntimeException("Test exception")).when(filterChain).doFilter(request, response);

    try {
      filter.doFilter(request, response, filterChain);
    } catch (RuntimeException e) {
      // 예외는 예상됨
    }

    assertThat(TraceIdUtil.getTraceId()).isNull();
    assertThat(TraceIdUtil.hasTraceId()).isFalse();
  }
}
