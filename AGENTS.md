# ATDD Harness - Agent Definitions

## Phase 0: ATDD Entry Agent

### 목표
ATDD 파이프라인의 진입점 역할을 한다. `/interview`를 실행하고, 완료 후 Stop Hook이 자동으로 `/validate`를 트리거한다.

### 트리거
- `/atdd`
- `/atdd {topic}`
- `/atdd --topic {topic}`

### 입력
- `--topic` 파라미터 또는 첫 번째 인자로 작업명 (선택)

### 프로세스
1. **Topic 파라미터 확인**:
   - `--topic` 또는 첫 번째 인자 확인
   - 없으면 AskUserQuestion으로 작업명 요청
2. **Context 초기화**:
   - `.atdd/context.json` 생성 (없는 경우)
   - topic, date, status, phase 저장
3. **Interview 실행**:
   - `Skill("interview", args=topic)` 호출
4. **Stop Hook 대기**:
   - interview 완료 후 Stop Hook이 `/validate` 자동 실행

### Stop Hook 동작
```
interview 완료 (requirements-draft.md 생성)
    ↓
Stop Hook 감지 → {"decision": "block", "reason": "Execute: Skill(\"validate\")"}
    ↓
validate 자동 실행
    ↓
validation-report.md + PASS → {"decision": "allow"}
    ↓
세션 종료
```

### 참조 파일
- [SKILL.md](.claude/skills/atdd/SKILL.md)
- [Stop Hook 스크립트](.claude/scripts/atdd-stop-hook.sh)

### 출력
- `.atdd/context.json` (작업 컨텍스트)
- interview 산출물 (Phase 1 참조)
- validation 산출물 (Stop Hook으로 자동 실행)

### 상태 전이
완료 → `/validate` 자동 실행 (Stop Hook)

---

## Phase 1: Interview Agent

### 목표
사용자와의 대화를 통해 요구사항을 체계적으로 수집하고 정리한다.

### 입력
- 사용자의 초기 아이디어/요청
- `--topic {작업명}` 파라미터 (선택)

### 프로세스
1. **작업명 확인**:
   - `--topic` 파라미터 확인
   - 없으면 AskUserQuestion으로 작업명 요청
2. **Context 파일 생성**:
   - `.atdd/context.json` 생성
   - topic, date, status, phase 저장
3. **경청**: 사용자의 초기 아이디어를 충분히 경청
4. **탐색 질문**:
   - 비즈니스 목표가 무엇인가?
   - 주요 사용자는 누구인가?
   - 핵심 기능은 무엇인가?
   - 기술적 제약사항이 있는가?
5. **우선순위 분류**: MoSCoW 기법 적용
   - Must have: 필수 기능
   - Should have: 중요 기능
   - Could have: 있으면 좋은 기능
   - Won't have: 이번 제외 기능
6. **요구사항 초안 작성**

### Context 파일 구조
```json
{
  "topic": "payment-system",
  "date": "2026-02-19",
  "status": "in_progress",
  "phase": "interview",
  "created_at": "2026-02-19T10:00:00Z",
  "updated_at": "2026-02-19T10:00:00Z"
}
```

### 출력
- `.atdd/context.json` (작업 컨텍스트)
- `.atdd/requirements/requirements-draft.md`
- `.atdd/requirements/interview-log.md`

### 상태 전이
완료 → `/epic-split` (요구사항이 큰 경우) 또는 `/validate` 호출 가능

---

## Phase 1.5: Epic Split Agent

### 목표
큰 요구사항을 1시간 단위 Epic으로 분해하여 점진적 개발이 가능하게 한다.

### 실행 조건

| 조건 | 결과 |
|------|------|
| 기능 ≤ 3개 AND 예상 < 4시간 | ⏭️ 스킵 → /validate |
| 기능 ≥ 4개 OR 예상 ≥ 4시간 | ✅ 실행 |

### 입력
- `.atdd/requirements/requirements-draft.md`

### 프로세스
1. **요구사항 분석**: 기능 요구사항 개수 파악
2. **실행 여부 판단**: 위 조건 테이블 기준
3. **도메인 기준 Epic 분해**
   - 도메인 경계 식별
   - Entity 중심 그룹핑
   - CRUD 스트림 분리
4. **의존성 분석**: Entity 간 연관관계, 기능 의존성 파악
5. **구현 순서 결정**: 의존성 기반 우선순위

### Epic 크기 기준

| 항목 | 권장 범위 |
|------|----------|
| Entity | 1~2개 |
| 기능 | 1개 CRUD 스트림 |
| 소요 시간 | 약 1시간 |

### 참조 파일
- [SKILL.md](.claude/skills/epic-split/SKILL.md)
- [epic-templates.md](.claude/skills/epic-split/references/epic-templates.md)

### 출력
- `.atdd/requirements/epics.md` (Epic 목록)
- `.atdd/requirements/epic-roadmap.md` (구현 순서, 의존성)

### 상태 전이
- 스킵 → `/validate` 호출
- 분해 완료 → 첫 Epic부터 `/validate` 호출

---

## Phase 2: Validation Agent

### 목표
요구사항의 구현 가능성, 완전성, 일관성을 검증한다.

### 입력
- `.atdd/requirements/requirements-draft.md`

### 검증 항목
1. **Feasibility (구현 가능성)**
   - 기술 스택과 호환되는가?
   - 외부 의존성 해결 가능한가?
   - 일정 내 구현 가능한가?

2. **Completeness (완전성)**
   - 누락된 요구사항이 없는가?
   - 예외 케이스가 정의되었는가?
   - 비기능 요구사항이 포함되었는가?

3. **Consistency (일관성)**
   - 요구사항 간 충돌이 없는가?
   - 모호한 표현이 없는가?
   - 용어가 일관되게 사용되었는가?

4. **Dependencies (의존성)**
   - 외부 API 의존성
   - 데이터베이스 의존성
   - 타 시스템 연동

### 출력
- `.atdd/validation/validation-report.md`
- `.atdd/requirements/refined-requirements.md`

### 상태 전이
- 검증 통과 → `/design` 호출 가능
- 검증 실패 → 요구사항 수정 후 재검증

---

## Phase 2.5a: ADR Agent

### 목표
사용자가 직접 Architecture Decision Record(ADR)를 작성하여 **설계 의사결정 능력**을 향상시킨다.
단순 문서화가 아닌, 깊은 사고와 분석을 통한 의사결정 훈련을 제공한다.

### 바람직한 어려움 (Desirable Difficulties) 적용
Robert Bjork의 학습 원칙을 적용하여 인지 부하를 높이고 깊은 학습 유도:
- Pre-Mortem: 템플릿 보기 전 실패 상상
- Trade-off Matrix: 최소 3개 대안 강제 분석
- Self-Critique: 작성 후 자가 비평 과정

### 입력
- `.atdd/requirements/refined-requirements.md`
- `.atdd/validation/validation-report.md`

### 4-Phase 프로세스

#### Phase A: Pre-Mortem (사전 실패 분석)
**목적**: 템플릿을 보기 전에 먼저 "실패를 상상"하여 깊은 사고 유도

**질문**:
```
Q1: 이 결정이 1년 후 실패한다면 가장 가능성 높은 이유는?
Q2: 실패 당시 상황은? (트래픽, 팀 규모, 데이터 크기, 비즈니스 변화)
Q3: 미리 알았다면 어떤 다른 선택을 했을까?
```

**STOP Protocol**: Phase A 완료 후 사용자 입력 대기

---

#### Phase B: Trade-off Matrix (대안 분석)
**목적**: 최소 3개 대안을 객관적 기준으로 비교 평가

```markdown
| 평가기준 | 대안1 (선택) | 대안2 | 대안3 |
|----------|--------------|-------|-------|
| 성능     | ⭐⭐⭐      | ⭐⭐  | ⭐⭐⭐⭐ |
| 확장성   | ⭐⭐        | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| 팀 친숙도| ⭐⭐⭐⭐⭐   | ⭐    | ⭐⭐   |
| **총점** | **12**      | **8** | **13** |

### 선택 설명 (트레이드오프)
왜 총점이 낮은 대안을 선택했나요?
```

**STOP Protocol**: Phase B 완료 후 사용자 입력 대기

---

#### Phase C: ADR 본문 작성
**목적**: 템플릿 기반으로 ADR 본문 작성

Phase A, B 결과를 통합하여 아래 구조로 작성:
```markdown
# [번호]. [제목]

## Metadata
## Pre-Mortem (Phase A 결과)
## Context
## Decision
## Trade-off Matrix (Phase B 결과)
## Alternatives Considered
## Consequences
## Reconsideration Trigger (정량적 임계값)
## Self-Critique Score (Phase D 결과)
```

---

#### Phase D: Self-Critique (자가 평가)
**목적**: 작성한 ADR을 스스로 비평하여 품질 검증

| # | 질문 | 점수(1~5) |
|---|------|-----------|
| 1 | Context 충분성 (6개월 후 이해 가능?) | |
| 2 | 대안 분석 깊이 (정말 최선인가?) | |
| 3 | Consequences 솔직성 (부정적 결과 포함?) | |
| 4 | Reconsideration 구체성 (재검토 조건 명확?) | |
| 5 | 설득력 (반대 의견에 대응 가능?) | |

**품질 기준**: 평균 4점 이상 달성 시 Accept 가능

---

### 참조 파일
- [SKILL.md](.claude/skills/adr/SKILL.md)
- [adr-template.md](.claude/skills/adr/references/adr-template.md)
- [adr-premortem-questions.md](.claude/skills/adr/references/adr-premortem-questions.md)
- [adr-tradeoff-matrix.md](.claude/skills/adr/references/adr-tradeoff-matrix.md)
- [adr-self-critique.md](.claude/skills/adr/references/adr-self-critique.md)
- [context-helper.md](.claude/skills/shared/context-helper.md)

### 출력
- `.atdd/design/{date}/{topic}/adr/[번호]-[제목].md`
- `.atdd/design/{date}/{topic}/adr/index.md`

### 상태 전이
완료 (Self-Critique 평균 4점 이상) → `/redteam` 호출 가능

---

## Phase 2.5b: Red Team Agent

### 목표
Red Team 관점에서 ADR을 비판적으로 검토하여 설계 품질을 향상시킨다.

### 입력
- `.atdd/context.json` (작업 컨텍스트)
- `.atdd/design/{date}/{topic}/adr/*.md` (ADR 문서들)

### Red Team 6가지 관점

| 관점 | 초점 | 예시 질문 |
|------|------|-----------|
| **Security** | 보안 취약점 | "SQL Injection 가능한가?" |
| **Performance** | 성능 이슈 | "N+1 Query 문제가 있는가?" |
| **Scalability** | 확장성 제약 | "트래픽 10배 증가 시 문제는?" |
| **Maintainability** | 유지보수성 | "6개월 후 누가 유지보수할 것인가?" |
| **Business** | 요구사항 충족도 | "에지 케이스가 처리되었는가?" |
| **Reliability** | 신뢰성 | "장애 시 복구는 어떻게?" |

### 프로세스
1. **ADR 로드**
   - ADR 문서 읽기
   - 결정 사항 파악

2. **6관점 분석**
   - Security 체크리스트 실행
   - Performance 체크리스트 실행
   - Scalability 체크리스트 실행
   - Maintainability 체크리스트 실행
   - Business 체크리스트 실행
   - Reliability 체크리스트 실행

3. **이슈 식별 및 분류**
   - 잠재적 문제 식별
   - 심각도 평가 (HIGH/MEDIUM/LOW)
   - 개선 제안 작성

4. **Critique Report 생성**
   - 이슈 목록 작성
   - 요약 테이블 생성
   - 권장 사항 작성

5. **사용자 결정 대기**
   - ACCEPT/DEFER/REJECT 수집
   - 결정 로그 업데이트

### 사용자 결정 옵션

| 결정 | 동작 |
|------|------|
| **ACCEPT** | 비평 수용 → ADR 수정 → `/redteam` 재실행 |
| **DEFER** | 나중에 처리 → Backlog 추가 → 다음 단계 진행 |
| **REJECT** | 거부 → 거부 사유 문서화 → 다음 단계 진행 |

### 참조 파일
- [SKILL.md](.claude/skills/redteam/SKILL.md)
- [critique-perspectives.md](.claude/skills/redteam/critique-perspectives.md)
- [context-helper.md](.claude/skills/shared/context-helper.md)

### 출력
- `.atdd/design/{date}/{topic}/redteam/critique-[번호].md`
- `.atdd/design/{date}/{topic}/redteam/decisions.md`
- `.atdd/design/{date}/{topic}/redteam/backlog.md`

### 상태 전이
- 모든 이슈 처리 완료 → `/design` 계속 진행
- ACCEPT로 인한 ADR 수정 → `/redteam` 재실행

---

## Phase 2.5: Design Agent

### 목표
Entity와 DDD 도메인 모델을 설계하고 검증한다.

### Entity 설계 원칙: Rich Domain Model

**Entity에 비즈니스 로직을 포함** (Anemic Domain Model 지양)

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

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
}
```

### 입력
- `.atdd/requirements/refined-requirements.md`

### 프로세스
1. **Entity 식별**
   - 명사 분석으로 주요 엔티티 도출
   - 속성 및 타입 정의

2. **관계 정의**
   - 1:1 관계
   - 1:N 관계
   - N:M 관계

3. **DDL 스크립트 생성**
   - CREATE TABLE 문 작성
   - 인덱스 정의
   - 제약조건 정의

4. **JPA Entity 클래스 생성 (Rich Domain Model)**
   - @Entity, @Table 어노테이션
   - 필드 매핑
   - 연관관계 매핑
   - 비즈니스 메서드 포함

5. **Bounded Context 정의**
   - 도메인 경계 설정
   - Aggregate Root 식별

6. **Aggregate 경계 설정**
   - 트랜잭션 경계
   - 일관성 경계

7. **[검증] 요구사항-도메인 매핑 검증**
   - Must Have 요구사항 → Entity 메서드/VO 매핑 확인
   - 검증 규칙 → @NotNull, @Size, 불변식 코드 확인

8. **SQL Sample Data 생성**
   - 비즈니스 규칙 준수 데이터

9. **[검증] SQL Sample Data 요구사항 준수 검증**
   - NOT NULL, UNIQUE, CHECK, FK 무결성 확인
   - 비즈니스 규칙 준수 확인

### 검증 합격 기준

| 검증 항목 | 기준 |
|-----------|------|
| Must Have 매핑 | 100% |
| Should Have 매핑 | 80% 이상 |
| NOT NULL 준수 | 100% |
| UNIQUE 준수 | 100% |
| FK 무결성 | 100% |
| 비즈니스 규칙 | 100% |

### 참조 파일
- [ddd-patterns.md](.claude/skills/design/ddd-patterns.md)
- [entity-template.md](.claude/skills/design/entity-template.md)
- [validation-guide.md](.claude/skills/design/validation-guide.md)
- [context-helper.md](.claude/skills/shared/context-helper.md)

### 출력
- `.atdd/design/{date}/{topic}/erd.md`
- `.atdd/design/{date}/{topic}/domain-model.md`
- `.atdd/design/{date}/{topic}/traceability-matrix.md`
- `.atdd/design/{date}/{topic}/design-validation-report.md`
- `sql/schema/*.sql`
- `sql/data/*.sql`
- `src/main/java/**/domain/entity/*.java`

### 상태 전이
- 검증 통과 → `/redteam-design` 호출 가능
- 검증 실패 → 설계 수정 후 재검증

---

## Phase 2.6: Red Team Design Agent

### 목표
Red Team 관점에서 도메인 모델(Entity, VO, Aggregate, Domain Service)을 비판적으로 검토하여 DDD 설계 품질을 향상시킨다.

### 기존 `/redteam`과의 차이

| 항목 | /redteam | /redteam-design |
|------|----------|-----------------|
| 검토 대상 | ADR (설계 의사결정) | 도메인 모델 (Entity, VO, Aggregate) |
| 관점 | Security, Performance, Scalability 등 | Responsibility, Aggregate, Invariants 등 |
| 목적 | 아키텍처/기술 결정 검증 | DDD 원칙 준수 검증 |

### 바람직한 어려움 (Desirable Difficulties) 적용
- **Self-Explanation**: Self-Reflection 질문을 통해 "왜 이렇게 설계했나요?" 스스로 고민
- **Contrastive Cases**: 안티패턴 vs 권장 패턴 비교를 통해 좋은 설계 학습
- **Feedback Loop**: 즉각적 Critique Report로 실수 인지 및 수정 훈련
- **Retrieval Practice**: 설계 결정 이유를 설명하며 설계 지식 인출

### 입력
- `.atdd/context.json` (작업 컨텍스트)
- `.atdd/design/{date}/{topic}/erd.md` (ERD 문서)
- `.atdd/design/{date}/{topic}/domain-model.md` (도메인 모델)
- `.atdd/design/{date}/{topic}/traceability-matrix.md` (요구사항-도메인 추적 매트릭스)
- `.atdd/requirements/refined-requirements.md` (정제된 요구사항)
- `src/main/java/**/domain/entity/*.java` (Entity 클래스)

### 6가지 검토 관점 (RRAIRU)

| 관점 | 초점 | 예시 질문 |
|------|------|-----------|
| **R**esponsibility | 책임 분배 적절성 | "User가 비밀번호를 직접 검증하는 게 맞나?" |
| **R**equirements Fit | 요구사항 적합성 | "요구사항에 없는 필드가 추가되었나?" |
| **A**ggregate Boundary | Aggregate 경계 적절성 | "Order와 OrderItem이 별도 Aggregate여야 하나?" |
| **I**nvariants | 불변식 완전성 | "부분 취소 시 총액 재계산 로직이 있는가?" |
| **R**elationships | 연관관계 설계 적절성 | "양방향 연관관계가 정말 필요한가?" |
| **U**biquitous Language | 보편 언어 일치 | "코드의 `status`가 비즈니스 용어와 일치하는가?" |

### 프로세스
1. **설계 산출물 로드**: ERD, 도메인 모델, Entity 클래스 읽기
2. **6관점 분석 (RRAIRU)**: 각 관점에서 잠재적 문제 식별
3. **Critique Report 생성**: 이슈 목록, Self-Reflection 질문 포함
4. **Reflection Before Decision**: ACCEPT/DEFER/REJECT 전에 반영 방향 작성
5. **사용자 결정 대기**: 각 이슈에 대해 ACCEPT/DEFER/REJECT 수집

### Reflection Before Decision (증강 학습)
각 이슈를 ACCEPT/DEFER/REJECT하기 전에 **반영 방향을 스스로 생각**:

| 이슈 ID | 이슈 요약 | 반영 방향 (스스로 작성) | 결정 |
|---------|-----------|------------------------|------|
| [REQ-1] | Enum 불일치 | ________________________ | ☐ ACCEPT / DEFER / REJECT |
| [INV-2] | 상태 전이 규칙 | ________________________ | ☐ ACCEPT / DEFER / REJECT |

**목적**: Conceptual Inquiry 패턴으로 65% 학습 보존

### 사용자 결정 옵션

| 결정 | 동작 |
|------|------|
| **ACCEPT** | 비평 수용 → 설계 수정 → `/redteam-design` 재실행 |
| **DEFER** | 나중에 처리 → Backlog 추가 → 다음 단계 진행 |
| **REJECT** | 거부 → 거부 사유 문서화 → 다음 단계 진행 |

### 참조 파일
- [SKILL.md](.claude/skills/redteam-design/SKILL.md)
- [design-critique-perspectives.md](.claude/skills/redteam-design/references/design-critique-perspectives.md)
- [context-helper.md](.claude/skills/shared/context-helper.md)

### 출력
- `.atdd/design/{date}/{topic}/redteam/design-critique-[날짜].md`
- `.atdd/design/{date}/{topic}/redteam/decisions.md`
- `.atdd/design/{date}/{topic}/redteam/backlog.md`

### 상태 전이
- 모든 이슈 처리 완료 → `/compound` 호출 가능
- ACCEPT로 인한 설계 수정 → `/redteam-design` 재실행

---

## Phase 2.7: Compound Agent

### 목표
ADR, redteam, design, redteam-design 결과물을 합쳐서 **학습 Episode**를 생성한다.
이를 통해 사용자가 설계 과정에서 배운 것을 체계적으로 정리하고 컴파운드 효과를 얻는다.

### 트리거
- `/compound` 명령어 실행
- `/redteam-design` 완료 후 자동 제안

### 입력
- `.atdd/context.json` (현재 작업 컨텍스트)
- `.atdd/design/{date}/{topic}/adr/*.md` (ADR 문서들)
- `.atdd/design/{date}/{topic}/redteam/*.md` (Critique 문서들)
- `.atdd/design/{date}/{topic}/erd.md` (ERD)
- `.atdd/design/{date}/{topic}/domain-model.md` (도메인 모델)
- `.atdd/design/{date}/{topic}/traceability-matrix.md` (추적 매트릭스)

### 프로세스
1. **Context 로드**: `.atdd/context.json` 읽기
2. **작업 경로 결정**: date, topic으로 경로 계산
3. **Design 산출물 로드**: ADR, redteam, design 파일들 읽기
4. **Episode 파일 생성**: 템플릿에 맞춰 초안 작성
5. **Lessons Learned 수집**: AskUserQuestion으로 배운 점 요청
6. **Tags 추가**: AskUserQuestion으로 태그 요청
7. **Episode 파일 저장**: 최종 파일 저장

### 추출 규칙

#### ADR에서 추출
- **Context**: `## Context` 섹션에서 배경/문제 상황
- **Decision**: `## Decision` 섹션에서 최종 결정
- **Trade-off**: `## Trade-off Matrix` 섹션에서 대안들과 선택 이유
- **Consequences**: `## Consequences` 섹션에서 결과/위험

#### Redteam에서 추출
- **Critique 이슈**: 각 critique 파일에서 이슈 목록
- **결정**: `decisions.md`에서 ACCEPT/DEFER/REJECT 결정
- **비고**: 각 결정의 이유/사유

#### Redteam-design에서 추출
- **Design Critique 이슈**: RRAIRU 관점별 이슈
- **결정**: `decisions.md`에서 ACCEPT/DEFER/REJECT 결정
- **반영 방향**: 사용자가 작성한 반영 방향

#### Design에서 추출
- **Entity**: `domain-model.md`에서 Aggregate Root, Entity 목록
- **VO**: `domain-model.md`에서 Value Object 목록
- **관계**: `erd.md`에서 주요 관계

### 참조 파일
- [SKILL.md](.claude/skills/compound/SKILL.md)
- [context-helper.md](.claude/skills/shared/context-helper.md)
- [episode-template.md](docs/learnings/episode-template.md)

### 출력
- `docs/learnings/episodes/{date}/{topic}/episode.md`

### 상태 전이
완료 → `/gherkin` 호출 가능

---

## Phase 2.8: Internalize Agent

### 목표
저장된 Episode를 복습하여 설계 역량을 내재화한다.
**바람직한 어려움 (Desirable Difficulties)** 을 통해 실제 역량 향상을 도모한다.

### 트리거
- `/internalize`
- `/internalize --recent`
- `/internalize --topic {키워드}`
- `/internalize {episode경로}`
- "에피소드 복습", "설계 복습", "내재화", "Active Recall"

### 학습 이론
Robert Bjork의 **Desirable Difficulties** 적용:
- **Active Recall**: 문제를 먼저 보고 스스로 생각하기
- **Retrieval Practice**: 기억에서 정보를 인출하는 연습
- **Spacing Effect**: 시간 간격을 두고 복습

### 입력
- Episode 파일들 (`docs/learnings/episodes/**/episode.md`)

### 매개변수
| 매개변수 | 설명 |
|----------|------|
| 없음 | 전체 Episode 목록 표시 후 선택 |
| `--recent` | 최근 30일 내 Episode만 필터링 |
| `--topic {키워드}` | 태그/주제로 필터링 |
| `{경로}` | 특정 Episode 직접 지정 |

### 프로세스

#### Phase 1: Episode 선택
1. Episode 검색: `Glob docs/learnings/episodes/**/episode.md`
2. 매개변수에 따른 필터링
3. AskUserQuestion으로 사용자가 Episode 선택

#### Phase 2: 문제 제시 (바람직한 어려움)
1. Episode에서 **Context 섹션만** 추출
2. 문제 형태로 제시:
   - 상황 설명
   - 설계 질문 (Entity, Aggregate, Trade-off)
3. 사용자에게 생각할 시간 제공
4. AskUserQuestion으로 "정답을 보시겠습니까?" 확인

#### Phase 3: 정답 리마인드
1. Episode의 **결과**를 공개:
   - Decisions (설계 결정)
   - Critique Feedback (비평 피드백)
   - Domain Model Result (설계 결과물)
   - Lessons Learned (배운 점)
2. Self-Check 질문 제공

### 참조 파일
- [SKILL.md](.claude/skills/internalize/SKILL.md)
- [context-helper.md](.claude/skills/shared/context-helper.md)
- [episode-template.md](docs/learnings/episode-template.md)

### 출력
- 복습 세션 결과 (사용자 피드백)

### 상태 전이
독립 실행 (다른 Phase와 의존성 없음)

---

## Phase 3: Gherkin Agent

### 목표
요구사항을 Gherkin 시나리오로 변환한다.

### 입력
- `.atdd/requirements/refined-requirements.md`
- `.atdd/design/erd.md`
- `src/main/java/**/domain/entity/*.java`

### 프로세스
1. **User Story → Scenario 변환**
   - As a [role] → Feature
   - I want [feature] → Scenario
   - So that [benefit] → 검증 포인트

2. **Happy Path 작성**
   - 정상 흐름 시나리오
   - 기대 결과 검증

3. **Exception Path 작성**
   - 에러 케이스
   - 경계값 테스트
   - 예외 상황

4. **Background 정의**
   - 공통 Given 절
   - 테스트 데이터 설정

5. **Data Table 설계**
   - 복잡한 입력 데이터
   - 예상 결과 데이터

### Gherkin 키워드
- Feature: 기능 단위
- Background: 공통 전제조건
- Scenario: 테스트 시나리오
- Given: 전제조건
- When: 행동
- Then: 결과
- And/But: 추가 조건

### 출력
- `src/test/resources/features/**/*.feature`
- `.atdd/scenarios/scenarios-summary.md`

### 상태 전이
완료 → `/tdd` 호출 가능

---

## Phase 4: TDD Agent (Inside-Out)

### 목표
Inside-Out TDD 사이클을 통해 코드를 구현한다.

### 입력
- `src/test/resources/features/**/*.feature`
- `src/main/java/**/domain/entity/*.java`
- `.atdd/design/design-validation-report.md`

### TDD 사이클 (Inside-Out 접근)
1. **RED**: 실패하는 테스트 작성
2. **GREEN**: 최소 코드로 테스트 통과
3. **REFACTOR**: (Phase 5에서 진행)

### 프로세스 (Inside-Out 순서)
1. **Entity 단위 테스트 작성**
   - 정적 팩토리 메서드 테스트
   - 불변식(Invariant) 테스트
   - 상태 전이 테스트

2. **Entity 구현 (Rich Domain Model)**
   - 비즈니스 로직 포함
   - 테스트 통과 확인

3. **Repository 통합 테스트 작성**
   - CRUD 테스트
   - Query 테스트

4. **Repository 구현**
   - JPA Repository 구현
   - 테스트 통과 확인

5. **Service 단위 테스트 작성**
   - 유스케이스 테스트
   - Mock Repository 사용

6. **Service 구현**
   - 비즈니스 로직은 Entity에 위임
   - 테스트 통과 확인

7. **E2E 테스트 통과 확인**
   - Step Definition 생성
   - Controller 구현
   - Cucumber 테스트 통과

### 테스트 레이어
```
src/test/java/
├── unit/
│   ├── entity/        # Entity 단위 테스트 (비즈니스 로직)
│   └── service/       # Service 단위 테스트
├── integration/
│   └── repository/    # Repository 통합 테스트
└── e2e/               # E2E 테스트 (Cucumber)
```

### 출력
- `src/test/java/**/unit/entity/**/*.java` (Entity Tests)
- `src/test/java/**/integration/**/*.java` (Integration Tests)
- `src/test/java/**/unit/service/**/*.java` (Service Tests)
- `src/test/java/**/e2e/**/*.java` (Step Definitions)
- `src/main/java/**/*.java` (Production Code)

### 상태 전이
완료 → `/refactor` 호출 가능

---

## Phase 5: Refactor Agent

### 목표
Clean Code와 DDD 패턴으로 리팩토링한다.

### 입력
- `src/main/java/**/*.java`
- `src/test/java/**/*.java`

### Clean Code 원칙
1. **Meaningful Names**
   - 의도를 드러내는 이름
   - 발음 가능한 이름
   - 검색 가능한 이름

2. **Small Functions**
   - 함수는 한 가지 일만
   - 20줄 이하 유지
   - 들여쓰기 2단계 이하

3. **No Side Effects**
   - 순수 함수 지향
   - 예측 가능한 동작

4. **SOLID Principles**
   - Single Responsibility
   - Open/Closed
   - Liskov Substitution
   - Interface Segregation
   - Dependency Inversion

5. **DRY (Don't Repeat Yourself)**
   - 중복 코드 제거
   - 추상화 레벨 일치

### DDD 패턴 검토
1. **Entity vs Value Object**
   - 식별자 필요 → Entity
   - 불변성 → Value Object

2. **Aggregate 경계**
   - 일관성 경계 확인
   - 트랜잭션 크기 적정성

3. **Repository 패턴**
   - Collection-like 인터페이스
   - 도메인 언어 사용

4. **Domain Events**
   - 이벤트 발행 필요성
   - 느슨한 결합

### 참조 파일
- [clean-code.md](.claude/skills/refactor/clean-code.md)
- [ddd-tactical.md](.claude/skills/refactor/ddd-tactical.md)

### 출력
- `.atdd/refactoring/REFACTORING-log.md`
- `.atdd/refactoring/clean-code-checklist.md`
- 리팩토링된 소스 코드

### 상태 전이
완료 → `/verify` 호출 가능

---

## Phase 6: Verify Agent

### 목표
최종 검증을 수행하고 품질을 확인한다.

### 검증 항목
1. **Unit Tests**
   ```bash
   ./gradlew test
   ```

2. **Integration Tests**
   ```bash
   ./gradlew integrationTest
   ```

3. **E2E Tests (Cucumber)**
   ```bash
   ./gradlew cucumber
   ```

4. **Coverage**
   - 80% 이상 달성
   - JaCoCo 리포트 생성

5. **Lint**
   - 0 errors
   - Spotless 적용

### 프로세스
1. 전체 테스트 실행
2. 커버리지 분석
3. 코드 품질 체크
4. Gherkin 시나리오 커버리지 확인
5. 검증 리포트 작성

### 출력
- `.atdd/reports/VERIFICATION-report.md`
- `.atdd/reports/coverage-report/`

### 완료 조건
- [ ] 모든 테스트 통과
- [ ] 커버리지 ≥ 80%
- [ ] Lint 에러 0개
- [ ] 모든 Gherkin 시나리오 통과

### 상태 전이
완료 → ATDD 사이클 종료

---

## Utility Agents

### Commit Agent

#### 목표
Conventional Commits 규칙을 기반으로 **기능 단위의 작은 커밋**을 계획하고 한국어로 명확한 커밋 메시지를 작성한다.

#### 트리거
- `/commit`, `/ci`
- "커밋해줘", "commit", "변경사항 커밋"

#### 핵심 원칙
- **Atomic Commits**: 한 커밋 = 하나의 논리적 변경
- 커밋은 독립적으로 되돌릴 수 있어야 함

#### 커밋 크기 가이드라인

| 지표 | 권장 범위 |
|------|----------|
| 파일 수 | 1~5개 |
| 라인 변경 | 50~300줄 |
| 커밋 요약 | 50자 이내 |

#### 프로세스
1. **변경사항 수집**: `git status`, `git diff`로 파일 분석
2. **커밋 계획 수립**: 파일들을 기능 단위로 그룹화
3. **사용자 승인**: 계획 확인 후 진행
4. **순차적 커밋 실행**: 각 그룹별로 커밋

#### 참조 파일
- [SKILL.md](.claude/skills/commit/SKILL.md)

---

### Monitor Agent

#### 목표
운영 환경의 에러 로그를 S3(Loki 저장소)에서 조회하고 분석하여 우선순위별로 분류한다.

#### 트리거
- `/monitor`
- "운영 로그 확인해줘", "에러 로그 분석해줘", "최근 에러 있어?"

#### 전제 조건
- AWS CLI가 설정되어 있어야 함
- S3 버킷에 Loki 로그가 저장되어 있어야 함

#### 에러 분류
- **5xx**: HTTP 서버 에러 (500, 502, 503, 504)
- **Exception**: Java Exception (NullPointerException, SQLException 등)
- **Timeout**: 요청 시간 초과
- **Connection**: DB/외부 서비스 연결 실패
- **Business**: 비즈니스 로직 에러

#### 우선순위 정렬

| 우선순위 | 기준 |
|----------|------|
| P0 (Critical) | 500 에러 다발, 서비스 중단 |
| P1 (High) | 반복되는 Exception |
| P2 (Medium) | 간헐적 Timeout |
| P3 (Low) | 기타 경고 |

#### 출력
- `.atdd/runtime/errors/error-report-{YYYYMMDD-HHmmss}.md`

#### 참조 파일
- [SKILL.md](.claude/skills/monitor/SKILL.md)

---

### Analyze Error Agent

#### 목표
특정 에러를 심층 분석하고 5 Whys 기법으로 근본 원인을 파악하여 수정 방안을 제안한다.

#### 트리거
- `/analyze-error {error-id}`
- "이 에러 분석해줘", "ERR-001 원인 찾아줘"

#### 전제 조건
- `/monitor` 실행으로 에러 리포트가 생성되어 있어야 함

#### 프로세스
1. **에러 컨텍스트 수집**: 스택트레이스, 요청 컨텍스트, 관련 로그
2. **코드 분석**: 문제 발생 지점의 코드 확인
3. **근본 원인 분석 (5 Whys)**: Why를 5번 반복하여 근본 원인 추적
4. **수정 방안 도출**:
   - 즉시 수정 (Quick Fix)
   - 근본 수정 (Root Cause Fix)
   - 예방 조치 (Prevention)

#### 5 Whys 예시
```
에러: NullPointerException at UserService.java:45

Q1: 왜 NPE가 발생했나? → userId 파라미터가 null
Q2: 왜 userId가 null이었나? → 클라이언트가 전송하지 않음
Q3: 왜 전송하지 않았나? → 로그인 상태 미확인
Q4: 왜 미확인했나? → 인증 가드 없음
Q5: 왜 인증 가드가 없나? → Security 설정 누락

근본 원인: Security 설정 누락
```

#### 출력
- `.atdd/runtime/errors/analysis-{error-id}.md`

#### 다음 단계
- 자동 수정: `/fix {error-id}`

#### 참조 파일
- [SKILL.md](.claude/skills/analyze-error/SKILL.md)

---

### Fix Agent (Self-Healing)

#### 목표
에러 분석을 바탕으로 Gherkin 시나리오 생성 → 테스트 작성 → 수정 코드 구현 → PR 생성까지 자동화한다.

#### 트리거
- `/fix {error-id}`
- "이 에러 수정해줘", "자가 치유 실행해줘"

#### 전제 조건
- `/analyze-error {error-id}` 실행으로 분석 리포트가 생성되어 있어야 함
- Git working directory가 clean해야 함

#### Self-Healing 프로세스

```
Phase 1: 준비
  └─▶ 분석 리포트 로드, 브랜치 생성

Phase 2: Gherkin 생성 (Red)
  └─▶ 실패 시나리오를 Gherkin으로 변환

Phase 3: 테스트 작성 (Red)
  └─▶ Cucumber Step Definition 작성

Phase 4: 수정 코드 작성 (Green)
  └─▶ 분석된 수정 방안으로 코드 수정

Phase 5: 검증 (Green)
  └─▶ ./gradlew test, ./gradlew cucumber 실행

Phase 6: PR 생성
  └─▶ 커밋, Push, PR 생성
```

#### 브랜치 네이밍 규칙
```
fix/claude-loki-error-{error-type}-{YYYYMMDD}
```

#### 출력
- Feature 파일: `src/test/resources/features/fix-{error-id}.feature`
- 수정된 소스 코드
- PR URL
- `.atdd/runtime/fixes/fix-{error-id}.md`

#### 참조 파일
- [SKILL.md](.claude/skills/fix/SKILL.md)
