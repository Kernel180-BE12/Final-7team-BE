package com.softlabs.aicontents.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

  @Bean
  public RestTemplate restTemplate() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

    // 연결 타임아웃: 5초
    factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());

    // 읽기 타임아웃: 30초
    factory.setReadTimeout((int) Duration.ofSeconds(30).toMillis());

    return new RestTemplate(factory);
  }
}
