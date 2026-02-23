# ATDD Harness - Workflows

## 전체 워크플로우

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────────────────────────────────────────────────┐     ┌─────────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│    /atdd    │ ──▶ │  /interview │ ──▶ │  /validate  │ ──▶ │ /epic-split │ ──▶ │   /gherkin  │ ──▶ │                       /design                           │ ──▶ │ /redteam-design │ ──▶ │  /compound  │ ──▶ │    /tdd     │ ──▶ │  /refactor  │ ──▶ │   /verify   │
│   Phase 0   │     │   Phase 1   │     │   Phase 2   │     │  Phase 2.1  │     │  Phase 2.2  │     │  ┌──────────┐    ┌───────────┐                         │     │   Phase 2.6     │     │  Phase 2.7  │     │  Phase 3    │     │   Phase 4   │     │   Phase 5   │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘     │  │   /adr   │◀──▶│  /redteam │  (반복 루프)            │     └─────────────────┘     └─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
       │                   │                   │                   │                   │             │  │ Phase2.5a│    │ Phase2.5b │                         │            │                    │                   │                   │                   │
       │                   │                   ▼                   ▼                   ▼             │  └──────────┘    └───────────┘                         │            ▼                    ▼                   ▼                   ▼                   ▼
       │                   │             validation/           epics/           scenarios/                      │                                              ├─ design-          ├─ episode.md     Inside-Out TDD      refactoring/        reports/
       │                   │               ├─ report.md      ├─ epics.md        ├─ *.feature                   ▼                                               │  critique-*.md    └─ tags          1. Entity Test       ├─ log.md           ├─ verification.md
       │                   │               └─ refined.md     └─ roadmap.md      └─ summary.md               design/                                         ├─ decisions.md
       │                   │                                                                 ├─ erd.md                                      └─ backlog.md
       │                   │                                                                 ├─ domain.md
       │                   │                                                                 ├─ traceability.md
       │                   │                                                                 ├─ validation.md
       │                   │                                                                 ├─ adr/               # ADR 문서들
       │                   │                                                                 │   ├─ 001-*.md
       │                   │                                                                 │   └─ index.md
       │                   │                                                                 ├─ redteam/           # Red Team 결과 (ADR)
       │                   │                                                                 │   ├─ critique-*.md
       │                   │                                                                 │   ├─ decisions.md
       │                   │                                                                 │   └─ backlog.md
       │                   │                                                                 ├─ *.sql
       │                   │                                                                 └─ *.java
       ▼                   ▼
   context.json      requirements/
                       ├─ draft.md
                       └─ log.md

                                              ┌─────────────────┐
                                              │  /internalize   │ ◀── 독립 실행
                                              │   Phase 2.8     │     Episode 복습
                                              └─────────────────┘     Active Recall
```

**Stop Hook**: `/atdd` → `/interview` 완료 후 자동으로 `/validate` → `/epic-split` → `/gherkin` 실행

**Red Team 계열 스킬 분담**:
- `/redteam` (Phase 2.5b): ADR(설계 의사결정) 비평 - Security, Performance, Scalability 등
- `/redteam-design` (Phase 2.6): 도메인 모델 비평 - Responsibility, Aggregate, Invariants 등 (RRAIRU)

---

## Phase 0: ATDD Entry Workflow

### 진입 조건
- 사용자가 `/atdd` 명령어 실행
- 새 프로젝트/기능 시작

### 실행 흐름

```
1. Topic 파라미터 확인
   ├─▶ /atdd {topic} → topic 사용
   ├─▶ /atdd --topic {topic} → topic 사용
   └─▶ /atdd (파라미터 없음) → AskUserQuestion으로 요청

2. Context 초기화
   ├─▶ .atdd/context.json 생성
   └─▶ topic, date, status, phase 저장

3. Interview 실행
   └─▶ Skill("interview", args=topic)

4. Interview Phase 대기
   ├─▶ Phase A에서 멈춤 → 사용자 입력 대기
   └─▶ Phase B에서 멈춤 → 사용자 입력 대기

5. Interview 완료 확인
   └─▶ {basePath}/interview/requirements-draft.md 존재 확인

6. Stop Hook 대기
   └─▶ Stop Hook이 자동으로 /validate 실행

7. Validate 완료 확인
   └─▶ validation-report.md + PASS → 세션 종료
```

### Stop Hook 동작

```
interview 완료 (requirements-draft.md 생성)
    ↓
Stop Hook 감지
    ↓
{"decision": "block", "reason": "Execute: Skill(\"validate\")"}
    ↓
validate 자동 실행
    ↓
validation-report.md + PASS
    ↓
{"decision": "allow"}
    ↓
세션 종료 👋
```

### 출력 예시

**context.json**
```json
{
  "topic": "payment-system",
  "date": "2026-02-21",
  "status": "in_progress",
  "phase": "interview",
  "created_at": "2026-02-21T10:00:00Z",
  "updated_at": "2026-02-21T10:00:00Z"
}
```

### 다음 단계
- Stop Hook에 의해 자동으로 `/validate` 실행
- 사용자는 다음 세션에서 `/design` 진행 가능

---

## Phase 2.7: Compound Workflow

### 진입 조건
- `/redteam-design` 완료
- `.atdd/context.json` 존재

### 실행 흐름

```
1. Context 로드
   └─▶ Read .atdd/context.json → date, topic 확인

2. 작업 경로 결정
   ├─▶ base_path = .atdd/design/{date}/{topic}
   └─▶ output_path = docs/learnings/episodes/{date}/{topic}/episode.md

3. Design 산출물 로드
   ├─▶ Glob .atdd/design/{date}/{topic}/adr/*.md → Read
   ├─▶ Glob .atdd/design/{date}/{topic}/redteam/*.md → Read
   ├─▶ Read .atdd/design/{date}/{topic}/erd.md
   ├─▶ Read .atdd/design/{date}/{topic}/domain-model.md
   └─▶ Read .atdd/design/{date}/{topic}/traceability-matrix.md

4. Episode 파일 생성
   ├─▶ Context 섹션 (ADR Context에서 추출)
   ├─▶ Decisions 섹션 (ADR Decision + Trade-off)
   ├─▶ Critique Feedback 섹션 (redteam + redteam-design)
   └─▶ Domain Model Result 섹션 (erd, domain-model)

5. Lessons Learned 수집
   └─▶ AskUserQuestion으로 배운 점 요청

6. Tags 추가
   └─▶ AskUserQuestion으로 태그 요청

7. Episode 파일 저장
   └─▶ Write docs/learnings/episodes/{date}/{topic}/episode.md

8. 완료 알림
   └─▶ "Episode 생성 완료. 다음 단계: /tdd"
```

### Episode 구조

```markdown
# Episode: [작업명]

## Meta
- **날짜**: {date}
- **관련 ADR**: [ADR 목록 링크]
- **요구사항**: [요구사항 링크]

---

## Context (맥락)
[ADR Context에서 추출]
- 어떤 문제/요구사항이 있었나?
- 도메인 상황

## Decisions (결정)
[ADR Decision + Trade-off에서 추출]

### ADR-001: [제목]
- **선택**: 무엇을 결정했나?
- **대안들**: 고려했던 선택지
- **이유**: 왜 이 결정을?

## Critique Feedback (비평 피드백)
[redteam + redteam-design에서 추출]

### Architecture (redteam)
| 이슈 | 관점 | 결정 | 비고 |
|------|------|------|------|

### Domain Model (redteam-design)
| 이슈 | 관점 | 결정 | 비고 |
|------|------|------|------|

## Domain Model Result (설계 결과)
[erd.md, domain-model.md 요약]

### 핵심 Entity
- Entity 목록

### 핵심 VO
- VO 목록

## Lessons Learned (배운 점)
1.
2.

## Tags
`#태그1` `#태그2`
```

### 출력 예시

**episode.md**
```markdown
# Episode: payment-system

## Meta
- **날짜**: 2026-02-19
- **관련 ADR**: [ADR-001](../../.atdd/design/2026-02-19/payment-system/adr/001-database.md)
- **요구사항**: [requirements-draft.md](../../.atdd/requirements/requirements-draft.md)

---

## Context (맥락)
결제 시스템에서 다중 통화 지원과 동시성 제어가 필요한 상황.
트래픽 1000 TPS 예상, 결제 실패 시 보상 트랜잭션 필요.

## Decisions (결정)

### ADR-001: 데이터베이스 선택
- **선택**: MySQL 8.0
- **대안들**: PostgreSQL, MongoDB
- **이유**: 팀 친숙도 높음, 트랜잭션 ACID 보장

## Critique Feedback (비평 피드백)

### Architecture (redteam)
| 이슈 | 관점 | 결정 | 비고 |
|------|------|------|------|
| SQL Injection 취약점 | Security | ACCEPT | Prepared Statement 적용 |

### Domain Model (redteam-design)
| 이슈 | 관점 | 결정 | 비고 |
|------|------|------|------|
| Payment의 상태 전이 | Invariants | ACCEPT | 상태 머신 패턴 적용 |

## Domain Model Result (설계 결과)

### 핵심 Entity
- Payment (Aggregate Root)
- PaymentMethod
- Refund

### 핵심 VO
- Money, Currency, PaymentStatus

## Lessons Learned (배운 점)

1. 낙관적 락은 충돌이 적을 때 유리하지만, 재시도 로직이 필수다.
2. Aggregate 경계를 어떻게 나누느냐가 트랜잭션 복잡도를 결정한다.

## Tags
`#결제` `#동시성` `#DDD` `#낙관적락`
```

---

## Phase 1: Interview Workflow

### 진입 조건
- 사용자가 기능 요청
- 새 프로젝트 시작
- 요구사항 정리 요청

### 실행 흐름

```
1. 사용자 아이디어 경청
   └─▶ "어떤 기능을 만들고 싶으신가요?"

2. 탐색 질문 (순차적)
   ├─▶ "비즈니스 목표가 무엇인가요?"
   ├─▶ "주요 사용자는 누구인가요?"
   ├─▶ "핵심 기능은 무엇인가요?"
   └─▶ "기술적 제약사항이 있나요?"

3. MoSCoW 분류
   ├─▶ Must have (필수)
   ├─▶ Should have (중요)
   ├─▶ Could have (선택)
   └─▶ Won't have (제외)

4. 산출물 생성
   ├─▶ .atdd/requirements/requirements-draft.md
   └─▶ .atdd/requirements/interview-log.md

5. 완료 알림
   └─▶ "요구사항 인터뷰 완료. 다음 단계: /validate"
```

---

## Phase 1.5: Epic Split Workflow

### 진입 조건
- `.atdd/requirements/requirements-draft.md` 존재

### 실행 여부 판단

| 조건 | 결과 |
|------|------|
| 기능 ≤ 3개 AND 예상 < 4시간 | ⏭️ 스킵 → /validate |
| 기능 ≥ 4개 OR 예상 ≥ 4시간 | ✅ 실행 |

### 실행 흐름

```
1. 요구사항 분석
   └─▶ Read requirements-draft.md

2. 기능 요구사항 개수 파악
   └─▶ 기능 개수, 예상 소요 시간 계산

3. 실행 여부 판단
   ├─▶ 기능 ≤ 3개 AND 예상 < 4시간 → 스킵
   └─▶ 기능 ≥ 4개 OR 예상 ≥ 4시간 → 실행

4. (스킵 시)
   └─▶ "요구사항이 작습니다. 바로 /validate를 실행하세요."

5. (실행 시) 도메인 기준 Epic 분해
   ├─▶ 도메인 경계 식별
   ├─▶ Entity 중심 그룹핑
   └─▶ CRUD 스트림 분리

6. 의존성 분석 및 순서 결정
   ├─▶ Entity 간 연관관계 파악
   ├─▶ 기능 의존성 파악
   └─▶ 구현 순서 결정

7. 산출물 생성
   ├─▶ epics.md
   └─▶ epic-roadmap.md

8. 완료 알림
   └─▶ "Epic 분해 완료. 첫 Epic부터 /validate 진행하세요."
```

### Epic 크기 기준

| 항목 | 권장 범위 |
|------|----------|
| Entity | 1~2개 |
| 기능 | 1개 CRUD 스트림 |
| 소요 시간 | 약 1시간 |

### Epic 목록 예시

**epics.md**
```markdown
# Epic 목록

## Epic 1: 사용자 관리
- **범위**: 사용자 CRUD
- **Entity**: User, Role
- **DoD**: 회원가입, 로그인, 로그아웃 E2E 테스트 통과
- **의존성**: 없음

## Epic 2: 주문 관리
- **범위**: 주문 생성, 조회, 취소
- **Entity**: Order, OrderItem
- **DoD**: 주문 CRUD E2E 테스트 통과
- **의존성**: Epic 1 (사용자)

## Epic 3: 결제 연동
- **범위**: 결제 요청, 확인
- **Entity**: Payment
- **DoD**: 결제 E2E 테스트 통과
- **의존성**: Epic 2 (주문)
```

### 출력 예시

**requirements-draft.md**
```markdown
# 요구사항 초안

## 프로젝트명
사용자 관리 시스템

## 비즈니스 목표
- 사용자 가입 및 관리 프로세스 자동화
- 보안 수준 향상

## 사용자 페르소나
- 일반 사용자: 회원가입, 로그인, 정보 수정
- 관리자: 사용자 관리, 권한 부여

## 기능 요구사항

### Must have
- [ ] 회원가입
- [ ] 로그인/로그아웃
- [ ] 비밀번호 암호화

### Should have
- [ ] 이메일 인증
- [ ] 비밀번호 찾기

### Could have
- [ ] 소셜 로그인
- [ ] 2단계 인증

### Won't have (이번 버전)
- [ ] SSO 연동

## 비기능 요구사항
- 응답 시간: 200ms 이하
- 동시 사용자: 1000명
```

---

## Phase 2: Validate Workflow

### 진입 조건
- `.atdd/requirements/requirements-draft.md` 존재

### 실행 흐름

```
1. 요구사항 로드
   └─▶ Read requirements-draft.md

2. Feasibility 검증
   ├─▶ 기술 스택 호환성 확인
   ├─▶ 외부 의존성 확인
   └─▶ 일정 내 구현 가능성 평가

3. Completeness 검증
   ├─▶ 누락 요구사항 체크
   ├─▶ 예외 케이스 확인
   └─▶ 비기능 요구사항 확인

4. Consistency 검증
   ├─▶ 요구사항 간 충돌 확인
   ├─▶ 모호한 표현 식별
   └─▶ 용어 일관성 확인

5. Dependencies 검증
   ├─▶ 외부 API 의존성
   ├─▶ DB 의존성
   └─▶ 타 시스템 연동

6. 산출물 생성
   ├─▶ validation-report.md (PASS/FAIL)
   └─▶ refined-requirements.md

7. 분기 처리
   ├─▶ PASS: "검증 통과. 다음 단계: /design"
   └─▶ FAIL: "검증 실패. 요구사항 수정 필요."
```

### 검증 리포트 예시

**validation-report.md**
```markdown
# 요구사항 검증 리포트

## 검증 일시
2024-01-15 14:30:00

## 검증 결과: ✅ PASS

## 세부 검증

### 1. Feasibility
| 항목 | 결과 | 비고 |
|------|------|------|
| 기술 스택 호환성 | ✅ | Spring Boot 3.x 호환 |
| 외부 의존성 | ✅ | 이메일 서비스 연동 가능 |
| 일정 | ✅ | 2주 내 구현 가능 |

### 2. Completeness
| 항목 | 결과 | 비고 |
|------|------|------|
| 누락 요구사항 | ⚠️ | 로그아웃 기능 명세 보완 필요 |
| 예외 케이스 | ✅ | 정의됨 |
| 비기능 요구사항 | ✅ | 응답 시간, 동시 사용자 명시 |

### 3. Consistency
| 항목 | 결과 | 비고 |
|------|------|------|
| 충돌 | ✅ | 없음 |
| 모호성 | ⚠️ | "빠른 응답" 구체화 필요 |
| 용어 | ✅ | 일관됨 |

### 4. Dependencies
| 의존성 | 결과 | 비고 |
|--------|------|------|
| MySQL | ✅ | 프로젝트 내 포함 |
| Redis | ✅ | 세션 저장용 추가 필요 |

## 조치 사항
1. 로그아웃 기능 명세 보완
2. "빠른 응답" → "200ms 이하"로 구체화
```

---

## Phase 2.5a: ADR Workflow

### 진입 조건
- `.atdd/requirements/refined-requirements.md` 존재
- 검증 리포트 PASS

### 실행 흐름

```
1. 요구사항 분석
   └─▶ Read refined-requirements.md

2. 결정 사항 식별
   ├─▶ 기술 스택 선택 필요 여부
   ├─▶ 데이터 모델링 결정 필요 여부
   ├─▶ API 설계 결정 필요 여부
   └─▶ 아키텍처 결정 필요 여부

3. ADR 번호 할당
   └─▶ 다음 순번 확인 (001, 002, ...)

4. ADR 작성 가이드
   ├─▶ 템플릿 제공
   ├─▶ 필수 섹션 안내
   └─▶ 작성 예시 제공

5. 사용자 ADR 작성
   ├─▶ Context: 결정 배경 작성
   ├─▶ Decision: 결정 사항 작성
   ├─▶ Alternatives: 대안 분석 작성
   └─▶ Consequences: 결과 작성

6. ADR 저장
   ├─▶ .atdd/design/adr/[번호]-[제목].md
   └─▶ .atdd/design/adr/index.md 업데이트

7. 완료 알림
   └─▶ "ADR 작성 완료. 다음 단계: /redteam"
```

### ADR 작성 예시

**001-데이터베이스-선택.md**
```markdown
# 001. 데이터베이스 선택

## Status
Proposed

## Context
사용자 관리 시스템의 데이터베이스를 선택해야 한다.
요구사항:
- 동시 사용자 1,000명 지원
- 응답 시간 200ms 이하
- 트랜잭션 무결성 필요

## Decision
MySQL 8.0을 사용한다.

## Alternatives Considered

### PostgreSQL
- 장점: JSON 지원 우수, 확장성 높음
- 단점: 팀 경험 부족
- 선택하지 않은 이유: 팀의 MySQL 경험이 풍부함

### MongoDB
- 장점: 스키마 유연성
- 단점: 트랜잭션 지원 제한적
- 선택하지 않은 이유: ACID 트랜잭션 필요

## Consequences

### 긍정적
- 팀의 MySQL 노하우 활용 가능
- 안정적인 운영 환경

### 부정적
- 수평 확장 시 복잡도 증가

### 위험
- 트래픽 급증 시 샤딩 필요
```

---

## Phase 2.5b: Red Team Workflow

### 진입 조건
- `.atdd/design/adr/*.md` 존재

### 실행 흐름

```
1. ADR 로드
   └─▶ Read .atdd/design/adr/*.md

2. 6관점 분석
   ├─▶ Security: 보안 취약점 검토
   ├─▶ Performance: 성능 이슈 검토
   ├─▶ Scalability: 확장성 검토
   ├─▶ Maintainability: 유지보수성 검토
   ├─▶ Business: 요구사항 충족도 검토
   └─▶ Reliability: 신뢰성 검토

3. 이슈 식별
   ├─▶ 잠재적 문제 목록화
   ├─▶ 심각도 분류 (HIGH/MEDIUM/LOW)
   └─▶ 개선 제안 작성

4. Critique Report 생성
   ├─▶ .atdd/design/redteam/critique-[번호].md
   └─▶ 이슈 요약 테이블 작성

5. 사용자 결정 수집
   ├─▶ 각 이슈에 대한 결정 요청
   ├─▶ ACCEPT/DEFER/REJECT 선택
   └─▶ .atdd/design/redteam/decisions.md 업데이트

6. 분기 처리
   ├─▶ ACCEPT 있음 → ADR 수정 → Step 1로 돌아감
   ├─▶ 모두 DEFER/REJECT → 다음 단계 진행
   └─▶ 완료 알림
```

### Critique Flow

```
┌─────────────────────────────────────────────────────────────────┐
│  Red Team Critique                                              │
│  ├─▶ Security 체크리스트 실행                                    │
│  ├─▶ Performance 체크리스트 실행                                 │
│  ├─▶ Scalability 체크리스트 실행                                 │
│  ├─▶ Maintainability 체크리스트 실행                             │
│  ├─▶ Business 체크리스트 실행                                    │
│  └─▶ Reliability 체크리스트 실행                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Critique Report 생성                                           │
│  ├─▶ 이슈 목록 (ID, 관점, 심각도, 설명, 제안)                     │
│  ├─▶ 요약 테이블                                                │
│  └─▶ 권장 사항                                                   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  사용자 결정                                                     │
│  ├─▶ ACCEPT → ADR 수정 → Critique 재실행                        │
│  ├─▶ DEFER → Backlog 추가 → 진행                                │
│  └─▶ REJECT → 거부 사유 기록 → 진행                              │
└─────────────────────────────────────────────────────────────────┘
                              │
                     모든 이슈 처리 완료
                              │
                              ▼
                      Phase 2.5: Design (Entity/Domain)
```

### Critique Report 예시

**critique-001.md**
```markdown
# Critique Report: ADR-001

## 개요
- **ADR**: 001. 데이터베이스 선택
- **검토 일시**: 2024-01-15 16:00:00
- **전체 위험도**: MEDIUM

## 이슈 목록

### [SEC-1] 비밀번호 저장 방식 미정의
- **관점**: Security
- **심각도**: HIGH
- **설명**: ADR에서 비밀번호 저장 방식이 명시되지 않음
- **영향**: 평문 저장 시 보안 사고 위험
- **제안**: bcrypt 또는 argon2 사용 명시

### [PERF-1] 인덱스 전략 미정의
- **관점**: Performance
- **심각도**: MEDIUM
- **설명**: 주요 쿼리 패턴에 대한 인덱스 전략 없음
- **영향**: 응답 시간 200ms 목표 달성 불가능할 수 있음
- **제안**: 이메일로 조회频繁 → email 컬럼 인덱스 추가

### [SCAL-1] 샤딩 전략 미정의
- **관점**: Scalability
- **심각도**: LOW
- **설명**: 트래픽 증가 시 샤딩 전략이 없음
- **영향**: 장기적으로 확장성 제약
- **제안**: 향후 ADR에서 다루거나 Backlog에 추가

## 요약

| 관점 | 이슈 수 | HIGH | MEDIUM | LOW |
|------|---------|------|--------|-----|
| Security | 1 | 1 | 0 | 0 |
| Performance | 1 | 0 | 1 | 0 |
| Scalability | 1 | 0 | 0 | 1 |
| Maintainability | 0 | 0 | 0 | 0 |
| Business | 0 | 0 | 0 | 0 |
| Reliability | 0 | 0 | 0 | 0 |
| **Total** | **3** | **1** | **1** | **1** |

## 권장 사항
1. **[필수]** 비밀번호 저장 방식 명시 (bcrypt 권장)
2. **[권장]** 인덱스 전략 추가
3. **[선택]** 샤딩 전략은 Backlog에 추가
```

### 사용자 결정 예시

**decisions.md**
```markdown
# 사용자 결정 로그

## ADR-001 Critique 결정 (2024-01-15)

### [SEC-1] 비밀번호 저장 방식 미정의
- **결정**: ACCEPT
- **이유**: 명백한 보안 취약점
- **조치**: ADR에 bcrypt 사용 명시 추가

### [PERF-1] 인덱스 전략 미정의
- **결정**: ACCEPT
- **이유**: 성능 목표 달성을 위해 필요
- **조치**: ADR에 인덱스 전략 섹션 추가

### [SCAL-1] 샤딩 전략 미정의
- **결정**: DEFER
- **이유**: MVP 단계에서는 과도한 엔지니어링
- **조치**: Backlog에 추가
```

---

## Phase 2.5: Design Workflow

### 진입 조건
- `.atdd/requirements/refined-requirements.md` 존재
- 검증 리포트 PASS

### Entity 설계 원칙: Rich Domain Model

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

### 실행 흐름

```
1. 요구사항 분석
   └─▶ Read refined-requirements.md

2. Entity 식별 (명사 분석)
   ├─▶ 사용자 → User Entity
   ├─▶ 권한 → Role Entity
   └─▶ 세션 → Session Entity

3. 관계 정의
   ├─▶ User : Role = N : 1
   ├─▶ User : Session = 1 : N
   └─▶ ERD 작성

4. DDL 생성
   ├─▶ sql/schema/001_user.sql
   ├─▶ sql/schema/002_role.sql
   └─▶ sql/schema/003_session.sql

5. JPA Entity 골격 생성 (인터페이스만)
   ├─▶ User.java (메서드 시그니처만, 빈 구현)
   ├─▶ Role.java
   └─▶ Session.java
   ※ 실제 구현은 /tdd Phase에서

6. Bounded Context 정의
   └─▶ User Context (회원 관리)

7. Aggregate 경계 설정
   └─▶ User Aggregate: User, Session

8. [검증] 요구사항-도메인 매핑 검증 ← 신규
   ├─▶ Must Have 요구사항 → Entity 메서드/VO 매핑 확인
   ├─▶ 검증 규칙 → @NotNull, @Size, 불변식 코드 확인
   └─▶ traceability-matrix.md 생성

9. [신규] SQL Sample Data 생성
   ├─▶ sql/data/001_user_data.sql
   ├─▶ sql/data/002_role_data.sql
   └─▶ 비즈니스 규칙 준수 데이터

10. [검증] SQL Sample Data 요구사항 준수 검증 ← 신규
    ├─▶ NOT NULL, UNIQUE, CHECK, FK 무결성 확인
    ├─▶ 비즈니스 규칙 준수 확인
    └─▶ design-validation-report.md 생성

11. 산출물 생성
    ├─▶ .atdd/design/erd.md
    ├─▶ .atdd/design/domain-model.md
    ├─▶ .atdd/design/traceability-matrix.md (신규)
    ├─▶ .atdd/design/design-validation-report.md (신규)
    └─▶ Entity Classes

12. 완료 알림
    └─▶ "설계 및 검증 완료. 다음 단계: /redteam-design"
```

### 검증 Flow

```
┌─────────────────────────────────────────────────────────────────┐
│  Step 8: 요구사항-도메인 매핑 검증                               │
│  ├─▶ Must Have 요구사항 → Entity 메서드/VO 매핑 확인             │
│  ├─▶ 검증 규칙 → @NotNull, @Size, 불변식 코드 확인               │
│  └─▶ traceability-matrix.md 생성                                │
└─────────────────────────────────────────────────────────────────┘
                              │
                    PASS (100% Must Have)
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 10: SQL Sample Data 검증                                  │
│  ├─▶ NOT NULL, UNIQUE, CHECK, FK 무결성 확인                    │
│  ├─▶ 비즈니스 규칙 준수 확인                                    │
│  └─▶ design-validation-report.md 생성                           │
└─────────────────────────────────────────────────────────────────┘
                              │
                           PASS
                              │
                              ▼
              Phase 2.6: /redteam-design (도메인 모델 비평)
```

### 합격 기준

| 검증 항목 | 기준 |
|-----------|------|
| Must Have 매핑 | 100% |
| Should Have 매핑 | 80% 이상 |
| NOT NULL 준수 | 100% |
| UNIQUE 준수 | 100% |
| FK 무결성 | 100% |
| 비즈니스 규칙 | 100% |

---

## Phase 2.6: Red Team Design Critique Workflow

### 진입 조건
- `.atdd/design/domain-model.md` 존재
- `.atdd/design/erd.md` 존재
- `/design` Phase D 완료

### 기존 `/redteam`과의 차이

| 항목 | /redteam | /redteam-design |
|------|----------|-----------------|
| 검토 대상 | ADR (설계 의사결정) | 도메인 모델 (Entity, VO, Aggregate) |
| 관점 | Security, Performance, Scalability, Maintainability, Business, Reliability | Responsibility, Requirements Fit, Aggregate Boundary, Invariants, Relationships, Ubiquitous Language |
| 목적 | 아키텍처/기술 결정 검증 | DDD 원칙 준수 검증 |

### 바람직한 어려움 적용

| 원칙 | 적용 방식 | 훈련 효과 |
|------|-----------|-----------|
| Self-Explanation | Self-Reflection 질문 | 메타인지 향상 |
| Contrastive Cases | 안티패턴 vs 권장 패턴 비교 | 좋은 설계 학습 |
| Feedback Loop | 즉각적 Critique Report | 실수 인지 및 수정 훈련 |
| Retrieval Practice | "왜 이렇게 설계했나요?" 질문 | 설계 지식 인출 |

### 실행 흐름

```
1. 설계 산출물 로드
   ├─▶ Read .atdd/design/erd.md
   ├─▶ Read .atdd/design/domain-model.md
   ├─▶ Read .atdd/requirements/refined-requirements.md
   └─▶ Read src/main/java/**/domain/entity/*.java

2. 6관점 분석 (RRAIRU)
   ├─▶ Responsibility: 책임 분배 적절성
   ├─▶ Requirements Fit: 요구사항 적합성
   ├─▶ Aggregate Boundary: Aggregate 경계 적절성
   ├─▶ Invariants: 불변식 완전성
   ├─▶ Relationships: 연관관계 설계 적절성
   └─▶ Ubiquitous Language: 보편 언어 일치

3. Self-Reflection 질문 준비
   ├─▶ "왜 이 메서드가 이 Entity에 위치해야 하나요?"
   ├─▶ "이 Entity들이 항상 함께 변경되나요?"
   └─▶ "이 상태 변경 시 항상 유효한가요?"

4. Critique Report 생성
   ├─▶ .atdd/design/redteam/design-critique-[날짜].md
   ├─▶ 이슈 목록 (ID, 관점, 심각도, 설명, 제안)
   └─▶ Self-Reflection 질문 포함

5. 사용자 Self-Reflection
   └─▶ 각 이슈에 대해 사용자에게 질문

6. 사용자 결정 수집
   ├─▶ ACCEPT: 설계 수정
   ├─▶ DEFER: Backlog 추가
   └─▶ REJECT: 거부 사유 기록

7. 분기 처리
   ├─▶ ACCEPT 있음 → 설계 수정 → Step 1로 돌아감
   ├─▶ 모두 DEFER/REJECT → 다음 단계 진행
   └─▶ 완료 알림
```

### Critique Flow

```
┌─────────────────────────────────────────────────────────────────┐
│  Red Team Design Critique (RRAIRU)                              │
│  ├─▶ Responsibility 체크리스트 실행                              │
│  ├─▶ Requirements Fit 체크리스트 실행                            │
│  ├─▶ Aggregate Boundary 체크리스트 실행                          │
│  ├─▶ Invariants 체크리스트 실행                                  │
│  ├─▶ Relationships 체크리스트 실행                               │
│  └─▶ Ubiquitous Language 체크리스트 실행                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Design Critique Report 생성                                    │
│  ├─▶ Self-Reflection Questions                                  │
│  ├─▶ 이슈 목록 (ID, 관점, 심각도, 안티패턴, 권장패턴)            │
│  ├─▶ 요약 테이블                                                │
│  └─▶ 권장 사항                                                   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  사용자 결정                                                     │
│  ├─▶ ACCEPT → 설계 수정 → Critique 재실행                       │
│  ├─▶ DEFER → Backlog 추가 → 진행                                │
│  └─▶ REJECT → 거부 사유 기록 → 진행                              │
└─────────────────────────────────────────────────────────────────┘
                              │
                     모든 이슈 처리 완료
                              │
                              ▼
                      Phase 2.5: Design (또는 Phase 2.2: Gherkin로 돌아가서 재검토)
```

### Design Critique Report 예시

**design-critique-2024-01-15.md**
```markdown
# Design Critique Report

## 개요
- **검토 일시**: 2024-01-15 16:00:00
- **검토 대상**: domain-model.md, erd.md, User.java
- **전체 위험도**: MEDIUM

---

## Self-Reflection Questions
설계를 다시 생각해볼 질문들입니다:

1. "User가 비밀번호 검증 책임을 직접 가져야 하나요?"
2. "User와 Session이 하나의 Aggregate여야 할까요?"
3. "상태 전이 시 불변식이 항상 유지되나요?"

---

## 이슈 목록

### [RESP-1] User Entity의 과도한 책임
- **관점**: Responsibility
- **심각도**: HIGH
- **설명**: User Entity가 로그인 검증, 상태 관리, 비밀번호 변경 등 너무 많은 책임을 가짐
- **현재 코드**:
  ```java
  // 안티패턴: God Entity
  public class User {
      public boolean validatePassword(String raw, PasswordEncoder encoder) { ... }
      public boolean canLogin() { ... }
      public void lock() { ... }
      public void changePassword(...) { ... }
      public void updateProfile(...) { ... }
  }
  ```
- **권장 패턴**:
  ```java
  // VO로 책임 분산
  public class User {
      @Embedded private LoginStatus loginStatus;
      @Embedded private Password password;

      public void login(Password input, PasswordEncoder encoder) {
          loginStatus.attemptLogin(password, input, encoder);
      }
  }
  ```
- **Self-Reflection**: "왜 이 메서드들이 User에 있어야 하나요? VO로 분리할 수 없나요?"
- **제안**: LoginStatus VO 생성, 로그인 관련 책임 이동

### [INV-1] 주문 취소 시 총액 재계산 누락
- **관점**: Invariants
- **심각도**: HIGH
- **설명**: Order.cancel()에서 총액을 0으로 설정하지 않음
- **Self-Reflection**: "취소된 주문의 총액이 0이어야 한다는 규칙이 보장되나요?"
- **제안**: cancel() 메서드에서 totalAmount = Money.zero() 추가

## 요약

| 관점 | 이슈 수 | HIGH | MEDIUM | LOW |
|------|---------|------|--------|-----|
| Responsibility | 1 | 1 | 0 | 0 |
| Requirements Fit | 0 | 0 | 0 | 0 |
| Aggregate Boundary | 0 | 0 | 0 | 0 |
| Invariants | 1 | 1 | 0 | 0 |
| Relationships | 0 | 0 | 0 | 0 |
| Ubiquitous Language | 0 | 0 | 0 | 0 |
| **Total** | **2** | **2** | **0** | **0** |

## 권장 사항
1. **[필수]** User Entity 책임 분산 (LoginStatus VO 생성)
2. **[필수]** Order.cancel() 불변식 보완
```

### 사용자 결정 예시

**decisions.md**
```markdown
# 사용자 결정 로그

## Design Critique 2024-01-15

### [RESP-1] User Entity의 과도한 책임
- **결정**: ACCEPT
- **이유**: 단일 책임 원칙 위반
- **조치**: LoginStatus VO 생성, 로그인 관련 로직 이동

### [INV-1] 주문 취소 시 총액 재계산 누락
- **결정**: ACCEPT
- **이유**: 불변식 누락으로 무효 상태 가능
- **조치**: cancel() 메서드에 totalAmount = Money.zero() 추가
```

### ERD 예시

**erd.md**
```markdown
# ERD

## Entity Relationship Diagram

\`\`\`
┌─────────────┐       ┌─────────────┐
│    User     │       │    Role     │
├─────────────┤       ├─────────────┤
│ id (PK)     │       │ id (PK)     │
│ email       │       │ name        │
│ password    │       │ description │
│ name        │       └─────────────┘
│ role_id(FK) │──N──1▶
│ created_at  │
│ updated_at  │
└─────────────┘
       │1
       │
       │N
       ▼
┌─────────────┐
│   Session   │
├─────────────┤
│ id (PK)     │
│ user_id(FK) │
│ token       │
│ expired_at  │
└─────────────┘
\`\`\`

## 테이블 정의

### User
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| password | VARCHAR(255) | NOT NULL |
| name | VARCHAR(100) | NOT NULL |
| role_id | BIGINT | FK (role.id) |
| created_at | DATETIME | NOT NULL |
| updated_at | DATETIME | NOT NULL |
| deleted_at | DATETIME | NULLABLE |
```

---

## Phase 2.2: Gherkin Workflow

### 진입 조건
- `.atdd/validate/refined-requirements.md` 존재

### 실행 흐름

```
1. 입력 로드
   └─▶ Read refined-requirements.md

2. User Story → Scenario 변환
   ├─▶ "회원가입" → Feature: 회원 관리
   └─▶ 각 기능별 Scenario 작성

3. Happy Path 작성
   ├─▶ 정상 회원가입
   ├─▶ 정상 로그인
   └─▶ 정상 로그아웃

4. Exception Path 작성
   ├─▶ 중복 이메일 회원가입
   ├─▶ 잘못된 비밀번호 로그인
   └─▶ 존재하지 않는 이메일 로그인

5. Background 정의
   └─▶ 공통 Given 절

6. Data Table 설계
   └─▶ 복잡한 입력/출력 데이터

7. 산출물 생성
   ├─▶ src/test/resources/features/user.feature
   └─▶ .atdd/scenarios/scenarios-summary.md

8. 완료 알림
   └─▶ "시나리오 작성 완료. 다음 단계: /adr (또는 /epic-split)"
```

### Feature File 예시

**user.feature**
```gherkin
Feature: 회원 관리

  Background:
    Given 데이터베이스가 초기화되어 있다
    And 다음 Role이 존재한다
      | id | name  |
      | 1  | USER  |
      | 2  | ADMIN |

  Scenario: 정상적인 회원가입
    When 회원가입 요청을 보낸다
      | email    | password123! | name   |
      | test@test.com | password123! | 테스터 |
    Then 상태 코드 201를 받는다
    And 응답의 "email" 필드는 "test@test.com"이다

  Scenario: 중복 이메일로 회원가입
    Given 다음 사용자가 존재한다
      | id | email         | password     | name |
      | 1  | test@test.com | password123! | 테스터 |
    When 회원가입 요청을 보낸다
      | email         | password     | name   |
      | test@test.com | password456! | 테스터2 |
    Then 상태 코드 409를 받는다

  Scenario Outline: 잘못된 형식으로 회원가입
    When 회원가입 요청을 보낸다
      | email      | password   | name       |
      | <email>    | <password> | <name>     |
    Then 상태 코드 400를 받는다

    Examples:
      | email          | password     | name   |
      |                | password123! | 테스터 |
      | invalid-email  | password123! | 테스터 |
      | test@test.com  | 123          | 테스터 |
      | test@test.com  | password123! |        |
```

---

## Phase 3: TDD Workflow (Inside-Out)

### 진입 조건
- `src/test/resources/features/**/*.feature` 존재
- `/compound` 완료

### TDD 사이클 (Inside-Out 접근)

```
┌──────────────────────────────────────────────────────────┐
│                 Inside-Out TDD Cycle                      │
│                                                           │
│   Layer 1: Entity (Domain Core)                           │
│   ├─▶ 비즈니스 로직 단위 테스트                            │
│   └─▶ Entity 구현                                         │
│                                                           │
│   Layer 2: Repository (Persistence)                       │
│   ├─▶ CRUD 통합 테스트                                    │
│   └─▶ Repository 구현                                     │
│                                                           │
│   Layer 3: Service (Application)                          │
│   ├─▶ 유스케이스 단위 테스트                              │
│   └─▶ Service 구현 (Entity 위임)                          │
│                                                           │
│   Layer 4: Controller/E2E (Interface)                     │
│   ├─▶ E2E 테스트 (Cucumber)                               │
│   └─▶ Controller 구현                                     │
└──────────────────────────────────────────────────────────┘
```

### 실행 흐름

```
1. Feature 파일 분석
   └─▶ Read *.feature files

2. [Inside-Out] Entity 단위 테스트 작성 (RED)
   ├─▶ 정적 팩토리 메서드 테스트
   ├─▶ 불변식(Invariant) 테스트
   ├─▶ 상태 전이 테스트
   └─▶ 테스트 실행 → 실패 확인

3. [Inside-Out] Entity 구현 (GREEN)
   ├─▶ Rich Domain Model 구현
   ├─▶ 비즈니스 메서드 구현
   └─▶ Entity 단위 테스트 통과

4. [Inside-Out] Repository 통합 테스트 작성 (RED)
   ├─▶ CRUD 테스트
   ├─▶ Query 테스트
   └─▶ 테스트 실행 → 실패 확인

5. [Inside-Out] Repository 구현 (GREEN)
   ├─▶ JPA Repository 구현
   └─▶ Repository 통합 테스트 통과

6. [Inside-Out] Service 단위 테스트 작성 (RED)
   ├─▶ 유스케이스 테스트
   ├─▶ Mock Repository 사용
   └─▶ 테스트 실행 → 실패 확인

7. [Inside-Out] Service 구현 (GREEN)
   ├─▶ 비즈니스 로직은 Entity에 위임
   ├─▶ 트랜잭션 관리
   └─▶ Service 단위 테스트 통과

8. Step Definition 생성 (RED)
   ├─▶ Given/When/Then 메서드 작성
   └─▶ 테스트 실행 → 실패 확인

9. Controller 구현 (GREEN)
   ├─▶ REST API 구현
   └─▶ E2E 테스트 통과

10. 커버리지 확인
    └─▶ 80% 이상 달성

11. 산출물 생성
    ├─▶ Entity Unit Tests
    ├─▶ Repository Integration Tests
    ├─▶ Service Unit Tests
    ├─▶ Step Definitions
    └─▶ Production Code

12. 완료 알림
    └─▶ "TDD 완료. 다음 단계: /refactor"
```

### Entity 단위 테스트 예시

```java
@DisplayName("User Entity 테스트")
class UserTest {

    @Test
    @DisplayName("정상적인 사용자 등록")
    void register_success() {
        // given
        Email email = Email.of("test@test.com");
        Password password = Password.of("password123!");

        // when
        User user = User.register(email, password);

        // then
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
    }

    @Test
    @DisplayName("이메일 인증 성공")
    void verifyEmail_success() {
        // given
        User user = User.register(Email.of("test@test.com"), Password.of("password123!"));

        // when
        user.verifyEmail();

        // then
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("이미 인증된 사용자는 인증 실패")
    void verifyEmail_alreadyVerified_throwsException() {
        // given
        User user = User.register(Email.of("test@test.com"), Password.of("password123!"));
        user.verifyEmail();

        // when & then
        assertThatThrownBy(user::verifyEmail)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("이미 인증된 사용자입니다.");
    }
}
```

### 테스트 실행 명령

```bash
# 단위 테스트
./gradlew test

# 통합 테스트
./gradlew integrationTest

# E2E 테스트 (Cucumber)
./gradlew cucumber

# 커버리지 리포트
./gradlew jacocoTestReport
```

---

## Phase 4: Refactor Workflow

### 진입 조건
- 모든 테스트 통과
- 커버리지 80% 이상

### 실행 흐름

```
1. 코드 로드
   ├─▶ Read src/main/java/**/*.java
   └─▶ Read src/test/java/**/*.java

2. Clean Code 검토
   ├─▶ Meaningful Names
   ├─▶ Small Functions
   ├─▶ No Side Effects
   ├─▶ SOLID Principles
   └─▶ DRY

3. DDD 패턴 검토
   ├─▶ Entity vs Value Object
   ├─▶ Aggregate 경계
   ├─▶ Repository 패턴
   └─▶ Domain Events

4. 리팩토링 실행
   ├─▶ 코드 정리
   ├─▶ 중복 제거
   └─▶ 구조 개선

5. 테스트 재실행
   └─▶ 리팩토링 후에도 테스트 통과

6. 산출물 생성
   ├─▶ .atdd/refactoring/REFACTORING-log.md
   └─▶ .atdd/refactoring/clean-code-checklist.md

7. 완료 알림
   └─▶ "리팩토링 완료. 다음 단계: /verify"
```

### Clean Code 체크리스트 예시

**clean-code-checklist.md**
```markdown
# Clean Code 체크리스트

## Meaningful Names
- [x] 의도를 드러내는 이름 사용
- [x] 발음 가능한 이름
- [x] 검색 가능한 이름
- [x] 헝가리안 표기법 미사용

## Functions
- [x] 함수는 한 가지 일만
- [x] 20줄 이하 유지
- [x] 들여쓰기 2단계 이하
- [x] 인자 3개 이하

## Comments
- [x] 주석 대신 코드로 의도 표현
- [x] 불필요한 주석 제거

## Formatting
- [x] 일관된 들여쓰기
- [x] 빈 줄로 개념 분리
- [x] 가로 길이 120자 이하

## SOLID
- [x] Single Responsibility Principle
- [x] Open/Closed Principle
- [x] Liskov Substitution Principle
- [x] Interface Segregation Principle
- [x] Dependency Inversion Principle

## DDD
- [x] Aggregate 경계 적절
- [x] Value Object 불변
- [x] Repository 인터페이스 도메인 언어 사용
```

---

## Phase 5: Verify Workflow

### 진입 조건
- 리팩토링 완료
- 모든 테스트 통과

### 실행 흐름

```
1. 전체 테스트 실행
   ├─▶ ./gradlew test
   ├─▶ ./gradlew integrationTest
   └─▶ ./gradlew cucumber

2. 커버리지 분석
   ├─▶ ./gradlew jacocoTestReport
   └─▶ 80% 이상 확인

3. 코드 품질 체크
   ├─▶ ./gradlew spotlessCheck
   └─▶ 0 errors 확인

4. Gherkin 시나리오 커버리지
   └─▶ 모든 시나리오 통과 확인

5. 산출물 생성
   ├─▶ .atdd/reports/VERIFICATION-report.md
   └─▶ .atdd/reports/coverage-report/

6. 완료 알림
   └─▶ "검증 완료. ATDD 사이클 종료."
```

### 검증 리포트 예시

**VERIFICATION-report.md**
```markdown
# 최종 검증 리포트

## 검증 일시
2024-01-20 16:00:00

## 검증 결과: ✅ ALL PASS

## 테스트 결과

### Unit Tests
```
./gradlew test

BUILD SUCCESSFUL
Tests: 45, Failures: 0, Skipped: 0
```

### Integration Tests
```
./gradlew integrationTest

BUILD SUCCESSFUL
Tests: 12, Failures: 0, Skipped: 0
```

### E2E Tests (Cucumber)
```
./gradlew cucumber

BUILD SUCCESSFUL
Scenarios: 15, Passed: 15, Failed: 0
```

## 커버리지

| Package | Line | Branch |
|---------|------|--------|
| domain | 92% | 88% |
| application | 85% | 80% |
| interfaces | 78% | 75% |
| **Total** | **85%** | **81%** |

## 코드 품질
- Lint Errors: 0
- Lint Warnings: 0

## 완료 조건 체크리스트
- [x] 모든 테스트 통과
- [x] 커버리지 ≥ 80%
- [x] Lint 에러 0개
- [x] 모든 Gherkin 시나리오 통과

## ATDD 사이클 완료 🎉
```

---

## Phase 2.8: Internalize Workflow (Active Recall)

### 진입 조건
- Episode 파일이 존재해야 함 (`docs/learnings/episodes/**/episode.md`)

### 실행 방법

```bash
/internalize                    # 전체 Episode 목록에서 선택
/internalize --recent           # 최근 30일 Episode 복습
/internalize --topic {키워드}   # 특정 주제 Episode 복습
/internalize {episode경로}      # 특정 Episode 직접 지정
```

### 실행 흐름

```
1. Episode 검색
   └─▶ Glob docs/learnings/episodes/**/episode.md

2. 매개변수 필터링
   ├─▶ 없음: 전체 목록 표시 (날짜순 정렬)
   ├─▶ --recent: 최근 30일 내 필터링
   ├─▶ --topic {키워드}: 태그/Context 검색
   └─▶ {경로}: 직접 지정

3. Episode 선택
   └─▶ AskUserQuestion으로 사용자가 선택

4. Phase 2: 문제 제시 (바람직한 어려움)
   ├─▶ Episode에서 Context 섹션만 추출
   ├─▶ 설계 문제 형태로 제시
   │   ├─▶ 상황 설명
   │   ├─▶ Q1: 핵심 Entity와 Aggregate 경계는?
   │   └─▶ Q2: 가장 중요한 Trade-off는?
   └─▶ AskUserQuestion: "정답을 보시겠습니까?"

5. Phase 3: 정답 리마인드
   ├─▶ Episode 결과 공개
   │   ├─▶ Decisions (설계 결정)
   │   ├─▶ Critique Feedback (비평 피드백)
   │   ├─▶ Domain Model Result (설계 결과물)
   │   └─▶ Lessons Learned (배운 점)
   └─▶ Self-Check 질문 제공
```

### Episode 필수 구조

| 섹션 | 용도 | Phase |
|------|------|-------|
| `## Context (맥락)` | 문제 제시용 | Phase 2 |
| `## Decisions (결정)` | 정답 공개용 | Phase 3 |
| `## Critique Feedback (비평 피드백)` | 정답 공개용 | Phase 3 |
| `## Domain Model Result (설계 결과)` | 정답 공개용 | Phase 3 |
| `## Lessons Learned (배운 점)` | 정답 공개용 | Phase 3 |
| `## Tags` | 검색용 | Phase 1 |

### 문제 제시 예시

```markdown
## 🎯 설계 문제

### 상황
결제 시스템에서 다중 통화 지원과 동시성 제어가 필요한 상황.
트래픽 1000 TPS 예상, 결제 실패 시 보상 트랜잭션 필요.

### 질문
이 상황에서 다음을 설계하세요:

1. **핵심 Entity와 Aggregate 경계는?**
   - 어떤 도메인 개념이 Entity가 되어야 할까요?
   - Aggregate Root는 무엇이고, 경계는 어디까지일까요?

2. **가장 중요한 Trade-off는 무엇이고, 어떤 선택을 해야 할까요?**
   - 고려해야 할 대안들은 무엇인가요?
   - 어떤 기준으로 결정해야 할까요?

---
💡 **스스로 생각한 후 아래로 스크롤하세요**
```

### 정답 리마인드 예시

```markdown
## 📖 설계 결과

### 설계 결정 (ADR)
- **선택**: MySQL 8.0 + 낙관적 락
- **대안들**: PostgreSQL, MongoDB
- **이유**: 팀 친숙도 높음, 트랜잭션 ACID 보장

### 비평 피드백 (Red Team)
| 이슈 | 관점 | 결정 | 비고 |
|------|------|------|------|
| SQL Injection 취약점 | Security | ACCEPT | Prepared Statement 적용 |
| Payment의 상태 전이 | Invariants | ACCEPT | 상태 머신 패턴 적용 |

### 설계 결과물
- Payment (Aggregate Root)
- PaymentMethod, Refund
- VO: Money, Currency, PaymentStatus

## 💡 교훈 (Lesson Learned)
1. 낙관적 락은 충돌이 적을 때 유리하지만, 재시도 로직이 필수다.
2. Aggregate 경계를 어떻게 나누느냐가 트랜잭션 복잡도를 결정한다.

---

## 🔄 복기 (Self-Check)

스스로에게 질문해보세요:

1. **내가 생각한 설계와 실제 결정의 차이는?**
2. **놓친 Trade-off가 있었나?**
3. **다음에 비슷한 문제를 만나면?**
```

### 다음 단계
- 독립 실행 (다른 Phase와 의존성 없음)
- 언제든지 `/internalize`로 복습 가능

---

## Hook을 통한 자동 전환

Hook 구성으로 Phase 간 자동 전환 알림 제공:

```json
{
  "hooks": [
    {
      "event": "tool_use",
      "tool": "Skill",
      "when": "interview",
      "after": [
        {
          "action": "notify",
          "message": "요구사항 인터뷰 완료. 다음 단계: /validate"
        }
      ]
    },
    {
      "event": "tool_use",
      "tool": "Skill",
      "when": "validate",
      "after": [
        {
          "action": "read_file",
          "path": ".atdd/validation/validation-report.md",
          "check": "status == 'PASS'",
          "on_success": {
            "action": "notify",
            "message": "검증 통과. 다음 단계: /design"
          },
          "on_failure": {
            "action": "notify",
            "message": "검증 실패. 요구사항 수정 필요."
          }
        }
      ]
    }
  ]
}
```

---

## Utility Workflows

### Monitor Workflow

### 진입 조건
- AWS CLI가 설정되어 있어야 함
- S3 버킷에 Loki 로그가 저장되어 있어야 함

### 실행 흐름

```
1. S3 로그 조회
   └─▶ aws s3 ls s3://${LOKI_BUCKET}/loki/

2. 에러 패턴 분석
   ├─▶ 5xx: HTTP 서버 에러
   ├─▶ Exception: Java Exception
   ├─▶ Timeout: 요청 시간 초과
   ├─▶ Connection: DB/외부 서비스 연결 실패
   └─▶ Business: 비즈니스 로직 에러

3. 우선순위 분류
   ├─▶ P0 (Critical): 500 에러 다발, 서비스 중단
   ├─▶ P1 (High): 반복되는 Exception
   ├─▶ P2 (Medium): 간헐적 Timeout
   └─▶ P3 (Low): 기타 경고

4. 리포트 생성
   └─▶ .atdd/runtime/errors/error-report-{YYYYMMDD-HHmmss}.md

5. 완료 알림
   └─▶ "에러 분석 완료. 상세 내용은 리포트 참조."
```

### 출력 예시

```
📊 에러 로그 분석 리포트
기간: 2026-02-15 00:00 ~ 2026-02-16 00:00
총 에러: 42건

🔴 Critical (P0): 3건
🟠 High (P1): 8건
🟡 Medium (P2): 15건
🟢 Low (P3): 16건

상세 리포트: .atdd/runtime/errors/error-report-20260216-143022.md
```

---

### Analyze Error Workflow

### 진입 조건
- `/monitor` 실행으로 에러 리포트가 생성되어 있어야 함

### 실행 흐름

```
1. 에러 컨텍스트 수집
   ├─▶ 스택트레이스
   ├─▶ 요청 컨텍스트
   └─▶ 관련 로그 (전후 5분)

2. 코드 분석
   ├─▶ 문제 발생 지점의 코드 확인
   ├─▶ 관련 의존성 파악
   └─▶ 테스트 코드 존재 여부 확인

3. 근본 원인 분석 (5 Whys)
   └─▶ Why를 5번 반복하여 근본 원인 추적

4. 수정 방안 도출
   ├─▶ 즉시 수정 (Quick Fix)
   ├─▶ 근본 수정 (Root Cause Fix)
   └─▶ 예방 조치 (Prevention)

5. 분석 리포트 생성
   └─▶ .atdd/runtime/errors/analysis-{error-id}.md

6. 완료 알림
   └─▶ "분석 완료. 다음 단계: /fix {error-id}"
```

### 5 Whys 예시

```
에러: NullPointerException at UserService.java:45

Q1: 왜 NPE가 발생했나? → userId 파라미터가 null
Q2: 왜 userId가 null이었나? → 클라이언트가 전송하지 않음
Q3: 왜 전송하지 않았나? → 로그인 상태 미확인
Q4: 왜 미확인했나? → 인증 가드 없음
Q5: 왜 인증 가드가 없나? → Security 설정 누락

근본 원인: Security 설정 누락
```

---

### Fix Workflow (Self-Healing)

### 진입 조건
- `/analyze-error {error-id}` 실행으로 분석 리포트가 생성되어 있어야 함
- Git working directory가 clean해야 함

### Self-Healing Flow

```
┌─────────────────────────────────────────────────────────────────┐
│  Phase 1: 준비                                                  │
│  ├─▶ 분석 리포트 로드                                           │
│  └─▶ 브랜치 생성: fix/claude-loki-error-{type}-{date}           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Phase 2: Gherkin 생성 (Red)                                    │
│  └─▶ 실패 시나리오를 Gherkin으로 변환                           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Phase 3: 테스트 작성 (Red)                                     │
│  ├─▶ Cucumber Step Definition 작성                              │
│  └─▶ 테스트 실행 → 실패 확인                                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Phase 4: 수정 코드 작성 (Green)                                │
│  ├─▶ 분석된 수정 방안으로 코드 수정                             │
│  └─▶ 테스트 실행 → 성공 확인                                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Phase 5: 검증                                                  │
│  ├─▶ ./gradlew test                                             │
│  ├─▶ ./gradlew cucumber                                         │
│  └─▶ 커버리지 80% 이상 확인                                     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Phase 6: PR 생성                                               │
│  ├─▶ 변경사항 커밋                                              │
│  ├─▶ Push to origin                                             │
│  └─▶ GitHub PR 생성                                             │
└─────────────────────────────────────────────────────────────────┘
```

### 브랜치 네이밍 규칙
```
fix/claude-loki-error-{error-type}-{YYYYMMDD}
```

예시:
- `fix/claude-loki-error-NullPointerException-20260216`
- `fix/claude-loki-error-SQLException-20260216`

### 검증 체크리스트

PR 생성 전 확인 사항:
- [ ] 모든 테스트 통과
- [ ] 커버리지 80% 이상
- [ ] 코드 스타일 검사 통과
- [ ] Gherkin 시나리오가 실제 에러 재현
- [ ] 수정 코드가 근본 원인 해결

---

### Commit Workflow

### 진입 조건
- 변경된 파일이 존재해야 함

### 실행 흐름

```
1. 변경사항 수집
   ├─▶ git status --short
   └─▶ git diff --stat

2. 커밋 계획 수립
   ├─▶ 파일들을 기능 단위로 그룹화
   └─▶ 커밋 메시지 초안 작성

3. 사용자 승인
   ├─▶ ✅ 예, 진행해주세요
   ├─▶ 🔄 계획 수정
   ├─▶ 📝 커밋 메시지 직접 편집
   └─▶ ❌ 취소

4. 순차적 커밋 실행
   └─▶ 각 그룹별로 git add + git commit
```

### 커밋 분할 전략

| 변경 유형 | 별도 커밋 |
|----------|----------|
| 새 기능 + 테스트 | 2개 커밋으로 분리 |
| 리팩토링 + 기능 | 2개 커밋으로 분리 |
| 설정 + 코드 | 2개 커밋으로 분리 |
| 여러 도메인 | 도메인별 분리 |

### 커밋 메시지 형식

```
<타입>(<스코프>): <요약>

[본문]

[푸터]
```

| 타입 | 설명 |
|------|------|
| feat | 새로운 기능 |
| fix | 버그 수정 |
| docs | 문서 변경 |
| style | 코드 포맷팅 |
| refactor | 코드 리팩토링 |
| test | 테스트 추가/수정 |
| chore | 빌드/도구 변경 |
| perf | 성능 개선 |
