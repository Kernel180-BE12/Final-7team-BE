package com.softlabs.aicontents.common.aspect;

import com.softlabs.aicontents.common.annotation.Loggable;
import com.softlabs.aicontents.common.exception.BusinessException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

  @Around("@annotation(loggable) || @within(loggable)")
  public Object logExecutionTime(ProceedingJoinPoint joinPoint, Loggable loggable)
      throws Throwable {
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    Object[] args = joinPoint.getArgs();
    String traceId = MDC.get("traceId");

    long startTime = System.currentTimeMillis();

    log.info(
        "[{}] [{}#{}] Starting method execution with parameters: {}",
        traceId,
        className,
        methodName,
        Arrays.toString(args));

    try {
      Object result = joinPoint.proceed();
      long endTime = System.currentTimeMillis();
      long executionTime = endTime - startTime;

      log.info(
          "[{}] [{}#{}] Method completed successfully in {}ms with result: {}",
          traceId,
          className,
          methodName,
          executionTime,
          result);

      return result;

    } catch (BusinessException e) {
      long endTime = System.currentTimeMillis();
      long executionTime = endTime - startTime;

      log.warn(
          "[{}] [{}#{}] Business exception occurred after {}ms: {} - {}",
          traceId,
          className,
          methodName,
          executionTime,
          e.getErrorCode(),
          e.getEffectiveMessage());

      throw e;

    } catch (Exception e) {
      long endTime = System.currentTimeMillis();
      long executionTime = endTime - startTime;

      log.error(
          "[{}] [{}#{}] Unexpected exception occurred after {}ms: {}",
          traceId,
          className,
          methodName,
          executionTime,
          e.getMessage(),
          e);

      throw e;
    }
  }
}
