package com.softlabs.aicontents.common.security;

import com.softlabs.aicontents.domain.user.mapper.UserMapper;
import com.softlabs.aicontents.domain.user.vo.User;
import java.util.Collections;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserMapper userMapper;

  public CustomUserDetailsService(UserMapper userMapper) {
    this.userMapper = userMapper;
  }

  @Override
  public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
    User user = userMapper.findByLoginId(loginId);
    if (user == null) {
      throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId);
    }

    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getLoginId())
        .password(user.getPassword())
        .authorities(Collections.emptyList())
        .build();
  }
}
