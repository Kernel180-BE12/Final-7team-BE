package com.softlabs.aicontents.domain.auth.service;

import io.jsonwebtoken.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

  private static final String BLACKLIST_PREFIX = "blacklist:";

  private final RedisTemplate<String, String> redisTemplate;
  private final SecretKey secretKey;

  public TokenBlacklistService(
      RedisTemplate<String, String> redisTemplate,
      @Value("${jwt.secret:mySecretKey1234567890123456789012}") String secretKey) {
    this.redisTemplate = redisTemplate;
    this.secretKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(secretKey.getBytes());
  }

  public void addToBlacklist(String accessToken) {
    try {
      Claims claims =
          Jwts.parserBuilder()
              .setSigningKey(secretKey)
              .build()
              .parseClaimsJws(accessToken)
              .getBody();

      Date expiration = claims.getExpiration();
      long ttl = expiration.getTime() - System.currentTimeMillis();

      if (ttl > 0) {
        String key = BLACKLIST_PREFIX + accessToken;
        redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.MILLISECONDS);
      }
    } catch (JwtException | IllegalArgumentException e) {
    }
  }

  public boolean isBlacklisted(String accessToken) {
    String key = BLACKLIST_PREFIX + accessToken;
    return redisTemplate.hasKey(key);
  }
}
