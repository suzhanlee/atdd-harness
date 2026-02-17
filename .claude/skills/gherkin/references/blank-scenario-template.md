# 빈 시나리오 템플릿

## 목적
Phase A에서 사용자가 직접 Given-When-Then 시나리오를 작성하여 테스트 사고력을 훈련한다.

---

## 빈 시나리오 템플릿

```gherkin
Feature: [기능명]

  Background:
    Given [공통 전제조건]

  Scenario: [시나리오명 - Happy Path]
    Given [전제조건]
    When [행동]
    Then [예상 결과]

  Scenario: [시나리오명 - 예외 케이스]
    Given [전제조건]
    When [행동]
    Then [예상 결과]
```

---

## 시나리오 작성 워크시트

### 1. Feature 식별

```markdown
## Feature 식별

**기능명**: [무엇을 테스트하는가?]

**비즈니스 가치**: [왜 이 기능이 중요한가?]

**관련 요구사항**: [어떤 요구사항을 커버하는가?]
```

### 2. Happy Path 작성

```markdown
## Happy Path

**시나리오명**: 정상적인 [기능명]

**Given (전제조건)**:
- [ ] 무엇이 주어져야 하는가?
- [ ] 어떤 데이터가 필요한가?
- [ ] 어떤 상태여야 하는가?

**When (행동)**:
- [ ] 사용자가 무엇을 하는가?
- [ ] 어떤 요청을 보내는가?

**Then (결과)**:
- [ ] 무엇이 변경되어야 하는가?
- [ ] 어떤 응답을 받아야 하는가?
- [ ] 어떤 상태가 되어야 하는가?
```

### 3. 예외 케이스 작성

```markdown
## 예외 케이스

### 케이스 1: [예외 상황명]
**Given**: [예외 상황의 전제조건]
**When**: [동일한 행동]
**Then**: [예외 결과]

### 케이스 2: [예외 상황명]
**Given**: ...
**When**: ...
**Then**: ...
```

---

## 작성 예시 (참고용)

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
    And 에러 메시지는 "이미 존재하는 이메일입니다"이다
```

---

## 작성 가이드

### Given 작성법

```
좋은 Given:
- 구체적인 데이터 상태 명시
- 테이블 형식으로 데이터 정의
- 필수 선행 조건 포함

나쁜 Given:
- "어떤 데이터" (모호함)
- "사용자가 로그인함" (어떤 사용자?)
```

### When 작성법

```
좋은 When:
- 단일 행동 명시
- 구체적인 요청 파라미터
- API 엔드포인트 포함

나쁜 When:
- 여러 행동 혼합
- "버튼 클릭 후 폼 제출" (두 개의 행동)
```

### Then 작성법

```
좋은 Then:
- 검증 가능한 결과 명시
- 상태 코드, 응답 필드, DB 상태 포함

나쁜 Then:
- "성공한다" (무엇이 성공인가?)
- "에러가 발생한다" (어떤 에러?)
```

---

## STOP Protocol (Phase A 완료)

```
---
👆 핵심 시나리오(Happy Path)를 작성해주세요.
작성 완료 후 "완료" 또는 "다음"이라고 입력해주세요.
Phase B (Edge Case Hunt)로 진행합니다.
```
