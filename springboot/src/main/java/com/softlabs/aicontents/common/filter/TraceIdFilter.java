package com.softlabs.aicontents.common.filter;

import com.softlabs.aicontents.common.util.TraceIdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

public class TraceIdFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(TraceIdFilter.class);
  private static final String TRACE_ID_HEADER = "X-Trace-Id";
  private static final String REQUEST_URI_KEY = "requestUri";
  private static final String HTTP_METHOD_KEY = "httpMethod";
  private static final String CLIENT_IP_KEY = "clientIp";

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();
    return path.startsWith("/actuator/health")
        || path.startsWith("/static/")
        || path.startsWith("/css/")
        || path.startsWith("/js/")
        || path.startsWith("/images/")
        || path.startsWith("/favicon.ico");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    try {
      String existingTraceId = request.getHeader(TRACE_ID_HEADER);

      if (existingTraceId != null && !existingTraceId.trim().isEmpty()) {
        TraceIdUtil.setTraceId(existingTraceId);
      } else {
        TraceIdUtil.setNewTraceId();
      }

      String clientIp = getClientIp(request);
      String requestUri = request.getRequestURI();
      String httpMethod = request.getMethod();

      MDC.put(REQUEST_URI_KEY, requestUri);
      MDC.put(HTTP_METHOD_KEY, httpMethod);
      MDC.put(CLIENT_IP_KEY, clientIp);

      response.setHeader(TRACE_ID_HEADER, TraceIdUtil.getTraceId());

      log.info(
          "Request started - {} {} from {} [TraceId: {}]",
          httpMethod,
          requestUri,
          clientIp,
          TraceIdUtil.getTraceId());

      long startTime = System.currentTimeMillis();

      chain.doFilter(request, response);

      long duration = System.currentTimeMillis() - startTime;
      log.info(
          "Request completed - {} {} [{}ms] [TraceId: {}]",
          httpMethod,
          requestUri,
          duration,
          TraceIdUtil.getTraceId());

    } finally {
      TraceIdUtil.clearTraceId();
      MDC.remove(REQUEST_URI_KEY);
      MDC.remove(HTTP_METHOD_KEY);
      MDC.remove(CLIENT_IP_KEY);
    }
  }

  private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }

    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.trim().isEmpty()) {
      return xRealIp;
    }

    return request.getRemoteAddr();
  }
}
