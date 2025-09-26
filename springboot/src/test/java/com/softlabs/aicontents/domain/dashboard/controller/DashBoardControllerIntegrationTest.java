package com.softlabs.aicontents.domain.dashboard.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class DashBoardControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("DashBoard 요청 - 정상 입력값 검증 성공")
  void testValidDashBoardRequest() throws Exception {
    // When & Then
    mockMvc
        .perform(
            get("/v1/dashboard/connectTest").param("role", "admin").param("userName", "testUser"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("testUser Hello:)"));
  }

  @Test
  @DisplayName("DashBoard 요청 - role이 admin이 아닐 때 권한 없음")
  void testInvalidDashBoardRequest_InvalidRole() throws Exception {
    // When & Then
    mockMvc
        .perform(
            get("/v1/dashboard/connectTest").param("role", "user").param("userName", "testUser"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("권한이 없습니다."));
  }

  @Test
  @DisplayName("DashBoard 요청 - userName 파라미터 테스트")
  void testUserNameParameter() throws Exception {
    // When & Then
    mockMvc
        .perform(get("/v1/dashboard/connectTest").param("role", "admin").param("userName", "john"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string("john Hello:)"));
  }

  @Test
  @DisplayName("DashBoard 요청 - 네비게이션 메뉴 조회")
  void testGetNavMenu() throws Exception {
    // When & Then
    mockMvc.perform(get("/v1/dashboard/navMenu")).andDo(print()).andExpect(status().isOk());
  }
}
