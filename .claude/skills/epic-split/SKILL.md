---
name: epic-split
description: This skill should be used when the user asks to "/epic-split", "에픽 분해", "요구사항 쪼개", "Epic 나눠", or needs to split large requirements into manageable epics.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Write, Glob
---

# Epic 분해

## 목표
큰 요구사항을 1시간 단위 Epic으로 분해하여 점진적 개발이 가능하게 한다.

## 입력 파일 선택

context.json을 먼저 확인하여 적절한 요구사항 파일 선택:

```
1. context.json 읽기
   ├─▶ phase == "validate" AND status == "completed"
   │       → refined-requirements.md 사용
   └─▶ 그 외
           → requirements-draft.md 사용
```

| 조건 | 파일 경로 |
|------|----------|
| validate 완료 후 | `{basePath}/validate/refined-requirements.md` |
| interview 직후 | `.atdd/requirements/requirements-draft.md` |

## 실행 여부 판단

| 조건 | 결과 |
|------|------|
| 기능 ≤ 3개 AND 예상 < 4시간 | ⏭️ 스킵 → /validate |
| 기능 ≥ 4개 OR 예상 ≥ 4시간 | ✅ 실행 |

## 프로세스

```
1. 입력 파일 결정 및 분석
   ├─▶ context.json 확인 → validate 완료 여부 판단
   ├─▶ 적절한 요구사항 파일 선택 (refined-requirements.md 또는 requirements-draft.md)
   └─▶ 기능 요구사항 개수 파악

2. 실행 여부 판단
   ├─▶ 기능 ≤ 3개 AND 예상 < 4시간 → 스킵
   └─▶ 기능 ≥ 4개 OR 예상 ≥ 4시간 → 실행

3. (스킵 시)
   └─▶ "요구사항이 작습니다. 바로 /validate를 실행하세요." 메시지

4. (실행 시) 도메인 기준 Epic 분해
   ├─▶ 도메인 경계 식별
   ├─▶ Entity 중심 그룹핑
   └─▶ CRUD 스트림 분리

5. 의존성 분석 및 순서 결정
   ├─▶ Entity 간 연관관계 파악
   ├─▶ 기능 의존성 파악
   └─▶ 구현 순서 결정

6. 출력 파일 작성
   ├─▶ epics.md
   └─▶ epic-roadmap.md
```

## Epic 크기 기준

| 항목 | 권장 범위 |
|------|----------|
| Entity | 1~2개 |
| 기능 | 1개 CRUD 스트림 |
| 소요 시간 | 약 1시간 |

> 상세 템플릿: [references/epic-templates.md](references/epic-templates.md)

## 출력 파일

| 파일 | 설명 |
|------|------|
| `epics.md` | Epic 목록 (제목, 범위, Entity, DoD, 의존성) |
| `epic-roadmap.md` | 구현 순서, 의존성, 마일스톤 |

## 분기 처리

### SKIP (요구사항 작음)
```
요구사항 분석 결과 ⏭️

- 기능 개수: N개
- 예상 소요: X시간

요구사항이 충분히 작습니다.
바로 /validate를 실행하세요.
```

### PASS (분해 완료)
```
Epic 분해 완료 ✅

- Epic 개수: N개
- 예상 총 소요: X시간

각 Epic별로 순차적으로 /validate → /gherkin → /adr → /design → ... 실행하세요.
로드맵: epic-roadmap.md 참조
```

## ⚠️ 종료 전 필수 체크리스트

**스킬 종료 전 반드시 수행:**
- [ ] context.json의 `status`를 "completed"로 변경
- [ ] context.json의 `updated_at`을 현재 시간으로 변경
- [ ] 산출물 파일이 올바른 경로에 생성되었는지 확인

**❌ 위 체크리스트 미완료 시 스킬이 완료되지 않은 것으로 간주**

---

## MUST 체크리스트 (실행 전)

- [ ] context.json 확인 → validate 완료 여부 파악
- [ ] 적절한 요구사항 파일 존재 확인
- [ ] 기능 요구사항 개수 파악
- [ ] 실행 여부 판단 (기능 >= 4 OR 예상 >= 4시간)

## MUST 체크리스트 (실행 후)

- [ ] 각 Epic에 제목, 범위, Entity 목록, DoD 포함
- [ ] Epic 간 의존성 분석 완료
- [ ] epic-roadmap.md에 구현 순서 명시
- [ ] 결과: 분해 완료 → 첫 Epic부터 /validate 진행
- [ ] context.json 업데이트: `status`를 "completed"로 변경

### 완료 시 context.json 업데이트

```json
{
  "phase": "epic-split",
  "status": "completed",
  "updated_at": "{ISO8601}"
}
```
Edit: .atdd/context.json

## 다음 단계

- Epic 분해 완료 시 첫 번째 Epic부터 `/validate` 실행
- 각 Epic별로 `/validate → /gherkin → /adr → /redteam → /design → /tdd → /refactor → /verify` 수행
  > 참고: `/adr ↔ /redteam`은 반복 루프 (설계 품질 향상까지)

---

## Definition of Done (DoD)

**⚠️ 스킬 완료로 인정받기 위해 다음 조건을 모두 충족해야 함:**

| # | 조건 | 검증 |
|---|------|------|
| 1 | context.json `status` = "completed" | 필수 |
| 2 | context.json `updated_at` = 현재 시간 | 필수 |
| 3 | 산출물 파일 생성 완료 (`epics.md`, `epic-roadmap.md`) | 필수 |
| 4 | 각 Epic에 제목, 범위, Entity 목록, DoD 포함 | 필수 |

**context.json 업데이트 예시:**
```json
{
  "phase": "epic-split",
  "status": "completed",
  "updated_at": "{ISO8601}"
}
```

## 참조

- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
- Epic 템플릿: [references/epic-templates.md](references/epic-templates.md)
