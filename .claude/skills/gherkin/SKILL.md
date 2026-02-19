---
name: gherkin
description: This skill should be used when the user asks to "/gherkin", "시나리오 추출", "Gherkin 시나리오 작성", "테스트 시나리오 변환", or needs to convert requirements to test scenarios.
disable-model-invocation: true
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit, Bash
references:
  - references/blank-scenario-template.md
  - references/edge-case-checklist.md
  - references/coverage-matrix.md
  - references/scenario-template.md
---

# Gherkin 시나리오 추출

## 목표
사용자가 직접 테스트 시나리오를 작성하고 예외 케이스를 식별하여 **테스트 사고력**을 향상시킨다.
AI가 시나리오를 생성하는 방식이 아닌, 사용자가 주도적으로 작성하는 훈련을 제공한다.

---

## STOP PROTOCOL

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
📁 파일 생성 완료: .atdd/scenarios/draft-happy-path.md
👆 파일을 열어 핵심 시나리오(Happy Path)를 작성해주세요.
작성 완료 후 "완료" 또는 "다음"이라고 입력해주세요.
Phase B (Edge Case Hunt)로 진행합니다.
```

### Phase B 종료 필수 문구
```
---
📁 파일 생성 완료: .atdd/scenarios/draft-edge-cases.md
👆 파일을 열어 최소 5개의 예외 케이스를 식별해주세요.
식별 완료 후 "완료" 또는 "다음"이라고 입력해주세요.
Phase C (Generation)로 진행합니다.
```

---

## 입력
- `.atdd/requirements/refined-requirements.md`
- `.atdd/design/erd.md`
- `.atdd/design/domain-model.md`
- `src/main/java/**/domain/entity/*.java`

---

## 4-Phase 워크플로우

### Phase A: Happy Path (핵심 시나리오)

**목적**: 사용자가 정상적인 사용자 흐름을 직접 작성

**진행 방식**:
1. 요구사항 분석 → Feature명 추출
2. `.atdd/scenarios/` 디렉토리 생성 (존재하지 않는 경우)
3. `.atdd/scenarios/draft-happy-path.md` 파일 생성
4. 사용자가 파일을 열어 Given-When-Then 작성
5. "완료" 또는 "다음" 입력 시 Phase B 진행

**파일 생성 액션**:
```
Write: .atdd/scenarios/draft-happy-path.md
```

**생성할 템플릿 파일 내용**:

```markdown
# Happy Path 템플릿

> 작성 가이드: [ ] 항목을 채워주세요. 완료 후 "완료" 또는 "다음"을 입력하세요.

## Feature 정보
- **Feature명**: [요구사항에서 추출한 기능명]
- **관련 요구사항**: [M1, M2, ...]

---

## 시나리오 작성

### Scenario 1: [시나리오명을 작성하세요]

**Given (전제조건)**:
- [ ] _______________

**When (행동)**:
- [ ] _______________

**Then (결과)**:
- [ ] _______________

---

### Scenario 2: [시나리오명을 작성하세요]

**Given (전제조건)**:
- [ ] _______________

**When (행동)**:
- [ ] _______________

**Then (결과)**:
- [ ] _______________

---

## 작성 팁
- Given: 구체적인 데이터 상태, 테이블 형식 활용
- When: 단일 행동, 구체적인 요청 파라미터
- Then: 검증 가능한 결과, 상태 코드/응답 필드
```

**작성 가이드**:

| 섹션 | 작성 요령 |
|------|-----------|
| Given | 구체적인 데이터 상태, 테이블 형식 활용 |
| When | 단일 행동, 구체적인 요청 파라미터 |
| Then | 검증 가능한 결과, 상태 코드/응답 필드 |

**상세 가이드**: [blank-scenario-template.md](references/blank-scenario-template.md)

**Phase A 종료 후**:
- STOP Protocol 적용 → 사용자 파일 편집 대기
- "완료" 또는 "다음" 입력 시 Phase B 진행

---

### Phase B: Edge Case Hunt (예외 케이스 탐지)

**목적**: 사용자가 예외 케이스를 최소 5개 이상 식별

**진행 방식**:
1. `.atdd/scenarios/draft-edge-cases.md` 파일 생성
2. 사용자가 파일을 열어 Edge Case 식별
3. "완료" 또는 "다음" 입력 시 Phase C 진행

**파일 생성 액션**:
```
Write: .atdd/scenarios/draft-edge-cases.md
```

**생성할 워크시트 파일 내용**:

```markdown
# Edge Case Hunt 워크시트

> 작성 가이드: 최소 5개의 예외 케이스를 식별하세요. 완료 후 "완료" 또는 "다음"을 입력하세요.

## 기능: [Feature명]

---

## 식별된 예외 케이스

### 1. 입력 검증 실패
- [ ] 케이스 1: _______________
- [ ] 케이스 2: _______________

### 2. 비즈니스 규칙 위반
- [ ] 케이스 1: _______________
- [ ] 케이스 2: _______________

### 3. 외부 의존성 실패
- [ ] 케이스 1: _______________

### 4. 동시성 문제
- [ ] 케이스 1: _______________

### 5. 경계값
- [ ] 케이스 1: _______________

---

## 총 식별 개수: ___개 (최소 5개 필요)

---

## 참고: 예외 케이스 카테고리
| 카테고리 | 예시 |
|----------|------|
| 입력 검증 실패 | 필수값 누락, 잘못된 형식, 길이 제한 초과 |
| 비즈니스 규칙 위반 | 중복 데이터, 상태 위반, 권한 부족 |
| 외부 의존성 실패 | API 타임아웃, DB 연결 실패 |
| 동시성 문제 | 동시 수정, 레이스 컨디션 |
| 경계값 | 최소/최대값, 빈 컬렉션, null |
```

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
1. `.atdd/scenarios/draft-happy-path.md` 읽기
2. `.atdd/scenarios/draft-edge-cases.md` 읽기
3. 사용자 작성 내용을 통합하여 .feature 파일 생성
4. Scenario Outline로 데이터 기반 테스트 작성

**파일 읽기 액션**:
```
Read: .atdd/scenarios/draft-happy-path.md
Read: .atdd/scenarios/draft-edge-cases.md
```

**시나리오 작성 원칙**:

| 좋은 시나리오 | 나쁜 시나리오 |
|---------------|---------------|
| 하나의 행동만 테스트 | 여러 행동 혼합 |
| Given-When-Then 명확 | 모호한 표현 |
| 비기술자도 이해 가능 | 기술적 구현 노출 |
| 구체적인 데이터 | "어떤 데이터" |

**상세 가이드**: [scenario-template.md](references/scenario-template.md)

**Phase C 완료 후**:
- STOP Protocol 없음
- 즉시 Phase D 진행

---

### Phase D: Coverage Check (커버리지 검증)

**목적**: 모든 요구사항이 시나리오로 커버되었는지 검증

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

## Gherkin 키워드

| 키워드 | 설명 | 예시 |
|--------|------|------|
| Feature | 기능 단위 | Feature: 회원 관리 |
| Background | 공통 전제조건 | Given 데이터베이스 초기화 |
| Scenario | 테스트 시나리오 | Scenario: 회원가입 |
| Given | 전제조건 | Given 로그인 페이지 |
| When | 행동 | When 회원가입 버튼 클릭 |
| Then | 결과 | Then 회원가입 성공 메시지 |
| And/But | 추가 조건 | And 이메일 인증 완료 |
| Scenario Outline | 데이터 기반 테스트 | Examples 테이블 사용 |

---

## 트리거
- `/gherkin` 명령어 실행
- Entity 설계 완료 후 자동 제안

## MUST 체크리스트 (실행 전)
- [ ] refined-requirements.md 존재
- [ ] design 파일 존재 (erd.md 또는 domain-model.md)

## MUST 체크리스트 (실행 후)
- [ ] Phase A: Happy Path 작성 완료
- [ ] Phase B: 예외 케이스 5개 이상 식별
- [ ] Phase C: .feature 파일 생성
- [ ] Phase D: 커버리지 검증 (Must Have 100%)
- [ ] scenarios-summary.md 생성

---

## 출력 파일

### features/*.feature
```gherkin
Feature: 회원가입

  Background:
    Given 데이터베이스가 초기화되어 있다

  Scenario: 정상적인 회원가입
    Given 회원가입 페이지에 접속한다
    When 다음 정보로 회원가입 요청을 보낸다
      | email         | password     | name   |
      | test@test.com | password123! | 테스터 |
    Then 상태 코드 201을 받는다
    And 응답의 "email" 필드는 "test@test.com"이다

  Scenario: 중복 이메일로 회원가입
    Given 다음 사용자가 이미 존재한다
      | id | email         |
      | 1  | test@test.com |
    When 다음 정보로 회원가입 요청을 보낸다
      | email         | password     | name   |
      | test@test.com | password456! | 테스터2 |
    Then 상태 코드 409를 받는다

  Scenario Outline: 잘못된 형식으로 회원가입
    When 다음 정보로 회원가입 요청을 보낸다
      | email    | password   | name    |
      | <email>  | <password> | <name>  |
    Then 상태 코드 400를 받는다

    Examples:
      | email         | password     | name   |
      |               | password123! | 테스터 |
      | invalid-email | password123! | 테스터 |
      | test@test.com | 123          | 테스터 |
```

### scenarios-summary.md
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

## 다음 단계
커버리지 검증 완료 후 `/tdd` 실행

---

## 참조
- 빈 시나리오 템플릿: [blank-scenario-template.md](references/blank-scenario-template.md)
- 예외 케이스 체크리스트: [edge-case-checklist.md](references/edge-case-checklist.md)
- 커버리지 매트릭스: [coverage-matrix.md](references/coverage-matrix.md)
- 시나리오 템플릿: [scenario-template.md](references/scenario-template.md)
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
