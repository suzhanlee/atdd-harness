---
name: gherkin
description: Gherkin 시나리오를 추출한다. 요구사항을 테스트 시나리오로 변환할 때 사용.
disable-model-invocation: true
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit
references:
  - references/scenario-template.md
---

# Gherkin 시나리오 추출

## 목표
요구사항을 Gherkin 문법의 테스트 시나리오로 변환한다.

## 입력
- `.atdd/requirements/refined-requirements.md`
- `.atdd/design/erd.md`
- `.atdd/design/domain-model.md`
- `src/main/java/**/domain/entity/*.java`

## 템플릿
- 시나리오 템플릿: [scenario-template.md](references/scenario-template.md)

## 트리거
- `/gherkin` 명령어 실행
- Entity 설계 완료 후 자동 제안

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

## 프로세스

### 1. User Story → Scenario 변환
```
User Story:
"사용자로서 회원가입을 하고 싶다.
 그래야 서비스를 이용할 수 있다."

→
Feature: 회원 관리
  Scenario: 정상적인 회원가입
```

### 2. Happy Path 작성
정상적인 사용자 흐름 작성

```gherkin
Scenario: 정상적인 회원가입
  When 회원가입 요청을 보낸다
    | email         | password     | name   |
    | test@test.com | password123! | 테스터 |
  Then 상태 코드 201를 받는다
  And 응답의 "email" 필드는 "test@test.com"이다
```

### 3. Exception Path 작성
에러 케이스, 경계값 테스트 작성

```gherkin
Scenario: 중복 이메일로 회원가입
  Given 다음 사용자가 존재한다
    | id | email         |
    | 1  | test@test.com |
  When 회원가입 요청을 보낸다
    | email         | password     | name   |
    | test@test.com | password456! | 테스터2 |
  Then 상태 코드 409를 받는다
```

### 4. Background 정의
공통 Given 절 추출

```gherkin
Background:
  Given 데이터베이스가 초기화되어 있다
  And 다음 Role이 존재한다
    | id | name  |
    | 1  | USER  |
    | 2  | ADMIN |
```

### 5. Data Table 설계
복잡한 입력/출력 데이터 정의

```gherkin
When 회원가입 요청을 보낸다
  | email         | password     | name   | role |
  | user@test.com | password123! | 사용자 | USER |
  | admin@test.com| password123! | 관리자 | ADMIN|
```

### 6. Scenario Outline 작성
데이터 기반 테스트

```gherkin
Scenario Outline: 잘못된 형식으로 회원가입
  When 회원가입 요청을 보낸다
    | email    | password   | name    |
    | <email>  | <password> | <name>  |
  Then 상태 코드 400를 받는다

  Examples:
    | email         | password     | name   |
    |               | password123! | 테스터 |
    | invalid-email | password123! | 테스터 |
    | test@test.com | 123          | 테스터 |
```

## 시나리오 작성 원칙

### 좋은 시나리오
- ✅ 하나의 시나리오는 하나의 행동만 테스트
- ✅ Given-When-Then 구조 명확
- ✅ 비기술자도 이해 가능한 언어
- ✅ 구체적인 데이터 사용

### 나쁜 시나리오
- ❌ 여러 행동을 한 시나리오에 포함
- ❌ 모호한 표현 ("어떤 데이터")
- ❌ 기술적 구현 노출 ("SQL 실행")
- ❌ 과도하게 긴 시나리오

## 출력 파일

### features/*.feature
```gherkin
Feature: [기능명]

  Background:
    Given [공통 전제조건]

  Scenario: [시나리오명]
    Given [전제조건]
    When [행동]
    Then [결과]

  Scenario: [시나리오명 2]
    ...
```

### scenarios-summary.md
```markdown
# 시나리오 요약

## Feature 목록
1. [Feature명] - [시나리오 수]
2. ...

## 시나리오 통계
- 총 Feature 수: N
- 총 Scenario 수: M
- Happy Path: X
- Exception Path: Y

## 커버리지
- [요구사항]: [커버하는 시나리오]
```

## 검증 체크리스트

- [ ] 모든 Must have 요구사항에 시나리오 존재
- [ ] Happy Path 작성 완료
- [ ] 주요 Exception Path 작성 완료
- [ ] Background 적절히 활용
- [ ] Data Table 명확하게 작성
- [ ] Scenario Outline로 중복 최소화

## 다음 단계
시나리오 작성 완료 후 `/tdd` 실행

## 참조
- 시나리오 템플릿: [scenario-template.md](references/scenario-template.md)
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
