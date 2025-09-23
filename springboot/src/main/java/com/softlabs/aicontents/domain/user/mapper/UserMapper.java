package com.softlabs.aicontents.domain.user.mapper;

import com.softlabs.aicontents.domain.user.vo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

  int insertUser(User user);

  User selectUserByLoginId(@Param("loginId") String loginId);

  User selectUserByEmail(@Param("email") String email);

  User selectUserById(@Param("id") Long id);

  int updateUser(User user);

  int deleteUser(@Param("id") Long id);

  boolean existsByLoginId(@Param("loginId") String loginId);

  boolean existsByEmail(@Param("email") String email);
}
