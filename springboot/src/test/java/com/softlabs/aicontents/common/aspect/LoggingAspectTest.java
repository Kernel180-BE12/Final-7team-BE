package com.softlabs.aicontents.common.aspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.softlabs.aicontents.common.annotation.Loggable;
import com.softlabs.aicontents.common.enums.ErrorCode;
import com.softlabs.aicontents.common.exception.BusinessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

  private LoggingAspect loggingAspect;

  @Mock private ProceedingJoinPoint joinPoint;

  @Mock private Signature signature;

  @Mock private Loggable loggable;

  private TestService testTarget;

  @BeforeEach
  void setUp() {
    loggingAspect = new LoggingAspect();
    testTarget = new TestService();
    MDC.put("traceId", "test-trace-id");
  }

  @Test
  void testLogExecutionTime_Success() throws Throwable {
    String expectedResult = "test result";
    Object[] args = {"param1", "param2"};

    when(joinPoint.getTarget()).thenReturn(testTarget);
    when(joinPoint.getSignature()).thenReturn(signature);
    when(signature.getName()).thenReturn("testMethod");
    when(joinPoint.getArgs()).thenReturn(args);
    when(joinPoint.proceed()).thenReturn(expectedResult);

    Object result = loggingAspect.logExecutionTime(joinPoint, loggable);

    assertThat(result).isEqualTo(expectedResult);
    verify(joinPoint).proceed();
  }

  @Test
  void testLogExecutionTime_BusinessException() throws Throwable {
    BusinessException businessException = new BusinessException(ErrorCode.INVALID_INPUT);
    Object[] args = {"param1"};

    when(joinPoint.getTarget()).thenReturn(testTarget);
    when(joinPoint.getSignature()).thenReturn(signature);
    when(signature.getName()).thenReturn("testMethod");
    when(joinPoint.getArgs()).thenReturn(args);
    when(joinPoint.proceed()).thenThrow(businessException);

    assertThatThrownBy(() -> loggingAspect.logExecutionTime(joinPoint, loggable))
        .isInstanceOf(BusinessException.class)
        .hasMessage(businessException.getMessage());

    verify(joinPoint).proceed();
  }

  @Test
  void testLogExecutionTime_UnexpectedException() throws Throwable {
    RuntimeException exception = new RuntimeException("Unexpected error");
    Object[] args = {};

    when(joinPoint.getTarget()).thenReturn(testTarget);
    when(joinPoint.getSignature()).thenReturn(signature);
    when(signature.getName()).thenReturn("testMethod");
    when(joinPoint.getArgs()).thenReturn(args);
    when(joinPoint.proceed()).thenThrow(exception);

    assertThatThrownBy(() -> loggingAspect.logExecutionTime(joinPoint, loggable))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Unexpected error");

    verify(joinPoint).proceed();
  }

  @Test
  void testLogExecutionTime_WithTraceId() throws Throwable {
    String traceId = "custom-trace-id";
    MDC.put("traceId", traceId);

    when(joinPoint.getTarget()).thenReturn(testTarget);
    when(joinPoint.getSignature()).thenReturn(signature);
    when(signature.getName()).thenReturn("testMethod");
    when(joinPoint.getArgs()).thenReturn(new Object[] {});
    when(joinPoint.proceed()).thenReturn("success");

    Object result = loggingAspect.logExecutionTime(joinPoint, loggable);

    assertThat(result).isEqualTo("success");
    verify(joinPoint).proceed();
  }

  @Test
  void testLogExecutionTime_WithNullTraceId() throws Throwable {
    MDC.remove("traceId");

    when(joinPoint.getTarget()).thenReturn(testTarget);
    when(joinPoint.getSignature()).thenReturn(signature);
    when(signature.getName()).thenReturn("testMethod");
    when(joinPoint.getArgs()).thenReturn(new Object[] {});
    when(joinPoint.proceed()).thenReturn("success");

    Object result = loggingAspect.logExecutionTime(joinPoint, loggable);

    assertThat(result).isEqualTo("success");
    verify(joinPoint).proceed();
  }

  private static class TestService {
    public String testMethod(String param) {
      return "test result";
    }
  }
}
