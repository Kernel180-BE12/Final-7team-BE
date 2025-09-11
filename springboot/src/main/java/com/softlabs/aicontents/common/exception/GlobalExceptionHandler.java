package com.softlabs.aicontents.common.exception;

import com.softlabs.aicontents.common.dto.response.ErrorResponseDTO;
import com.softlabs.aicontents.common.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    //BusinessException에 정의된 예외 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(
            BusinessException ex, 
            HttpServletRequest request) {
        
        log.warn("Business exception occurred: {}", ex.getMessage(), ex);
        
        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
            errorCode, 
            request.getRequestURI(),
            ex.getEffectiveMessage()
        );
        
        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(errorResponse);
    }

    //@Valid나 @Validated 검증 실패 시 발생하는 예외 처리 ex)이메일 형식이 잘못되었을 때, 비밀번호 최소 몇 자 이상 등
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        log.warn("Validation exception occurred: {}", ex.getMessage());
        
        String errorMessage = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
            ErrorCode.INVALID_INPUT,
            request.getRequestURI(),
            "입력값 검증 실패: " + errorMessage
        );
        
        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT.getHttpStatus())
            .body(errorResponse);
    }

    //요청 데이터를 객체로 바인딩할 때 타입 불일치나 값 변환 실패가 발생하면 BindException이 발생. ex)검색어 누락 -> 검색어는 필수입니다.
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponseDTO> handleBindException(
            BindException ex,
            HttpServletRequest request) {
        
        log.warn("Bind exception occurred: {}", ex.getMessage());
        
        String errorMessage = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
            ErrorCode.INVALID_INPUT,
            request.getRequestURI(),
            "바인딩 오류: " + errorMessage
        );
        
        return ResponseEntity
            .status(ErrorCode.INVALID_INPUT.getHttpStatus())
            .body(errorResponse);
    }

    //필수 쿼리 파라미터가 요청에서 빠졌을 때 발생. ex) id 파라미터 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingParameterException(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        
        log.warn("Missing parameter exception occurred: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
            ErrorCode.MISSING_REQUIRED_FIELD,
            request.getRequestURI(),
            "필수 파라미터 누락: " + ex.getParameterName()
        );
        
        return ResponseEntity
            .status(ErrorCode.MISSING_REQUIRED_FIELD.getHttpStatus())
            .body(errorResponse);
    }

    //요청 파라미터 또는 경로 변수의 타입 변환 실패 시 발생. ex)id가 Long이어야 하는데 "abc" 전달
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        
        log.warn("Type mismatch exception occurred: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
            ErrorCode.INVALID_FORMAT,
            request.getRequestURI(),
            "잘못된 파라미터 타입: " + ex.getName()
        );
        
        return ResponseEntity
            .status(ErrorCode.INVALID_FORMAT.getHttpStatus())
            .body(errorResponse);
    }

    //요청 바디(JSON 등)를 Spring이 읽거나 파싱할 수 없을 때 발생. ex) JSON 문법 오류, 잘못된 타입, 비어있는 본문 등.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        
        log.warn("Http message not readable exception occurred: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
            ErrorCode.INVALID_FORMAT,
            request.getRequestURI(),
            "요청 본문을 읽을 수 없습니다."
        );
        
        return ResponseEntity
            .status(ErrorCode.INVALID_FORMAT.getHttpStatus())
            .body(errorResponse);
    }

    //지원하지 않는 HTTP 메서드를 호출할 때 발생. ex)GET 요청을 보냈지만 컨트롤러는 POST만 지원
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        
        log.warn("Method not supported exception occurred: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
            ErrorCode.METHOD_NOT_ALLOWED,
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus())
            .body(errorResponse);
    }

    //매핑된 컨트롤러(핸들러)가 없는 잘못된 URL 호출 시 발생.
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpServletRequest request) {
        
        log.warn("No handler found exception occurred: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
            ErrorCode.NOT_FOUND,
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(ErrorCode.NOT_FOUND.getHttpStatus())
            .body(errorResponse);
    }

    //위에서 처리하지 못한 모든 예외를 처리하는 최후의 방어선.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneralException(
            Exception ex,
            HttpServletRequest request) {
        
        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.of(
            ErrorCode.INTERNAL_SERVER_ERROR,
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
            .body(errorResponse);
    }
}