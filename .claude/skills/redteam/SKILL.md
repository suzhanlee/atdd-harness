---
name: redteam
description: This skill should be used when the user asks to "/redteam", "Red Team", "비판적 검토", "설계 비평", or needs to critically review design quality from 6 perspectives.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit, AskUserQuestion, EnterPlanMode
references:
  - references/critique-perspectives.md
  - ../shared/context-helper.md
---

# Red Team Critique

## 목표
Red Team 관점에서 ADR(설계 결정)을 비판적으로 검토하여 설계 품질을 향상시킨다.

## 범위
이 스킬은 **설계 비평**에 집중합니다.
- ✅ 6가지 관점에서 설계 검토
- ✅ Critique Report 작성
- ✅ 개선 제안
- ❌ 구현 가이드 제공 (별도 `/design`, `/tdd` 스킬 사용)

---

## STOP PROTOCOL

### ⚠️ 종료 전 필수 체크리스트

**스킬 종료 전 반드시 수행:**
- [ ] context.json의 **최상위 `status`**를 "completed"로 변경
- [ ] context.json의 `updated_at`을 현재 시간으로 변경
- [ ] 산출물 파일이 올바른 경로에 생성되었는지 확인

**❌ 위 체크리스트 미완료 시 Hook이 다음 skill을 트리거하지 않음**

### 완료 시 context.json 업데이트 예시

```json
{
  "phase": "redteam",
  "status": "completed",  // ⚠️ 반드시 "completed"로 변경
  "updated_at": "2026-02-24T21:30:00+09:00",
  "redteam": {
    "completed": true,
    ...
  }
}
```

---

## Context Helper
- [context-helper.md](../shared/context-helper.md)

---

## Context 로드

Red Team 시작 전, context.json을 읽어서 작업 경로를 결정합니다.

```markdown
Read .atdd/context.json
```

경로 결정:
- `basePath = context.basePath` (또는 `.atdd/{date}/{topic}`으로 계산)
- `adrPath = {basePath}/adr`
- `redteamPath = {basePath}/redteam`

context.json이 없으면 에러:
```
⚠️ 현재 작업 컨텍스트가 없습니다.
먼저 `/interview`를 실행하여 새 작업을 시작해주세요.
```

---

## 입력
- `.atdd/context.json` (작업 컨텍스트)
- `{basePath}/adr/*.md` (ADR 문서들)

## 출력
- `{basePath}/redteam/critique-[ADR번호].md`
- `{basePath}/redteam/decisions.md`
- `{basePath}/redteam/backlog.md`

---

## 트리거
- `/redteam` 명령어 실행
- `/adr` 완료 후 자동 제안

## Red Team이란?

Red Team은 설계에 대한 "악의적" 또는 "비판적" 관점을 취하여 잠재적인 문제를 사전에 발견하는 기법이다.
설계자가 놓칠 수 있는 약점, 보안 취약점, 확장성 문제 등을 찾아낸다.

## 6가지 검토 관점

| 관점 | 초점 | 예시 질문 |
|------|------|-----------|
| **Security** | 보안 취약점 | "SQL Injection 가능한가?" |
| **Performance** | 성능 이슈 | "N+1 Query 문제가 있는가?" |
| **Scalability** | 확장성 제약 | "트래픽 10배 증가 시 문제는?" |
| **Maintainability** | 유지보수성 | "6개월 후 누가 유지보수할 것인가?" |
| **Business** | 요구사항 충족도 | "에지 케이스가 처리되었는가?" |
| **Reliability** | 신뢰성 | "장애 시 복구는 어떻게?" |

## 상세 가이드
- 6가지 관점: [critique-perspectives.md](references/critique-perspectives.md)

## Critique 프로세스

### 1. ADR 로드
```
Read {basePath}/adr/*.md
```

### 2. 6관점 분석
각 관점에서 ADR 검토:
- 잠재적 문제 식별
- 위험도 평가 (HIGH/MEDIUM/LOW)
- 개선 제안 작성

### 3. Critique Report 생성
```
{basePath}/redteam/critique-[ADR번호].md
```

### 4. 사용자 결정 대기
각 이슈에 대해 사용자가 결정
- **ACCEPT**: 비평 수용, ADR 수정
- **DEFER**: 나중에 처리, Backlog 추가
- **REJECT**: 거부, 사유 문서화

### 5. 종료 처리
모든 결정 완료 후:
1. context.json의 `status`를 "completed"로 변경
2. `updated_at` 업데이트
3. 세션 종료 → Hook이 `/design` 자동 실행

## Critique Report 구조

```markdown
# Critique Report: ADR-[번호]

## 개요
- **ADR**: [ADR 제목]
- **검토 일시**: YYYY-MM-DD HH:mm
- **전체 위험도**: [HIGH | MEDIUM | LOW]

## 이슈 목록

### [SEC-1] 보안 이슈 제목
- **관점**: Security
- **심각도**: HIGH
- **설명**: 문제 설명
- **영향**: 어떤 영향이 있는가
- **제안**: 개선 방안

### [PERF-1] 성능 이슈 제목
- **관점**: Performance
- **심각도**: MEDIUM
- **설명**: 문제 설명
- **영향**: 어떤 영향이 있는가
- **제안**: 개선 방안

...

## 요약

| 관점 | 이슈 수 | HIGH | MEDIUM | LOW |
|------|---------|------|--------|-----|
| Security | 2 | 1 | 1 | 0 |
| Performance | 1 | 0 | 1 | 0 |
| Scalability | 0 | 0 | 0 | 0 |
| Maintainability | 1 | 0 | 0 | 1 |
| Business | 0 | 0 | 0 | 0 |
| Reliability | 1 | 1 | 0 | 0 |
| **Total** | **5** | **2** | **2** | **1** |

## 권장 사항
1. [가장 중요한 권장 사항]
2. [다음 우선순위]
3. ...
```

## 사용자 결정 프로세스

Critique Report를 받은 후, 각 이슈에 대해 결정:

### ACCEPT (수용)
```
비평을 수용하고 Plan Mode에서 수정 계획 수립
→ `EnterPlanMode` 툴로 Plan Mode 진입
→ 수정 계획 작성 (어떤 섹션을, 어떻게 수정할지)
→ 사용자 승인 후 ADR 수정
→ /redteam 재실행
```

### DEFER (보류)
```
나중에 처리
→ {basePath}/redteam/backlog.md에 추가
→ 다음 단계 진행
```

### REJECT (거부)
```
비평을 거부
→ {basePath}/redteam/decisions.md에 거부 사유 기록
→ 다음 단계 진행
```

## 결정 기록 예시

**decisions.md**
```markdown
# 사용자 결정 로그

## ADR-001 Critique 결정

### [SEC-1] SQL Injection 취약점
- **결정**: ACCEPT
- **이유**: 명백한 보안 취약점
- **조치**: Prepared Statement 사용하도록 ADR 수정

### [PERF-1] N+1 Query 가능성
- **결정**: DEFER
- **이유**: 현재 트래픽에서는 문제되지 않음
- **조치**: 성능 모니터링 후 필요시 개선

### [REL-1] 장애 복구 미정의
- **결정**: REJECT
- **이유**: MVP 단계에서는 과도한 엔지니어링
- **사유**: 안정화 후 별도 ADR로 다룰 예정
```

## 다음 단계
모든 결정 완료 후 `/design` 계속 진행 (Entity/Domain 구현)

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
- [ ] `{basePath}/adr/*.md` 파일 존재

## MUST 체크리스트 (실행 후)
- [ ] 6관점 분석 완료
- [ ] Critique Report 생성
- [ ] 각 이슈에 대해 ACCEPT/DEFER/REJECT 결정 수집
- [ ] context.json 업데이트: `status`를 "completed"로 변경

### 완료 시 context.json 업데이트

```json
{
  "phase": "redteam",
  "status": "completed",
  "updated_at": "{ISO8601}"
}
```
Edit: .atdd/context.json

---

## Definition of Done (DoD)

**⚠️ 스킬 완료로 인정받기 위해 다음 조건을 모두 충족해야 함:**

| # | 조건 | 검증 |
|---|------|------|
| 1 | context.json **최상위** `status` = "completed" | 필수 |
| 2 | context.json `phase` = "redteam" | 필수 |
| 3 | 산출물 파일 생성 완료 (`critique-*.md`, `decisions.md`) | 필수 |
| 4 | 6관점 분석 완료 (Security, Performance, Scalability, Maintainability, Business, Reliability) | 필수 |

**context.json 업데이트 예시:**
```json
{
  "phase": "redteam",
  "status": "completed",  // ⚠️ 최상위 레벨의 status
  "updated_at": "{ISO8601}"
}
```

---

## 참조
- 6관점 체크리스트: [critique-perspectives.md](references/critique-perspectives.md)
- Context Helper: [context-helper.md](../shared/context-helper.md)
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
