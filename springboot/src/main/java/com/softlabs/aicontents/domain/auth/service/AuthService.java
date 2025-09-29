package com.softlabs.aicontents.domain.auth.service;

import com.softlabs.aicontents.common.enums.ErrorCode;
import com.softlabs.aicontents.common.exception.BusinessException;
import com.softlabs.aicontents.common.security.JwtTokenProvider;
import com.softlabs.aicontents.domain.auth.dto.LoginRequestDto;
import com.softlabs.aicontents.domain.auth.dto.LoginResponseDto;
import com.softlabs.aicontents.domain.auth.dto.LogoutRequestDto;
import com.softlabs.aicontents.domain.auth.dto.RefreshTokenRequestDto;
import com.softlabs.aicontents.domain.auth.dto.RefreshTokenResponseDto;
import com.softlabs.aicontents.domain.user.mapper.UserMapper;
import com.softlabs.aicontents.domain.user.vo.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenService refreshTokenService;
  private final TokenBlacklistService tokenBlacklistService;

  public AuthService(
      UserMapper userMapper,
      PasswordEncoder passwordEncoder,
      JwtTokenProvider jwtTokenProvider,
      RefreshTokenService refreshTokenService,
      TokenBlacklistService tokenBlacklistService) {
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenProvider = jwtTokenProvider;
    this.refreshTokenService = refreshTokenService;
    this.tokenBlacklistService = tokenBlacklistService;
  }

  public LoginResponseDto login(LoginRequestDto loginRequestDto) {
    User user = userMapper.findByLoginId(loginRequestDto.getLoginId());
    if (user == null) {
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }

    if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }

    String accessToken = jwtTokenProvider.createAccessToken(user.getLoginId());
    String refreshToken = jwtTokenProvider.createRefreshToken(user.getLoginId());

    refreshTokenService.saveRefreshToken(user.getLoginId(), refreshToken);

    return new LoginResponseDto(accessToken, refreshToken, user.getLoginId());
  }

  public void logout(LogoutRequestDto logoutRequestDto) {
    String accessToken = logoutRequestDto.getAccessToken();

    String loginId = jwtTokenProvider.getLoginId(accessToken);

    tokenBlacklistService.addToBlacklist(accessToken);
    refreshTokenService.deleteRefreshToken(loginId);
  }

  public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequestDto) {
    String refreshToken = refreshTokenRequestDto.getRefreshToken();

    if (!jwtTokenProvider.validateToken(refreshToken)) {
      throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    String loginId = jwtTokenProvider.getLoginId(refreshToken);

    if (!refreshTokenService.validateRefreshToken(loginId, refreshToken)) {
      throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    String newAccessToken = jwtTokenProvider.createAccessToken(loginId);

    return new RefreshTokenResponseDto(newAccessToken, loginId);
  }
}
