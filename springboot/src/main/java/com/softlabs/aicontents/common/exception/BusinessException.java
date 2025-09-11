package com.softlabs.aicontents.common.exception;

import com.softlabs.aicontents.common.enums.ErrorCode;

public class BusinessException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final String customMessage;

    //기본 생성자 (ErrorCode만 사용)
    //사용 예시 throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
    }

    //커스텀 메시지 사용 가능
    //사용 예시 throw new BusinessException(ErrorCode.INVALID_INPUT, "이메일 형식이 잘못되었습니다.");
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }

    //원인 예외(cause) 전달 가능
    //사용 예시
    // try {
    //    externalApi.call();
    //} catch (IOException e) {
    //    throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, e);
    //}
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.customMessage = null;
    }


    //커스텀 메시지 + 원인 예외 둘 다 사용
    //사용 예시
    //try {
    //    paymentGateway.call();
    //} catch (Exception e) {
    //    throw new BusinessException(
    //        ErrorCode.EXTERNAL_API_ERROR,
    //        "결제 서비스 호출 중 오류 발생",
    //        e
    //    );
    //}
    public BusinessException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    public String getCustomMessage() {
        return customMessage;
    }
    
    public String getEffectiveMessage() {
        return customMessage != null ? customMessage : errorCode.getMessage();
    }
}