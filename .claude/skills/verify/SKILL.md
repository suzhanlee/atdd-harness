---
name: verify
description: This skill should be used when the user asks to "/verify", "검증", "최종 검증", "배포 전 검증", or needs to run all tests and verify code quality before deployment.
disable-model-invocation: true
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit, Bash
---

# Verify Skill

최종 검증 단계에서 로컬 테스트와 운영 로그 기반 회귀 테스트를 수행합니다.

## 트리거
- `/verify`
- "검증해줘"
- "최종 검증 실행"
- "배포 전 검증"

## 전제 조건
- TDD/리팩토링 단계가 완료되어야 함
- 또는 `/fix` 완료 후 검증이 필요한 경우

## 입력
- `src/main/java/**/*.java`
- `src/test/java/**/*.java`
- `src/test/resources/features/**/*.feature`

## 프로세스

### Phase 1: 로컬 테스트 실행

#### Unit Tests
```bash
./gradlew test
```

**목표**:
- 모든 테스트 통과
- 0 Failures, 0 Errors

#### Integration Tests
```bash
./gradlew integrationTest
```

**목표**:
- 모든 테스트 통과
- DB 연동 정상

#### E2E Tests (Cucumber)
```bash
./gradlew cucumber
```

**목표**:
- 모든 시나리오 통과
- 0 Failed Scenarios

#### Coverage Report
```bash
./gradlew jacocoTestReport
```

**목표**:
- Line Coverage ≥ 80%
- Branch Coverage ≥ 75%

### Phase 2: 코드 품질 검사

#### 정적 분석
```bash
./gradlew spotlessCheck    # 코드 스타일
./gradlew detekt           # 정적 분석 (Kotlin)
./gradlew pmdMain          # PMD 검사
```

#### 커버리지 확인
- 최소 커버리지: 80%
- 새로운 코드: 90% 이상 권장

### Phase 3: 운영 로그 기반 회귀 테스트 (Loki 연동)

수정 후 운영 환경에서 동일한 에러가 발생하지 않는지 확인합니다.

#### S3 로그 조회
```bash
# 최근 1시간 로그에서 해당 에러 패턴 검색
aws s3api select-object-content \
  --bucket ${LOKI_BUCKET} \
  --key logs/app.json \
  --expression "SELECT * FROM s3object s WHERE s.level = 'ERROR' AND s.timestamp > '${LAST_DEPLOY_TIME}'" \
  --expression-type SQL \
  --input-serialization '{"JSON": {"Type": "LINES"}}' \
  --output-serialization '{"JSON": {}}' \
  recent-errors.json
```

#### 회귀 확인 항목
| 항목 | 기준 |
|------|------|
| 동일 에러 발생 | 0건 (통과) |
| 유사 에러 발생 | 0건 (통과) |
| 새로운 에러 | 분석 필요 |

### Phase 4: Gherkin 시나리오 커버리지

모든 시나리오가 실행되었는지 확인:
- Feature 파일 수 = 실행된 Feature 수
- Scenario 수 = 실행된 Scenario 수

### Phase 5: 검증 리포트 생성

`.atdd/reports/verification-{timestamp}.md` 파일 생성

## 출력 형식

### 콘솔 출력
```
✅ 최종 검증 시작

📋 Phase 1: 로컬 테스트
┌─────────────────────────────────────────────────────┐
│ Unit Tests          ✅ 45/45 통과                    │
│ Integration Tests   ✅ 12/12 통과                    │
│ Cucumber Tests      ✅ 8/8 통과                      │
│ Coverage            ✅ 85% (기준: 80%)               │
└─────────────────────────────────────────────────────┘

🔍 Phase 2: 코드 품질
┌─────────────────────────────────────────────────────┐
│ Spotless            ✅ 스타일 검사 통과              │
│ PMD                 ✅ 정적 분석 통과                │
│ Security Scan       ✅ 취약점 없음                   │
└─────────────────────────────────────────────────────┘

📊 Phase 3: 운영 회귀 테스트 (Loki)
┌─────────────────────────────────────────────────────┐
│ 조회 기간           2026-02-16 14:00 ~ 15:00        │
│ 분석 대상           NullPointerException (ERR-001)  │
│                                                      │
│ 동일 에러           0건 ✅                           │
│ 유사 에러           0건 ✅                           │
│ 새로운 에러         0건 ✅                           │
│                                                      │
│ 결론: 회귀 없음, 수정 성공                          │
└─────────────────────────────────────────────────────┘

📁 검증 리포트: .atdd/reports/verification-20260216-150000.md

🎉 모든 검증 통과 - 배포 준비 완료
```

### 검증 리포트 파일 구조
```markdown
# 검증 리포트 - 2026-02-16

## 개요
- 검증 시간: 2026-02-16 15:00:00
- 검증 대상: fix/claude-loki-error-NullPointerException-20260216
- 관련 PR: #42

## Phase 1: 로컬 테스트

### Unit Tests
- 실행: 45개
- 통과: 45개
- 실패: 0개
- 스킵: 0개

### Integration Tests
- 실행: 12개
- 통과: 12개
- 실패: 0개

### E2E Tests (Cucumber)
- Features: 8개
- Scenarios: 15개
- 통과: 15개
- 실패: 0개

### Coverage
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

## Phase 2: 코드 품질

### 정적 분석 결과
| 항목 | 결과 |
|------|------|
| Spotless | ✅ 통과 |
| PMD | ✅ 통과 |
| Security Scan | ✅ 통과 |

### 발견된 이슈
없음

## Phase 3: 운영 회귀 테스트 (Loki)

### 조회 조건
- 기간: 2026-02-16 14:00 ~ 15:00
- 대상 에러: NullPointerException (ERR-001)

### 결과
| 항목 | 발생 횟수 | 상태 |
|------|----------|------|
| 동일 에러 | 0 | ✅ 통과 |
| 유사 에러 | 0 | ✅ 통과 |
| 새로운 에러 | 0 | ✅ 통과 |

### 상세 로그 분석
```
조회된 에러 로그 없음
```

## 종합 평가

| 항목 | 결과 |
|------|------|
| 모든 테스트 통과 | ✅ |
| 커버리지 기준 충족 | ✅ (85% > 80%) |
| 코드 품질 통과 | ✅ |
| 운영 회귀 없음 | ✅ |

## 결론
🎉 **배포 준비 완료**

## 완료 조건 체크리스트

- [x] 모든 테스트 통과
- [x] 커버리지 ≥ 80%
- [x] Lint 에러 0개
- [x] 모든 Gherkin 시나리오 통과
- [x] 운영 회귀 없음

---
생성 시간: 2026-02-16 15:00:00
```

## 환경 변수
```bash
# 필수
LOKI_BUCKET=your-loki-bucket-name

# 선택
COVERAGE_THRESHOLD=80          # 최소 커버리지
VERIFICATION_WINDOW_HOURS=1    # Loki 조회 기간
```

## 실패 시 대응

| 실패 항목 | 대응 |
|----------|------|
| 테스트 실패 | `/tdd`로 돌아가서 수정 |
| 커버리지 미달 | 테스트 케이스 추가 |
| 코드 품질 실패 | `/refactor`로 수정 |
| 운영 회귀 발견 | `/analyze-error`로 원인 분석 |

### 테스트 실패 예시
```
❌ Unit Tests: 2 Failures

Failed Tests:
1. UserServiceTest.createUser - NullPointerException
2. UserRepositoryTest.findByEmail - AssertionError

조치: 테스트 실패 원인 분석 후 수정
```

### 커버리지 미달 예시
```
❌ Coverage: 72% (목표: 80%)

부족한 패키지:
- infrastructure: 65%
- interfaces: 68%

조치: 테스트 케이스 추가 필요
```

### Lint 에러 예시
```
❌ Lint: 3 Errors

1. UserService.java:25 - Unused import
2. OrderController.java:42 - Line too long
3. Product.java:15 - Missing Javadoc

조치: ./gradlew spotlessApply 실행
```

## 완료 조건

- [x] 모든 테스트 통과
- [x] 커버리지 ≥ 80%
- [x] Lint 에러 0개
- [x] 모든 Gherkin 시나리오 통과
- [x] 운영 회귀 없음 (Loki)

## ATDD 사이클 완료

모든 검증 항목 통과 시 ATDD 사이클 종료 🎉

## 다음 단계
- 배포 진행 (수동)
- 추가 모니터링: `/monitor`

## 참조
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
