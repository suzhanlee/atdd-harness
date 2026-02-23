---
name: compound
description: This skill should be used when the user asks to "/compound", "Episode 생성", "배운 것 저장", "학습 정리", or needs to consolidate design artifacts into a learning episode.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit, AskUserQuestion
references:
  - ../shared/context-helper.md
---

# Compound Learning - Episode 생성

## 목표
ADR, redteam, design, redteam-design 결과물을 합쳐서 **학습 Episode**를 생성한다.
이를 통해 사용자가 설계 과정에서 배운 것을 체계적으로 정리하고 컴파운드 효과를 얻는다.

## 범위
이 스킬은 **Episode 생성**에 집중합니다.
- ✅ Design 산출물 통합
- ✅ Episode 파일 생성
- ✅ Lessons Learned 수집
- ✅ 태그 추가

---

## 입력
- `.atdd/context.json` (현재 작업 컨텍스트)
- `{basePath}/adr/*.md` (ADR 문서들 - **Self-Critique 점수 포함**)
- `{basePath}/redteam/*.md` (Critique 문서들)
- `{basePath}/design/erd.md` (ERD)
- `{basePath}/design/domain-model.md` (도메인 모델)
- `{basePath}/design/traceability-matrix.md` (추적 매트릭스)
- `{basePath}/interview/requirements-draft.md` (요구사항 초안)
- `{basePath}/validate/refined-requirements.md` (정제된 요구사항)
- `{basePath}/validate/validation-report.md` (검증 리포트)
- `{basePath}/scenarios/*.md` (시나리오 파일들)

## 출력
- `docs/learnings/episodes/{date}/{topic}/episode.md`

---

## 트리거
- `/compound` 명령어 실행
- `/redteam-design` 완료 후 자동 제안

---

## Context Helper
- [context-helper.md](../shared/context-helper.md)

---

## Episode 생성 프로세스

### 1. Context 로드

```markdown
Read .atdd/context.json
```

context.json이 없으면 에러 메시지 출력:
```
⚠️ 현재 작업 컨텍스트가 없습니다.
먼저 `/interview`를 실행하여 새 작업을 시작해주세요.
```

### 2. 작업 경로 결정

```markdown
basePath = context.basePath (또는 `.atdd/{date}/{topic}`으로 계산)
outputPath = `docs/learnings/episodes/{date}/{topic}/episode.md`
```

### 3. Design 산출물 로드

모든 관련 파일을 읽습니다:

```markdown
# ADR 파일들
Glob {basePath}/adr/*.md
→ 각 파일 Read

# Redteam 파일들
Glob {basePath}/redteam/*.md
→ 각 파일 Read

# Design 파일들
Read {basePath}/design/erd.md
Read {basePath}/design/domain-model.md
Read {basePath}/design/traceability-matrix.md

# Validate 파일들
Read {basePath}/validate/refined-requirements.md
Read {basePath}/validate/validation-report.md

# Interview 파일들
Read {basePath}/interview/requirements-draft.md

# Scenarios 파일들
Glob {basePath}/scenarios/*.md
→ 각 파일 Read
```

### 4. Episode 파일 생성

읽은 파일들을 기반으로 Episode를 작성합니다.

**Episode 구조**:
```markdown
# Episode: [작업명]

## Meta
- **날짜**: {date}
- **관련 ADR**: [ADR 목록 링크]
- **요구사항**: [요구사항 링크]

---

## Competency Scores (역량 점수)

### ADR Self-Critique 점수
| ADR 번호 | Context | 대안분석 | Consequences | Reconsideration | 설득력 | 총점 | 등급 |
|----------|---------|----------|--------------|-----------------|--------|------|------|
| ADR-001 | X/5 | X/5 | X/5 | X/5 | X/5 | XX/25 | A/B/C/D/F |

### 역량 성장 추세
| 일시 | 평균 점수 | 비고 |
|------|-----------|------|
| {date} | XX/25 | [이번 작업] |

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

### ADR-002: [제목]
...

## Critique Feedback (비평 피드백)
[redteam + redteam-design에서 추출]

### Architecture (redteam)
| 이슈 | 관점 | 결정 | 비고 |
|------|------|------|------|
| ... | ... | ... | ... |

### Domain Model (redteam-design)
| 이슈 | 관점 | 결정 | 비고 |
|------|------|------|------|
| ... | ... | ... | ... |

## Domain Model Result (설계 결과)
[erd.md, domain-model.md 요약]

### 핵심 Entity
- Entity 목록

### 핵심 VO
- VO 목록

## Lessons Learned (배운 점)
[AI가 요약 또는 사용자가 작성]

1.
2.

## Tags
`#태그1` `#태그2`
```

### 5. Lessons Learned 수집

Episode 초안 생성 후 **AskUserQuestion**으로 사용자에게 Lessons Learned를 요청합니다.

```markdown
AskUserQuestion:
  questions:
    - question: "이번 설계 과정에서 가장 인상 깊게 배운 점은 무엇인가요?"
      header: "Lessons"
      multiSelect: false
      options: []
    - question: "이번 작업과 관련된 태그를 추가해주세요 (예: #결제 #동시성 #DDD)"
      header: "Tags"
      multiSelect: false
      options: []
```

### 6. Episode 파일 저장

사용자 입력을 반영하여 최종 Episode 파일을 저장합니다.

```markdown
Write docs/learnings/episodes/{date}/{topic}/episode.md
```

---

## Episode 템플릿

상세 템플릿: [episode-template.md](../../../docs/learnings/episode-template.md)

---

## 추출 규칙

### ADR에서 추출
- **Self-Critique 점수**: `## AI Self-Critique 평가 결과` 섹션에서 각 항목별 점수와 총점 추출
- **Context**: `## Context` 섹션에서 배경/문제 상황 추출
- **Decision**: `## Decision` 섹션에서 최종 결정 추출
- **Trade-off**: `## Trade-off Matrix` 섹션에서 대안들과 선택 이유 추출
- **Consequences**: `## Consequences` 섹션에서 결과/위험 추출

### Redteam에서 추출
- **Critique 이슈**: 각 critique 파일에서 이슈 목록 추출
- **결정**: `decisions.md`에서 ACCEPT/DEFER/REJECT 결정 추출
- **비고**: 각 결정의 이유/사유

### Redteam-design에서 추출
- **Design Critique 이슈**: RRAIRU 관점별 이슈 추출
- **결정**: `decisions.md`에서 ACCEPT/DEFER/REJECT 결정 추출
- **반영 방향**: 사용자가 작성한 반영 방향

### Design에서 추출
- **Entity**: `domain-model.md`에서 Aggregate Root, Entity 목록
- **VO**: `domain-model.md`에서 Value Object 목록
- **관계**: `erd.md`에서 주요 관계

---

## ⚠️ 종료 전 필수 체크리스트

**스킬 종료 전 반드시 수행:**
- [ ] context.json의 `status`를 "completed"로 변경
- [ ] context.json의 `updated_at`을 현재 시간으로 변경
- [ ] 산출물 파일이 올바른 경로에 생성되었는지 확인

**❌ 위 체크리스트 미완료 시 스킬이 완료되지 않은 것으로 간주**

---

## MUST 체크리스트 (실행 전)
- [ ] context.json 존재
- [ ] `{basePath}/` 디렉토리 존재
- [ ] 최소 1개 이상의 ADR 파일 존재

## MUST 체크리스트 (실행 후)
- [ ] Episode 파일 생성 완료
- [ ] Lessons Learned 수집 완료
- [ ] Tags 추가 완료
- [ ] context.json 업데이트: `status`를 "completed"로 변경

### 완료 시 context.json 업데이트

```json
{
  "phase": "compound",
  "status": "completed",
  "updated_at": "{ISO8601}"
}
```
Edit: .atdd/context.json

---

## 예시 출력

```markdown
# Episode: payment-system

## Meta
- **날짜**: 2026-02-20
- **관련 ADR**: [ADR-001](../../.atdd/2026-02-20/payment-system/adr/001-database.md)
- **요구사항**: [requirements-draft.md](../../.atdd/2026-02-20/payment-system/interview/requirements-draft.md)

---

## Competency Scores (역량 점수)

### ADR Self-Critique 점수
| ADR 번호 | Context | 대안분석 | Consequences | Reconsideration | 설득력 | 총점 | 등급 |
|----------|---------|----------|--------------|-----------------|--------|------|------|
| ADR-001 | 4/5 | 3/5 | 5/5 | 4/5 | 4/5 | 20/25 | B |
| ADR-002 | 5/5 | 4/5 | 4/5 | 3/5 | 4/5 | 20/25 | B |

### 역량 성장 추세
| 일시 | 평균 점수 | 비고 |
|------|-----------|------|
| 2026-02-15 | 14/25 | 첫 ADR |
| 2026-02-20 | 20/25 | +6 향상 |

---

## Context (맥락)
결제 시스템에서 다중 통화 지원과 동시성 제어가 필요한 상황.
트래픽 1000 TPS 예상, 결제 실패 시 보상 트랜잭션 필요.

## Decisions (결정)

### ADR-001: 데이터베이스 선택
- **선택**: MySQL 8.0
- **대안들**: PostgreSQL, MongoDB
- **이유**: 팀 친숙도 높음, 트랜잭션 ACID 보장

### ADR-002: 동시성 제어 방식
- **선택**: Optimistic Lock (Version)
- **대안들**: Pessimistic Lock, Distributed Lock
- **이유**: 충돌 빈도 낮음, 성능 우선

## Critique Feedback (비평 피드백)

### Architecture (redteam)
| 이슈 | 관점 | 결정 | 비고 |
|------|------|------|------|
| SQL Injection 취약점 | Security | ACCEPT | Prepared Statement 적용 |
| N+1 Query 가능성 | Performance | DEFER | 모니터링 후 재검토 |
| 샤딩 미지원 | Scalability | DEFER | DAU 50만 돌파 시 재검토 |

### Domain Model (redteam-design)
| 이슈 | 관점 | 결정 | 비고 |
|------|------|------|------|
| Payment의 상태 전이 | Invariants | ACCEPT | 상태 머신 패턴 적용 |
| PaymentMethod 경계 | Aggregate | DEFER | 현재은 같은 Aggregate 유지 |

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

## 다음 단계

### [권장] 설계 내재화
- `/internalize` 실행하여 이번 설계를 복습하고 내재화하세요.
- **Active Recall** 방식으로 역량을 강화합니다.

### 다음 Phase 진행
- `/gherkin` 실행하여 테스트 시나리오 작성.

---

## Definition of Done (DoD)

**⚠️ 스킬 완료로 인정받기 위해 다음 조건을 모두 충족해야 함:**

| # | 조건 | 검증 |
|---|------|------|
| 1 | context.json `status` = "completed" | 필수 |
| 2 | context.json `updated_at` = 현재 시간 | 필수 |
| 3 | 산출물 파일 생성 완료 (`episode.md`) | 필수 |
| 4 | Lessons Learned 수집 완료 | 필수 |

**context.json 업데이트 예시:**
```json
{
  "phase": "compound",
  "status": "completed",
  "updated_at": "{ISO8601}"
}
```

---

## 참조
- Context Helper: [context-helper.md](../shared/context-helper.md)
- Episode 템플릿: [episode-template.md](../../../docs/learnings/episode-template.md)
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
