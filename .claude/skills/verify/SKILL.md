---
name: verify
description: 최종 검증을 수행한다. 모든 테스트 실행 및 품질 확인 시 사용.
disable-model-invocation: true
user-invocable: true
allowed-tools: Read, Grep, Glob, Bash
---

# 최종 검증

## 목표
모든 테스트를 실행하고 최종 품질을 확인한다.

## 입력
- `src/main/java/**/*.java`
- `src/test/java/**/*.java`
- `src/test/resources/features/**/*.feature`

## 트리거
- `/verify` 명령어 실행
- 리팩토링 완료 후 자동 제안

## 검증 항목

### 1. Unit Tests
```bash
./gradlew test
```

**목표**:
- 모든 테스트 통과
- 0 Failures, 0 Errors

### 2. Integration Tests
```bash
./gradlew integrationTest
```

**목표**:
- 모든 테스트 통과
- DB 연동 정상

### 3. E2E Tests (Cucumber)
```bash
./gradlew cucumber
```

**목표**:
- 모든 시나리오 통과
- 0 Failed Scenarios

### 4. Coverage
```bash
./gradlew jacocoTestReport
```

**목표**:
- Line Coverage ≥ 80%
- Branch Coverage ≥ 75%

### 5. Lint
```bash
./gradlew spotlessCheck
```

**목표**:
- 0 Errors
- 0 Warnings (권장)

## 프로세스

### 1. 전체 테스트 실행

```bash
# 순차 실행
./gradlew test
./gradlew integrationTest
./gradlew cucumber
```

또는 한 번에:
```bash
./gradlew check
```

### 2. 커버리지 분석

```bash
./gradlew jacocoTestReport
```

리포트 위치: `build/reports/jacoco/test/html/index.html`

### 3. 코드 품질 체크

```bash
./gradlew spotlessCheck
```

### 4. Gherkin 시나리오 커버리지

모든 시나리오가 실행되었는지 확인:
- Feature 파일 수 = 실행된 Feature 수
- Scenario 수 = 실행된 Scenario 수

### 5. 검증 리포트 작성

## 검증 리포트 예시

```markdown
# 최종 검증 리포트

## 검증 일시
2024-01-20 16:00:00

## 검증 결과: ✅ ALL PASS

## 1. Unit Tests

\`\`\`
./gradlew test

BUILD SUCCESSFUL

Tests: 45
- Passed: 45
- Failed: 0
- Skipped: 0

Time: 3.5s
\`\`\`

**결과**: ✅ PASS

## 2. Integration Tests

\`\`\`
./gradlew integrationTest

BUILD SUCCESSFUL

Tests: 12
- Passed: 12
- Failed: 0
- Skipped: 0

Time: 15.2s
\`\`\`

**결과**: ✅ PASS

## 3. E2E Tests (Cucumber)

\`\`\`
./gradlew cucumber

BUILD SUCCESSFUL

Features: 5
Scenarios: 15
- Passed: 15
- Failed: 0

Time: 28.7s
\`\`\`

**결과**: ✅ PASS

## 4. Coverage

| Package | Line | Branch |
|---------|------|--------|
| domain.entity | 95% | 90% |
| domain.vo | 100% | 100% |
| domain.service | 88% | 85% |
| application | 82% | 78% |
| infrastructure | 75% | 70% |
| interfaces | 80% | 75% |
| **Total** | **85%** | **81%** |

**결과**: ✅ PASS (80% 이상)

## 5. Lint

\`\`\`
./gradlew spotlessCheck

BUILD SUCCESSFUL

Errors: 0
Warnings: 0
\`\`\`

**결과**: ✅ PASS

## 완료 조건 체크리스트

- [x] 모든 테스트 통과
- [x] 커버리지 ≥ 80%
- [x] Lint 에러 0개
- [x] 모든 Gherkin 시나리오 통과

## ATDD 사이클 완료 🎉

모든 검증 항목을 통과했습니다.
```

## 실패 시 대응

### 테스트 실패
```
❌ Unit Tests: 2 Failures

Failed Tests:
1. UserServiceTest.createUser - NullPointerException
2. UserRepositoryTest.findByEmail - AssertionError

조치: 테스트 실패 원인 분석 후 수정
```

### 커버리지 미달
```
❌ Coverage: 72% (목표: 80%)

부족한 패키지:
- infrastructure: 65%
- interfaces: 68%

조치: 테스트 케이스 추가 필요
```

### Lint 에러
```
❌ Lint: 3 Errors

1. UserService.java:25 - Unused import
2. OrderController.java:42 - Line too long
3. Product.java:15 - Missing Javadoc

조치: ./gradlew spotlessApply 실행
```

## 출력 파일

### VERIFICATION-report.md
```markdown
# 최종 검증 리포트

## 검증 일시
[날짜 시간]

## 검증 결과: ✅ ALL PASS / ❌ FAIL

## 상세 결과
[위 예시 참조]

## 완료 조건
- [ ] 모든 테스트 통과
- [ ] 커버리지 ≥ 80%
- [ ] Lint 에러 0개
- [ ] 모든 Gherkin 시나리오 통과
```

### coverage-report/
JaCoCo HTML 리포트 복사

## 완료 조건

- [x] 모든 테스트 통과
- [x] 커버리지 ≥ 80%
- [x] Lint 에러 0개
- [x] 모든 Gherkin 시나리오 통과

## ATDD 사이클 완료

모든 검증 항목 통과 시 ATDD 사이클 종료 🎉

## 참조
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
