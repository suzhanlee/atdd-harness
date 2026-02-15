---
name: design
description: Entity와 DDD 도메인 모델을 설계한다. 데이터베이스 스키마, 도메인 구조 설계 시 사용.
disable-model-invocation: true
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit
---

# Entity & Domain 설계

## 목표
요구사항을 바탕으로 Entity와 DDD 도메인 모델을 설계한다.

## 입력
- `.atdd/requirements/refined-requirements.md`
- `.atdd/validation/validation-report.md`

## 상세 가이드
- DDD 패턴: [ddd-patterns.md](ddd-patterns.md)
- Entity 템플릿: [entity-template.md](entity-template.md)

## 트리거
- `/design` 명령어 실행
- 요구사항 검증 통과 후 자동 제안

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
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME
);

CREATE INDEX idx_user_email ON user(email);
```

### 4. JPA Entity 클래스 생성
```java
@Entity
@Table(name = "user")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    // ... 기타 필드 및 메서드
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

### sql/schema/*.sql
각 테이블별 DDL 스크립트

### domain/entity/*.java
JPA Entity 클래스

## 검증 체크리스트

- [ ] 모든 Entity에 기본키 존재
- [ ] 외래키 관계가 올바르게 정의됨
- [ ] 인덱스가 적절히 생성됨
- [ ] 소프트 삭제 컬럼 포함 (deleted_at)
- [ ] 생성/수정 시간 컬럼 포함
- [ ] Aggregate 경계가 명확함
- [ ] Value Object가 불변으로 설계됨

## 다음 단계
설계 완료 후 `/gherkin` 실행

## 참조
- DDD 패턴: [ddd-patterns.md](ddd-patterns.md)
- Entity 템플릿: [entity-template.md](entity-template.md)
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
