# Final-7team-BE

# 로깅·예외처리·Validation·TraceId·AOP 기능 가이드

## 로깅 기능 (@Loggable)

### 사용법
메서드나 클래스에 `@Loggable` 어노테이션을 추가하면 자동으로 로깅됩니다.

```java
@Service
@Loggable  // 클래스 전체에 적용
public class UserService {

    @Loggable  // 특정 메서드에만 적용
    public User createUser(CreateUserRequest request) {
        // 비즈니스 로직
        return user;
    }
}
```

### 로그 출력 내용
- **메서드 시작**: 파라미터 정보와 함께
- **메서드 완료**: 실행시간과 결과값 포함
- **예외 발생**: BusinessException(WARN), 기타 예외(ERROR)

### 로그 예시
```
2024-01-15 10:30:15 [http-nio-8080-exec-1] INFO  [TR001] LoggingAspect - [TR001] [UserService#createUser] Starting method execution with parameters: [CreateUserRequest(name=홍길동, email=hong@test.com)]
2024-01-15 10:30:16 [http-nio-8080-exec-1] INFO  [TR001] LoggingAspect - [TR001] [UserService#createUser] Method completed successfully in 150ms with result: User(id=1, name=홍길동)
```

## 예외 처리

### BusinessException 사용법
비즈니스 로직 예외는 `BusinessException`을 사용합니다.

```java
// 기본 사용법
throw new BusinessException(ErrorCode.USER_NOT_FOUND);

// 커스텀 메시지 사용
throw new BusinessException(ErrorCode.INVALID_INPUT, "이메일 형식이 잘못되었습니다.");

// 원인 예외와 함께 사용
try {
    externalApi.call();
} catch (IOException e) {
    throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, e);
}
```

### 에러 응답 형식
모든 예외는 일관된 JSON 형식으로 응답됩니다.

```json
{
  "success": false,
  "errorCode": "USER_NOT_FOUND",
  "message": "사용자를 찾을 수 없습니다.",
  "path": "/api/users/123",
  "timestamp": "2024-01-15T10:30:15",
  "traceId": "TR001"
}
```

### 자동 처리되는 예외들
- **Validation 실패**: `@Valid` 검증 실패 시 자동 처리
- **파라미터 누락**: 필수 파라미터 누락 시
- **타입 불일치**: 파라미터 타입 변환 실패 시
- **메서드 미지원**: 잘못된 HTTP 메서드 사용 시

## Validation

### 기본 사용법
DTO에 Bean Validation 어노테이션을 추가하고, Controller에서 `@Valid`를 사용합니다.

```java
// DTO 클래스
public class CreateUserRequest {
    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 50, message = "이름은 2-50자 사이여야 합니다.")
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotNull(message = "나이는 필수입니다.")
    @Min(value = 1, message = "나이는 1 이상이어야 합니다.")
    @Max(value = 150, message = "나이는 150 이하여야 합니다.")
    private Integer age;
}

// Controller
@PostMapping("/users")
public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest request) {
    // 검증 통과 시에만 실행됨
    return ResponseEntity.ok(userService.createUser(request));
}
```

### 검증 실패 응답 예시
```json
{
  "success": false,
  "errorCode": "INVALID_INPUT",
  "message": "입력값 검증 실패: 이름은 필수입니다., 올바른 이메일 형식이 아닙니다.",
  "path": "/api/users",
  "timestamp": "2024-01-15T10:30:15",
  "traceId": "TR001"
}
```

## TraceId 추적

### 자동 생성
모든 HTTP 요청에 대해 자동으로 TraceId가 생성됩니다.

### 외부에서 TraceId 전달
클라이언트에서 `X-Trace-Id` 헤더로 TraceId를 전달할 수 있습니다.

```bash
curl -H "X-Trace-Id: custom-trace-123" http://localhost:8080/api/users
```

### 코드에서 TraceId 사용
```java
@Service
public class UserService {

    public void someMethod() {
        String traceId = TraceIdUtil.getTraceId();
        log.info("현재 TraceId: {}", traceId);

        // 외부 API 호출 시 TraceId 전달
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Trace-Id", traceId);
    }
}
```

### TraceId 로그 확인
모든 로그에는 TraceId가 포함됩니다.

```
2024-01-15 10:30:15 [http-nio-8080-exec-1] INFO [TR001] TraceIdFilter - Request started - POST /api/users from 127.0.0.1 [TraceId: TR001]
2024-01-15 10:30:16 [http-nio-8080-exec-1] INFO [TR001] TraceIdFilter - Request completed - POST /api/users [150ms] [TraceId: TR001]
```

## AOP 설정

### LoggingAspect 동작 방식
- `@Loggable` 어노테이션이 있는 메서드/클래스 대상
- 메서드 실행 전후로 로그 출력
- 예외 발생 시 BusinessException과 일반 Exception 구분 처리

### 적용 범위
```java
@Around("@annotation(loggable) || @within(loggable)")
```
- `@annotation`: 메서드에 직접 붙은 @Loggable
- `@within`: 클래스에 붙은 @Loggable (클래스 내 모든 메서드 적용)

## 실제 사용 예시

### 1. 서비스 클래스 작성
```java
@Service
@Loggable
public class UserService {

    public User createUser(CreateUserRequest request) {
        // 비즈니스 로직 검증
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 존재하는 이메일입니다.");
        }

        // 사용자 생성
        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .age(request.getAge())
            .build();

        return userRepository.save(user);
    }
}
```

### 2. 컨트롤러 작성
```java
@RestController
@RequestMapping("/api/users")
@Loggable
public class UserController {

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        return ResponseEntity.ok(user);
    }
}
```

### 3. API 호출 및 로그 확인
```bash
# 정상 요청
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "X-Trace-Id: TEST-001" \
  -d '{"name":"홍길동","email":"hong@test.com","age":25}'
```

**로그 출력:**
```
2024-01-15 10:30:15 [http-nio-8080-exec-1] INFO [TEST-001] TraceIdFilter - Request started - POST /api/users from 127.0.0.1 [TraceId: TEST-001]
2024-01-15 10:30:15 [http-nio-8080-exec-1] INFO [TEST-001] LoggingAspect - [TEST-001] [UserController#createUser] Starting method execution with parameters: [CreateUserRequest(name=홍길동, email=hong@test.com, age=25)]
2024-01-15 10:30:15 [http-nio-8080-exec-1] INFO [TEST-001] LoggingAspect - [TEST-001] [UserService#createUser] Starting method execution with parameters: [CreateUserRequest(name=홍길동, email=hong@test.com, age=25)]
2024-01-15 10:30:16 [http-nio-8080-exec-1] INFO [TEST-001] LoggingAspect - [TEST-001] [UserService#createUser] Method completed successfully in 150ms with result: User(id=1, name=홍길동)
2024-01-15 10:30:16 [http-nio-8080-exec-1] INFO [TEST-001] LoggingAspect - [TEST-001] [UserController#createUser] Method completed successfully in 155ms with result: <200 OK OK,User(id=1, name=홍길동),[]>
2024-01-15 10:30:16 [http-nio-8080-exec-1] INFO [TEST-001] TraceIdFilter - Request completed - POST /api/users [160ms] [TraceId: TEST-001]
```

## 팀 사용 가이드

### 1. 새로운 API 개발 시
- DTO에 Validation 어노테이션 추가
- Service 클래스에 `@Loggable` 추가
- 비즈니스 예외는 `BusinessException` 사용

### 2. 디버깅 시
- TraceId로 특정 요청의 전체 로그 추적
- 메서드 실행시간으로 성능 병목 지점 파악

### 3. 외부 API 연동 시
- TraceId를 헤더에 포함하여 전달
- 연동 실패 시 BusinessException으로 래핑