package com.softlabs.aicontents.common.util;

import java.util.UUID;
import org.slf4j.MDC;

public class TraceIdUtil {

  private static final String TRACE_ID_KEY = "traceId";

  public enum TraceIdFormat {
    COMPACT_LOWERCASE,
    COMPACT_UPPERCASE,
    HYPHENATED_LOWERCASE,
    HYPHENATED_UPPERCASE
  }

  public static String generateTraceId() {
    return generateTraceId(TraceIdFormat.COMPACT_LOWERCASE);
  }

  public static String generateTraceId(TraceIdFormat format) {
    String uuid = UUID.randomUUID().toString();
    String traceId;

    switch (format) {
      case COMPACT_LOWERCASE:
        traceId = uuid.replace("-", "").substring(0, 16).toLowerCase();
        break;
      case COMPACT_UPPERCASE:
        traceId = uuid.replace("-", "").substring(0, 16).toUpperCase();
        break;
      case HYPHENATED_LOWERCASE:
        traceId = uuid.substring(0, 18).toLowerCase();
        break;
      case HYPHENATED_UPPERCASE:
        traceId = uuid.substring(0, 18).toUpperCase();
        break;
      default:
        traceId = uuid.replace("-", "").substring(0, 16).toLowerCase();
    }

    return traceId;
  }

  public static void setTraceId(String traceId) {
    if (traceId != null && !traceId.trim().isEmpty()) {
      MDC.put(TRACE_ID_KEY, traceId);
    }
  }

  public static void setNewTraceId() {
    setNewTraceId(TraceIdFormat.COMPACT_LOWERCASE);
  }

  public static void setNewTraceId(TraceIdFormat format) {
    String newTraceId = generateTraceId(format);
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
