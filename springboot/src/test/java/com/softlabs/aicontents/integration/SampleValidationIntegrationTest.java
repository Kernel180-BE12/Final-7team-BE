package com.softlabs.aicontents.integration;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softlabs.aicontents.domain.sample.dto.SampleRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("샘플 검증 통합 테스트")
public class SampleValidationIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("정상적인 데이터로 POST 요청 시 200 OK 응답")
  void testValidRequest() throws Exception {
    // given
    SampleRequestDTO validRequest = new SampleRequestDTO();
    validRequest.setName("홍길동");
    validRequest.setEmail("hong@example.com");
    validRequest.setAge(25);
    validRequest.setPhone("010-1234-5678");
    validRequest.setAddress("서울시 강남구");

    // when & then
    mockMvc
        .perform(
            post("/api/sample/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", is("처리 성공")))
        .andExpect(jsonPath("$.processedData", containsString("홍길동")))
        .andExpect(jsonPath("$.timestamp", notNullValue()));
  }

  @Test
  @DisplayName("이름이 비어있을 때 400 Bad Request 응답")
  void testBlankName() throws Exception {
    // given
    SampleRequestDTO invalidRequest = new SampleRequestDTO();
    invalidRequest.setName(""); // 빈 문자열
    invalidRequest.setEmail("hong@example.com");
    invalidRequest.setAge(25);

    // when & then
    mockMvc
        .perform(
            post("/api/sample/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is("INVALID_INPUT")))
        .andExpect(jsonPath("$.message", containsString("이름은 필수입니다")))
        .andExpect(jsonPath("$.traceId", notNullValue()))
        .andExpect(jsonPath("$.path", is("/api/sample/validate")))
        .andExpect(jsonPath("$.status", is(400)));
  }

  @Test
  @DisplayName("잘못된 이메일 형식일 때 400 Bad Request 응답")
  void testInvalidEmail() throws Exception {
    // given
    SampleRequestDTO invalidRequest = new SampleRequestDTO();
    invalidRequest.setName("홍길동");
    invalidRequest.setEmail("invalid-email"); // 잘못된 이메일 형식
    invalidRequest.setAge(25);

    // when & then
    mockMvc
        .perform(
            post("/api/sample/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is("INVALID_INPUT")))
        .andExpect(jsonPath("$.message", containsString("올바른 이메일 형식이 아닙니다")))
        .andExpect(jsonPath("$.traceId", notNullValue()));
  }

  @Test
  @DisplayName("나이가 범위를 벗어날 때 400 Bad Request 응답")
  void testInvalidAge() throws Exception {
    // given
    SampleRequestDTO invalidRequest = new SampleRequestDTO();
    invalidRequest.setName("홍길동");
    invalidRequest.setEmail("hong@example.com");
    invalidRequest.setAge(10); // 18세 미만

    // when & then
    mockMvc
        .perform(
            post("/api/sample/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message", containsString("나이는 18세 이상이어야 합니다")));
  }

  @Test
  @DisplayName("잘못된 전화번호 형식일 때 400 Bad Request 응답")
  void testInvalidPhone() throws Exception {
    // given
    SampleRequestDTO invalidRequest = new SampleRequestDTO();
    invalidRequest.setName("홍길동");
    invalidRequest.setEmail("hong@example.com");
    invalidRequest.setAge(25);
    invalidRequest.setPhone("01012345678"); // 하이픈 없는 형식

    // when & then
    mockMvc
        .perform(
            post("/api/sample/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message", containsString("전화번호 형식이 올바르지 않습니다")));
  }

  @Test
  @DisplayName("비즈니스 예외 발생 시 해당 상태코드와 메시지 응답")
  void testBusinessException() throws Exception {
    // when & then - USER_NOT_FOUND (404)
    mockMvc
        .perform(get("/api/sample/business-error/notfound"))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code", is("USER_NOT_FOUND")))
        .andExpect(jsonPath("$.traceId", notNullValue()));

    // when & then - UNAUTHORIZED (401)
    mockMvc
        .perform(get("/api/sample/business-error/unauthorized"))
        .andDo(print())
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code", is("UNAUTHORIZED")));

    // when & then - FORBIDDEN (403)
    mockMvc
        .perform(get("/api/sample/business-error/forbidden"))
        .andDo(print())
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code", is("FORBIDDEN")));
  }

  @Test
  @DisplayName("서버 예외 발생 시 500 Internal Server Error 응답")
  void testServerException() throws Exception {
    // when & then
    mockMvc
        .perform(get("/api/sample/server-error"))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code", is("INTERNAL_SERVER_ERROR")))
        .andExpect(jsonPath("$.status", is(500)))
        .andExpect(jsonPath("$.traceId", notNullValue()));
  }

  @Test
  @DisplayName("NPE 발생 시 500 Internal Server Error 응답")
  void testNullPointerException() throws Exception {
    // when & then
    mockMvc
        .perform(get("/api/sample/npe-error"))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code", is("INTERNAL_SERVER_ERROR")));
  }

  @Test
  @DisplayName("잘못된 HTTP 메소드 사용 시 405 Method Not Allowed 응답")
  void testMethodNotAllowed() throws Exception {
    // when & then
    mockMvc
        .perform(delete("/api/sample/success")) // GET만 지원하는 엔드포인트에 DELETE 요청
        .andDo(print())
        .andExpect(status().isMethodNotAllowed());
  }

  @Test
  @DisplayName("존재하지 않는 엔드포인트 호출 시 404 Not Found 응답")
  void testNotFoundEndpoint() throws Exception {
    // when & then
    mockMvc
        .perform(get("/api/sample/non-existent"))
        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("잘못된 경로 변수 타입 전달 시 400 Bad Request 응답")
  void testInvalidPathVariableType() throws Exception {
    // when & then - Long 타입 경로변수에 문자열 전달
    mockMvc
        .perform(get("/api/sample/type-error/invalid-id"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is("INVALID_FORMAT")))
        .andExpect(jsonPath("$.message", containsString("잘못된 파라미터 타입")));
  }

  @Test
  @DisplayName("정상 요청 시 ErrorResponseDTO 구조 검증")
  void testErrorResponseStructure() throws Exception {
    // given
    SampleRequestDTO invalidRequest = new SampleRequestDTO();
    invalidRequest.setName(""); // validation 실패

    // when & then
    mockMvc
        .perform(
            post("/api/sample/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.timestamp", notNullValue()))
        .andExpect(jsonPath("$.code", notNullValue()))
        .andExpect(jsonPath("$.message", notNullValue()))
        .andExpect(jsonPath("$.traceId", notNullValue()))
        .andExpect(jsonPath("$.path", notNullValue()))
        .andExpect(jsonPath("$.status", notNullValue()));
  }
}
