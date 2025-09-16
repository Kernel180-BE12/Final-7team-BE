package com.softlabs.aicontents.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.softlabs.aicontents.common.util.TraceIdUtil.TraceIdFormat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TraceIdUtilTest {

  @AfterEach
  void tearDown() {
    TraceIdUtil.clearAllMdc();
  }

  @Test
  @DisplayName("기본 형식으로 TraceId 생성 - 16자 소문자 하이픈 없음")
  void generateTraceId_defaultFormat() {
    String traceId = TraceIdUtil.generateTraceId();

    assertThat(traceId).isNotNull();
    assertThat(traceId).hasSize(16);
    assertThat(traceId).matches("[a-f0-9]{16}");
    assertThat(traceId).doesNotContain("-");
  }

  @Test
  @DisplayName("COMPACT_LOWERCASE 형식으로 TraceId 생성")
  void generateTraceId_compactLowercase() {
    String traceId = TraceIdUtil.generateTraceId(TraceIdFormat.COMPACT_LOWERCASE);

    assertThat(traceId).isNotNull();
    assertThat(traceId).hasSize(16);
    assertThat(traceId).matches("[a-f0-9]{16}");
    assertThat(traceId).doesNotContain("-");
  }

  @Test
  @DisplayName("COMPACT_UPPERCASE 형식으로 TraceId 생성")
  void generateTraceId_compactUppercase() {
    String traceId = TraceIdUtil.generateTraceId(TraceIdFormat.COMPACT_UPPERCASE);

    assertThat(traceId).isNotNull();
    assertThat(traceId).hasSize(16);
    assertThat(traceId).matches("[A-F0-9]{16}");
    assertThat(traceId).doesNotContain("-");
  }

  @Test
  @DisplayName("HYPHENATED_LOWERCASE 형식으로 TraceId 생성")
  void generateTraceId_hyphenatedLowercase() {
    String traceId = TraceIdUtil.generateTraceId(TraceIdFormat.HYPHENATED_LOWERCASE);

    assertThat(traceId).isNotNull();
    assertThat(traceId).hasSize(18);
    assertThat(traceId).matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}");
    assertThat(traceId).contains("-");
  }

  @Test
  @DisplayName("HYPHENATED_UPPERCASE 형식으로 TraceId 생성")
  void generateTraceId_hyphenatedUppercase() {
    String traceId = TraceIdUtil.generateTraceId(TraceIdFormat.HYPHENATED_UPPERCASE);

    assertThat(traceId).isNotNull();
    assertThat(traceId).hasSize(18);
    assertThat(traceId).matches("[A-F0-9]{8}-[A-F0-9]{4}-[A-F0-9]{4}");
    assertThat(traceId).contains("-");
  }

  @Test
  @DisplayName("TraceId 설정 및 조회")
  void setAndGetTraceId() {
    String testTraceId = "test1234567890ab";
    TraceIdUtil.setTraceId(testTraceId);

    assertThat(TraceIdUtil.getTraceId()).isEqualTo(testTraceId);
    assertThat(TraceIdUtil.hasTraceId()).isTrue();
  }

  @Test
  @DisplayName("null TraceId 설정 시 MDC에 저장되지 않음")
  void setTraceId_null() {
    TraceIdUtil.setTraceId(null);

    assertThat(TraceIdUtil.getTraceId()).isNull();
    assertThat(TraceIdUtil.hasTraceId()).isFalse();
  }

  @Test
  @DisplayName("빈 문자열 TraceId 설정 시 MDC에 저장되지 않음")
  void setTraceId_empty() {
    TraceIdUtil.setTraceId("");

    assertThat(TraceIdUtil.getTraceId()).isNull();
    assertThat(TraceIdUtil.hasTraceId()).isFalse();
  }

  @Test
  @DisplayName("새로운 TraceId 설정 - 기본 형식")
  void setNewTraceId_default() {
    TraceIdUtil.setNewTraceId();

    String traceId = TraceIdUtil.getTraceId();
    assertThat(traceId).isNotNull();
    assertThat(traceId).hasSize(16);
    assertThat(traceId).matches("[a-f0-9]{16}");
    assertThat(TraceIdUtil.hasTraceId()).isTrue();
  }

  @Test
  @DisplayName("새로운 TraceId 설정 - 지정된 형식")
  void setNewTraceId_withFormat() {
    TraceIdUtil.setNewTraceId(TraceIdFormat.HYPHENATED_UPPERCASE);

    String traceId = TraceIdUtil.getTraceId();
    assertThat(traceId).isNotNull();
    assertThat(traceId).hasSize(18);
    assertThat(traceId).matches("[A-F0-9]{8}-[A-F0-9]{4}-[A-F0-9]{4}");
    assertThat(TraceIdUtil.hasTraceId()).isTrue();
  }

  @Test
  @DisplayName("TraceId 삭제")
  void clearTraceId() {
    TraceIdUtil.setNewTraceId();
    assertThat(TraceIdUtil.hasTraceId()).isTrue();

    TraceIdUtil.clearTraceId();
    assertThat(TraceIdUtil.getTraceId()).isNull();
    assertThat(TraceIdUtil.hasTraceId()).isFalse();
  }

  @Test
  @DisplayName("TraceId가 없을 때 getOrCreateTraceId 호출 시 새로 생성")
  void getOrCreateTraceId_whenEmpty() {
    assertThat(TraceIdUtil.hasTraceId()).isFalse();

    String traceId = TraceIdUtil.getOrCreateTraceId();

    assertThat(traceId).isNotNull();
    assertThat(traceId).hasSize(16);
    assertThat(traceId).matches("[a-f0-9]{16}");
    assertThat(TraceIdUtil.hasTraceId()).isTrue();
    assertThat(TraceIdUtil.getTraceId()).isEqualTo(traceId);
  }

  @Test
  @DisplayName("TraceId가 있을 때 getOrCreateTraceId 호출 시 기존 값 반환")
  void getOrCreateTraceId_whenExists() {
    String existingTraceId = "existing12345678";
    TraceIdUtil.setTraceId(existingTraceId);

    String traceId = TraceIdUtil.getOrCreateTraceId();

    assertThat(traceId).isEqualTo(existingTraceId);
    assertThat(TraceIdUtil.getTraceId()).isEqualTo(existingTraceId);
  }

  @Test
  @DisplayName("모든 MDC 데이터 삭제")
  void clearAllMdc() {
    TraceIdUtil.setNewTraceId();
    assertThat(TraceIdUtil.hasTraceId()).isTrue();

    TraceIdUtil.clearAllMdc();
    assertThat(TraceIdUtil.getTraceId()).isNull();
    assertThat(TraceIdUtil.hasTraceId()).isFalse();
  }
}