# SH Budget - Backend API

공유 가계부 서비스의 백엔드 REST API 서버입니다.

## 기술 스택

| 구분 | 기술 |
|------|------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 4.0.2 |
| **Database** | MySQL 8.0 |
| **ORM** | Spring Data JPA |
| **인증** | JWT (Access + Refresh Token) |
| **문서화** | SpringDoc OpenAPI (Swagger UI) |
| **빌드** | Gradle |
| **배포** | Docker |

## 프로젝트 구조

```
src/main/java/com/shbudget/
├── global/
│   ├── auth/           # @CurrentMemberId 어노테이션 & 리졸버
│   ├── common/         # ApiResult, BaseEntity, ResponseStatus
│   ├── config/         # Security, JWT, Swagger, VAPID 설정
│   └── exception/      # 전역 예외 처리 (ErrorCode, GlobalExceptionHandler)
│
└── domain/
    ├── auth/           # 회원가입, 로그인, 토큰 갱신
    ├── member/         # 회원 관리
    ├── book/           # 가계부(장부) 관리 & 멤버 초대
    ├── category/       # 카테고리 관리
    ├── asset/          # 자산 관리
    ├── transaction/    # 거래 내역 (수입/지출/이체)
    ├── recurring/      # 반복 거래 스케줄링
    ├── statistics/     # 월별 통계, 카테고리별, 멤버별 기여도
    └── pushsubscription/  # 웹 푸시 알림
```

## API 엔드포인트

### 인증 (`/api/auth`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/register` | 회원가입 |
| POST | `/login` | 로그인 (JWT 발급) |
| POST | `/refresh` | Access Token 갱신 |

### 가계부 (`/api/books`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/my` | 내 가계부 조회 |
| GET | `/my/all` | 내 가계부 전체 목록 |
| PUT | `/{id}` | 가계부 이름 수정 |
| POST | `/{id}/invite-code` | 초대 코드 재발급 |
| DELETE | `/{id}` | 가계부 삭제 |
| POST | `/join` | 초대 코드로 가계부 참여 |
| GET | `/{id}/members` | 멤버 목록 조회 |
| DELETE | `/{id}/members/{memberId}` | 멤버 탈퇴/제거 |

### 거래 내역 (`/api/transactions`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/` | 거래 생성 |
| GET | `/` | 거래 목록 (월별, 타입 필터) |
| GET | `/{id}` | 거래 상세 |
| PUT | `/{id}` | 거래 수정 |
| DELETE | `/{id}` | 거래 삭제 |

### 자산 (`/api/assets`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/` | 자산 생성 |
| GET | `/` | 자산 목록 |
| PUT | `/{id}` | 자산 수정 |
| DELETE | `/{id}` | 자산 삭제 |
| GET | `/total` | 총 자산 요약 |

### 카테고리 (`/api/categories`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/` | 카테고리 생성 |
| GET | `/` | 카테고리 목록 |
| PUT | `/{id}` | 카테고리 수정 |
| DELETE | `/{id}` | 카테고리 삭제 |

### 통계 (`/api/statistics`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/monthly-summary` | 월별 수입/지출 요약 |
| GET | `/category` | 카테고리별 지출 통계 |
| GET | `/member-contribution` | 멤버별 기여도 통계 |

### 반복 거래 (`/api/recurring-transactions`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/` | 반복 거래 생성 |
| GET | `/` | 반복 거래 목록 |
| DELETE | `/{id}` | 반복 거래 삭제 |

### 푸시 알림 (`/api/push`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/vapid-key` | VAPID 공개키 조회 |
| POST | `/subscribe` | 푸시 구독 등록 |
| POST | `/unsubscribe` | 푸시 구독 해제 |

## ERD

```
┌──────────────┐       ┌──────────────────┐       ┌──────────────┐
│   Member     │       │   BookMember      │       │    Book      │
├──────────────┤       ├──────────────────┤       ├──────────────┤
│ id           │──┐    │ id               │    ┌──│ id           │
│ email        │  │    │ book_id       ───│────┘  │ name         │
│ password     │  └────│ member_id     ───│       │ invite_code  │
│ nickname     │       │ role (OWNER/     │       │ owner_id     │
│ profileImage │       │       MEMBER)    │       └──────────────┘
└──────┬───────┘       │ joined_at        │              │
       │               └──────────────────┘              │
       │                                                 │
       │    ┌──────────────────┐    ┌──────────────────┐ │
       │    │   Asset           │    │   Category       │ │
       │    ├──────────────────┤    ├──────────────────┤ │
       │    │ id               │    │ id               │ │
       └────│ owner_member_id  │    │ book_id       ───│─┤
            │ book_id       ───│────│ name             │ │
            │ name             │    │ color            │ │
            │ balance          │    │ icon             │ │
            └────────┬─────────┘    └────────┬─────────┘ │
                     │                       │           │
                     │    ┌──────────────────┐           │
                     │    │  Transaction      │           │
                     │    ├──────────────────┤           │
                     │    │ id               │           │
                     └────│ asset_id         │           │
                     ┌────│ from_asset_id    │           │
                     └────│ to_asset_id      │           │
                          │ category_id   ───│───────────┘
                          │ book_id          │
                          │ type (INCOME/    │
                          │   EXPENSE/       │
                          │   TRANSFER)      │
                          │ amount           │
                          │ date             │
                          │ memo             │
                          │ created_by       │
                          └──────────────────┘

┌──────────────────────────┐    ┌──────────────────────────┐
│  RecurringTransaction     │    │  PushSubscription         │
├──────────────────────────┤    ├──────────────────────────┤
│ id                       │    │ id                       │
│ book_id                  │    │ member_id                │
│ type                     │    │ endpoint                 │
│ amount                   │    │ p256dh                   │
│ day_of_month             │    │ auth                     │
│ asset_id / from / to     │    └──────────────────────────┘
│ category_id              │
│ created_by               │
│ active                   │
└──────────────────────────┘
```

## 주요 플로우

### 1. 인증 플로우

```
┌────────┐                    ┌────────────┐                  ┌──────────┐
│ Client │                    │ AuthController│                │ Database │
└───┬────┘                    └─────┬──────┘                  └────┬─────┘
    │  POST /register               │                              │
    │  (email, password, nickname)  │                              │
    │──────────────────────────────>│                              │
    │                               │  이메일 중복 확인             │
    │                               │─────────────────────────────>│
    │                               │  비밀번호 암호화 후 저장      │
    │                               │─────────────────────────────>│
    │                               │  기본 가계부 자동 생성        │
    │                               │─────────────────────────────>│
    │   { accessToken, refreshToken }│                              │
    │<──────────────────────────────│                              │
    │                               │                              │
    │  POST /login                  │                              │
    │  (email, password)            │                              │
    │──────────────────────────────>│                              │
    │                               │  비밀번호 검증               │
    │                               │─────────────────────────────>│
    │   { accessToken, refreshToken }│                              │
    │<──────────────────────────────│                              │
    │                               │                              │
    │  POST /refresh                │                              │
    │  (refreshToken)               │                              │
    │──────────────────────────────>│                              │
    │                               │  토큰 유효성 검증            │
    │   { new accessToken }         │                              │
    │<──────────────────────────────│                              │
```

### 2. 거래 생성 플로우

```
┌────────┐           ┌───────────────────┐          ┌─────────────┐       ┌────────┐
│ Client │           │TransactionController│          │TransactionSvc│       │Database│
└───┬────┘           └────────┬──────────┘          └──────┬──────┘       └───┬────┘
    │ POST /transactions      │                            │                  │
    │ {type, amount, assetId, │                            │                  │
    │  categoryId, date, memo}│                            │                  │
    │────────────────────────>│                            │                  │
    │                         │  create(dto, memberId)     │                  │
    │                         │───────────────────────────>│                  │
    │                         │                            │                  │
    │                         │           ┌────────────────┤                  │
    │                         │           │ type별 분기처리 │                  │
    │                         │           └────────────────┤                  │
    │                         │                            │                  │
    │                         │   [INCOME]                 │                  │
    │                         │   asset.balance += amount  │                  │
    │                         │                            │─────────────────>│
    │                         │                            │                  │
    │                         │   [EXPENSE]                │                  │
    │                         │   asset.balance -= amount  │                  │
    │                         │                            │─────────────────>│
    │                         │                            │                  │
    │                         │   [TRANSFER]               │                  │
    │                         │   fromAsset.balance -= amt │                  │
    │                         │   toAsset.balance += amt   │                  │
    │                         │                            │─────────────────>│
    │                         │                            │                  │
    │                         │   거래 내역 저장            │                  │
    │                         │                            │─────────────────>│
    │   { transaction }       │                            │                  │
    │<────────────────────────│                            │                  │
```

### 3. 가계부 공유 플로우

```
┌──────────┐         ┌──────────┐         ┌────────────┐         ┌──────────┐
│ 가계부    │         │  서버     │         │  서버       │         │ 참여자    │
│ 소유자    │         │ (초대코드) │         │ (참여처리)  │         │          │
└────┬─────┘         └────┬─────┘         └─────┬──────┘         └────┬─────┘
     │                    │                     │                     │
     │ POST /{id}/        │                     │                     │
     │   invite-code      │                     │                     │
     │───────────────────>│                     │                     │
     │                    │                     │                     │
     │ { inviteCode:      │                     │                     │
     │   "ABC123" }       │                     │                     │
     │<───────────────────│                     │                     │
     │                    │                     │                     │
     │  초대코드를 참여자에게 공유 (카톡 등)     │                     │
     │──────────────────────────────────────────────────────────────>│
     │                    │                     │                     │
     │                    │                     │  POST /books/join   │
     │                    │                     │  { code: "ABC123" } │
     │                    │                     │<────────────────────│
     │                    │                     │                     │
     │                    │                     │  코드 유효성 검증    │
     │                    │                     │  BookMember 생성    │
     │                    │                     │  (role: MEMBER)     │
     │                    │                     │                     │
     │                    │                     │  { book 정보 }      │
     │                    │                     │────────────────────>│
     │                    │                     │                     │
     │    이제 같은 가계부의 거래/자산/통계를 공유합니다              │
```

### 4. 반복 거래 스케줄링 플로우

```
┌──────────────────┐              ┌───────────────────────┐       ┌────────┐
│    Scheduler     │              │RecurringTransactionSvc │       │Database│
│  (매일 자정 실행) │              └───────────┬───────────┘       └───┬────┘
└────────┬─────────┘                          │                      │
         │                                    │                      │
         │  오늘 날짜의 day_of_month와        │                      │
         │  일치하는 반복 거래 조회            │                      │
         │───────────────────────────────────>│                      │
         │                                    │─────────────────────>│
         │                                    │  active=true &       │
         │                                    │  dayOfMonth=today    │
         │                                    │<─────────────────────│
         │                                    │                      │
         │  각 반복 거래에 대해:               │                      │
         │  ┌─────────────────────────┐       │                      │
         │  │ Transaction 자동 생성    │       │                      │
         │  │ 자산 잔액 업데이트       │       │                      │
         │  │ 푸시 알림 발송           │       │                      │
         │  └─────────────────────────┘       │─────────────────────>│
         │                                    │                      │
```

## 로컬 개발 환경 설정

### 1. MySQL (Docker)

```bash
docker-compose up -d
```

MySQL이 `localhost:3307`에서 실행됩니다.

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

서버가 `http://localhost:8080`에서 실행됩니다.

### 3. API 문서 확인

Swagger UI: `http://localhost:8080/swagger-ui.html`

## 프로필 설정

| 프로필 | 용도 | DB |
|--------|------|----|
| `dev` | 로컬 개발 | MySQL (localhost:3307) |
| `test` | 테스트 | H2 In-Memory |
| `prod` | 운영 | MySQL (환경변수) |

## Docker 빌드 & 배포

```bash
# 이미지 빌드
docker build -t sh-budget-be .

# 컨테이너 실행
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:mysql://host:3306/sh_budget \
  -e DB_USERNAME=user \
  -e DB_PASSWORD=password \
  sh-budget-be
```
