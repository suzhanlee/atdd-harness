---
name: adr
description: This skill should be used when the user asks to "/adr", "ADR 작성", "설계 의사결정", "아키텍처 결정 문서화", or needs to document architecture decisions.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit, EnterPlanMode
references:
  - references/adr-template.md
  - references/adr-premortem-questions.md
  - references/adr-self-critique.md
  - references/adr-tradeoff-matrix.md
  - ../shared/context-helper.md
---

# ADR (Architecture Decision Record) 작성

## 목표
사용자가 직접 Architecture Decision Record를 작성하여 **설계 의사결정 능력**을 향상시킨다.
단순 문서화가 아닌, 깊은 사고와 분석을 통한 의사결정 훈련을 제공한다.

## 범위
이 스킬은 **의사결정 문서화**에 집중합니다.
- ✅ Pre-Mortem 분석
- ✅ Trade-off Matrix 작성
- ✅ ADR 본문 작성
- ✅ Self-Critique 수행
- ❌ 구현 가이드 제공 (별도 `/design`, `/tdd` 스킬 사용)

---

## Context Helper
- [context-helper.md](../shared/context-helper.md)

---

## Context 로드

ADR 작성 시작 전, context.json을 읽어서 작업 경로를 결정합니다.

```markdown
Read .atdd/context.json
```

경로 결정:
- `basePath = context.basePath` (또는 `.atdd/{date}/{topic}`으로 계산)
- `adrPath = {basePath}/adr`

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
Phase A (Pre-Mortem)    → 사용자 입력 대기 → "완료"/"다음" → Phase B
Phase B (Trade-off)     → 사용자 입력 대기 → "완료"/"다음" → Phase C
Phase C (ADR 작성)      → EnterPlanMode → 사용자 승인 → Phase D
Phase D (Self-Critique) → ADR 완료
```

### Phase A 종료 필수 문구
```
---
👆 Pre-Mortem 질문에 답변해주세요.
답변 완료 후 "완료" 또는 "다음"이라고 입력해주세요.
```

### Phase B 종료 필수 문구
```
---
👆 Trade-off Matrix를 완성해주세요.
완료 후 "완료" 또는 "다음"이라고 입력해주세요.
```

### Phase D 종료 필수 문구 (C등급 이하 시)
```
---
👆 AI Self-Critique 점수가 {점수}/25 ({등급})입니다.
수정 후 재평가를 받으시겠습니까? ("예" / "아니오, 진행")
```

---

## 4-Phase 워크플로우

### Phase A: Pre-Mortem (사전 실패 분석)

**목적**: 템플릿을 보기 전에 먼저 "실패를 상상"하여 깊은 사고 유도

**진행 방식**:
1. ADR 주제(결정 사항)를 사용자에게 확인
2. 아래 3가지 질문을 사용자에게 제시

**Pre-Mortem 질문**:

```
Q1: 이 결정이 1년 후 실패한다면 가장 가능성 높은 이유는?
    → 구체적이고 측정 가능한 실패 조건으로 작성

Q2: 실패 당시 상황은?
    → 트래픽, 팀 규모, 데이터 크기, 비즈니스 변화

Q3: 미리 알았다면 어떤 다른 선택을 했을까?
    → "만약 X를 알았다면 Y를 선택했을 것" 형식
```

**상세 가이드**: [adr-premortem-questions.md](references/adr-premortem-questions.md)

**Phase A 종료 후**:
- STOP Protocol 적용 → 사용자 입력 대기
- "완료" 또는 "다음" 입력 시 Phase B 진행

---

### Phase B: Trade-off Matrix (대안 분석)

**목적**: 최소 3개 대안을 객관적 기준으로 비교 평가

**진행 방식**:
1. 최소 3개 이상의 대안 식별
2. 평가 기준 선정 (3~5개)
3. Matrix 작성

**Trade-off Matrix 형식**:

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

**필수 요소**:
- 최소 3개 대안
- 각 대안의 실패 시나리오 포함
- 총점이 가장 높지 않은 대안 선택 시 이유 설명

**상세 가이드**: [adr-tradeoff-matrix.md](references/adr-tradeoff-matrix.md)

**Phase B 종료 후**:
- STOP Protocol 적용 → 사용자 입력 대기
- "완료" 또는 "다음" 입력 시 Phase C 진행

---

### Phase C: ADR 본문 작성

**목적**: Plan Mode에서 ADR 작성 계획을 수립하고 사용자 승인 후 작성

**진행 방식**:
1. `EnterPlanMode` 툴로 Plan Mode 진입
2. Plan 파일에 ADR 작성 계획 작성:
   - ADR 번호 및 제목
   - Phase A, B 결과 통합 방안
   - 각 섹션별 작성 내용 요약
3. 사용자 승인 후 ADR 파일 작성
4. Phase D 진행

**ADR 구조**:
```markdown
# [번호]. [제목]

## Metadata
## Pre-Mortem (Phase A 결과)
## Context
## Decision
## Trade-off Matrix (Phase B 결과)
## Alternatives Considered
## Consequences
## Reconsideration Trigger
```

**상세 템플릿**: [adr-template.md](references/adr-template.md)

**Phase C 완료 후**:
- Plan Mode로 전환하여 사용자 승인 대기
- 승인 후 ADR 파일 작성 및 Phase D 진행

---

### Phase D: AI Self-Critique (AI 평가)

**목적**: AI가 ADR 품질을 객관적으로 평가하고 구체적 개선 방안 제시

**진행 방식**:
1. AI가 작성된 ADR 분석
2. 5개 항목에서 AI 평가 수행
3. 평가 결과 및 개선 방안 출력
4. 역량 점수 저장

**AI 평가 항목 (5개)**:

| # | 항목 | AI 평가 기준 |
|---|------|-------------|
| 1 | Context 충분성 | 비즈니스 요구사항, 기술 제약사항, 팀 상황이 구체적으로 명시되었는가? |
| 2 | 대안 분석 깊이 | 3개 이상 대안이 고려되었는가? 각 대안의 장단점이 구체적인가? |
| 3 | Consequences 솔직성 | 부정적 결과, 위험, 위험 완화 전략이 포함되었는가? |
| 4 | Reconsideration 구체성 | 정량적 임계값으로 재검토 조건이 명시되었는가? |
| 5 | 설득력 | 결정의 논리적 근거가 명확한가? 예상 반론이 대응되었는가? |

**등급별 진행**:
| 총점 | 등급 | 진행 |
|------|------|------|
| 22-25 | A | `/redteam`으로 진행하세요 |
| 18-21 | B | `/redteam`으로 진행하세요 |
| 14-17 | C | 수정 후 재평가를 권장합니다. 계속 진행하시겠습니까? |
| 10-13 | D | 수정이 필요합니다. Phase C로 돌아가시겠습니까? |
| 5-9 | F | 재작성이 필요합니다. Phase A부터 다시 시작하시겠습니까? |

**Red Team과의 분담**:
| 구분 | AI Self-Critique (Phase D) | Red Team |
|------|---------------------------|----------|
| 평가 대상 | "어떻게 작성했는가" (형식, 깊이, 완전성) | "무엇을 결정했는가" (내용) |
| 관점 | 역량 평가 | 6관점 비평 (Security, Performance 등) |
| 결과 | 점수 (25점 만점) | 이슈 목록 |

**상세 가이드**: [adr-self-critique.md](references/adr-self-critique.md)

**Phase D 종료 후**:
- A/B등급: `/redteam` 진행 안내
- C등급 이하: STOP Protocol → 사용자 결정 대기

---

## 입력
- `.atdd/context.json` (작업 컨텍스트)
- `{basePath}/validate/refined-requirements.md`
- `{basePath}/validate/validation-report.md`

## 출력
- `{basePath}/adr/[번호]-[제목].md`
- `{basePath}/adr/index.md`

---

## 트리거
- `/adr` 명령어 실행
- `/design` Phase의 서브단계

---

## ADR이란?

Architecture Decision Record는 아키텍처 의사결정을 기록하는 가벼운 문서 형식이다.
각 ADR은 하나의 결정을 설명하며, 결정의 배경, 대안, 결과를 포함한다.

---

## 작성 프로세스 요약

### 1. ADR 번호 할당
```
001-데이터베이스-선택.md
002-인증-방식-결정.md
003-API-아키텍처.md
```

### 2. 결정 사항 식별
요구사항에서 아키텍처 결정이 필요한 영역 파악:
- 기술 스택 선택
- 데이터 모델링
- API 설계
- 보안 아키텍처
- 확장성 전략

### 3. 4-Phase 워크플로우 실행
1. **Phase A**: Pre-Mortem 질문 답변
2. **Phase B**: Trade-off Matrix 작성 (최소 3개 대안)
3. **Phase C**: ADR 본문 작성
4. **Phase D**: Self-Critique 수행 (평균 4점 이상 달성)

### 4. 저장 위치
```
{basePath}/adr/
├── 001-*.md
├── 002-*.md
└── index.md
```

---

## Status 값

| Status | 설명 |
|--------|------|
| **Proposed** | 제안됨, 아직 승인되지 않음 |
| **Accepted** | 승인됨, 현재 유효함 |
| **Deprecated** | 더 이상 권장되지 않음 |
| **Superseded** | 다른 ADR로 대체됨 |

---

## 예시: 데이터베이스 선택 ADR

```markdown
# 001. 데이터베이스 선택

## Metadata
| 항목 | 값 |
|------|-----|
| 작성자 | 홍길동 |
| 작성일 | 2024-01-15 |
| 검토일 | 2024-07-15 |
| Status | Proposed |

## Pre-Mortem

### Q1: 1년 후 실패 이유
- 단일 테이블이 1억 건을 초과하여 쿼리 성능 급저하
- 트래픽 급증 시 샤딩 복잡도로 인한 장애

### Q2: 실패 상황
- 트래픽: DAU 5,000 → DAU 500,000
- 데이터: 10GB → 500GB

### Q3: 다른 선택
- PostgreSQL: JSON 데이터가 많았다면

## Context
사용자 관리 시스템의 데이터베이스를 선택해야 한다.
요구사항:
- 동시 사용자 1,000명 지원
- 응답 시간 200ms 이하
- 트랜잭션 무결성 필요

## Decision
MySQL 8.0을 사용한다.

## Trade-off Matrix

| 평가기준 | MySQL | PostgreSQL | MongoDB |
|----------|-------|------------|---------|
| 성능     | 4     | 4          | 3       |
| 확장성   | 2     | 3          | 5       |
| 팀 친숙도| 5     | 2          | 1       |
| **총점** | **11**| **9**      | **9**   |

### 선택 설명
팀 친숙도가 높아 개발 속도 향상. 확장성은 추후 샤딩으로 보완.

## Consequences

### 긍정적
- 팀의 MySQL 노하우 활용 가능
- 안정적인 운영 환경

### 부정적
- 수평 확장 시 복잡도 증가

### 위험
- DAU 50만 돌파 시 샤딩 필요

## Reconsideration Trigger

| 조건 | 임계값 | 현재 | 재검토 |
|------|--------|------|--------|
| 트래픽 | DAU 500,000 | DAU 5,000 | ❌ |
| 데이터 | 100GB | 10GB | ❌ |

## Self-Critique Score

| # | 항목 | 점수 |
|---|------|------|
| 1 | Context 충분성 | 4 |
| 2 | 대안 분석 깊이 | 4 |
| 3 | Consequences 솔직성 | 5 |
| 4 | Reconsideration 구체성 | 4 |
| 5 | 설득력 | 4 |
| **총점** | | **21/25** |
```

---

## 다음 단계
ADR 작성 완료 후 `/redteam` 실행하여 설계 비평 수행

---

## Definition of Done (DoD)

**⚠️ 스킬 완료로 인정받기 위해 다음 조건을 모두 충족해야 함:**

| # | 조건 | 검증 |
|---|------|------|
| 1 | context.json `status` = "completed" | 필수 |
| 2 | context.json `updated_at` = 현재 시간 | 필수 |
| 3 | 산출물 파일 생성 완료 (`{ADR번호}-*.md`, `index.md`) | 필수 |
| 4 | Self-Critique 등급 B 이상 달성 | 필수 |

**context.json 업데이트 예시:**
```json
{
  "phase": "adr",
  "status": "completed",
  "updated_at": "{ISO8601}"
}
```

---

## MUST 체크리스트 (실행 전)
- [ ] context.json 존재
- [ ] `{basePath}/validate/refined-requirements.md` 존재

## MUST 체크리스트 (실행 후)
- [ ] Phase A: Pre-Mortem 답변 완료
- [ ] Phase B: Trade-off Matrix 작성 (최소 3개 대안)
- [ ] Phase C: ADR 파일 생성
- [ ] Phase D: Self-Critique 수행 (등급 B 이상)
- [ ] context.json 업데이트: `status`를 "completed"로 변경

### 완료 시 context.json 업데이트

```json
{
  "phase": "adr",
  "status": "completed",
  "updated_at": "{ISO8601}"
}
```
Edit: .atdd/context.json

---

## 참조
- ADR 템플릿: [adr-template.md](references/adr-template.md)
- Pre-Mortem 가이드: [adr-premortem-questions.md](references/adr-premortem-questions.md)
- Trade-off Matrix 가이드: [adr-tradeoff-matrix.md](references/adr-tradeoff-matrix.md)
- Self-Critique 가이드: [adr-self-critique.md](references/adr-self-critique.md)
- Context Helper: [context-helper.md](../shared/context-helper.md)
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
