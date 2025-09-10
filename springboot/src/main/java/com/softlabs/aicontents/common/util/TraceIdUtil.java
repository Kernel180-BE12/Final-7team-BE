package com.softlabs.aicontents.common.util;

import java.util.UUID;
import org.slf4j.MDC;

public class TraceIdUtil {

  private static final String TRACE_ID_KEY = "traceId";

  public static String generateTraceId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
  }

  public static void setTraceId(String traceId) {
    if (traceId != null && !traceId.trim().isEmpty()) {
      MDC.put(TRACE_ID_KEY, traceId);
    }
  }

  public static void setNewTraceId() {
    String newTraceId = generateTraceId();
    MDC.put(TRACE_ID_KEY, newTraceId);
  }

  public static String getTraceId() {
    return MDC.get(TRACE_ID_KEY);
  }

  public static void clearTraceId() {
    MDC.remove(TRACE_ID_KEY);
  }

  public static void clearAllMdc() {
    MDC.clear();
  }

  public static boolean hasTraceId() {
    return MDC.get(TRACE_ID_KEY) != null;
  }

  public static String getOrCreateTraceId() {
    String traceId = getTraceId();
    if (traceId == null) {
      setNewTraceId();
      traceId = getTraceId();
    }
    return traceId;
  }
}
