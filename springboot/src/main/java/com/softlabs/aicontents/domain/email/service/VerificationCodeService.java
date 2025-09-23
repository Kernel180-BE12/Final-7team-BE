package com.softlabs.aicontents.domain.email.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VerificationCodeService {

  private static final int CODE_LENGTH = 6;
  private static final int EXPIRY_MINUTES = 5;
  private final SecureRandom random = new SecureRandom();
  private final ConcurrentHashMap<String, VerificationData> verificationCodes =
      new ConcurrentHashMap<>();

  public String generateVerificationCode(String email) {
    String code = String.format("%06d", random.nextInt(1000000));
    VerificationData data = new VerificationData(code, LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
    verificationCodes.put(email, data);

    log.info("인증코드 생성 완료: {}", email);
    return code;
  }

  public boolean verifyCode(String email, String inputCode) {
    VerificationData data = verificationCodes.get(email);

    if (data == null) {
      log.warn("인증코드가 존재하지 않음: {}", email);
      return false;
    }

    if (LocalDateTime.now().isAfter(data.getExpiryTime())) {
      verificationCodes.remove(email);
      log.warn("인증코드 만료: {}", email);
      return false;
    }

    boolean isValid = data.getCode().equals(inputCode);
    if (isValid) {
      verificationCodes.remove(email);
      log.info("인증코드 검증 성공: {}", email);
    } else {
      log.warn("인증코드 불일치: {}", email);
    }

    return isValid;
  }

  public void cleanupExpiredCodes() {
    LocalDateTime now = LocalDateTime.now();
    verificationCodes.entrySet().removeIf(entry -> now.isAfter(entry.getValue().getExpiryTime()));
  }

  private static class VerificationData {
    private final String code;
    private final LocalDateTime expiryTime;

    public VerificationData(String code, LocalDateTime expiryTime) {
      this.code = code;
      this.expiryTime = expiryTime;
    }

    public String getCode() {
      return code;
    }

    public LocalDateTime getExpiryTime() {
      return expiryTime;
    }
  }
}