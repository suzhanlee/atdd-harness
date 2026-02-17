# Epic 모드 가이드

## 실행 모드

| 명령어 | 동작 |
|--------|------|
| `/validate` | 모든 미검증 Epic을 의존성 순서대로 검증 |
| `/validate 3` | Epic 3만 검증 (의존 Epic 완료 여부 체크) |
| `/validate --fast` | 의존성 무시하고 모든 Epic 병렬 검증 |

## 의존성 체크 로직

- **일반 모드**: 의존 Epic이 검증되지 않았으면 경고 후 중단
- **`--fast` 모드**: 의존성 무시하고 검증 수행
- **단일 Epic 모드** (`/validate N`): 의존 Epic 완료 여부 체크만 수행

## 병렬화 전략

> Epic이 3개 이상일 때는 Task 도구로 `validation-report-writer` agent를 병렬 실행하여
> 각 Epic 검증을 동시에 수행한다. 각 agent 인스턴스가 하나의 Epic을 담당하여
> `validation-report-epic-{id}.md`를 생성한다.

```
Epic ≥3개: Task 도구로 validation-report-writer 병렬 실행
Epic <3개: 직접 순차 검증
```

## Epic별 검증 프로세스

```
1. 실행 모드 파악
   ├─▶ 인자 없음: 전체 검증 모드
   ├─▶ 숫자 N: 단일 Epic 검증 모드
   └─▶ --fast: 병렬 검증 모드

2. 전체 검증 모드
   ├─▶ 미검증 Epic 목록 추출
   ├─▶ 의존성 순서대로 정렬 (위상 정렬)
   ├─▶ Epic ≥3개: validation-report-writer 병렬 실행
   ├─▶ Epic <3개: 직접 순차 검증
   └─▶ 진행 상황 표시 ("Epic 1/8 검증 중...")

3. 단일 Epic 검증 모드
   ├─▶ 의존 Epic 검증 완료 여부 확인
   │   ├─▶ 완료: 검증 진행
   │   └─▶ 미완료: 경고 메시지 후 종료
   └─▶ 지정 Epic만 검증

4. 병렬 검증 모드 (--fast)
   ├─▶ 의존성 무시
   ├─▶ Epic ≥3개: Task 도구로 validation-report-writer 병렬 실행
   ├─▶ Epic <3개: 직접 수행
   └─▶ 결과 취합 후 validation-summary.md 작성
```

## 위상 정렬 (Topological Sort)

의존성 그래프를 기반으로 Epic 검증 순서를 결정:

```
Epic 1 (기반, 의존성 없음)
   ↓
Epic 2 → Epic 3 (Epic 1에 의존, 병렬 가능)
   ↓
Epic 4 (Epic 2, 3에 의존)
```
