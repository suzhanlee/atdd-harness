# Feature File 분리 가이드

## 개요

Epic 기반 Feature 파일 분리 규칙과 Best Practice를 정의한다.

---

## 분리 기준

### Epic 기반 분리 (기본)

각 Epic = 별도 Feature 파일

```
{topic}-{epic번호}-{epic제목-kebab}.feature
```

**예시**:
| Epic | Feature 파일명 |
|------|---------------|
| Epic 1: 구매 요청 생성 | `apple-iap-subscription-01-purchase.feature` |
| Epic 2: 영수증 검증 | `apple-iap-subscription-02-verification.feature` |
| Epic 3: 구독 상태 관리 | `apple-iap-subscription-03-subscription.feature` |

### 크기 기반 분리 (예외)

- 시나리오 15개 초과 시 하위 기능으로 분리 고려
- 라인 400 초과 시 분리 필수

---

## 크기 가이드라인

| 지표 | 권장 | 제한 | 기준 |
|------|------|------|------|
| 시나리오/Feature | ≤ 10 | ≤ 15 | 가독성 |
| 라인/Feature | ≤ 250 | ≤ 400 | 유지보수성 |
| Background Steps | ≤ 5 | ≤ 10 | 중복 최소화 |

---

## 파일 네이밍 규칙

### 기본 형식

```
{topic}-{nn}-{title-in-kebab-case}.feature
```

### 네이밍 규칙

| 규칙 | 설명 | 예시 |
|------|------|------|
| topic | context.json의 topic 값 | `apple-iap-subscription` |
| nn | Epic 번호 (2자리) | `01`, `02`, `03` |
| title | Epic 제목 (kebab-case) | `purchase`, `verification` |

### 잘못된 예시

| ❌ BAD | ✅ GOOD | 이유 |
|--------|---------|------|
| `purchase.feature` | `apple-iap-subscription-01-purchase.feature` | topic, 번호 누락 |
| `01_Purchase.feature` | `apple-iap-subscription-01-purchase.feature` | 언더스코어 사용 |
| `apple-iap-01.feature` | `apple-iap-subscription-01-purchase.feature` | 제목 누락 |

---

## Background 공유 전략

### 공통 Background 복제

각 Feature 파일에 동일한 Background를 복제:

```gherkin
# apple-iap-subscription-01-purchase.feature
Feature: 구매 요청 생성

  Background:
    Given 데이터베이스가 초기화되어 있다
    And Apple IAP 설정이 되어 있다

# apple-iap-subscription-02-verification.feature
Feature: 영수증 검증

  Background:
    Given 데이터베이스가 초기화되어 있다
    And Apple IAP 설정이 되어 있다
```

### Why 복제인가?

- 각 Feature 파일이 독립적으로 실행 가능
- 병렬 테스트 실행 시 간섭 없음
- Feature 파일만 읽어도 전체 컨텍스트 파악 가능

---

## 태그 규칙

### Epic 태그

```gherkin
@epic-1 @feature-purchase
Feature: 구매 요청 생성
```

### 태그 구조

| 태그 | 형식 | 예시 | 용도 |
|------|------|------|------|
| Epic | `@epic-{n}` | `@epic-1` | Epic 식별 |
| Feature | `@feature-{name}` | `@feature-purchase` | Feature 식별 |
| 시나리오 유형 | `@happy`, `@edge` | `@happy` | Happy/Edge 구분 |
| 카테고리 | `@validation`, `@business` | `@validation` | 예외 카테고리 |

### 태그 예시

```gherkin
@epic-1 @feature-purchase
Feature: 구매 요청 생성

  Background:
    Given 데이터베이스가 초기화되어 있다

  @happy
  Scenario: 정상적인 구매 요청
    ...

  @edge @validation
  Scenario: 필수값 누락
    ...
```

---

## 분리 처리 프로세스

### Step 1: Epic 정보 로드

```
{basePath}/epic-split/epics.md 읽기
```

### Step 2: Epic 파싱

```markdown
# epics.md 예시
## Epic 1: 구매 요청 생성
## Epic 2: 영수증 검증
## Epic 3: 구독 상태 관리
```

### Step 3: 시나리오 분류

draft-happy-path.md와 draft-edge-cases.md에서 Epic별 시나리오 분류:

```markdown
# draft-happy-path.md 예시

# Feature 1: 구매 요청 (Epic 1)

## Happy Path
...

# Feature 2: 영수증 검증 (Epic 2)

## Happy Path
...
```

### Step 4: Epic별 .feature 파일 생성

```
src/test/resources/features/
├── apple-iap-subscription-01-purchase.feature
├── apple-iap-subscription-02-verification.feature
└── apple-iap-subscription-03-subscription.feature
```

### Step 5: context.json 업데이트

```json
{
  "phase": "gherkin",
  "featurePath": "src/test/resources/features/apple-iap-subscription/",
  "featurePaths": [
    "src/test/resources/features/apple-iap-subscription-01-purchase.feature",
    "src/test/resources/features/apple-iap-subscription-02-verification.feature",
    "src/test/resources/features/apple-iap-subscription-03-subscription.feature"
  ]
}
```

---

## 호환성 가이드

### Epic 정보 없을 때

```markdown
# epics.md가 없으면 기존 방식 유지
src/test/resources/features/{topic}.feature
```

### 기존 프로젝트

이미 생성된 단일 `.feature` 파일 유지

### TDD 스킬 연동

```markdown
# TDD 스킬은 featurePaths 배열로 다중 파일 지원
context.json → featurePaths 배열 순회
```

---

## 분리 예시

### Before (단일 파일)

```
src/test/resources/features/apple-iap-subscription.feature
- 698 라인
- 7개 Epic
- 56개 시나리오
```

### After (Epic별 분리)

```
src/test/resources/features/
├── apple-iap-subscription-01-purchase.feature      # 95 라인, 8 시나리오
├── apple-iap-subscription-02-verification.feature  # 80 라인, 7 시나리오
├── apple-iap-subscription-03-subscription.feature  # 110 라인, 9 시나리오
├── apple-iap-subscription-04-notification.feature  # 75 라인, 6 시나리오
├── apple-iap-subscription-05-renewal.feature       # 90 라인, 8 시나리오
├── apple-iap-subscription-06-refund.feature        # 85 라인, 7 시나리오
└── apple-iap-subscription-07-admin.feature         # 70 라인, 11 시나리오
```

---

## 체크리스트

### 분리 전

- [ ] epics.md 존재 확인
- [ ] Epic별 시나리오 개수 파악
- [ ] Background 공통 요소 식별

### 분리 후

- [ ] 각 Feature 파일 ≤ 400 라인
- [ ] 각 Feature 파일 ≤ 15 시나리오
- [ ] Epic 태그 (`@epic-{n}`) 포함
- [ ] Feature 태그 (`@feature-{name}`) 포함
- [ ] context.json featurePaths 업데이트

---

## 참조

- [scenario-template.md](scenario-template.md) - 시나리오 템플릿
- [step-naming-convention.md](step-naming-convention.md) - Step 네이밍 컨벤션
- [coverage-matrix.md](coverage-matrix.md) - 커버리지 매트릭스
