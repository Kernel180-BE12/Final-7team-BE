package com.softlabs.aicontents.common.exception;

import com.softlabs.aicontents.common.enums.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessExceptionTest {
    
    @Test
    void testBusinessExceptionWithErrorCode() {
        ErrorCode errorCode = ErrorCode.NOT_FOUND;
        
        BusinessException exception = new BusinessException(errorCode);
        
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
        assertThat(exception.getCustomMessage()).isNull();
        assertThat(exception.getEffectiveMessage()).isEqualTo(errorCode.getMessage());
    }
    
    @Test
    void testBusinessExceptionWithCustomMessage() {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT;
        String customMessage = "사용자 정의 오류 메시지";
        
        BusinessException exception = new BusinessException(errorCode, customMessage);
        
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(customMessage);
        assertThat(exception.getCustomMessage()).isEqualTo(customMessage);
        assertThat(exception.getEffectiveMessage()).isEqualTo(customMessage);
    }
    
    @Test
    void testBusinessExceptionWithCause() {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        Throwable cause = new RuntimeException("원인 예외");
        
        BusinessException exception = new BusinessException(errorCode, cause);
        
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCustomMessage()).isNull();
        assertThat(exception.getEffectiveMessage()).isEqualTo(errorCode.getMessage());
    }
    
    @Test
    void testBusinessExceptionWithCustomMessageAndCause() {
        ErrorCode errorCode = ErrorCode.DATABASE_ERROR;
        String customMessage = "데이터베이스 연결 실패";
        Throwable cause = new RuntimeException("Connection timeout");
        
        BusinessException exception = new BusinessException(errorCode, customMessage, cause);
        
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(customMessage);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCustomMessage()).isEqualTo(customMessage);
        assertThat(exception.getEffectiveMessage()).isEqualTo(customMessage);
    }
    
    @Test
    void testEffectiveMessageWithoutCustomMessage() {
        ErrorCode errorCode = ErrorCode.FORBIDDEN;
        BusinessException exception = new BusinessException(errorCode);
        
        assertThat(exception.getEffectiveMessage()).isEqualTo(errorCode.getMessage());
    }
    
    @Test
    void testEffectiveMessageWithCustomMessage() {
        ErrorCode errorCode = ErrorCode.FORBIDDEN;
        String customMessage = "특정 리소스에 대한 접근 권한이 없습니다";
        BusinessException exception = new BusinessException(errorCode, customMessage);
        
        assertThat(exception.getEffectiveMessage()).isEqualTo(customMessage);
    }
    
    @Test
    void testBusinessExceptionInheritance() {
        ErrorCode errorCode = ErrorCode.BAD_REQUEST;
        BusinessException exception = new BusinessException(errorCode);
        
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(Exception.class);
        assertThat(exception).isInstanceOf(Throwable.class);
    }
    
    @Test
    void testBusinessExceptionWithDifferentErrorCodes() {
        BusinessException badRequestException = new BusinessException(ErrorCode.BAD_REQUEST);
        assertThat(badRequestException.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
        
        BusinessException unauthorizedException = new BusinessException(ErrorCode.UNAUTHORIZED);
        assertThat(unauthorizedException.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
        
        BusinessException notFoundException = new BusinessException(ErrorCode.NOT_FOUND);
        assertThat(notFoundException.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
        
        BusinessException serverErrorException = new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        assertThat(serverErrorException.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    }
    
    @Test
    void testBusinessExceptionMessageConsistency() {
        ErrorCode errorCode = ErrorCode.CONFLICT;
        BusinessException exception = new BusinessException(errorCode);
        
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
        assertThat(exception.getEffectiveMessage()).isEqualTo(errorCode.getMessage());
    }
    
    @Test
    void testCustomMessageOverridesDefaultMessage() {
        ErrorCode errorCode = ErrorCode.SERVICE_UNAVAILABLE;
        String customMessage = "현재 시스템 점검 중입니다";
        BusinessException exception = new BusinessException(errorCode, customMessage);
        
        assertThat(exception.getMessage()).isEqualTo(customMessage);
        assertThat(exception.getEffectiveMessage()).isEqualTo(customMessage);
        assertThat(exception.getMessage()).isNotEqualTo(errorCode.getMessage());
    }
}