package com.softlabs.aicontents.domain.user.service;

import com.softlabs.aicontents.domain.user.dto.UserSignupDto;
import com.softlabs.aicontents.domain.user.vo.User;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final PasswordService passwordService;

  @Autowired
  public UserService(PasswordService passwordService) {
    this.passwordService = passwordService;
  }

  public User createUser(UserSignupDto signupDto) {
    String hashedPassword = passwordService.hashPassword(signupDto.getPassword());

    LocalDateTime now = LocalDateTime.now();
    return new User(
        null, signupDto.getLoginId(), signupDto.getEmail(), hashedPassword, now, now, null);
  }

  public boolean validatePassword(String plainPassword, String hashedPassword) {
    return passwordService.matches(plainPassword, hashedPassword);
  }
}
