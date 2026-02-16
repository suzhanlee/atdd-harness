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

## 프로세스

### Phase 1: 로컬 테스트 실행

#### Unit Tests
```bash
./gradlew test
```

#### Integration Tests
```bash
./gradlew integrationTest
```

#### E2E Tests (Cucumber)
```bash
./gradlew cucumber
```

#### Coverage Report
```bash
./gradlew jacocoTestReport
```

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

### Phase 4: 검증 리포트 생성

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
- Line Coverage: 85%
- Branch Coverage: 78%
- Method Coverage: 90%

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

## 다음 단계
- 배포 진행 (수동)
- 추가 모니터링: `/monitor`
