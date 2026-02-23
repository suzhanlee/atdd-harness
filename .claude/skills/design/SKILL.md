---
name: design
description: This skill should be used when the user asks to "/design", "도메인 설계", "Entity 설계", "DDD 설계", or needs to design domain models using DDD patterns.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit, AskUserQuestion, EnterPlanMode
references:
  - references/domain-questions.md
  - references/blank-erd-template.md
  - references/blank-architecture-template.md
  - references/ddd-patterns.md
  - references/entity-template.md
  - references/validation-guide.md
  - ../shared/context-helper.md
---

# Entity & Domain 설계

## 목표
사용자가 직접 도메인 모델과 ERD를 설계하여 **설계 역량**을 향상시킨다.
AI가 설계안을 제시하는 방식이 아닌, 사용자가 주도적으로 설계하는 훈련을 제공한다.

---

## Context Helper
- [context-helper.md](../shared/context-helper.md)

---

## Context 로드

Design 시작 전, context.json을 읽어서 작업 경로를 결정합니다.

```markdown
Read .atdd/context.json
```

경로 결정:
- `basePath = context.basePath` (또는 `.atdd/{date}/{topic}`으로 계산)

context.json이 없으면 에러:
```
⚠️ 현재 작업 컨텍스트가 없습니다.
먼저 `/interview`를 실행하여 새 작업을 시작해주세요.
```

---

## STOP PROTOCOL

### ⚠️ 종료 전 필수 체크리스트

**스킬 종료 전 반드시 수행:**
- [ ] context.json의 `status`를 "completed"로 변경
- [ ] context.json의 `updated_at`을 현재 시간으로 변경
- [ ] 산출물 파일이 올바른 경로에 생성되었는지 확인

**❌ 위 체크리스트 미완료 시 스킬이 완료되지 않은 것으로 간주**

---

### 4-Phase 진행 규칠
각 Phase는 반드시 **별도 턴**으로 진행한다. 사용자가 다음 단계로 진행할 준비가 될 때까지 대기한다.

```
Phase A (Domain Q&A)      → AskUserQuestion으로 1개씩 질문 → 자동 Phase B 진행
Phase B (Blank Model)     → 사용자 입력 대기 → "완료"/"다음" → Phase C
Phase C (Interface Def.)  → Phase D 즉시 진행 (대기 없음)
Phase D (Validation)      → 설계 완료
```

**Phase A**: AskUserQuestion을 통해 질문을 하나씩 진행하므로 별도 대기 불필요. 모든 질문 완료 시 자동으로 Phase B 진행.

### Phase B 종료 필수 문구
```
---
👆 빈 아키텍처 템플릿을 작성해주세요.
작성 완료 후 "완료" 또는 "다음"이라고 입력해주세요.
Phase C (Interface Definition)로 진행합니다.
```

---

## 입력
- `.atdd/context.json` (작업 컨텍스트)
- `{basePath}/validate/refined-requirements.md`
- `{basePath}/validate/validation-report.md`
- `src/test/resources/features/{topic}.feature` (선택 - 있으면 참조)

## 출력
- `{basePath}/design/erd.md`
- `{basePath}/design/domain-model.md`
- `{basePath}/design/traceability-matrix.md`
- `src/main/java/**/domain/entity/*.java` (Entity 클래스)

---

## 4-Phase 워크플로우

### Phase A: Domain Q&A (도메인 질문)

**목적**: 사용자가 도메인에 대해 깊이 있게 생각하도록 유도

**진행 방식**:
1. 요구사항 분석
2. AskUserQuestion 도구를 사용하여 각 질문을 하나씩 제시
3. 사용자가 각 질문에 주관식으로 답변
4. 답변 완료 후 자동으로 다음 질문 진행
5. 모든 질문 완료 후 Phase B 진행

**질문 순서**:

| 순서 | 질문 | 목적 |
|------|------|------|
| Q1 | 비즈니스에서 다루는 핵심 "사물"이나 "개념"은 무엇인가요? | Entity 후보 식별 |
| Q2 | 이 개체들을 어떻게 구분하나요? | 식별자(ID) 결정 |
| Q3 | 개체 간 어떤 관계가 있나요? | 1:1, 1:N, N:M 관계 파악 |
| Q4 | 각 개체가 수행하는 핵심 행동은 무엇인가요? | 비즈니스 메서드 식별 |
| Q5 | 어떤 규칙이 행동을 제약하나요? | 불변식(Invariant) 파악 |
| Q6 | 어떤 개체들이 함께 생성/수정/삭제되나요? | Aggregate 경계 식별 |
| Q7 | 두 개 이상 Entity가 관여하는 로직이 있나요? | Domain Service 후보 |
| Q8 | 상태 변경 시 다른 시스템/사용자에게 알려야 하나요? | Domain Event 후보 |
| Q9 | 외부에서 데이터를 받아오나요? 어떤 형식인가요? | Parser/Extractor 후보 |
| Q10 | 복잡한 비즈니스 규칙이 있나요? | Policy/Specification 후보 |

**AskUserQuestion 사용법**:
```
각 질문마다 AskUserQuestion 도구를 호출:
- question: 질문 내용
- header: "Q1", "Q2" 등
- multiSelect: false
- options: 주관식 입력을 위해 빈 배열 []
```

**Phase A 종료 후**:
- 모든 질문 답변 완료 시 자동으로 Phase B 진행
- 별도의 STOP Protocol 불필요 (AskUserQuestion이 대기 역할 수행)

**상세 가이드**: [domain-questions.md](references/domain-questions.md)

---

### Phase B: Blank Architecture (빈 아키텍처 작성)

**목적**: 사용자가 직접 전체 아키텍처를 스케치

**진행 방식**:
1. 빈 아키텍처 템플릿 제시
2. 사용자가 직접 전체 계층 구조 작성

**빈 아키텍처 템플릿**:

```markdown
# 아키텍처 스케치

## 1. Domain Layer

### Entities
| Entity | 식별자 | 핵심 속성 | 핵심 행동 |
|--------|--------|----------|----------|
|        |        |          |          |

### Value Objects
| VO | 속성 | 불변식 |
|----|------|--------|
|    |      |        |

### Domain Services
| Service | 책임 | 사용 Entity |
|---------|------|-------------|
|         |      |             |

### Domain Events
| Event | 발생 시점 | 포함 정보 |
|-------|----------|----------|
|       |          |           |

### Policies / Specifications
| 이름 | 규칙/조건 |
|------|----------|
|      |          |

---

## 2. Application Layer

### Use Cases / Application Services
| UseCase | 사용자 행동 | 참여 객체 |
|---------|-------------|----------|
|         |             |          |

### Event Handlers
| Handler | 처리 Event | 후속 작업 |
|---------|-----------|----------|
|         |           |          |

---

## 3. Infrastructure Layer

### Parsers
| Parser | 입력 형식 | 출력 |
|--------|----------|------|
|        |          |      |

### Extractors
| Extractor | 소스 | 추출 대상 |
|-----------|------|----------|
|           |      |          |

### External Clients
| Client | 외부 시스템 | 통신 방식 |
|--------|-----------|----------|
|        |           |          |

---

## 4. Interface Layer

### Controllers
| Controller | API 그룹 | 주요 Endpoint |
|------------|---------|---------------|
|            |         |               |

---

## 5. 협력 흐름도 (Collaboration Flow)

### [유스케이스명] 흐름
```
[사용자가 직접 그리는 영역 - Mermaid 또는 텍스트]
```

### 이벤트 흐름
```
[사용자가 직접 그리는 영역]
```
```

**상세 가이드**: [blank-architecture-template.md](references/blank-architecture-template.md)

**Phase B 종료 후**:
- STOP Protocol 적용 → 사용자 입력 대기
- "완료" 또는 "다음" 입력 시 Phase C 진행

---

### Phase C: Interface Definition (인터페이스 정의)

**목적**: 사용자가 작성한 설계안을 바탕으로 인터페이스와 메서드 시그니처만 정의

**진행 방식**:
1. **EnterPlanMode 호출** - 인터페이스 정의 계획 수립
2. 사용자 승인 후 계획 실행
3. Entity 클래스 골격 생성 (속성만, 메서드는 빈 구현)
4. Repository 인터페이스 정의 (메서드 시그니처만)
5. Service 인터페이스 정의 (메서드 시그니처만)
6. 실제 구현은 `/tdd`에서 수행

**Plan Mode에서 수행할 작업**:
- 작성할 Entity/VO/Repository/Service 목록 정리
- 각 클래스의 속성 및 메서드 시그니처 계획
- 파일 경로 및 패키지 구조 결정
- 사용자 승인 후 일괄 파일 생성

**인터페이스 정의 원칙**:
- 메서드 시그니처와 반환 타입만 정의
- 빈 구현: `throw new UnsupportedOperationException("TODO: TDD에서 구현")`
- Javadoc으로 의도만 표현

**예시**:
```java
@Entity
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Email email;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    protected User() {}

    // 정적 팩토리 - 시그니처만
    public static User register(Email email, Password password) {
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }

    // 비즈니스 메서드 - 시그니처만
    public void verifyEmail() {
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
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

| 계층 | 검증 항목 | 합격 기준 |
|------|----------|----------|
| Domain | Must Have 매핑 | 100% |
| Domain | Should Have 매핑 | 80% 이상 |
| Domain | Entity 불변식 | 100% |
| Domain | VO 유효성 검증 | 100% |
| Application | UseCase 커버리지 | 80% 이상 |
| Infrastructure | Parser 예외 처리 | 100% |
| Infrastructure | NOT NULL 준수 | 100% |
| Infrastructure | UNIQUE 준수 | 100% |
| Infrastructure | FK 무결성 | 100% |
| Interface | API 스펙 준수 | 100% |

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
- [ ] `{basePath}/validate/refined-requirements.md` 존재
- [ ] `{basePath}/validate/validation-report.md` 존재 (PASS 상태)

## MUST 체크리스트 (실행 후)
- [ ] Phase A: 도메인 질문 답변 완료
- [ ] Phase B: 아키텍처 스케치 작성 완료
- [ ] Phase C: 인터페이스/시그니처 정의 완료 (빈 구현)
- [ ] Phase D: 검증 완료 (Must Have 100%)
- [ ] `{basePath}/design/erd.md`, `{basePath}/design/domain-model.md` 생성
- [ ] `{basePath}/design/traceability-matrix.md` 생성
- [ ] context.json 업데이트: `status`를 "completed"로 변경

### 완료 시 context.json 업데이트

```json
{
  "phase": "design",
  "status": "completed",
  "updated_at": "{ISO8601}"
}
```
Edit: .atdd/context.json

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
설계 검증 완료 후 `/redteam-design` 실행

---

## Definition of Done (DoD)

**⚠️ 스킬 완료로 인정받기 위해 다음 조건을 모두 충족해야 함:**

| # | 조건 | 검증 |
|---|------|------|
| 1 | context.json `status` = "completed" | 필수 |
| 2 | context.json `updated_at` = 현재 시간 | 필수 |
| 3 | 산출물 파일 생성 완료 (`erd.md`, `domain-model.md`, `traceability-matrix.md`) | 필수 |
| 4 | 검증 완료 (Must Have 100%) | 필수 |

**context.json 업데이트 예시:**
```json
{
  "phase": "design",
  "status": "completed",
  "updated_at": "{ISO8601}"
}
```

---

## 참조
- 도메인 질문 가이드: [domain-questions.md](references/domain-questions.md)
- 빈 ERD 템플릿: [blank-erd-template.md](references/blank-erd-template.md)
- 빈 아키텍처 템플릿: [blank-architecture-template.md](references/blank-architecture-template.md)
- DDD 패턴: [ddd-patterns.md](references/ddd-patterns.md)
- Entity 템플릿: [entity-template.md](references/entity-template.md)
- 검증 가이드: [validation-guide.md](references/validation-guide.md)
- Context Helper: [context-helper.md](../shared/context-helper.md)
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
