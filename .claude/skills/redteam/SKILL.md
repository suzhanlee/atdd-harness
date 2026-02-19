---
name: redteam
description: This skill should be used when the user asks to "/redteam", "Red Team", "비판적 검토", "설계 비평", or needs to critically review design quality from 6 perspectives.
disable-model-invocation: true
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit, AskUserQuestion, EnterPlanMode
references:
  - references/critique-perspectives.md
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

## 입력
- `.atdd/design/adr/*.md` (ADR 문서들)

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
Read .atdd/design/adr/*.md
```

### 2. 6관점 분석
각 관점에서 ADR 검토:
- 잠재적 문제 식별
- 위험도 평가 (HIGH/MEDIUM/LOW)
- 개선 제안 작성

### 3. Critique Report 생성
```
.atdd/design/redteam/critique-[ADR번호].md
```

### 4. 사용자 결정 대기
각 이슈에 대해 사용자가 결정
- **ACCEPT**: 비평 수용, ADR 수정
- **DEFER**: 나중에 처리, Backlog 추가
- **REJECT**: 거부, 사유 문서화

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
→ .atdd/design/redteam/backlog.md에 추가
→ 다음 단계 진행
```

### REJECT (거부)
```
비평을 거부
→ .atdd/design/redteam/decisions.md에 거부 사유 기록
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

## 참조
- 6관점 체크리스트: [critique-perspectives.md](references/critique-perspectives.md)
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
