package com.softlabs.aicontents.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Softlabs 7팀 백엔드 API")
                        .description("Softlabs 프로젝트 백엔드 API 명세서")
                        .version("v1.0"));
    }
}