//package com.softlabs.aicontents.domain.user.service;
//
//import com.softlabs.aicontents.domain.user.dto.UserSignupDto;
//import com.softlabs.aicontents.domain.user.mapper.UserMapper;
//import com.softlabs.aicontents.domain.user.vo.User;
//import java.time.LocalDateTime;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//public class UserService {
//
//  private final PasswordService passwordService;
//  private final UserMapper userMapper;
//
//  @Autowired
//  public UserService(PasswordService passwordService, UserMapper userMapper) {
//    this.passwordService = passwordService;
//    this.userMapper = userMapper;
//  }
//
//  public User createUser(UserSignupDto signupDto) {
//    String hashedPassword = passwordService.hashPassword(signupDto.getPassword());
//
//    LocalDateTime now = LocalDateTime.now();
//    return new User(
//        null, signupDto.getLoginId(), signupDto.getEmail(), hashedPassword, now, now, null);
//  }
//
//  public boolean validatePassword(String plainPassword, String hashedPassword) {
//    return passwordService.matches(plainPassword, hashedPassword);
//  }
//
//  public boolean isLoginIdDuplicate(String loginId) {
//    return userMapper.existsByLoginId(loginId);
//  }
//
//  public boolean isEmailDuplicate(String email) {
//    return userMapper.existsByEmail(email);
//  }
//
//  @Transactional
//  public User signupUser(UserSignupDto signupDto) {
//    User user = createUser(signupDto);
//    userMapper.insertUser(user);
//    return user;
//  }
//}
