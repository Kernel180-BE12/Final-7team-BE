package com.softlabs.aicontents.common.filter;

import com.softlabs.aicontents.common.util.TraceIdUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraceIdFilter implements Filter {

  private static final Logger log = LoggerFactory.getLogger(TraceIdFilter.class);
  private static final String TRACE_ID_HEADER = "X-Trace-Id";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    try {
      String existingTraceId = httpRequest.getHeader(TRACE_ID_HEADER);

      if (existingTraceId != null && !existingTraceId.trim().isEmpty()) {
        TraceIdUtil.setTraceId(existingTraceId);
        log.debug("Using existing trace ID from header: {}", existingTraceId);
      } else {
        TraceIdUtil.setNewTraceId();
        log.debug("Generated new trace ID: {}", TraceIdUtil.getTraceId());
      }

      httpResponse.setHeader(TRACE_ID_HEADER, TraceIdUtil.getTraceId());

      chain.doFilter(request, response);

    } finally {
      TraceIdUtil.clearTraceId();
    }
  }
}
