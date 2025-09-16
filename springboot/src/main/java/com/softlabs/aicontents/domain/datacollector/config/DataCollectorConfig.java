package com.softlabs.aicontents.domain.datacollector.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConfigurationProperties(prefix = "datacollector")
@Getter
@Setter
public class DataCollectorConfig {

  @Value("${PAPAGO_CLIENT_ID}")
  private String papagoClientId;

  @Value("${PAPAGO_CLIENT_SECRET}")
  private String papagoClientSecret;

  @Value("${NAVER_SHOPPING_API_KEY:}")
  private String naverShoppingApiKey;

  @Value("${DATACOLLECTOR_PLAYWRIGHT_HEADLESS:true}")
  private boolean playwrightHeadless;

  @Value("${DATACOLLECTOR_BROWSER_TIMEOUT:30000}")
  private int browserTimeout;

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public CloseableHttpClient httpClient() {
    return HttpClients.createDefault();
  }

  @Bean
  public Playwright playwright() {
    return Playwright.create();
  }

  @Bean
  public Browser browser(Playwright playwright) {
    BrowserType.LaunchOptions launchOptions =
        new BrowserType.LaunchOptions()
            .setHeadless(playwrightHeadless)
            .setSlowMo(2000)
            .setArgs(java.util.Collections.singletonList("--lang=ko-KR"));

    return playwright.chromium().launch(launchOptions);
  }
}