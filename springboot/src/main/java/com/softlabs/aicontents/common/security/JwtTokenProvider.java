package com.softlabs.aicontents.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private final SecretKey secretKey;
  private final long accessTokenValidityInMilliseconds;
  private final long refreshTokenValidityInMilliseconds;
  private final ApplicationContext applicationContext;

  public JwtTokenProvider(
      @Value("${jwt.secret:mySecretKey1234567890123456789012}") String secretKey,
      @Value("${jwt.access-token-validity-in-seconds:3600}") long accessTokenValidityInSeconds,
      @Value("${jwt.refresh-token-validity-in-seconds:1209600}") long refreshTokenValidityInSeconds,
      ApplicationContext applicationContext) {
    this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
    this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
    this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;
    this.applicationContext = applicationContext;
  }

  public String createAccessToken(String loginId) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + accessTokenValidityInMilliseconds);

    return Jwts.builder()
        .setSubject(loginId)
        .setIssuedAt(now)
        .setExpiration(validity)
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public String createRefreshToken(String loginId) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

    return Jwts.builder()
        .setSubject(loginId)
        .setIssuedAt(now)
        .setExpiration(validity)
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  @Deprecated
  public String createToken(String loginId) {
    return createAccessToken(loginId);
  }

  public Authentication getAuthentication(String token) {
    UserDetailsService userDetailsService = applicationContext.getBean(UserDetailsService.class);
    UserDetails userDetails = userDetailsService.loadUserByUsername(getLoginId(token));
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }

  public String getLoginId(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }
}
