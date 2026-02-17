---
name: validate
description: 요구사항을 검증한다. 구현 가능성, 완전성, 일관성을 체크할 때 사용.
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

## 트리거
- `/validate` 명령어 실행 (전체 Epic 검증)
- `/validate N` Epic N만 검증
- `/validate --fast` 병렬 검증 (의존성 무시)
- 인터뷰 완료 후 자동 제안
- Epic 분해 후 자동 제안

## 실행 모드

| 명령어 | 동작 |
|--------|------|
| `/validate` | 모든 미검증 Epic을 의존성 순서대로 검증 |
| `/validate 3` | Epic 3만 검증 (의존 Epic 완료 여부 체크) |
| `/validate --fast` | 의존성 무시하고 모든 Epic 병렬 검증 |

### 의존성 체크 로직
- 일반 모드: 의존 Epic이 검증되지 않았으면 경고 후 중단
- `--fast` 모드: 의존성 무시하고 검증 수행
- 단일 Epic 모드 (`/validate N`): 의존 Epic 완료 여부 체크만 수행

## 검증 항목

### 1. Feasibility (구현 가능성)

| 항목 | 체크 내용 |
|------|----------|
| 기술 스택 | Java 17+, Spring Boot 3.x 호환 여부 |
| 외부 의존성 | 외부 API, 라이브러리 사용 가능 여부 |
| 일정 | 요청된 기간 내 구현 가능 여부 |
| 리소스 | 필요한 인프라, 인력 확보 가능 여부 |

### 2. Completeness (완전성)

| 항목 | 체크 내용 |
|------|----------|
| 기능 요구사항 | 모든 기능이 명세되었는가? |
| 예외 케이스 | 에러 상황이 정의되었는가? |
| 비기능 요구사항 | 성능, 보안, 확장성 요구사항이 있는가? |
| 사용자 시나리오 | 주요 사용자 흐름이 정의되었는가? |

### 3. Consistency (일관성)

| 항목 | 체크 내용 |
|------|----------|
| 충돌 | 요구사항 간 모순이 없는가? |
| 모호성 | "빠른", "많은" 등 모호한 표현이 없는가? |
| 용어 | 동일 개념에 동일 용어를 사용하는가? |
| 수준 | 요구사항의 상세도가 일관적인가? |

### 4. Dependencies (의존성)

| 의존성 유형 | 체크 내용 |
|-------------|----------|
| 외부 API | 연동 필요 API 목록 |
| 데이터베이스 | 필요 DB, 스키마 |
| 타 시스템 | 연동 필요 내/외부 시스템 |
| 인프라 | 필요 인프라 리소스 |

### 5. Epic 전용 검증 항목 (Epic 모드 시)

| 항목 | 체크 내용 |
|------|----------|
| 의존 Epic | 의존하는 Epic이 먼저 완료/검증되었는가? |
| 범위 | 포함/제외 기능이 명확한가? |
| DoD | 완료 기준이 테스트 가능한가? |
| Entity | Entity 설계가 적절한가? (1~2개 권장) |

## 검증 프로세스

### 입력 소스 확인

```
1. epics.md 존재 확인
   ├─▶ 존재: Epic별 검증 모드
   └─▶ 미존재: 통합 검증 모드
```

### 통합 검증 모드 (기존 동작)

```
1. requirements-draft.md 로드
   └─▶ Read 파일

2. Feasibility 검증
   ├─▶ 기술 스택 호환성 확인
   ├─▶ 외부 의존성 확인
   └─▶ 일정/리소스 평가

3. Completeness 검증
   ├─▶ 누락 요구사항 체크
   ├─▶ 예외 케이스 확인
   └─▶ 비기능 요구사항 확인

4. Consistency 검증
   ├─▶ 충돌 확인
   ├─▶ 모호성 식별
   └─▶ 용어 일관성 확인

5. Dependencies 검증
   ├─▶ 외부 API 의존성
   ├─▶ DB 의존성
   └─▶ 타 시스템 연동

6. 검증 리포트 작성
   ├─▶ validation-report.md
   └─▶ refined-requirements.md
```

### Epic별 검증 모드

> **병렬화 전략**: Epic이 3개 이상일 때는 Task 도구로 `validation-report-writer`
> agent를 병렬 실행하여 각 Epic 검증을 동시에 수행한다. 각 agent 인스턴스가
> 하나의 Epic을 담당하여 `validation-report-epic-{id}.md`를 생성한다.

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

5. 검증 완료 요약
   ├─▶ 전체 통과: "모든 Epic 검증 완료 ✅. /design 진행 가능"
   └─▶ 일부 실패: "N개 Epic 검증 실패. 조치 후 재실행하세요."
```

## 출력 파일

### 통합 모드 출력 (기본)

```
.atdd/requirements/
├── validation-report.md
└── refined-requirements.md
```

### Epic 모드 출력

.atdd/requirements/
├── epics.md
├── epic-roadmap.md
├── validation-reports/
│   ├── validation-report-epic-1.md
│   ├── validation-report-epic-2.md
│   └── ...
├── refined-epics/              # 필요 시
│   └── ...
└── validation-summary.md       # 전체 검증 완료 시 생성

### validation-report.md (통합 모드)
```markdown
# 요구사항 검증 리포트

## 검증 일시
[날짜 시간]

## 검증 결과: ✅ PASS / ❌ FAIL

## 세부 검증

### 1. Feasibility
| 항목 | 결과 | 비고 |
|------|------|------|
| 기술 스택 호환성 | ✅/❌ | [상세 내용] |
| 외부 의존성 | ✅/❌ | [상세 내용] |
| 일정 | ✅/❌ | [상세 내용] |

### 2. Completeness
| 항목 | 결과 | 비고 |
|------|------|------|
| 누락 요구사항 | ✅/⚠️/❌ | [상세 내용] |
| 예외 케이스 | ✅/⚠️/❌ | [상세 내용] |
| 비기능 요구사항 | ✅/⚠️/❌ | [상세 내용] |

### 3. Consistency
| 항목 | 결과 | 비고 |
|------|------|------|
| 충돌 | ✅/❌ | [상세 내용] |
| 모호성 | ✅/⚠️ | [상세 내용] |
| 용어 | ✅/⚠️ | [상세 내용] |

### 4. Dependencies
| 의존성 | 결과 | 비고 |
|--------|------|------|
| [의존성명] | ✅/❌ | [상세 내용] |

## 조치 사항
1. [조치 항목 1]
2. [조치 항목 2]

## 결론
- [PASS/FAIL 사유]
```

### refined-requirements.md
```markdown
# 정제된 요구사항

## 프로젝트명
[프로젝트 이름]

## 검증 완료 요구사항

### 기능 요구사항

#### Must have
- [ ] [구체화된 요구사항 1]
  - 상세: [상세 내용]
  - 예외: [예외 케이스]
  - 검증: [검증 기준]

#### Should have
- [ ] [요구사항]

[...]
```

### validation-report-epic-N.md (Epic 모드)
```markdown
# Epic N 검증 리포트

## Epic 정보
- **제목**: [Epic 제목]
- **검증 일시**: [날짜 시간]

## 검증 결과: ✅ PASS / ❌ FAIL

## 세부 검증

### 1. Feasibility
| 항목 | 결과 | 비고 |
|------|------|------|
| 기술 스택 호환성 | ✅/❌ | [상세 내용] |
| Entity 설계 적절성 | ✅/❌ | [제안 Entity: 1~2개] |
| 일정 | ✅/❌ | [상세 내용] |

### 2. Completeness
| 항목 | 결과 | 비고 |
|------|------|------|
| 범위 명확성 | ✅/⚠️/❌ | [포함/제외 기능 확인] |
| DoD 완전성 | ✅/⚠️/❌ | [완료 기준 테스트 가능성] |
| 예외 케이스 | ✅/⚠️/❌ | [상세 내용] |

### 3. Consistency
| 항목 | 결과 | 비고 |
|------|------|------|
| 용어 통일 | ✅/⚠️ | [상세 내용] |
| 의존성 충돌 | ✅/❌ | [상세 내용] |

### 4. Dependencies
| 의존성 | 결과 | 비고 |
|--------|------|------|
| 의존 Epic 완료 여부 | ✅/⏳/❌ | [의존 Epic 번호 및 상태] |
| 외부 의존성 | ✅/❌ | [상세 내용] |

## 조치 사항
1. [조치 항목 1]
2. [조치 항목 2]

## 결론
- [PASS/FAIL 사유]

## 다음 단계
- [ ] Epic N+1 검증 (있을 경우)
- [ ] 또는 /design 진행
```

### validation-summary.md (전체 검증 완료 시)
```markdown
# 검증 요약 리포트

## 검증 일시
[날짜 시간]

## 전체 결과: ✅ PASS / ⚠️ PARTIAL / ❌ FAIL

## Epic별 결과

| Epic | 제목 | 결과 | 비고 |
|------|------|------|------|
| 1 | 사용자 인증 | ✅ | |
| 2 | 훈련 기록 CRUD | ✅ | |
| ... | ... | ... | ... |

## 다음 단계
- [ ] /design 진행
```

## 분기 처리

### 통합 모드 PASS
```
검증 통과 ✅
모든 검증 항목을 충족했습니다.
다음 단계: /design
```

### 통합 모드 FAIL
```
검증 실패 ❌
다음 항목에 대한 조치가 필요합니다:
1. [조치 항목 1]
2. [조치 항목 2]

조치 후 /validate를 다시 실행하세요.
```

### 전체 검증 완료 (모두 PASS)
```
검증 완료 ✅

| Epic | 제목 | 결과 |
|------|------|------|
| 1 | 사용자 인증 | ✅ PASS |
| 2 | 훈련 기록 CRUD | ✅ PASS |
| ... | ... | ... |

모든 Epic 검증 완료.
다음 단계: /design
```

### 일부 검증 실패
```
검증 결과 ⚠️

| Epic | 제목 | 결과 |
|------|------|------|
| 1 | 사용자 인증 | ✅ PASS |
| 2 | 훈련 기록 CRUD | ❌ FAIL |
| ... | ... | ... |

2개 Epic 검증 실패.
- Epic 2: [실패 사유]
- Epic 5: [실패 사유]

조치 후 /validate를 다시 실행하세요.
```

### 의존성 미충족 (단일 Epic 모드)
```
Epic 3 검증 불가 ❌

의존 Epic이 아직 검증되지 않았습니다:
- Epic 2 (훈련 기록 CRUD): 미검증

먼저 `/validate 2` 또는 `/validate`를 실행하세요.
```

### Epic 모드 PASS (개별 Epic)
```
Epic N 검증 통과 ✅
[Epic 제목] 검증 완료

[추가 Epic이 있을 경우]
다음 Epic 검증: /validate 실행

[모든 Epic 완료 시]
모든 Epic 검증 완료 ✅
다음 단계: /design
```

### Epic 모드 FAIL (개별 Epic)
```
Epic N 검증 실패 ❌
[Epic 제목] - 다음 항목 조치 필요:
1. [조치 항목 1]
2. [조치 항목 2]

조치 후 /validate를 다시 실행하세요.
```

## 다음 단계
- 검증 통과 시 `/design` 실행
- 검증 실패 시 요구사항 수정 후 재검증

## 참조
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
