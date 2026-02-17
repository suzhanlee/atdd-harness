---
name: design
description: Entity와 DDD 도메인 모델을 설계한다. 바람직한 어려움(Desirable Difficulties)을 적용한 4-Phase 워크플로우로 사용자의 도메인 모델링 역량을 훈련한다. 데이터베이스 스키마, 도메인 구조 설계 시 사용.
disable-model-invocation: true
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit
references:
  - references/domain-questions.md
  - references/blank-erd-template.md
  - references/ddd-patterns.md
  - references/entity-template.md
  - references/validation-guide.md
---

# Entity & Domain 설계

## 목표
사용자가 직접 도메인 모델과 ERD를 설계하여 **설계 역량**을 향상시킨다.
AI가 설계안을 제시하는 방식이 아닌, 사용자가 주도적으로 설계하는 훈련을 제공한다.

---

## STOP PROTOCOL

### 4-Phase 진행 규칙
각 Phase는 반드시 **별도 턴**으로 진행한다. 사용자가 다음 단계로 진행할 준비가 될 때까지 대기한다.

```
Phase A (Domain Q&A)      → 사용자 입력 대기 → "완료"/"다음" → Phase B
Phase B (Blank Model)     → 사용자 입력 대기 → "완료"/"다음" → Phase C
Phase C (Implementation)  → Phase D 즉시 진행 (대기 없음)
Phase D (Validation)      → 설계 완료
```

### Phase A 종료 필수 문구
```
---
👆 도메인 질문에 답변해주세요.
답변 완료 후 "완료" 또는 "다음"이라고 입력해주세요.
Phase B (Blank Model)로 진행합니다.
```

### Phase B 종료 필수 문구
```
---
👆 빈 ERD/도메인 모델을 작성해주세요.
작성 완료 후 "완료" 또는 "다음"이라고 입력해주세요.
Phase C (Implementation)로 진행합니다.
```

---

## 입력
- `.atdd/requirements/refined-requirements.md`
- `.atdd/validation/validation-report.md`

---

## 4-Phase 워크플로우

### Phase A: Domain Q&A (도메인 질문)

**목적**: 사용자가 도메인에 대해 깊이 있게 생각하도록 유도

**진행 방식**:
1. 요구사항 분석
2. 사용자에게 도메인 질문 제시
3. 사용자가 답변

**핵심 도메인 질문**:

```
Q1: 비즈니스에서 다루는 핵심 "사물"이나 "개념"은 무엇인가요?
    → Entity 후보 식별

Q2: 이 개체들을 어떻게 구분하나요?
    → 식별자(ID) 결정

Q3: 개체 간 어떤 관계가 있나요?
    → 1:1, 1:N, N:M 관계 파악

Q4: 각 개체가 수행하는 핵심 행동은 무엇인가요?
    → 비즈니스 메서드 식별

Q5: 어떤 규칙이 행동을 제약하나요?
    → 불변식(Invariant) 파악

Q6: 어떤 개체들이 함께 생성/수정/삭제되나요?
    → Aggregate 경계 식별
```

**상세 가이드**: [domain-questions.md](references/domain-questions.md)

**Phase A 종료 후**:
- STOP Protocol 적용 → 사용자 입력 대기
- "완료" 또는 "다음" 입력 시 Phase B 진행

---

### Phase B: Blank Model (빈 모델 작성)

**목적**: 사용자가 직접 ERD와 도메인 모델을 스케치

**진행 방식**:
1. 빈 템플릿 제시
2. 사용자가 직접 ERD/도메인 모델 작성

**빈 ERD 템플릿**:

```markdown
# ERD 스케치

## 테이블 목록
1. [테이블명1]
2. [테이블명2]
3. ...

## [테이블명1]
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | | | |
| [필드명] | | | |
| created_at | | | |
| updated_at | | | |

### 인덱스
- idx_[테이블]_[컬럼]: [목적]

### 관계
- → [다른 테이블]: [관계 유형]
```

**빈 도메인 모델 템플릿**:

```markdown
# 도메인 모델 스케치

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
```

**상세 가이드**: [blank-erd-template.md](references/blank-erd-template.md)

**Phase B 종료 후**:
- STOP Protocol 적용 → 사용자 입력 대기
- "완료" 또는 "다음" 입력 시 Phase C 진행

---

### Phase C: Implementation (구현)

**목적**: 사용자가 작성한 설계안을 바탕으로 실제 코드 생성

**진행 방식**:
1. Phase A, B 결과를 바탕으로 DDL 생성
2. JPA Entity 클래스 생성 (Rich Domain Model)
3. Value Object 생성

**Rich Domain Model 원칙**:

```java
// ✅ Rich Domain Model (권장)
@Entity
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Email email;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

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
}
```

**상세 가이드**:
- DDD 패턴: [ddd-patterns.md](references/ddd-patterns.md)
- Entity 템플릿: [entity-template.md](references/entity-template.md)

**Phase C 완료 후**:
- STOP Protocol 없음
- 즉시 Phase D 진행

---

### Phase D: Validation (검증)

**목적**: 설계안이 요구사항을 충족하는지 검증

**진행 방식**:
1. 요구사항-도메인 매핑 검증
2. SQL Sample Data 무결성 검증
3. 검증 리포트 생성

**검증 항목**:

| 항목 | 합격 기준 |
|------|-----------|
| Must Have 매핑 | 100% |
| Should Have 매핑 | 80% 이상 |
| NOT NULL 준수 | 100% |
| UNIQUE 준수 | 100% |
| FK 무결성 | 100% |

**상세 가이드**: [validation-guide.md](references/validation-guide.md)

**검증 결과**:

```
설계 검증 완료 ✅

## 커버리지
- Must Have 매핑: 100% (3/3)
- Should Have 매핑: 100% (1/1)

## 무결성
- NOT NULL 준수: ✅
- UNIQUE 준수: ✅
- FK 무결성: ✅

다음 단계: /gherkin
```

---

## Entity 설계 원칙

### Rich Domain Model

**Entity에 비즈니스 로직을 포함** (Anemic Domain Model 지양)

| 패턴 | 설명 |
|------|------|
| 정적 팩토리 메서드 | 생성 로직 캡슐화 |
| 비즈니스 메서드 | 상태 변경 로직 포함 |
| 불변식 검증 | 항상 유효한 상태 보장 |
| Value Object | 불변 값 객체 사용 |

### DDD 전술적 패턴

| 패턴 | 설명 |
|------|------|
| Aggregate | 트랜잭션 일관성 경계 |
| Entity | 식별자로 구분되는 객체 |
| Value Object | 불변 값 객체 |
| Domain Service | Entity에 속하지 않는 도메인 로직 |
| Repository | 영속성 추상화 |

---

## 트리거
- `/design` 명령어 실행
- 요구사항 검증 통과 후 자동 제안

## MUST 체크리스트 (실행 전)
- [ ] refined-requirements.md 존재
- [ ] validation-report.md 존재 (PASS 상태)

## MUST 체크리스트 (실행 후)
- [ ] Phase A: 도메인 질문 답변 완료
- [ ] Phase B: ERD/도메인 모델 작성 완료
- [ ] Phase C: DDL, Entity 클래스 생성
- [ ] Phase D: 검증 완료 (Must Have 100%)
- [ ] erd.md, domain-model.md 생성
- [ ] traceability-matrix.md 생성

---

## 출력 파일

### erd.md
```markdown
# ERD

## 다이어그램
[Mermaid 또는 ASCII 다이어그램]

## 테이블 정의

### user
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AI | 식별자 |
| email | VARCHAR(255) | NN, UQ | 이메일 |
| status | VARCHAR(20) | NN | 상태 |
| created_at | DATETIME | NN | 생성일시 |
| updated_at | DATETIME | NN | 수정일시 |

## 인덱스
- idx_user_email: 로그인 검색용
```

### domain-model.md
```markdown
# 도메인 모델

## Bounded Context

### User Context
- **Aggregate**: User
- **Entity**: User
- **Value Object**: Email, Password

## Aggregate: User

### Root Entity: User
#### 속성
- id: Long
- email: Email
- status: UserStatus

#### 행동
- register(): 회원가입
- verifyEmail(): 이메일 인증

#### 불변식
- 이메일 형식 유효
- 상태 전이 규칙 준수
```

### traceability-matrix.md
```markdown
# 요구사항-도메인 추적 매트릭스

| ID | 요구사항 | Entity | 메서드/VO | 상태 |
|----|----------|--------|-----------|------|
| M1 | 회원가입 | User | User.register() | ✅ |
| M2 | 이메일 인증 | User | User.verifyEmail() | ✅ |
| S1 | 비밀번호 변경 | User | User.changePassword() | ✅ |
```

### sql/schema/*.sql
DDL 스크립트

### domain/entity/*.java
JPA Entity 클래스

---

## 다음 단계
설계 검증 완료 후 `/gherkin` 실행

---

## 참조
- 도메인 질문 가이드: [domain-questions.md](references/domain-questions.md)
- 빈 ERD 템플릿: [blank-erd-template.md](references/blank-erd-template.md)
- DDD 패턴: [ddd-patterns.md](references/ddd-patterns.md)
- Entity 템플릿: [entity-template.md](references/entity-template.md)
- 검증 가이드: [validation-guide.md](references/validation-guide.md)
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
