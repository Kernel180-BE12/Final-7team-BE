package com.softlabs.aicontents.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            // REST API에서는 보통 CSRF를 끕니다.
            .csrf(csrf -> csrf.disable())

            // 요청별 권한 설정
            .authorizeHttpRequests(authz -> authz
                    .requestMatchers("/users/send-verification-code").permitAll()
                    .requestMatchers("/users/verify-code").permitAll()
                    .requestMatchers("/users/check-login-id").permitAll()
                    .requestMatchers("/users/check-email").permitAll()
                    .requestMatchers("/users/signup").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .anyRequest().authenticated()
            );

    return http.build();
  }
}
