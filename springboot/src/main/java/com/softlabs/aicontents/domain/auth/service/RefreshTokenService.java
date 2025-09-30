package com.softlabs.aicontents.domain.auth.service;

import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

  private static final String REFRESH_TOKEN_PREFIX = "refresh:";
  private static final long REFRESH_TOKEN_VALIDITY_DAYS = 14;

  private final RedisTemplate<String, String> redisTemplate;

  public RefreshTokenService(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public void saveRefreshToken(String userId, String refreshToken) {
    String key = REFRESH_TOKEN_PREFIX + userId;
    redisTemplate.opsForValue().set(key, refreshToken, REFRESH_TOKEN_VALIDITY_DAYS, TimeUnit.DAYS);
  }

  public String getRefreshToken(String userId) {
    String key = REFRESH_TOKEN_PREFIX + userId;
    return redisTemplate.opsForValue().get(key);
  }

  public void deleteRefreshToken(String userId) {
    String key = REFRESH_TOKEN_PREFIX + userId;
    redisTemplate.delete(key);
  }

  public boolean validateRefreshToken(String userId, String refreshToken) {
    String storedToken = getRefreshToken(userId);
    return storedToken != null && storedToken.equals(refreshToken);
  }
}
