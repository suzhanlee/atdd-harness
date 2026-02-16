---
name: fix
description: 에러 분석을 바탕으로 Gherkin 시나리오 생성, 테스트 작성, 수정 코드 구현, PR 생성까지 자동화하는 Self-Healing 스킬.
disable-model-invocation: true
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit, Bash
---

# Fix Skill (Self-Healing)

에러 분석을 바탕으로 Gherkin 시나리오 생성 → 테스트 작성 → 수정 코드 구현 → PR 생성까지 자동화합니다.

## 트리거
- `/fix {error-id}`
- "이 에러 수정해줘"
- "자가 치유 실행해줘"

## 전제 조건
- `/analyze-error {error-id}` 실행으로 분석 리포트가 생성되어 있어야 함
- Git working directory가 clean해야 함

## 프로세스

### Phase 1: 준비
1. 에러 분석 리포트 로드 (`.atdd/runtime/errors/analysis-{error-id}.md`)
2. 수정 방안 확인
3. 브랜치 생성: `fix/claude-loki-error-{error-type}-{YYYYMMDD}`

### Phase 2: Gherkin 생성 (Red)
실패 시나리오를 Gherkin으로 변환:
```gherkin
Feature: 사용자 조회 에러 수정

  Scenario: 미인증 사용자의 사용자 조회 시 401 반환
    Given 인증되지 않은 클라이언트가
    When GET /api/users/1 요청을 보내면
    Then 401 Unauthorized 응답을 받는다

  Scenario: null userId로 조회 시 빈 결과 반환
    Given 인증된 클라이언트가
    When GET /api/users/null 요청을 보내면
    Then 404 Not Found 응답을 받는다
```

### Phase 3: 테스트 작성 (Red)
Cucumber Step Definition 작성:
```java
@Given("인증되지 않은 클라이언트가")
public void 인증되지_않은_클라이언트() {
    requestSpec = RestAssured.given();
}

@When("GET {string} 요청을 보내면")
public void GET_요청(String path) {
    response = requestSpec.get(path);
}

@Then("{int} {string} 응답을 받는다")
public void 응답_확인(int status, String reason) {
    response.then().statusCode(status);
}
```

### Phase 4: 수정 코드 작성 (Green)
분석된 수정 방안으로 코드 수정:
1. 즉시 수정 코드 적용
2. 근본 수정 코드 적용 (가능한 경우)

### Phase 5: 검증 (Green)
```bash
./gradlew test           # Unit Tests
./gradlew cucumber       # E2E Tests
```

### Phase 6: PR 생성
1. 변경사항 커밋
2. GitHub PR 생성
3. 리포트 저장 (`.atdd/runtime/fixes/fix-{error-id}.md`)

## 브랜치 네이밍 규칙
```
fix/claude-loki-error-{error-type}-{YYYYMMDD}
```

예시:
- `fix/claude-loki-error-NullPointerException-20260216`
- `fix/claude-loki-error-SQLException-20260216`
- `fix/claude-loki-error-500-20260216`

## 출력 형식

### 콘솔 진행 상황
```
🔧 Self-Healing 시작: ERR-001

📂 Phase 1: 준비
✅ 분석 리포트 로드 완료
✅ 브랜치 생성: fix/claude-loki-error-NullPointerException-20260216

📝 Phase 2: Gherkin 생성
✅ Feature 파일 생성: src/test/resources/features/fix-ERR-001.feature

🔴 Phase 3: 테스트 작성 (Red)
✅ Step Definition 작성 완료
⏳ 테스트 실행... (실패 예상)
✅ 테스트 실패 확인 (정상 - Red 단계)

🟢 Phase 4: 수정 코드 작성 (Green)
✅ UserService.java 수정 완료
✅ SecurityConfig.java 수정 완료
⏳ 테스트 실행... (성공 예상)
✅ 모든 테스트 통과 (Green 단계)

📊 Phase 5: 검증
✅ Unit Tests: 12/12 통과
✅ Integration Tests: 5/5 통과
✅ Cucumber Tests: 3/3 통과
✅ Coverage: 85%

🚀 Phase 6: PR 생성
✅ Commit: fix: NullPointerException in UserService (ERR-001)
✅ Push: origin/fix/claude-loki-error-NullPointerException-20260216
✅ PR 생성: #42

┌─────────────────────────────────────────────────────┐
│ PR #42: fix: NullPointerException in UserService    │
│                                                      │
│ URL: https://github.com/org/repo/pull/42            │
│ Branch: fix/claude-loki-error-NullPointerException-20260216 │
│ Files Changed: 4                                    │
│                                                      │
│ Changes:                                            │
│ - UserService.java: null 체크 추가                  │
│ - SecurityConfig.java: 인증 설정 추가               │
│ - fix-ERR-001.feature: E2E 테스트 시나리오          │
│ - UserStepDefs.java: Step Definition                │
└─────────────────────────────────────────────────────┘

📁 수정 이력: .atdd/runtime/fixes/fix-ERR-001.md
```

### 커밋 메시지 템플릿
```
fix: {error-type} in {class-name} (ERR-{id})

## 원인
{근본 원인}

## 수정 내용
- {수정 내용 1}
- {수정 내역 2}

## 테스트
- {테스트 시나리오}

Refs: ERR-{id}
Co-Authored-By: Claude Code <noreply@anthropic.com>
```

### 수정 이력 파일 구조
```markdown
# 수정 이력 - ERR-001

## 개요
- 에러 ID: ERR-001
- 수정 시간: 2026-02-16 14:45:30
- 브랜치: fix/claude-loki-error-NullPointerException-20260216
- PR: #42

## 원본 에러
- 타입: NullPointerException
- 위치: UserService.java:45
- 발생 횟수: 2회

## 근본 원인
Security 설정 누락으로 미인증 요청 허용

## 수정 내용

### UserService.java
```java
// Before
public User findById(Long userId) {
    return userRepository.findById(userId).orElse(null);
}

// After
public Optional<User> findById(Long userId) {
    if (userId == null) {
        return Optional.empty();
    }
    return userRepository.findById(userId);
}
```

### SecurityConfig.java
```java
// 추가된 설정
.requestMatchers("/api/users/**").authenticated()
```

## 테스트 결과
- Unit Tests: ✅ 12/12
- Integration Tests: ✅ 5/5
- Cucumber Tests: ✅ 3/3
- Coverage: 85%

## 파일 변경
| 파일 | 변경 유형 | 라인 수 |
|------|----------|---------|
| UserService.java | Modified | +5 -2 |
| SecurityConfig.java | Modified | +3 |
| fix-ERR-001.feature | Added | +25 |
| UserStepDefs.java | Added | +30 |

## PR 정보
- URL: https://github.com/org/repo/pull/42
- 상태: Open
- Reviewer: (대기 중)

---
생성 시간: 2026-02-16 14:45:30
```

## 검증 체크리스트

PR 생성 전 확인 사항:
- [ ] 모든 테스트 통과
- [ ] 커버리지 80% 이상
- [ ] 코드 스타일 검사 통과
- [ ] Gherkin 시나리오가 실제 에러 재현
- [ ] 수정 코드가 근본 원인 해결

## 에러 처리

| 상황 | 대응 |
|------|------|
| 테스트 실패 | 수정 코드 재작성 |
| 기존 테스트 깨짐 | 회귀 분석 후 수정 |
| 충돌 발생 | 사용자에게 알림 |
| PR 생성 실패 | 수동 커밋 가이드 제공 |

## 다음 단계
- PR 리뷰 대기
- 추가 에러 수정: `/fix {another-error-id}`
- 운영 재모니터링: `/monitor`
