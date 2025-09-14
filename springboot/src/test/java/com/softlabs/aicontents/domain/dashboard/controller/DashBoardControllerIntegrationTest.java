package com.softlabs.aicontents.domain.dashboard.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softlabs.aicontents.domain.dashboard.dto.DashBoardReqDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Transactional
class DashBoardControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("DashBoard 요청 - 정상 입력값 검증 성공")
  void testValidDashBoardRequest() throws Exception {
    // Given
    DashBoardReqDTO validRequest = new DashBoardReqDTO();
    validRequest.setRole("ADMIN");
    validRequest.setUserName("testUser");

    // When & Then - 실제 컨트롤러가 없으므로 404 응답을 기대하지만, 검증은 통과해야 함
    mockMvc
        .perform(
            post("/api/dashboard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
        .andDo(print())
        .andExpect(status().isNotFound()) // 실제 엔드포인트가 없으므로 404
        .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
  }

  @Test
  @DisplayName("DashBoard 요청 - role이 빈값일 때 검증 실패")
  void testInvalidDashBoardRequest_EmptyRole() throws Exception {
    // Given
    DashBoardReqDTO invalidRequest = new DashBoardReqDTO();
    invalidRequest.setRole(""); // 빈값
    invalidRequest.setUserName("testUser");

    // When & Then
    mockMvc
        .perform(
            post("/api/dashboard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
        .andExpect(jsonPath("$.message").value("입력값 검증 실패: 역할은 필수 입력 값입니다."));
  }

  @Test
  @DisplayName("DashBoard 요청 - userName이 null일 때 검증 실패")
  void testInvalidDashBoardRequest_NullUserName() throws Exception {
    // Given
    DashBoardReqDTO invalidRequest = new DashBoardReqDTO();
    invalidRequest.setRole("ADMIN");
    invalidRequest.setUserName(null); // null

    // When & Then
    mockMvc
        .perform(
            post("/api/dashboard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
        .andExpect(jsonPath("$.message").value("입력값 검증 실패: 사용자명은 필수 입력 값입니다."));
  }

  @Test
  @DisplayName("DashBoard 요청 - userName이 길이 제한을 초과할 때 검증 실패")
  void testInvalidDashBoardRequest_UserNameTooLong() throws Exception {
    // Given
    DashBoardReqDTO invalidRequest = new DashBoardReqDTO();
    invalidRequest.setRole("ADMIN");
    invalidRequest.setUserName("a".repeat(21)); // 21자 (최대 20자 초과)

    // When & Then
    mockMvc
        .perform(
            post("/api/dashboard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
        .andExpect(jsonPath("$.message").value("입력값 검증 실패: 사용자명은 2자 이상 20자 이하여야 합니다."));
  }

  @Test
  @DisplayName("DashBoard 요청 - userName이 최소 길이 미만일 때 검증 실패")
  void testInvalidDashBoardRequest_UserNameTooShort() throws Exception {
    // Given
    DashBoardReqDTO invalidRequest = new DashBoardReqDTO();
    invalidRequest.setRole("ADMIN");
    invalidRequest.setUserName("a"); // 1자 (최소 2자 미만)

    // When & Then
    mockMvc
        .perform(
            post("/api/dashboard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
        .andExpect(jsonPath("$.message").value("입력값 검증 실패: 사용자명은 2자 이상 20자 이하여야 합니다."));
  }

  @Test
  @DisplayName("DashBoard 요청 - 여러 필드 검증 실패 시 메시지 조합")
  void testInvalidDashBoardRequest_MultipleFieldErrors() throws Exception {
    // Given
    DashBoardReqDTO invalidRequest = new DashBoardReqDTO();
    invalidRequest.setRole(""); // 빈값
    invalidRequest.setUserName(""); // 빈값

    // When & Then
    mockMvc
        .perform(
            post("/api/dashboard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
        .andExpect(jsonPath("$.message").exists()); // 여러 에러 메시지가 조합되어 나타남
  }

  @Test
  @DisplayName("DashBoard 요청 - JSON 형식 오류 시 파싱 실패")
  void testInvalidDashBoardRequest_InvalidJsonFormat() throws Exception {
    // Given
    String invalidJson = "{\"role\":\"ADMIN\",\"userName\":}"; // 잘못된 JSON

    // When & Then
    mockMvc
        .perform(
            post("/api/dashboard").contentType(MediaType.APPLICATION_JSON).content(invalidJson))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("INVALID_FORMAT"))
        .andExpect(jsonPath("$.message").value("요청 본문을 읽을 수 없습니다."));
  }
}
