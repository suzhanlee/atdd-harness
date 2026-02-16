---
name: design
description: Entity와 DDD 도메인 모델을 설계한다. 데이터베이스 스키마, 도메인 구조 설계 시 사용.
disable-model-invocation: true
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit
references:
  - references/ddd-patterns.md
  - references/entity-template.md
  - references/validation-guide.md
---

# Entity & Domain 설계

## 목표
요구사항을 바탕으로 Entity와 DDD 도메인 모델을 설계하고 검증한다.

## 입력
- `.atdd/requirements/refined-requirements.md`
- `.atdd/validation/validation-report.md`

## 상세 가이드
- DDD 패턴: [ddd-patterns.md](references/ddd-patterns.md)
- Entity 템플릿: [entity-template.md](references/entity-template.md)
- 검증 가이드: [validation-guide.md](references/validation-guide.md)

## 트리거
- `/design` 명령어 실행
- 요구사항 검증 통과 후 자동 제안

## Entity 설계 원칙: Rich Domain Model

**Entity에 비즈니스 로직을 포함** (Anemic Domain Model 지양)

```java
// ❌ Anemic (피해야 할 패턴)
@Entity
public class User {
    private String email;
    private String password;
    // getter/setter만 존재
}

// ✅ Rich Domain Model (권장)
@Entity
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Email email;

    @Embedded
    private Password password;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    // 기본 생성자 (JPA용)
    protected User() {}

    // 정적 팩토리 메서드
    public static User register(Email email, Password password) {
        User user = new User();
        user.email = email;
        user.password = password;
        user.status = UserStatus.PENDING;
        return user;
    }

    // 비즈니스 메서드
    public void verifyEmail() {
        if (this.status != UserStatus.PENDING) {
            throw new IllegalStateException("이미 인증된 사용자입니다.");
        }
        this.status = UserStatus.ACTIVE;
    }

    public void changePassword(Password newPassword, PasswordEncoder encoder) {
        validatePasswordChange(newPassword, encoder);
        this.password = newPassword;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
}
```

## 프로세스

### 1. Entity 식별 (명사 분석)
요구사항에서 명사를 추출하여 Entity 후보 식별

```
예시:
- "사용자가 상품을 주문한다"
  → User, Product, Order

- "회원은 게시글에 댓글을 작성한다"
  → Member, Post, Comment
```

### 2. 관계 정의
Entity 간 관계 정의

| 관계 | 표기 | 예시 |
|------|------|------|
| 일대일 | 1:1 | User - Profile |
| 일대다 | 1:N | User - Orders |
| 다대다 | N:M | Student - Course (중간 테이블 필요) |

### 3. DDL 스크립트 생성
```sql
-- 예시: user 테이블
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_user_email ON user(email);
```

### 4. JPA Entity 클래스 생성 (Rich Domain Model)
```java
@Entity
@Table(name = "user")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Email email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.PENDING;

    // 정적 팩토리 메서드
    public static User register(Email email, Password password) {
        // ...
    }

    // 비즈니스 메서드
    public void verifyEmail() {
        // 불변식 검증 포함
    }
}
```

### 5. Bounded Context 정의
도메인 경계 설정

```
예시:
- User Context: 회원 관리
- Order Context: 주문 관리
- Product Context: 상품 관리
```

### 6. Aggregate 경계 설정
트랜잭션 일관성 경계 정의

```
예시:
- User Aggregate: User, Profile, Settings
- Order Aggregate: Order, OrderItem, ShippingInfo
```

### 7. [검증] 요구사항-도메인 매핑 검증

**상세 가이드**: [validation-guide.md](references/validation-guide.md)

```
검증 항목:
├─▶ Must Have 요구사항 → Entity 메서드/VO 매핑 확인
├─▶ 검증 규칙 → @NotNull, @Size, 불변식 코드 확인
└─▶ traceability-matrix.md 생성
```

**합격 기준**:
- Must Have 매핑: 100%
- Should Have 매핑: 80% 이상

### 8. SQL Sample Data 생성

```sql
-- sql/data/001_user_data.sql
INSERT INTO user (id, email, password, name, status, created_at, updated_at)
VALUES
    (1, 'user1@test.com', 'encrypted1', '사용자1', 'ACTIVE', NOW(), NOW()),
    (2, 'user2@test.com', 'encrypted2', '사용자2', 'PENDING', NOW(), NOW());
```

### 9. [검증] SQL Sample Data 요구사항 준수 검증

**상세 가이드**: [validation-guide.md](references/validation-guide.md)

```
검증 항목:
├─▶ NOT NULL 제약조건 준수 확인
├─▶ UNIQUE 제약조건 준수 확인
├─▶ CHECK 제약조건 준수 확인
├─▶ FK 무결성 확인
├─▶ 비즈니스 규칙 준수 확인
└─▶ design-validation-report.md 생성
```

**합격 기준**:
- NOT NULL 준수: 100%
- UNIQUE 준수: 100%
- FK 무결성: 100%
- 비즈니스 규칙: 100%

## 출력 파일

### erd.md
```markdown
# ERD (Entity Relationship Diagram)

## 다이어그램
[Mermaid 또는 ASCII 다이어그램]

## 테이블 정의

### [테이블명]
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | 식별자 |
| ... | ... | ... | ... |

## 인덱스
- idx_[테이블]_[컬럼]: [목적]
```

### domain-model.md
```markdown
# 도메인 모델

## Bounded Context
1. [Context명]
   - Aggregate: [Aggregate 목록]
   - Entity: [Entity 목록]
   - Value Object: [VO 목록]

## Aggregate 상세

### [Aggregate명]
- **Root**: [Root Entity]
- **구성요소**: [Entity, VO 목록]
- **불변식**: [비즈니스 규칙]
```

### traceability-matrix.md (신규)
```markdown
# 요구사항-도메인 추적 매트릭스

## Must Have 요구사항

| ID | 요구사항 | Entity | 메서드/VO | 상태 |
|----|----------|--------|-----------|------|
| M1 | 회원가입 | User | User.register() | ✅ |
| M2 | 이메일 인증 | User | User.verifyEmail() | ✅ |
| M3 | 로그인 | User | - | ⚠️ Service 필요 |

## Should Have 요구사항

| ID | 요구사항 | Entity | 메서드/VO | 상태 |
|----|----------|--------|-----------|------|
| S1 | 비밀번호 찾기 | User | User.changePassword() | ✅ |

## 요약
- Must Have 매핑: 100% (3/3)
- Should Have 매핑: 100% (1/1)
```

### design-validation-report.md (신규)
```markdown
# 설계 검증 리포트

## 검증 일시
2024-01-15 15:00:00

## 검증 결과: ✅ PASS

## 요구사항-도메인 매핑 검증

| 항목 | 결과 | 비고 |
|------|------|------|
| Must Have 매핑 | ✅ 100% | 3/3 |
| Should Have 매핑 | ✅ 100% | 1/1 |

## SQL Sample Data 검증

| 항목 | 결과 | 비고 |
|------|------|------|
| NOT NULL 준수 | ✅ | 모든 필드 준수 |
| UNIQUE 준수 | ✅ | email 중복 없음 |
| FK 무결성 | ✅ | 모든 FK 유효 |
| 비즈니스 규칙 | ✅ | 상태값 유효 |
```

### sql/schema/*.sql
각 테이블별 DDL 스크립트

### sql/data/*.sql (신규)
Sample Data 스크립트

### domain/entity/*.java
JPA Entity 클래스 (Rich Domain Model)

## 검증 체크리스트

### Entity 설계
- [ ] 모든 Entity에 기본키 존재
- [ ] 외래키 관계가 올바르게 정의됨
- [ ] 인덱스가 적절히 생성됨
- [ ] 소프트 삭제 컬럼 포함 (deleted_at)
- [ ] 생성/수정 시간 컬럼 포함
- [ ] Aggregate 경계가 명확함
- [ ] Value Object가 불변으로 설계됨

### Rich Domain Model
- [ ] 정적 팩토리 메서드 존재
- [ ] 비즈니스 메서드 포함
- [ ] 불변식 검증 코드 포함
- [ ] 상태 전이 메서드 포함
- [ ] Anemic Domain Model이 아님

### 검증
- [ ] Must Have 요구사항 100% 매핑
- [ ] Should Have 요구사항 80% 이상 매핑
- [ ] SQL Sample Data 무결성 확인
- [ ] traceability-matrix.md 생성
- [ ] design-validation-report.md 생성

## 다음 단계
설계 및 검증 완료 후 `/gherkin` 실행

## 참조
- DDD 패턴: [ddd-patterns.md](references/ddd-patterns.md)
- Entity 템플릿: [entity-template.md](references/entity-template.md)
- 검증 가이드: [validation-guide.md](references/validation-guide.md)
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
