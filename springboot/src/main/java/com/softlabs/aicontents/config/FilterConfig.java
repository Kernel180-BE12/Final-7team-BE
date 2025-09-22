package com.softlabs.aicontents.config;

import com.softlabs.aicontents.common.filter.TraceIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

  @Bean
  public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
    FilterRegistrationBean<TraceIdFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new TraceIdFilter());
    registrationBean.addUrlPatterns("/*");
    registrationBean.setOrder(1);

    // OncePerRequestFilter의 shouldNotFilter로 제외 처리하므로 여기서는 모든 경로 허용
    return registrationBean;
  }
}
