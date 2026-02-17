---
name: epic-split
description: 큰 요구사항을 1시간 단위 Epic으로 분해한다. 기능 ≥ 4개일 때 사용.
disable-model-invocation: true
user-invocable: true
allowed-tools: Read, Write, Glob
---

# Epic 분해

## 목표
큰 요구사항을 1시간 단위 Epic으로 분해하여 점진적 개발이 가능하게 한다.

## 입력
- `.atdd/requirements/requirements-draft.md`

## 트리거
- `/epic-split` 명령어 실행
- "에픽 분해해줘", "요구사항 쪼개줘", "사용자 스토리 만들어줘"
- 인터뷰 완료 후 자동 제안 (요구사항이 클 때)

## 실행 루브릭

### 실행 여부 판단
| 조건 | 결과 |
|------|------|
| 기능 ≤ 3개 AND 예상 < 4시간 | ⏭️ 스킵 → /validate |
| 기능 ≥ 4개 OR 예상 ≥ 4시간 | ✅ 실행 |

### Epic 크기 기준
| 항목 | 권장 범위 |
|------|----------|
| Entity | 1~2개 |
| 기능 | 1개 CRUD 스트림 |
| 코드 파일 | 10~20개 |
| 테스트 | 5~10개 시나리오 |
| 소요 시간 | 약 1시간 |

### 완료 루브릭
각 Epic은 다음을 포함해야 함:
- [ ] 제목 (Korean)
- [ ] 범위 (포함/제외 기능)
- [ ] Entity 목록
- [ ] 완료 기준 (DoD)
- [ ] 의존 Epic (있으면)

## 프로세스

```
1. requirements-draft.md 분석
   └─▶ Read 파일
   └─▶ 기능 요구사항 개수 파악

2. 실행 여부 판단
   ├─▶ 기능 ≤ 3개 AND 예상 < 4시간 → 스킵
   └─▶ 기능 ≥ 4개 OR 예상 ≥ 4시간 → 실행

3. (스킵 시)
   └─▶ "요구사항이 작습니다. 바로 /validate를 실행하세요." 메시지
   └─▶ 종료

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

## 출력 파일

### epics.md
```markdown
# Epic 목록

## Epic 1: [제목]
- **범위**: [포함 기능] / [제외 기능]
- **Entity**: [Entity 목록]
- **완료 기준 (DoD)**:
  - [ ] [기준 1]
  - [ ] [기준 2]
- **의존 Epic**: 없음 / Epic N
- **예상 소요**: X시간

## Epic 2: [제목]
...
```

### epic-roadmap.md
```markdown
# Epic 로드맵

## 구현 순서

```
Epic 1 (기반)
   ↓
Epic 2 → Epic 3 (병렬 가능)
   ↓
Epic 4 (통합)
```

## 상세 일정

| 순서 | Epic | 의존성 | 예상 소요 |
|------|------|--------|----------|
| 1 | Epic 1 | - | 1h |
| 2 | Epic 2 | Epic 1 | 1h |
| 3 | Epic 3 | Epic 1 | 1h |
| 4 | Epic 4 | Epic 2, 3 | 1h |

## 마일스톤
- **M1**: Epic 1 완료 → [기능 설명]
- **M2**: Epic 2, 3 완료 → [기능 설명]
- **M3**: Epic 4 완료 → [최종 기능]
```

## 다음 단계
- Epic 분해 완료 시 첫 번째 Epic부터 `/validate` 실행
- 각 Epic별로 `/validate → /design → /gherkin → /tdd → /refactor → /verify` 수행

## 참조
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
