package com.softlabs.aicontents.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void commence(
          HttpServletRequest request,
          HttpServletResponse response,
          AuthenticationException authException)
          throws IOException, ServletException {

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("error", "Unauthorized");
    errorResponse.put("message", "인증이 필요합니다.");
    errorResponse.put("path", request.getRequestURI());

    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}