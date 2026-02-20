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

## 입력
- `.atdd/requirements/requirements-draft.md`

## 실행 여부 판단

| 조건 | 결과 |
|------|------|
| 기능 ≤ 3개 AND 예상 < 4시간 | ⏭️ 스킵 → /validate |
| 기능 ≥ 4개 OR 예상 ≥ 4시간 | ✅ 실행 |

## 프로세스

```
1. requirements-draft.md 분석
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

각 Epic별로 순차적으로 /validate → /design → ... 실행하세요.
로드맵: epic-roadmap.md 참조
```

## MUST 체크리스트 (실행 전)

- [ ] requirements-draft.md 존재 확인
- [ ] 기능 요구사항 개수 파악
- [ ] 실행 여부 판단 (기능 >= 4 OR 예상 >= 4시간)

## MUST 체크리스트 (실행 후)

- [ ] 각 Epic에 제목, 범위, Entity 목록, DoD 포함
- [ ] Epic 간 의존성 분석 완료
- [ ] epic-roadmap.md에 구현 순서 명시
- [ ] 결과: 분해 완료 → 첫 Epic부터 /validate 진행

## 다음 단계

- Epic 분해 완료 시 첫 번째 Epic부터 `/validate` 실행
- 각 Epic별로 `/validate → /design → /gherkin → /tdd → /refactor → /verify` 수행

## 참조

- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
- Epic 템플릿: [references/epic-templates.md](references/epic-templates.md)
