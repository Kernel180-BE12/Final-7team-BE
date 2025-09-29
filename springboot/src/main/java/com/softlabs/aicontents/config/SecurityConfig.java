package com.softlabs.aicontents.config;

import com.softlabs.aicontents.common.security.JwtAuthenticationEntryPoint;
import com.softlabs.aicontents.common.security.JwtAuthenticationFilter;
import com.softlabs.aicontents.common.security.JwtTokenProvider;
import com.softlabs.aicontents.domain.auth.service.TokenBlacklistService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final TokenBlacklistService tokenBlacklistService;

  public SecurityConfig(
      JwtTokenProvider jwtTokenProvider,
      JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
      TokenBlacklistService tokenBlacklistService) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    this.tokenBlacklistService = tokenBlacklistService;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            exceptions -> exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint))
        .authorizeHttpRequests(
            authz ->
                authz
                    .requestMatchers("/", "/auth/login", "/auth/refresh")
                    .permitAll()
                    .requestMatchers("/users/send-verification-code")
                    .permitAll()
                    .requestMatchers("/users/verify-code")
                    .permitAll()
                    .requestMatchers("/users/check-login-id")
                    .permitAll()
                    .requestMatchers("/users/check-email")
                    .permitAll()
                    .requestMatchers("/users/signup")
                    .permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(
            new JwtAuthenticationFilter(jwtTokenProvider, tokenBlacklistService),
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
