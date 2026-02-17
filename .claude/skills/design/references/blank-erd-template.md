# 빈 ERD 템플릿

## 목적
Phase B에서 사용자가 직접 ERD와 도메인 모델을 스케치하여 설계 역량을 훈련한다.

---

## 빈 ERD 템플릿

### 1. 테이블 정의 템플릿

```markdown
# ERD 스케치

## 테이블 목록
1. [테이블명1]
2. [테이블명2]
3. ...

---

## [테이블명1]

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | | | |
| [필드명] | | | |
| ... | | | |
| created_at | | | |
| updated_at | | | |

### 인덱스
- idx_[테이블]_[컬럼]: [목적]

### 관계
- → [다른 테이블]: [관계 유형] (1:1, 1:N, N:M)

---

## [테이블명2]
...
```

### 2. 다이어그램 스케치 템플릿

```markdown
## ER 다이어그램

[ ] ─────── [ ] ─────── [ ]
 │            │
 │            │
 ▼            ▼
[ ]          [ ]

### 범례
- ─── 1:1
- ───< 1:N
- >──< N:M
```

### 3. 도메인 모델 스케치 템플릿

```markdown
# 도메인 모델 스케치

## Bounded Context

### [Context명]
- **Aggregate**: [Aggregate 목록]
- **Entity**: [Entity 목록]
- **Value Object**: [VO 목록]
- **Domain Service**: [서비스 목록]

---

## Aggregate: [Aggregate명]

### Root Entity: [Entity명]

#### 속성
- [속성명]: [타입]

#### 행동 (메서드)
- [메서드명](): [설명]

#### 불변식
- [규칙]

### 구성요소
- [Entity/VO명]: [설명]

---

## Value Object: [VO명]

### 속성
- [속성명]: [타입]

### 검증 규칙
- [규칙]
```

---

## 작성 가이드

### 타입 표기

| Java 타입 | DB 타입 | 비고 |
|-----------|---------|------|
| Long | BIGINT | ID |
| String | VARCHAR(N) | 문자열 |
| Integer | INT | 숫자 |
| Boolean | BOOLEAN/TINYINT | 플래그 |
| BigDecimal | DECIMAL(P,S) | 금액 |
| LocalDateTime | DATETIME | 시간 |
| Enum | VARCHAR(20) | 상태 |

### 제약조건 표기

| 표기 | 의미 |
|------|------|
| PK | Primary Key |
| FK | Foreign Key |
| NN | Not Null |
| UQ | Unique |
| AI | Auto Increment |

---

## ERD 작성 예시 (참고용)

```markdown
# ERD 스케치

## user

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AI | 사용자 ID |
| email | VARCHAR(255) | NN, UQ | 이메일 |
| password | VARCHAR(255) | NN | 암호화된 비밀번호 |
| status | VARCHAR(20) | NN | 상태 (PENDING/ACTIVE/INACTIVE) |
| created_at | DATETIME | NN | 생성일시 |
| updated_at | DATETIME | NN | 수정일시 |
| deleted_at | DATETIME | | 삭제일시 (소프트 삭제) |

### 인덱스
- idx_user_email: 로그인 검색용

### 관계
- → order: 1:N (한 사용자가 여러 주문)

---

## order

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AI | 주문 ID |
| user_id | BIGINT | FK, NN | 주문자 ID |
| status | VARCHAR(20) | NN | 주문상태 |
| total_amount | DECIMAL(10,2) | NN | 총 주문금액 |
| created_at | DATETIME | NN | 생성일시 |
| updated_at | DATETIME | NN | 수정일시 |

### 관계
- ← user: N:1
- → order_item: 1:N
- → payment: 1:1

---

## Aggregate: Order

### Root Entity: Order

#### 속성
- id: Long
- userId: Long
- status: OrderStatus
- totalAmount: BigDecimal
- items: List<OrderItem>

#### 행동
- addItem(): 주문항목 추가
- removeItem(): 주문항목 제거
- calculateTotal(): 총액 계산
- cancel(): 주문 취소

#### 불변식
- totalAmount = Σ items.amount
- status가 CANCELLED면 addItem 불가
```

---

## STOP Protocol (Phase B 완료)

```
---
👆 빈 ERD/도메인 모델을 작성해주세요.
작성 완료 후 "완료" 또는 "다음"이라고 입력해주세요.
Phase C (Implementation)로 진행합니다.
```
