---
name: gherkin
description: Use when the user asks to "/gherkin", "시나리오 추출", "Gherkin 시나리오 작성", "테스트 시나리오 변환", or needs to convert requirements to test scenarios.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit, Bash
references:
  - references/blank-scenario-template.md
  - references/edge-case-checklist.md
  - references/coverage-matrix.md
  - references/scenario-template.md
  - references/step-naming-convention.md
---

# Gherkin 시나리오 추출

## 목표
사용자가 직접 테스트 시나리오를 작성하고 예외 케이스를 식별하여 **테스트 사고력**을 향상시킨다.
AI가 시나리오를 생성하는 방식이 아닌, 사용자가 주도적으로 작성하는 훈련을 제공한다.

---

## STOP PROTOCOL

### ⚠️ 종료 전 필수 체크리스트

**스킬 종료 전 반드시 수행:**
- [ ] context.json의 `status`를 "completed"로 변경
- [ ] context.json의 `updated_at`을 현재 시간으로 변경
- [ ] 산출물 파일이 올바른 경로에 생성되었는지 확인

**❌ 위 체크리스트 미완료 시 스킬이 완료되지 않은 것으로 간주**

---

### 4-Phase 진행 규칙
각 Phase는 반드시 **별도 턴**으로 진행한다. 사용자가 다음 단계로 진행할 준비가 될 때까지 대기한다.

```
Phase A (Happy Path)      → 사용자 입력 대기 → "완료"/"다음" → Phase B
Phase B (Edge Case Hunt)  → 사용자 입력 대기 → "완료"/"다음" → Phase C
Phase C (Generation)      → Phase D 즉시 진행 (대기 없음)
Phase D (Coverage Check)  → 시나리오 완료
```

### Phase A 종료 필수 문구
```
---
📁 파일 생성 완료: {basePath}/scenarios/draft-happy-path.md
👆 파일을 열어 핵심 시나리오(Happy Path)를 작성해주세요.
작성 완료 후 "완료" 또는 "다음"이라고 입력해주세요.
Phase B (Edge Case Hunt)로 진행합니다.
```

### Phase B 종료 필수 문구
```
---
📁 파일 생성 완료: {basePath}/scenarios/draft-edge-cases.md
👆 파일을 열어 최소 5개의 예외 케이스를 식별해주세요.
식별 완료 후 "완료" 또는 "다음"이라고 입력해주세요.
Phase C (Generation)로 진행합니다.
```

---

## 입력
- `{basePath}/validate/refined-requirements.md`
- `src/test/resources/features/{topic}.feature` (선택 - 있으면 덮어쓰기)

---

## 4-Phase 워크플로우

### Phase A: Happy Path (핵심 시나리오)

**목적**: 사용자가 정상적인 사용자 흐름을 직접 작성

**진행 방식**:
1. 요구사항 분석 → Feature명 추출
2. `{basePath}/scenarios/` 디렉토리 생성 (존재하지 않는 경우)
3. `{basePath}/scenarios/draft-happy-path.md` 파일 생성
4. 사용자가 파일을 열어 Given-When-Then 작성
5. "완료" 또는 "다음" 입력 시 Phase B 진행

**파일 생성 액션**:
```
Write: {basePath}/scenarios/draft-happy-path.md
```

**작성 가이드**:

| 섹션 | 작성 요령 |
|------|-----------|
| Given | 구체적인 데이터 상태, 테이블 형식 활용 |
| When | 단일 행동, 구체적인 요청 파라미터 |
| Then | 검증 가능한 결과, 상태 코드/응답 필드 |

**상세 템플릿**: [blank-scenario-template.md](references/blank-scenario-template.md)

**Phase A 종료 후**:
- STOP Protocol 적용 → 사용자 파일 편집 대기
- "완료" 또는 "다음" 입력 시 Phase B 진행

---

### Phase B: Edge Case Hunt (예외 케이스 탐지)

**목적**: 사용자가 예외 케이스를 최소 5개 이상 식별

**진행 방식**:
1. `{basePath}/scenarios/draft-edge-cases.md` 파일 생성
2. 사용자가 파일을 열어 Edge Case 식별
3. "완료" 또는 "다음" 입력 시 Phase C 진행

**파일 생성 액션**:
```
Write: {basePath}/scenarios/draft-edge-cases.md
```

**작성 체크리스트**:
- [ ] 입력 검증: 2개 이상 (필수값 누락, 형식 오류)
- [ ] 비즈니스 규칙: 1개 이상 (중복, 상태 위반)
- [ ] 권한/인증: 1개 이상
- [ ] 경계값: 1개 이상 (최소/최대)
- [ ] 총 5개 이상 작성 완료

**예외 케이스 카테고리**:

| 카테고리 | 예시 |
|----------|------|
| 입력 검증 실패 | 필수값 누락, 잘못된 형식, 길이 제한 초과 |
| 비즈니스 규칙 위반 | 중복 데이터, 상태 위반, 권한 부족 |
| 외부 의존성 실패 | API 타임아웃, DB 연결 실패 |
| 동시성 문제 | 동시 수정, 레이스 컨디션 |
| 경계값 | 최소/최대값, 빈 컬렉션, null |

**상세 가이드**: [edge-case-checklist.md](references/edge-case-checklist.md)

**Phase B 종료 후**:
- STOP Protocol 적용 → 사용자 파일 편집 대기
- "완료" 또는 "다음" 입력 시 Phase C 진행

---

### Phase C: Generation (시나리오 생성)

**목적**: Happy Path와 Exception Path를 모두 포함한 시나리오 파일 생성

**진행 방식**:
1. `.atdd/context.json` 읽기 (topic, module, basePath 확인)
2. `{basePath}/scenarios/draft-happy-path.md` 읽기
3. `{basePath}/scenarios/draft-edge-cases.md` 읽기
4. **Step 정규화** (Step Naming Convention 적용)
5. **Data Table 구조화**
6. **Epic별 Feature 파일 분리** 또는 단일 파일 생성
7. context.json 업데이트 (featurePath/featurePaths, phase 기록)

**Context 로드**:
```
Read: .atdd/context.json
```

**모듈 탐지 로직**:
1. `context.module` 있으면 해당 모듈 사용
2. 없으면 `settings.gradle` 파싱하여 모듈 목록 확인
3. 2개 이상 모듈이면 AskUserQuestion으로 선택
4. 선택된 모듈을 context.json에 저장

**Feature 파일 경로 결정**:
```
# Epic별 분리 (epic-split/epics.md 존재 시)
src/test/resources/features/{topic}-{nn}-{title}.feature

# 단일 모듈 프로젝트 (epic-split 없음)
src/test/resources/features/{topic}.feature

# 멀티 모듈 프로젝트 (module이 있는 경우)
{module}/src/test/resources/features/{topic}.feature
```

#### Epic별 Feature 파일 분리

**파일 분리 조건**:
1. `{basePath}/epic-split/epics.md` 존재 시 Epic별 분리
2. 없으면 단일 파일 생성 (기존 방식)

**파일 네이밍 규칙**:
```
{topic}-{epic번호}-{epic제목-kebab}.feature
```

예시:
- `apple-iap-subscription-01-purchase.feature`
- `apple-iap-subscription-02-verification.feature`

**크기 가이드라인**:

| 지표 | 권장 | 제한 |
|------|------|------|
| 시나리오/Feature | ≤ 10 | ≤ 15 |
| 라인/Feature | ≤ 250 | ≤ 400 |
| Background Steps | ≤ 5 | ≤ 10 |

**분리 처리 순서**:
1. `{basePath}/epic-split/epics.md` 읽기
2. Epic 번호와 제목 파싱
3. draft-happy-path.md, draft-edge-cases.md에서 Epic별 시나리오 분류
   - 주석 마커 `# Feature N: ... (Epic N)` 기준
4. Epic별 .feature 파일 생성
5. context.json에 featurePaths 배열로 저장

**상세 가이드**: [feature-split-guide.md](references/feature-split-guide.md)

**파일 읽기 액션**:
```
Read: {basePath}/scenarios/draft-happy-path.md
Read: {basePath}/scenarios/draft-edge-cases.md
```

**Context 업데이트**:

단일 파일 (Epic 정보 없음):
```json
{
  ...기존필드,
  "phase": "gherkin",
  "featurePath": "src/test/resources/features/{topic}.feature",
  "module": "{선택된_모듈_또는_null}",
  "updated_at": "{현재시각}"
}
```

Epic별 분리 (epic-split/epics.md 존재):
```json
{
  ...기존필드,
  "phase": "gherkin",
  "featurePath": "src/test/resources/features/{topic}/",
  "featurePaths": [
    "src/test/resources/features/{topic}-01-{epic1}.feature",
    "src/test/resources/features/{topic}-02-{epic2}.feature",
    "src/test/resources/features/{topic}-03-{epic3}.feature"
  ],
  "module": "{선택된_모듈_또는_null}",
  "updated_at": "{현재시각}"
}
```
Edit: .atdd/context.json

**시나리오 작성 원칙**:

| 좋은 시나리오 | 나쁜 시나리오 |
|---------------|---------------|
| 하나의 행동만 테스트 | 여러 행동 혼합 |
| Given-When-Then 명확 | 모호한 표현 |
| 비기술자도 이해 가능 | 기술적 구현 노출 |
| 구체적인 데이터 | "어떤 데이터" |

**상세 가이드**: [scenario-template.md](references/scenario-template.md)
**Step Convention**: [step-naming-convention.md](references/step-naming-convention.md)

**Phase C 완료 후**:
- STOP Protocol 없음
- 즉시 Phase D 진행

---

### Phase D: Validation & Coverage (검증 및 커버리지)

**목적**: Gherkin 품질 검증과 요구사항 커버리지 확인

**진행 방식**:

#### Step 1: Gherkin 품질 검증

**검증 항목**:

| 항목 | 검증 내용 | 합격 기준 |
|------|-----------|-----------|
| Step 패턴 | TDD 인식 가능한 패턴 사용 | 100% 준수 |
| Data Table | 올바른 형식의 테이블 | 필수 필드 포함 |
| 상태 코드 | `{int}` 파라미터 사용 | 모든 Then에 명시 |
| 중복 Step | 동일 의미의 다른 표현 | 없음 |

**검증 결과**:
```
Gherkin 품질 검증 ✅

| 항목 | 상태 | 비고 |
|------|------|------|
| Step 패턴 | ✅ | 12/12 준수 |
| Data Table | ✅ | 8개 테이블 확인 |
| 상태 코드 | ✅ | 12개 시나리오 모두 명시 |
| 중복 Step | ✅ | 중복 없음 |
```

**상세 가이드**: [step-naming-convention.md](references/step-naming-convention.md)

#### Step 2: Coverage Check

**진행 방식**:
1. 요구사항-시나리오 매핑 검증
2. 커버리지 매트릭스 생성
3. 미커버 요구사항 보완

**커버리지 매트릭스**:

| ID | 요구사항 | 시나리오 | 커버 |
|----|----------|----------|------|
| M1 | 회원가입 | 정상적인 회원가입 | ✅ |
| M2 | 이메일 중복 검사 | 중복 이메일로 회원가입 | ✅ |
| S1 | 이메일 인증 | 이메일 인증 요청 | ✅ |
| C1 | 소셜 로그인 | - | ❌ |

**합격 기준**:

| 우선순위 | 커버리지 |
|----------|----------|
| Must Have | 100% |
| Should Have | 80% 이상 |
| Could Have | 50% 이상 |

**상세 가이드**: [coverage-matrix.md](references/coverage-matrix.md)

**검증 결과**:

```
커버리지 검증 완료 ✅

| 우선순위 | 커버리지 | 상태 |
|----------|----------|------|
| Must Have | 100% | ✅ |
| Should Have | 100% | ✅ |
| Could Have | 0% | ⚠️ (의도적 제외) |

다음 단계: /tdd
```

---

## 트리거
- `/gherkin` 명령어 실행
- Entity 설계 완료 후 자동 제안

## MUST 체크리스트 (실행 전)
- [ ] `{basePath}/validate/refined-requirements.md` 존재

**YOU MUST complete all items before proceeding.**

## MUST 체크리스트 (실행 후)
- [ ] Phase A: Happy Path 작성 완료
- [ ] Phase B: 예외 케이스 5개 이상 식별
- [ ] Phase C: .feature 파일 생성
- [ ] Phase D: 커버리지 검증 (Must Have 100%)
- [ ] `{basePath}/scenarios/scenarios-summary.md` 생성
- [ ] context.json 업데이트: `status`를 "completed"로 변경

**No exceptions:**
- Don't skip Edge Case Hunt
- Don't proceed without 5+ edge cases
- Don't ignore coverage gaps

### 완료 시 context.json 업데이트

```json
{
  "phase": "gherkin",
  "status": "completed",
  "updated_at": "{ISO8601}"
}
```
Edit: .atdd/context.json

---

## 출력 파일

### Draft 시나리오 (사용자 작성용)
- `{basePath}/scenarios/draft-happy-path.md` - Happy Path 템플릿
- `{basePath}/scenarios/draft-edge-cases.md` - Edge Case 워크시트

### 최종 Feature 파일
**경로**: `src/test/resources/features/{topic}.feature`

> context.json의 `featurePath` 필드에 이 경로가 기록됩니다.

### scenarios-summary.md
**경로**: `{basePath}/scenarios/scenarios-summary.md`

```markdown
# 시나리오 요약

## Feature 목록
1. 회원가입 - 4개 시나리오
2. 로그인 - 3개 시나리오

## 시나리오 통계
- 총 Feature 수: 2
- 총 Scenario 수: 7
- Happy Path: 2
- Exception Path: 5

## 커버리지
- Must Have: 100%
- Should Have: 100%
```

---

## Red Flags - STOP and Start Over

다음 중 하나라도 해당하면 **시나리오를 삭제하고 다시 작성**:

- "어떤 데이터", "특정 값" 등 모호한 표현 사용
- 하나의 시나리오에 여러 행동 혼합
- Then에 상태 코드가 없음
- Given 없이 When부터 시작 (입력 검증 제외)
- Data Table 없이 문장으로만 데이터 표현
- "성공한다", "실패한다" 등 구체적이지 않은 검증

**All of these mean: Delete scenario. Rewrite with specific data. No exceptions.**

---

## Gherkin 합리화 차단

| Excuse | Reality |
|--------|---------|
| "데이터는 나중에 채우면 돼" | 나중에 채워지지 않는다. 지금 작성하라. |
| "성공/실패만 알면 돼" | 상태 코드 없이는 TDD가 불가능하다. |
| "Edge Case는 나중에" | 나중에 오는 버그 리포트가 더 비싸다. |
| "Happy Path면 충분해" | Happy Path만 테스트하면 80%의 버그를 놓친다. |
| "이건 너무 간단해서" | 간단한 기능이 가장 많이 망가진다. |
| "문서화가 귀찮아" | Gherkin이 곧 문서이자 테스트다. |

---

## 다음 단계
커버리지 검증 완료 후 `/adr` 실행 (또는 `/epic-split`으로 Epic 분해)

---

## Definition of Done (DoD)

**⚠️ YOU MUST complete all items before proceeding. No exceptions.**

| # | 조건 | 검증 |
|---|------|------|
| 1 | context.json `status` = "completed" | 필수 |
| 2 | context.json `updated_at` = 현재 시간 | 필수 |
| 3 | 산출물 파일 생성 완료 (`.feature`, `scenarios-summary.md`) | 필수 |
| 4 | 품질 기준 달성 (Must Have 100%) | 필수 |

**No exceptions:**
- Don't skip Edge Case Hunt
- Don't proceed without 5+ edge cases
- Don't ignore coverage gaps

---

## Self-Check (스킬 완료 후 검증)

**스킬 실행 완료 후 스스로 확인:**
- [ ] 모든 시나리오에 Given-When-Then이 있는가?
- [ ] 모든 Then에 상태 코드가 명시되어 있는가?
- [ ] Data Table이 모든 데이터를 구체화하고 있는가?
- [ ] Edge Case가 최소 5개 이상인가?
- [ ] Must Have 요구사항 커버리지가 100%인가?

**하나라도 "아니오"라면 시나리오를 보완해야 합니다.**

---

## 참조
- 빈 시나리오 템플릿: [blank-scenario-template.md](references/blank-scenario-template.md)
- 예외 케이스 체크리스트: [edge-case-checklist.md](references/edge-case-checklist.md)
- 커버리지 매트릭스: [coverage-matrix.md](references/coverage-matrix.md)
- 시나리오 템플릿: [scenario-template.md](references/scenario-template.md)
- Step 네이밍 컨벤션: [step-naming-convention.md](references/step-naming-convention.md)
- Feature 파일 분리 가이드: [feature-split-guide.md](references/feature-split-guide.md)
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
