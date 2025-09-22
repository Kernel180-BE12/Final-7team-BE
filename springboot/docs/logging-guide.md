# 로깅 및 예외처리 상세 가이드

## 목차
1. [로깅 시스템 상세](#로깅-시스템-상세)
2. [예외 처리 상세](#예외-처리-상세)
3. [TraceId 추적 상세](#traceid-추적-상세)
4. [실제 로그 예시](#실제-로그-예시)
5. [API 응답 예시](#api-응답-예시)
6. [트러블슈팅](#트러블슈팅)

## 로깅 시스템 상세

### LoggingAspect 동작 원리
```java
@Aspect
@Component
@Slf4j
public class LoggingAspect {

  @Around("@annotation(loggable) || @within(loggable)")
  public Object logExecutionTime(ProceedingJoinPoint joinPoint, Loggable loggable) {
    // 1. 메서드 실행 전 로깅
    // 2. 메서드 실행
    // 3. 성공/실패에 따른 후처리 로깅
  }
}
```

### 로깅 레벨별 사용 기준
- **INFO**: 정상적인 메서드 시작/완료
- **WARN**: BusinessException 발생 (비즈니스 로직 오류)
- **ERROR**: 예상치 못한 시스템 오류

### 로그 포맷 설명
```
[TraceId] [클래스명#메서드명] 메시지 내용
```

## 예외 처리 상세

### ErrorCode 정의 예시
```java
public enum ErrorCode {
    // 4xx 클라이언트 오류
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),

    // 5xx 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");
}
```

### GlobalExceptionHandler 처리 순서
1. **BusinessException**: 비즈니스 로직 예외 (우선 처리)
2. **Validation 예외**: Bean Validation 실패
3. **HTTP 관련 예외**: 메서드 미지원, 리소스 없음 등
4. **일반 Exception**: 예상치 못한 오류 (최후 방어선)

## TraceId 추적 상세

### TraceIdFilter 동작 과정
```java
1. HTTP 요청 수신
2. X-Trace-Id 헤더 확인
   - 있으면: 기존 TraceId 사용
   - 없으면: 새로운 TraceId 생성
3. MDC에 TraceId 설정
4. 요청 처리
5. 응답 헤더에 TraceId 포함
6. MDC 정리
```

### TraceId 생성 규칙
- 포맷: `TID-{timestamp}-{random}`
- 예시: `TID-1705314615000-A7B2C`

### MDC 컨텍스트 정보
```java
MDC.put("traceId", traceId);
MDC.put("requestUri", requestUri);
MDC.put("httpMethod", httpMethod);
MDC.put("clientIp", clientIp);
```

## 실제 로그 예시

### 1. 정상 요청 처리
```
# 요청 시작
2024-01-15 10:30:15.123 [http-nio-8080-exec-1] INFO [TID-1705314615123-A7B2C] TraceIdFilter - Request started - POST /api/sample/validation from 127.0.0.1 [TraceId: TID-1705314615123-A7B2C]

# 컨트롤러 메서드 시작
2024-01-15 10:30:15.125 [http-nio-8080-exec-1] INFO [TID-1705314615123-A7B2C] LoggingAspect - [TID-1705314615123-A7B2C] [SampleValidationController#processValidation] Starting method execution with parameters: [SampleRequestDTO(name=홍길동, email=hong@test.com, age=25)]

# 서비스 메서드 시작
2024-01-15 10:30:15.127 [http-nio-8080-exec-1] INFO [TID-1705314615123-A7B2C] LoggingAspect - [TID-1705314615123-A7B2C] [SampleValidationService#processValidData] Starting method execution with parameters: [SampleRequestDTO(name=홍길동, email=hong@test.com, age=25)]

# 서비스 메서드 완료
2024-01-15 10:30:15.145 [http-nio-8080-exec-1] INFO [TID-1705314615123-A7B2C] LoggingAspect - [TID-1705314615123-A7B2C] [SampleValidationService#processValidData] Method completed successfully in 18ms with result: SampleResponseDTO(success=true, data=이름: 홍길동, 이메일: hong@test.com, 나이: 25, message=null, traceId=TID-1705314615123-A7B2C)

# 컨트롤러 메서드 완료
2024-01-15 10:30:15.147 [http-nio-8080-exec-1] INFO [TID-1705314615123-A7B2C] LoggingAspect - [TID-1705314615123-A7B2C] [SampleValidationController#processValidation] Method completed successfully in 22ms with result: <200 OK OK,SampleResponseDTO(...),[]>

# 요청 완료
2024-01-15 10:30:15.148 [http-nio-8080-exec-1] INFO [TID-1705314615123-A7B2C] TraceIdFilter - Request completed - POST /api/sample/validation [25ms] [TraceId: TID-1705314615123-A7B2C]
```

### 2. Validation 실패
```
# 요청 시작
2024-01-15 10:35:20.456 [http-nio-8080-exec-2] INFO [TID-1705314920456-B8C3D] TraceIdFilter - Request started - POST /api/sample/validation from 127.0.0.1 [TraceId: TID-1705314920456-B8C3D]

# Validation 실패 로그
2024-01-15 10:35:20.458 [http-nio-8080-exec-2] WARN [TID-1705314920456-B8C3D] GlobalExceptionHandler - Validation exception occurred: Validation failed for argument [0] in public org.springframework.http.ResponseEntity...

# 요청 완료 (에러)
2024-01-15 10:35:20.460 [http-nio-8080-exec-2] INFO [TID-1705314920456-B8C3D] TraceIdFilter - Request completed - POST /api/sample/validation [4ms] [TraceId: TID-1705314920456-B8C3D]
```

### 3. BusinessException 발생
```
# 서비스 메서드 시작
2024-01-15 10:40:30.789 [http-nio-8080-exec-3] INFO [TID-1705315230789-C9D4E] LoggingAspect - [TID-1705315230789-C9D4E] [SampleValidationService#throwBusinessException] Starting method execution with parameters: [notfound]

# BusinessException 발생 (WARN 레벨)
2024-01-15 10:40:30.792 [http-nio-8080-exec-3] WARN [TID-1705315230789-C9D4E] LoggingAspect - [TID-1705315230789-C9D4E] [SampleValidationService#throwBusinessException] Business exception occurred after 3ms: USER_NOT_FOUND - 사용자를 찾을 수 없습니다.

# 글로벌 예외 처리
2024-01-15 10:40:30.794 [http-nio-8080-exec-3] WARN [TID-1705315230789-C9D4E] GlobalExceptionHandler - Business exception occurred: 사용자를 찾을 수 없습니다.
```

### 4. 시스템 예외 발생
```
# 시스템 예외 발생 (ERROR 레벨)
2024-01-15 10:45:45.123 [http-nio-8080-exec-4] ERROR [TID-1705315545123-D0E5F] LoggingAspect - [TID-1705315545123-D0E5F] [SampleValidationService#throwNullPointerException] Unexpected exception occurred after 1ms: null
java.lang.NullPointerException: Cannot invoke "String.length()" because "nullString" is null
    at com.softlabs.aicontents.domain.sample.service.SampleValidationService.throwNullPointerException(SampleValidationService.java:42)
    ...

# 글로벌 예외 처리
2024-01-15 10:45:45.125 [http-nio-8080-exec-4] ERROR [TID-1705315545123-D0E5F] GlobalExceptionHandler - Unexpected exception occurred: Cannot invoke "String.length()" because "nullString" is null
```

## API 응답 예시

### 1. 정상 응답
```json
{
  "success": true,
  "data": "이름: 홍길동, 이메일: hong@test.com, 나이: 25",
  "message": null,
  "traceId": "TID-1705314615123-A7B2C"
}
```

### 2. Validation 실패 응답
```json
{
  "success": false,
  "errorCode": "INVALID_INPUT",
  "message": "입력값 검증 실패: 이름은 필수입니다., 올바른 이메일 형식이 아닙니다., 나이는 1 이상이어야 합니다.",
  "path": "/api/sample/validation",
  "timestamp": "2024-01-15T10:35:20.458",
  "traceId": "TID-1705314920456-B8C3D"
}
```

### 3. BusinessException 응답
```json
{
  "success": false,
  "errorCode": "USER_NOT_FOUND",
  "message": "사용자를 찾을 수 없습니다.",
  "path": "/api/sample/business-exception/notfound",
  "timestamp": "2024-01-15T10:40:30.794",
  "traceId": "TID-1705315230789-C9D4E"
}
```

### 4. 시스템 예외 응답
```json
{
  "success": false,
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "서버 내부 오류가 발생했습니다.",
  "path": "/api/sample/server-exception",
  "timestamp": "2024-01-15T10:45:45.125",
  "traceId": "TID-1705315545123-D0E5F"
}
```

## 테스트용 API 엔드포인트

프로젝트에 포함된 테스트용 API들:

```bash
# 1. 정상 처리 테스트
curl -X POST http://localhost:8080/api/sample/validation \
  -H "Content-Type: application/json" \
  -H "X-Trace-Id: TEST-001" \
  -d '{"name":"홍길동","email":"hong@test.com","age":25}'

# 2. Validation 실패 테스트
curl -X POST http://localhost:8080/api/sample/validation \
  -H "Content-Type: application/json" \
  -d '{"name":"","email":"invalid-email","age":-1}'

# 3. BusinessException 테스트
curl -X GET http://localhost:8080/api/sample/business-exception/notfound

# 4. 시스템 예외 테스트
curl -X GET http://localhost:8080/api/sample/server-exception

# 5. NullPointerException 테스트
curl -X GET http://localhost:8080/api/sample/null-exception
```

## 트러블슈팅

### 1. TraceId가 로그에 나타나지 않는 경우
- **원인**: TraceIdFilter가 제대로 동작하지 않음
- **해결**: FilterConfig 설정 확인
- **확인 방법**: `/actuator/health` 같은 제외 경로가 아닌지 확인

### 2. @Loggable이 동작하지 않는 경우
- **원인**: AOP Proxy 생성 실패 또는 Spring 관리 Bean이 아님
- **해결**:
  - 클래스에 `@Service`, `@Component` 등의 어노테이션 확인
  - `@EnableAspectJAutoProxy` 설정 확인
  - private 메서드는 AOP 적용 불가

### 3. 로그가 너무 많이 출력되는 경우
- **해결**: 특정 메서드에만 `@Loggable` 적용하거나 로그 레벨 조정
```yaml
logging:
  level:
    com.softlabs.aicontents.common.aspect.LoggingAspect: WARN
```

### 4. MDC 값이 다른 스레드에서 사라지는 경우
- **원인**: 비동기 처리 시 MDC 컨텍스트가 전파되지 않음
- **해결**: `@Async` 사용 시 별도의 MDC 전파 처리 필요

### 5. 외부 API 호출 시 TraceId 전파
```java
@Service
public class ExternalApiService {

    public void callExternalApi() {
        String traceId = TraceIdUtil.getTraceId();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Trace-Id", traceId);

        // RestTemplate 또는 WebClient 사용 시 헤더 포함
    }
}
```

## 베스트 프랙티스

### 1. 로깅 사용 권장사항
- **Service 계층**: 비즈니스 로직이 포함된 주요 메서드에 적용
- **Controller 계층**: 필요시에만 적용 (Filter에서 이미 요청/응답 로깅)
- **Repository 계층**: 성능상 민감한 부분에만 선택적 적용

### 2. 예외 처리 권장사항
- **비즈니스 예외**: 반드시 BusinessException 사용
- **예상 가능한 오류**: ErrorCode 미리 정의
- **디버깅 정보**: 원인 예외(cause) 함께 전달

### 3. TraceId 활용 권장사항
- **마이크로서비스 간 호출**: 반드시 TraceId 전파
- **배치 작업**: 별도의 TraceId 생성하여 추적
- **로그 분석**: TraceId로 특정 요청의 전체 흐름 추적