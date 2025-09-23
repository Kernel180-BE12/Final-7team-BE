package com.softlabs.aicontents.domain.user.vo;

import java.time.LocalDateTime;

public class EmailVerification {
  private Long emailVerificationId;
  private String email;
  private String verificationCode;
  private LocalDateTime expiredAt;
  private Boolean isVerified;
  private LocalDateTime verifiedAt;
  private LocalDateTime createdAt;

  public EmailVerification() {}

  public EmailVerification(
      Long emailVerificationId,
      String email,
      String verificationCode,
      LocalDateTime expiredAt,
      Boolean isVerified,
      LocalDateTime verifiedAt,
      LocalDateTime createdAt) {
    this.emailVerificationId = emailVerificationId;
    this.email = email;
    this.verificationCode = verificationCode;
    this.expiredAt = expiredAt;
    this.isVerified = isVerified;
    this.verifiedAt = verifiedAt;
    this.createdAt = createdAt;
  }

  public Long getEmailVerificationId() {
    return emailVerificationId;
  }

  public void setEmailVerificationId(Long emailVerificationId) {
    this.emailVerificationId = emailVerificationId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getVerificationCode() {
    return verificationCode;
  }

  public void setVerificationCode(String verificationCode) {
    this.verificationCode = verificationCode;
  }

  public LocalDateTime getExpiredAt() {
    return expiredAt;
  }

  public void setExpiredAt(LocalDateTime expiredAt) {
    this.expiredAt = expiredAt;
  }

  public Boolean getIsVerified() {
    return isVerified;
  }

  public void setIsVerified(Boolean isVerified) {
    this.isVerified = isVerified;
  }

  public LocalDateTime getVerifiedAt() {
    return verifiedAt;
  }

  public void setVerifiedAt(LocalDateTime verifiedAt) {
    this.verifiedAt = verifiedAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
