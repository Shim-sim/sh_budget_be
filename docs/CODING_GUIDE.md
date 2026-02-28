# SH Budget - 코딩 가이드

## 개발자 역할 정의
**3~5년차 Spring 백엔드 개발자**로서 다음 원칙을 준수합니다:
- 실무에서 검증된 방법론 사용
- 과도한 추상화보다는 명확하고 유지보수 가능한 코드
- 문서화와 테스트를 통한 코드 품질 보장

---

## 핵심 원칙

### 1. API Spec 먼저 작성
기능 구현 전 반드시 API 명세를 먼저 작성합니다.

#### API 명세 작성 순서
1. **엔드포인트 정의**: URL, HTTP Method
2. **Request/Response 정의**: DTO 구조
3. **비즈니스 요구사항 정리**: 유효성 검증, 예외 케이스
4. **API 문서 작성**: Swagger 어노테이션 추가
5. **구현 시작**

#### 예시
```java
/**
 * 회원 가입 API
 *
 * POST /api/members
 *
 * Request:
 * {
 *   "email": "user@example.com",
 *   "nickname": "홍길동"
 * }
 *
 * Response (201):
 * {
 *   "status": 201,
 *   "message": "Created",
 *   "data": {
 *     "id": 1,
 *     "email": "user@example.com",
 *     "nickname": "홍길동"
 *   }
 * }
 *
 * Exception:
 * - 409: 이미 존재하는 이메일
 * - 400: 유효하지 않은 이메일 형식
 */
@PostMapping
@Operation(summary = "회원 가입", description = "새로운 회원을 등록합니다.")
public ApiResult<MemberResponse> createMember(@Valid @RequestBody MemberCreateRequest request) {
    // ...
}
```

---

### 2. 도메인 중심 설계

#### 도메인별 패키지 구조
```
domain/
├── member/
│   ├── controller/      # API 진입점
│   ├── service/         # 비즈니스 로직
│   ├── repository/      # 데이터 접근
│   ├── entity/          # 도메인 엔티티
│   └── dto/             # 요청/응답 DTO
│       ├── request/     # 요청 DTO
│       └── response/    # 응답 DTO
```

#### 도메인 간 의존성 규칙
- **허용**: Service → Repository, Service → Service (다른 도메인)
- **금지**: Repository → Service, Entity 직접 노출

#### 예시
```java
// ✅ Good: Service가 다른 도메인 Service를 주입받음
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final MemberService memberService;  // 다른 도메인 Service
    private final CategoryService categoryService;
}

// ❌ Bad: Entity 직접 반환
@GetMapping("/{id}")
public Member getMember(@PathVariable Long id) {
    return memberRepository.findById(id).orElseThrow();
}

// ✅ Good: DTO 변환 후 반환
@GetMapping("/{id}")
public ApiResult<MemberResponse> getMember(@PathVariable Long id) {
    Member member = memberService.findById(id);
    return ApiResult.success(MemberResponse.from(member));
}
```

---

### 3. 과도한 추상화 금지

#### 불필요한 인터페이스 지양
Service Layer에 무분별한 인터페이스 생성을 지양합니다.

```java
// ❌ Bad: 구현체가 1개뿐인데 인터페이스 생성
public interface MemberService {
    MemberResponse createMember(MemberCreateRequest request);
}

@Service
public class MemberServiceImpl implements MemberService {
    // ...
}

// ✅ Good: 구체 클래스 직접 사용
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberResponse createMember(MemberCreateRequest request) {
        // ...
    }
}
```

#### 인터페이스가 필요한 경우
- 여러 구현체가 명확히 필요한 경우 (예: 결제 수단, 알림 채널)
- 테스트를 위한 Mock 대상이 복잡한 외부 시스템인 경우

---

### 4. 실무적인 선택 우선

#### Lombok 적극 활용
```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "members")
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Builder
    public Member(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
    }
}
```

#### 정적 팩토리 메서드 활용
```java
// DTO 변환
public record MemberResponse(
    Long id,
    String email,
    String nickname,
    LocalDateTime createdAt
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
            member.getId(),
            member.getEmail(),
            member.getNickname(),
            member.getCreatedAt()
        );
    }
}
```

#### Optional 실용적으로 사용
```java
// ✅ Good: orElseThrow로 예외 처리
public Member findById(Long id) {
    return memberRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
}

// ❌ Bad: Optional을 파라미터로 사용
public void updateMember(Optional<Long> id) { ... }

// ❌ Bad: Optional 중첩
public Optional<Optional<Member>> findMember() { ... }
```

---

### 5. 테스트 코드 필수 작성

#### 테스트 전략
- **단위 테스트**: Service 로직만 테스트 (Mockito 사용)

#### Service 단위 테스트
```java
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("회원 가입 성공")
    void createMember_Success() {
        // given
        MemberCreateRequest request = new MemberCreateRequest(
            "test@example.com",
            "테스터"
        );
        Member member = Member.builder()
            .email(request.email())
            .nickname(request.nickname())
            .build();

        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // when
        MemberResponse response = memberService.createMember(request);

        // then
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.nickname()).isEqualTo("테스터");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("이메일 중복으로 회원 가입 실패")
    void createMember_DuplicateEmail() {
        // given
        MemberCreateRequest request = new MemberCreateRequest(
            "test@example.com",
            "테스터"
        );
        when(memberRepository.existsByEmail(anyString())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.createMember(request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);
    }
}
```

**주의사항**:
- Repository와 Controller는 테스트하지 않음
- Service 레이어의 비즈니스 로직만 Mockito로 단위 테스트
- 모든 의존성은 @Mock으로 처리

---

## 코드 스타일

### Naming Convention

#### 클래스명
- **Entity**: `Member`, `Transaction`, `Category`
- **DTO**: `MemberCreateRequest`, `MemberResponse`, `TransactionUpdateRequest`
- **Service**: `MemberService`, `TransactionService`
- **Repository**: `MemberRepository`, `TransactionRepository`
- **Controller**: `MemberController`, `TransactionController`
- **Exception**: `CustomException`

#### 메서드명
- **조회**: `findById`, `findByEmail`, `findAll`
- **생성**: `create`, `save`
- **수정**: `update`, `modify`
- **삭제**: `delete`, `remove`
- **존재 확인**: `existsById`, `existsByEmail`
- **개수**: `count`, `countByStatus`

#### 변수명
- **카멜 케이스**: `memberId`, `transactionList`, `categoryName`
- **상수**: `MAX_LENGTH`, `DEFAULT_PAGE_SIZE`

### 파일 구성 순서
```java
@Service
@RequiredArgsConstructor
public class MemberService {

    // 1. 의존성 주입 (final 필드)
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 2. 상수
    private static final int MAX_NICKNAME_LENGTH = 20;

    // 3. public 메서드
    public MemberResponse createMember(MemberCreateRequest request) {
        // ...
    }

    public MemberResponse findById(Long id) {
        // ...
    }

    // 4. private 메서드
    private void validateDuplicateEmail(String email) {
        // ...
    }
}
```

---

## Validation

### DTO 유효성 검증
```java
public record MemberCreateRequest(
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String email,

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
    String nickname
) {}
```

### Service Layer 검증
```java
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberResponse createMember(MemberCreateRequest request) {
        // 비즈니스 검증
        if (memberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        Member member = Member.builder()
            .email(request.email())
            .nickname(request.nickname())
            .build();

        Member savedMember = memberRepository.save(member);
        return MemberResponse.from(savedMember);
    }
}
```

---

## 예외 처리

### CustomException 사용
```java
// Service에서 예외 발생
public Member findById(Long id) {
    return memberRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
}

// GlobalExceptionHandler가 자동으로 처리
@ExceptionHandler(CustomException.class)
public ApiResult<Void> handleCustomException(CustomException e) {
    ErrorCode errorCode = e.getErrorCode();
    return ApiResult.error(errorCode.getStatus(), errorCode.getMessage());
}
```

### ErrorCode 추가 시
```java
public enum ErrorCode {
    // 새로운 에러 코드 추가
    TRANSACTION_AMOUNT_INVALID(400, "거래 금액은 0보다 커야 합니다."),
    CATEGORY_IN_USE(409, "사용 중인 카테고리는 삭제할 수 없습니다.");

    // ...
}
```

---

## 트랜잭션 관리

### Service Layer에서 @Transactional 사용
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 기본은 읽기 전용
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final MemberService memberService;

    // 조회 메서드 (readOnly = true 적용됨)
    public TransactionResponse findById(Long id) {
        // ...
    }

    // 쓰기 메서드 (readOnly = false 명시)
    @Transactional
    public TransactionResponse createTransaction(TransactionCreateRequest request) {
        // ...
    }

    @Transactional
    public void deleteTransaction(Long id) {
        // ...
    }
}
```

---

## Swagger 문서화

### Controller 어노테이션
```java
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 관리 API")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @Operation(summary = "회원 가입", description = "새로운 회원을 등록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원 가입 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 입력"),
        @ApiResponse(responseCode = "409", description = "이메일 중복")
    })
    public ApiResult<MemberResponse> createMember(
        @Valid @RequestBody MemberCreateRequest request
    ) {
        MemberResponse response = memberService.createMember(request);
        return ApiResult.created(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "회원 조회", description = "ID로 회원을 조회합니다.")
    public ApiResult<MemberResponse> getMember(
        @Parameter(description = "회원 ID", required = true)
        @PathVariable Long id
    ) {
        MemberResponse response = memberService.findById(id);
        return ApiResult.success(response);
    }
}
```

---

## 체크리스트

### 새 기능 개발 시
- [ ] API 명세 작성
- [ ] Request/Response DTO 정의
- [ ] Controller 작성 (Swagger 어노테이션 포함)
- [ ] Service 비즈니스 로직 작성
- [ ] Repository 메서드 정의
- [ ] 단위 테스트 작성 (Service만)
- [ ] 예외 케이스 처리
- [ ] 코드 리뷰 요청

### 테스트 작성 시
- [ ] Service 레이어만 테스트 (Repository, Controller 제외)
- [ ] Mockito로 의존성 Mock 처리
- [ ] 성공 케이스 테스트
- [ ] 실패 케이스 테스트 (예외 상황)
- [ ] 경계값 테스트
- [ ] Given-When-Then 구조 준수
- [ ] @DisplayName으로 테스트 목적 명확히 작성

---

## 참고 자료

### Spring Boot 공식 문서
- https://spring.io/projects/spring-boot

### 테스트 작성 참고
- AssertJ: https://assertj.github.io/doc/
- Mockito: https://site.mockito.org/

### API 문서화
- SpringDoc OpenAPI: https://springdoc.org/

---

## 실무 코딩 패턴 (추가 규칙)

### 1. Entity는 정적 팩토리 메서드로 생성

Builder 패턴 대신 명확한 의도를 담은 정적 팩토리 메서드를 사용합니다.

#### ❌ Bad: Builder 패턴 직접 노출
```java
@Entity
public class Member extends BaseEntity {
    // ...
    
    @Builder
    public Member(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
    }
}

// 사용
Member member = Member.builder()
    .email("user@example.com")
    .nickname("홍길동")
    .build();
```

#### ✅ Good: 정적 팩토리 메서드 (@SuperBuilder 패턴 활용)
```java
@Entity
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Asset extends BaseEntity {
    // ...

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long balance;

    @Column(name = "owner_member_id")
    private Long ownerMemberId;

    // private 생성자 불필요 - @SuperBuilder가 자동 생성

    public static Asset create(Long bookId, String name, Long balance) {
        return Asset.builder()
                .bookId(bookId)
                .name(name)
                .balance(balance)
                .build();
    }

    public static Asset create(Long bookId, String name, Long balance, Long ownerMemberId) {
        return Asset.builder()
                .bookId(bookId)
                .name(name)
                .balance(balance)
                .ownerMemberId(ownerMemberId)
                .build();
    }
}

// 사용
Asset asset = Asset.create(1L, "신한은행", 1000000L);
```

**장점**:
- 생성 의도가 명확 (create, of, from 등)
- 필수/선택 파라미터 구분 가능
- 유효성 검증 로직 추가 용이
- 불변성 보장에 유리
- @SuperBuilder로 BaseEntity 필드까지 함께 빌드 가능
- private 생성자 불필요 (코드 간결화)

---

### 2. Swagger ApiResponse 어노테이션 사용

프로젝트에서는 `ApiResult` 클래스를 사용하므로 Swagger의 `@ApiResponse`와 이름 충돌이 없습니다. 따라서 **일반 import 방식**을 사용합니다.

#### ✅ Good: 일반 import 사용
```java
import com.shbudget.global.common.ApiResult;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@ApiResponses({
    @ApiResponse(responseCode = "200", description = "성공"),
    @ApiResponse(responseCode = "404", description = "실패")
})
public ResponseEntity<ApiResult<MemberResponse>> getMember(@PathVariable Long id) {
    // ...
}
```

**장점**:
- 코드 간결성
- 가독성 향상
- 이름 충돌 없음

---

### 3. ResponseStatus Enum으로 상태 코드 관리

하드코딩된 HTTP 상태 코드와 메시지를 Enum으로 중앙 관리합니다.

#### ResponseStatus Enum 정의
```java
@Getter
@RequiredArgsConstructor
public enum ResponseStatus {
    // 2xx Success
    SUCCESS(HttpStatus.OK, "Success"),
    CREATED(HttpStatus.CREATED, "Created"),

    // 4xx Client Error
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    CONFLICT(HttpStatus.CONFLICT, "중복된 데이터입니다."),

    // 5xx Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return httpStatus.value();
    }
}
```

#### ApiResult 사용
```java
public class ApiResult<T> {
    private final int status;
    private final String message;
    private final T data;

    private ApiResult(ResponseStatus responseStatus, T data) {
        this.status = responseStatus.getStatusCode();
        this.message = responseStatus.getMessage();
        this.data = data;
    }

    public static <T> ApiResult<T> of(ResponseStatus responseStatus, T data) {
        return new ApiResult<>(responseStatus, data);
    }
}
```

**장점**:
- 타입 안정성 확보
- 상태 코드 오타 방지
- 메시지 일관성 유지
- 중앙에서 응답 관리

---

### 4. ResponseEntity로 정확한 HTTP 상태 코드 반환

Controller에서는 `ResponseEntity`를 사용하여 실제 HTTP 상태 코드를 정확히 반환합니다.

#### ❌ Bad: @ResponseStatus 어노테이션 + ApiResult 반환
```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public ApiResult<MemberResponse> createMember(@Valid @RequestBody MemberCreateRequest request) {
    MemberResponse response = memberService.createMember(request);
    return ApiResult.created(response);
}
```
**문제점**: 예외 발생 시 @ResponseStatus가 무시됨

#### ✅ Good: ResponseEntity 사용
```java
@PostMapping
public ResponseEntity<ApiResult<MemberResponse>> createMember(
        @Valid @RequestBody MemberCreateRequest request
) {
    MemberResponse response = memberService.createMember(request);
    return ResponseEntity
            .status(ResponseStatus.CREATED.getHttpStatus())
            .body(ApiResult.of(ResponseStatus.CREATED, response));
}

@GetMapping("/{id}")
public ResponseEntity<ApiResult<MemberResponse>> getMember(@PathVariable Long id) {
    MemberResponse response = memberService.findById(id);
    return ResponseEntity.ok(ApiResult.of(ResponseStatus.SUCCESS, response));
}
```

**장점**:
- HTTP 상태 코드와 응답 body의 status 일치 보장
- Swagger 문서와 실제 응답 일치
- 예외 상황에서도 정확한 상태 코드 반환

#### GlobalExceptionHandler 예시
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResult<Void>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResult.error(errorCode.getStatus(), errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult().getFieldErrors().isEmpty()
                ? ResponseStatus.BAD_REQUEST.getMessage()
                : e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity
                .status(ResponseStatus.BAD_REQUEST.getHttpStatus())
                .body(ApiResult.of(ResponseStatus.BAD_REQUEST, message, null));
    }
}
```

---

## 체크리스트 업데이트

### 새 기능 개발 시
- [ ] API 명세 작성
- [ ] Request/Response DTO 정의
- [ ] **Entity는 정적 팩토리 메서드로 생성**
- [ ] **Swagger @ApiResponse 일반 import 사용**
- [ ] **ResponseStatus enum 사용**
- [ ] **Controller는 ResponseEntity로 정확한 HTTP 상태 반환**
- [ ] Service 비즈니스 로직 작성
- [ ] Repository 메서드 정의
- [ ] 단위 테스트 작성 (Service만)
- [ ] 예외 케이스 처리
- [ ] 코드 리뷰 요청

---

## 보안 (Security)

### XSS(Cross-Site Scripting) 방지

외부 입력값을 응답 메시지에 포함할 때는 반드시 HTML escape 처리를 합니다.

#### ❌ Bad: 외부 입력을 그대로 사용
```java
public static <T> ApiResult<T> error(int status, String message) {
    return new ApiResult<>(status, message, null);  // XSS 위험!
}
```

**문제점**: 
- 사용자가 `<script>alert('XSS')</script>` 같은 악의적인 입력을 validation error message에 포함시킬 수 있음
- 응답 JSON이 브라우저에서 렌더링될 때 스크립트 실행 가능

#### ✅ Good: HTML escape 처리
```java
import org.springframework.web.util.HtmlUtils;

public static <T> ApiResult<T> error(int status, String message) {
    // XSS 방지: message를 HTML escape 처리
    String safeMessage = message != null ? HtmlUtils.htmlEscape(message) : "오류가 발생했습니다.";
    return new ApiResult<>(status, safeMessage, null);
}

public static <T> ApiResult<T> of(ResponseStatus responseStatus, String customMessage, T data) {
    // XSS 방지: customMessage를 HTML escape 처리
    String safeMessage = customMessage != null ? HtmlUtils.htmlEscape(customMessage) : responseStatus.getMessage();
    return new ApiResult<>(responseStatus.getStatusCode(), safeMessage, data);
}
```

**적용 위치**:
- `ApiResult.error()` - 예외 메시지
- `ApiResult.of(status, customMessage, data)` - 커스텀 메시지
- `GlobalExceptionHandler` - validation error message

**HtmlUtils.htmlEscape()가 변환하는 문자**:
- `<` → `&lt;`
- `>` → `&gt;`
- `"` → `&quot;`
- `'` → `&#39;`
- `&` → `&amp;`

---

