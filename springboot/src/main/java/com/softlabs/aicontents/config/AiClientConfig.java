package com.softlabs.aicontents.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 *  FastAPI 서버로 HTTP 요청을 보낼 때 사용할 WebClient를 애플리케이션 전역 Bean으로 등록.
 * - baseUrl: FastAPI 실행 주소 (기본 127.0.0.1:8001)
 * - 다른 클래스에서 생성자 주입으로 바로 가져다 씀
 */

@Configuration
public class AiClientConfig {

    @Bean
    public WebClient llmwebClient() {
        return WebClient.builder()
                .baseUrl("http://127.0.0.1:8001")
                .build();
    }
}
