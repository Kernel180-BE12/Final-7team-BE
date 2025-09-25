package com.softlabs.aicontents.domain.auth.service;

import com.softlabs.aicontents.common.enums.ErrorCode;
import com.softlabs.aicontents.common.exception.BusinessException;
import com.softlabs.aicontents.common.security.JwtTokenProvider;
import com.softlabs.aicontents.domain.auth.dto.LoginRequestDto;
import com.softlabs.aicontents.domain.auth.dto.LoginResponseDto;
import com.softlabs.aicontents.domain.user.mapper.UserMapper;
import com.softlabs.aicontents.domain.user.vo.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

  public AuthService(
      UserMapper userMapper, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  public LoginResponseDto login(LoginRequestDto loginRequestDto) {
    User user = userMapper.findByLoginId(loginRequestDto.getLoginId());
    if (user == null) {
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }

    if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }

    String accessToken = jwtTokenProvider.createToken(user.getLoginId());
    return new LoginResponseDto(accessToken, user.getLoginId());
  }
}