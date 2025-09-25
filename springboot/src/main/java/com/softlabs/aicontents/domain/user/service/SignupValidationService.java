//package com.softlabs.aicontents.domain.user.service;
//
//import com.softlabs.aicontents.common.enums.ErrorCode;
//import com.softlabs.aicontents.common.exception.BusinessException;
//import com.softlabs.aicontents.domain.email.service.VerificationCodeService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//@Slf4j
//public class SignupValidationService {
//
//  private final UserService userService;
//  private final VerificationCodeService verificationCodeService;
//
//  @Autowired
//  public SignupValidationService(
//      UserService userService, VerificationCodeService verificationCodeService) {
//    this.userService = userService;
//    this.verificationCodeService = verificationCodeService;
//  }
//
//  public void validateSignupConditions(String loginId, String email, String verificationCode) {
//    if (userService.isLoginIdDuplicate(loginId)) {
//      throw new BusinessException(ErrorCode.LOGIN_ID_ALREADY_EXISTS);
//    }
//
//    if (userService.isEmailDuplicate(email)) {
//      throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
//    }
//
//    if (!verificationCodeService.verifyCode(email, verificationCode)) {
//      throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_FAILED);
//    }
//
//    log.info("회원가입 검증 완료 - loginId: {}, email: {}", loginId, email);
//  }
//}
