ShBudget (커플/부부형) 기능 명세서

PRD v2

1. 프로젝트 개요 (Overview)
   1.1 프로젝트명

ShBudget (가칭)
Shared Budget의 의미를 기반으로, 개인과 공동 자산 관리를 동시에 지원하는 가계부 서비스

1.2 핵심 컨셉

“따로 또 같이”

사용자는 개인 가계부로 사용할 수 있으며, 필요 시 연인 또는 배우자와 가계부를 연결하여 자산과 지출을 함께 관리할 수 있다.
실제 금융 계좌를 통합하지 않고, 앱 내에서만 공동 자산 관리를 제공한다.

1.3 타겟 사용자

신혼부부

동거 중인 커플

결혼을 준비하며 자산을 함께 관리하려는 예비 부부

생활비 및 공동 지출을 함께 관리하고 싶은 사용자

2. 주요 기능 구성 (개인 가계부 대비 변경점)
   2.1 가계부 그룹 (Book & Invite)
   가계부 생성

회원가입 시 기본적으로 개인 가계부(Book) 가 자동 생성된다.

초기 상태에서는 개인 가계부로 동작한다.

멤버 초대 (커플 연결)

설정 메뉴에서 “커플 연결하기” 선택

초대 코드 생성 (예: X8V29A)

상대방이 코드를 입력하면 동일한 가계부에 합류

합류 이후:

자산

카테고리

거래 내역

을 포함한 모든 데이터를 동일하게 조회한다.

2.2 메인 홈 & 캘린더 (Shared View)
공동 합계 표시

두 사용자의 수입 및 지출을 합산하여 표시

월별, 일별 기준으로 공동 소비 흐름 확인 가능

작성자 표시

각 거래 내역에 작성자를 표시

프로필 이미지 또는 이니셜 형태로 구분

예시:

🍔 햄버거 12,000원 (User A)


이를 통해 공동 가계부에서도 소비 주체를 명확히 구분한다.

2.3 자산 관리 (Shared Assets)
자산 소속 구조

자산은 개인이 아닌 가계부(Book) 에 속한다.

필요 시 실제 사용자를 표기할 수 있다.

예시:

남편 월급 통장

아내 생활비 카드

공동 데이트 통장

가상 통합 자산

실제 계좌는 분리되어 있으나, 앱 내에서는 모든 자산을 합산하여
“우리의 총 자산” 형태로 표시한다.

2.4 통계 (Comparison)

공동 사용 환경에서 다음 정보를 제공한다.

월별 수입 기여도 비교

지출 비율 비교

개인별 소비 비중 그래프

이를 통해 소비 패턴과 재정 기여도를 직관적으로 확인할 수 있다.

3. 데이터베이스 설계 (ERD & Schema)
   3.1 설계 핵심 변경점

기존 개인 가계부 구조에서의 가장 큰 변화는 다음과 같다.

Book 엔티티의 도입

모든 가계부 관련 데이터의 소유 기준을
member_id → book_id 로 변경

즉, 데이터의 소유 주체는 개인이 아닌 가계부 그룹(Book) 이 된다.

3.2 ER Diagram Concept (Revised)
erDiagram
BOOK ||--o{ BOOK_MEMBER : "has"
MEMBER ||--o{ BOOK_MEMBER : "joins"

    BOOK ||--o{ ASSET : "owns"
    BOOK ||--o{ CATEGORY : "defines"
    BOOK ||--o{ TRANSACTION : "records"

    MEMBER {
        Long id PK
        String email
        String nickname
        String profile_image
    }

    BOOK {
        Long id PK
        String name
        String invite_code
        Long owner_id
    }

    BOOK_MEMBER {
        Long id PK
        Long book_id FK
        Long member_id FK
        Enum role
    }

    ASSET {
        Long id PK
        Long book_id FK
        String name
        Long balance
        Long owner_member_id FK
    }

    TRANSACTION {
        Long id PK
        Long book_id FK
        Enum type
        Long amount
        Long created_by FK
        Long asset_id FK
        Long from_asset_id FK
        Long to_asset_id FK
        Long category_id FK
        Date date
        String memo
    }

3.3 주요 테이블 설명
1) members (사용자)

가계부 데이터와 분리된 순수 계정 정보

id (PK)

email (UK)

nickname

profile_image_url

※ 커플 환경에서 작성자 구분을 위해 프로필 정보의 중요도가 높음

2) books (가계부 / 그룹) [NEW]

하나의 가계부 단위

id (PK)

name (예: "우리집 가계부")

invite_code (Index)

owner_id

3) book_members (가계부 멤버 매핑) [NEW]

사용자와 가계부 간 N:M 관계 해소

id (PK)

book_id (FK)

member_id (FK)

role (OWNER, MEMBER)

Unique Constraint:

(book_id, member_id)

4) assets (자산)

자산은 개인이 아닌 가계부에 속한다.

id (PK)

book_id (FK)

name

balance

owner_member_id (Nullable)

※ 실제 소유자 표기 및 통계 계산 시 활용

5) transactions (거래 내역)

거래 타입에 따라 다른 필드 조합을 사용한다.

id (PK)

book_id (FK, Index)

type (INCOME, EXPENSE, TRANSFER)

amount

created_by (member_id, FK)

date

memo

**타입별 사용 필드**:

INCOME (수입):

asset_id (FK) - 입금될 자산

category_id (FK, Nullable)

from_asset_id (NULL)

to_asset_id (NULL)

EXPENSE (지출):

asset_id (FK) - 출금될 자산

category_id (FK, Nullable)

from_asset_id (NULL)

to_asset_id (NULL)

TRANSFER (이체):

asset_id (NULL)

category_id (NULL)

from_asset_id (FK) - 출발 자산

to_asset_id (FK) - 도착 자산

※ 이체는 총 자산 변동 없이 자산 간 잔액만 이동
※ 예: 현금 10만원 → 적금 10만원 (총 자산 유지)

4. 핵심 로직 흐름 (Scenario)
   4.1 회원가입 및 초대 프로세스
   A 사용자 가입

Member A 생성

Book A 자동 생성

BookMember (Book A - Member A) 생성

B 사용자 가입

Member B 생성

Book B 자동 생성

BookMember (Book B - Member B) 생성

커플 연결 (A → B 초대)

A가 Book A의 초대 코드 생성

B가 코드 입력 및 유효성 검증

기존 데이터 처리 선택

MVP 정책:

기존 Book B 데이터 초기화 안내 후 합류 권장

BookMember (Book A - Member B) 생성

이후 B는 Book A 데이터를 조회

4.2 거래 내역 생성 흐름

**수입 등록**:

사용자가 월급 300만원 입금

Transaction 생성:

type = INCOME

asset_id = "월급 통장"

amount = 3000000

category_id = "급여"

자산 잔액 자동 업데이트:

"월급 통장" balance += 3000000

**지출 등록**:

사용자가 식비 5만원 지출

Transaction 생성:

type = EXPENSE

asset_id = "체크카드"

amount = 50000

category_id = "식비"

자산 잔액 자동 업데이트:

"체크카드" balance -= 50000

**이체 등록**:

사용자가 현금 10만원을 적금으로 이체

Transaction 생성:

type = TRANSFER

from_asset_id = "현금"

to_asset_id = "적금"

amount = 100000

category_id = NULL

자산 잔액 자동 업데이트:

"현금" balance -= 100000

"적금" balance += 100000

총 자산 변동 없음 (0원)

4.3 거래 내역 조회 흐름 (API)

요청:

GET /transactions?month=2024-05


서버 처리 흐름:

로그인한 사용자의 활성 Book 조회

해당 Book 기준으로 거래 내역 조회

SELECT *
FROM transactions
WHERE book_id = ?
AND date BETWEEN ...


응답 시:

작성자 nickname

profile image

정보를 포함하여 반환한다.