---
name: validate
description: 요구사항 검증을 수행한다. /interview 완료 후 /design 전에 실행. 기능 요구사항이 누락 없이 명세되었는지 확인이 필요할 때 사용. Epic 모드에서는 병렬 검증 지원. "요구사항 검증", "validate", "검증해줘" 요청에 사용.
disable-model-invocation: true
user-invocable: true
allowed-tools: Read, Grep, Glob
---

# 요구사항 검증

## 목표
요구사항의 구현 가능성, 완전성, 일관성을 검증한다.

## 입력 우선순위

1. `.atdd/requirements/epics.md` (존재 시 Epic별 검증 모드)
2. `.atdd/requirements/requirements-draft.md` (기본 모드)
3. `.atdd/requirements/interview-log.md` (참조용)

## 실행 모드

| 명령어 | 동작 |
|--------|------|
| `/validate` | 모든 미검증 Epic을 의존성 순서대로 검증 |
| `/validate 3` | Epic 3만 검증 (의존 Epic 완료 여부 체크) |
| `/validate --fast` | 의존성 무시하고 모든 Epic 병렬 검증 |

## 검증 프로세스

### 통합 검증 모드 (기본)

```
1. requirements-draft.md 로드
2. Feasibility 검증 (기술 스택, 외부 의존성, 일정/리소스)
3. Completeness 검증 (누락 요구사항, 예외 케이스, 비기능)
4. Consistency 검증 (충돌, 모호성, 용어)
5. Dependencies 검증 (외부 API, DB, 타 시스템)
6. 검증 리포트 작성
```

### Epic별 검증 모드

```
1. 실행 모드 파악 (전체/단일/--fast)
2. 미검증 Epic 목록 추출 및 위상 정렬
3. Epic ≥3개: validation-report-writer 병렬 실행
4. Epic <3개: 직접 순차 검증
5. 결과 취합 후 validation-summary.md 작성
```

> Epic 모드 상세 가이드: [references/epic-mode-guide.md](references/epic-mode-guide.md)

## 검증 항목

| 항목 | 설명 |
|------|------|
| Feasibility | 기술 스택 호환성, 외부 의존성, 일정/리소스 |
| Completeness | 기능 명세, 예외 케이스, 비기능 요구사항 |
| Consistency | 충돌, 모호성, 용어 통일 |
| Dependencies | 외부 API, DB, 타 시스템 연동 |
| Epic 전용 | 의존 Epic, 범위, DoD, Entity 설계 |

> 검증 항목 상세: [references/validation-criteria.md](references/validation-criteria.md)

## 출력 파일

| 모드 | 파일 |
|------|------|
| 통합 | `validation-report.md`, `refined-requirements.md` |
| Epic | `validation-reports/validation-report-epic-N.md`, `validation-summary.md` |

> 리포트 템플릿: [references/validation-templates.md](references/validation-templates.md)

## 분기 처리

### PASS
```
검증 통과 ✅
모든 검증 항목을 충족했습니다.
다음 단계: /design
```

### FAIL
```
검증 실패 ❌
다음 항목에 대한 조치가 필요합니다:
1. [조치 항목 1]
2. [조치 항목 2]

조치 후 /validate를 다시 실행하세요.
```

### Epic 모드 전체 완료
```
검증 완료 ✅

| Epic | 제목 | 결과 |
|------|------|------|
| 1 | 사용자 인증 | ✅ PASS |
| 2 | 훈련 기록 CRUD | ✅ PASS |

모든 Epic 검증 완료.
다음 단계: /design
```

### 의존성 미충족 (단일 Epic 모드)
```
Epic 3 검증 불가 ❌

의존 Epic이 아직 검증되지 않았습니다:
- Epic 2 (훈련 기록 CRUD): 미검증

먼저 `/validate 2` 또는 `/validate`를 실행하세요.
```

## MUST 체크리스트 (실행 전)

- [ ] 입력 파일 존재: requirements-draft.md 또는 epics.md
- [ ] (Epic 모드) 미검증 Epic 목록 파악

## MUST 체크리스트 (실행 후)

- [ ] Feasibility, Completeness, Consistency, Dependencies 검증 완료
- [ ] validation-report.md 또는 validation-report-epic-N.md 생성
- [ ] (FAIL 시) 조치 사항 명시
- [ ] 결과 판단: PASS → /design | FAIL → 수정 후 재실행

## 다음 단계

- 검증 통과 시 `/design` 실행
- 검증 실패 시 요구사항 수정 후 재검증

## 참조

- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
- 검증 항목 상세: [references/validation-criteria.md](references/validation-criteria.md)
- 리포트 템플릿: [references/validation-templates.md](references/validation-templates.md)
- Epic 모드 가이드: [references/epic-mode-guide.md](references/epic-mode-guide.md)
