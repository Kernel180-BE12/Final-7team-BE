package com.softlabs.aicontents.domain.user.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

  private final BCryptPasswordEncoder passwordEncoder;

  public PasswordService() {
    this.passwordEncoder = new BCryptPasswordEncoder();
  }

  public String hashPassword(String plainPassword) {
    return passwordEncoder.encode(plainPassword);
  }

  public boolean matches(String plainPassword, String hashedPassword) {
    return passwordEncoder.matches(plainPassword, hashedPassword);
  }
}
