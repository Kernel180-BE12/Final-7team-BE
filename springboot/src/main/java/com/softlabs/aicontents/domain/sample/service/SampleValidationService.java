package com.softlabs.aicontents.domain.sample.service;

import com.softlabs.aicontents.common.enums.ErrorCode;
import com.softlabs.aicontents.common.exception.BusinessException;
import com.softlabs.aicontents.domain.sample.dto.SampleRequestDTO;
import com.softlabs.aicontents.domain.sample.dto.SampleResponseDTO;
import org.springframework.stereotype.Service;

@Service
public class SampleValidationService {

  public SampleResponseDTO processValidData(SampleRequestDTO request) {
    String processedData =
        String.format(
            "이름: %s, 이메일: %s, 나이: %d", request.getName(), request.getEmail(), request.getAge());
    return SampleResponseDTO.success(processedData);
  }

  public void throwBusinessException(String type) {
    switch (type.toLowerCase()) {
      case "notfound":
        throw new BusinessException(ErrorCode.USER_NOT_FOUND);
      case "unauthorized":
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
      case "forbidden":
        throw new BusinessException(ErrorCode.FORBIDDEN);
      case "conflict":
        throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
      default:
        throw new BusinessException(ErrorCode.BAD_REQUEST, "알 수 없는 비즈니스 에러 타입: " + type);
    }
  }

  public void throwServerException() {
    throw new RuntimeException("의도적인 서버 에러 - DB 연결 실패 시뮬레이션");
  }

  public void throwNullPointerException() {
    String nullString = null;
    int length = nullString.length();
  }
}
