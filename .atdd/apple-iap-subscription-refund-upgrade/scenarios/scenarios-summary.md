# Gherkin 시나리오 요약

> Apple IAP Subscription - Refund & Upgrade (Epic별 Feature 분리)

---

## 생성된 파일

| 파일 | 경로 | 설명 |
|------|------|------|
| Happy Path | `.atdd/.../scenarios/draft-happy-path.md` | 정상 흐름 시나리오 |
| Edge Cases | `.atdd/.../scenarios/draft-edge-cases.md` | 예외 흐름 시나리오 |
| Feature Files | `src/test/resources/features/apple-iap-subscription-*.feature` | Cucumber Feature 파일 (7개) |

---

## Epic별 Feature 파일

| Epic | Feature 파일 | Happy | Edge | Total |
|------|--------------|-------|------|-------|
| Epic 1: 구독 구매 | `apple-iap-subscription-01-purchase.feature` | 3 | 6 | 9 |
| Epic 2: 영수증 검증 | `apple-iap-subscription-02-verification.feature` | 1 | 5 | 6 |
| Epic 3: Webhook 수신 | `apple-iap-subscription-03-webhook.feature` | 2 | 7 | 9 |
| Epic 4: 이벤트 처리 | `apple-iap-subscription-04-event-processing.feature` | 6 | 4 | 10 |
| Epic 5: 구독 업그레이드 | `apple-iap-subscription-05-upgrade.feature` | 3 | 7 | 10 |
| Epic 6: 무료 체험 | `apple-iap-subscription-06-free-trial.feature` | 2 | 5 | 7 |
| Epic 7: 구독 이력 | `apple-iap-subscription-07-history.feature` | 1 | 4 | 5 |
| **Total** | | **17** | **39** | **56** |

---

## 시나리오 통계

### 전체 통계

| 구분 | 시나리오 수 |
|------|------------|
| Happy Path | 17 |
| Edge Cases | 39 |
| **Total** | **56** |

### Epic별 상세

#### Epic 1: 구독 구매 및 상태 조회 (9개)
- Happy Path: 3
- Edge Cases: 6
  - 입력 검증: 2
  - 비즈니스 규칙: 1
  - 권한/인증: 1
  - 외부 서비스: 2

#### Epic 2: Apple 영수증 검증 (6개)
- Happy Path: 1
- Edge Cases: 5
  - 입력 검증: 2
  - 외부 서비스: 2

#### Epic 3: Apple S2S Webhook 수신 (9개)
- Happy Path: 2
- Edge Cases: 7
  - 보안 검증: 2
  - 멱등성: 1
  - 입력 검증: 2
  - 처리 실패: 1

#### Epic 4: Webhook 이벤트 처리 (10개)
- Happy Path: 6 (갱신, 만료, 환불, 갱신실패, 유예기간만료, 비갱신형)
- Edge Cases: 4
  - 리소스 없음: 2
  - 비즈니스 규칙: 1
  - 경계값: 1

#### Epic 5: 구독 업그레이드 (10개)
- Happy Path: 3 (Basic→Pro, Basic→Ultra, Pro→Ultra)
- Edge Cases: 7
  - 비즈니스 규칙: 3
  - 리소스 없음: 2
  - 권한/인증: 1
  - 입력 검증: 1

#### Epic 6: 무료 체험 (7개)
- Happy Path: 2 (시작, 유료전환)
- Edge Cases: 5
  - 비즈니스 규칙: 2
  - 권한/인증: 1
  - 입력 검증: 1
  - 경계값: 1

#### Epic 7: 구독 이력 관리 (5개)
- Happy Path: 1
- Edge Cases: 4
  - 리소스 없음: 1
  - 권한/인증: 1
  - 입력 검증: 2

---

## 커버리지 매트릭스

| ID | 요구사항 | Epic | 시나리오 | 커버 |
|----|----------|------|----------|------|
| **M1** | 구독 구매 | Epic 1 | 정상적인 구독 구매 | ✅ |
| **M1** | 구독 상태 조회 | Epic 1 | 구독 상태 조회 | ✅ |
| **M1** | 중복 구매 방지 | Epic 1 | 활성 구독이 있는 사용자의 중복 구매 방지 | ✅ |
| **M2** | Apple 영수증 검증 | Epic 2 | 영수증 검증 성공 | ✅ |
| **M3** | Webhook 수신 | Epic 3 | Webhook 수신 성공 | ✅ |
| **M3** | JWS 서명 검증 | Epic 3 | JWS 서명 검증 실패 | ✅ |
| **M4** | 구독 갱신 | Epic 4 | 구독 갱신 성공 (DID_RENEW) | ✅ |
| **M4** | 구독 만료 | Epic 4 | 구독 만료 처리 (EXPIRED) | ✅ |
| **M5** | 환불 처리 | Epic 4 | 환불 처리 (REFUND) | ✅ |
| **M5** | 환불 시 권한 회수 | Epic 4 | 환불 처리 (REFUND) | ✅ |
| **M6** | 구독 업그레이드 | Epic 5 | Basic에서 Pro로 업그레이드 | ✅ |
| **M6** | 업그레이드 검증 | Epic 5 | 다운그레이드 요청 (Pro → Basic) | ✅ |
| **S6** | 무료 체험 | Epic 6 | 무료 체험 시작 | ✅ |
| **S6** | 유료 전환 | Epic 6 | 무료 체험 종료 후 유료 구독 전환 | ✅ |
| **C8** | 구독 이력 | Epic 7 | 구독 이력 조회 | ✅ |

### 커버리지 요약

| 우선순위 | 커버리지 | 상태 |
|----------|----------|------|
| Must Have | 100% | ✅ |
| Should Have | 100% | ✅ |
| Could Have | 100% | ✅ |

---

## Gherkin 품질 검증

| 항목 | 검증 내용 | 합격 기준 | 상태 |
|------|-----------|-----------|------|
| Step 패턴 | TDD 인식 가능한 패턴 사용 | 100% 준수 | ✅ |
| Data Table | 올바른 형식의 테이블 | 필수 필드 포함 | ✅ |
| 상태 코드 | `{int}` 파라미터 사용 | 모든 Then에 명시 | ✅ |
| 중복 Step | 동일 의미의 다른 표현 | 없음 | ✅ |
| Epic별 분리 | 7개 Epic → 7개 Feature | 파일당 ≤ 15 시나리오 | ✅ |

---

## 파일 구조

```
src/test/resources/features/
├── apple-iap-subscription-01-purchase.feature        # Epic 1: 구독 구매
├── apple-iap-subscription-02-verification.feature    # Epic 2: 영수증 검증
├── apple-iap-subscription-03-webhook.feature         # Epic 3: Webhook 수신
├── apple-iap-subscription-04-event-processing.feature # Epic 4: 이벤트 처리
├── apple-iap-subscription-05-upgrade.feature         # Epic 5: 구독 업그레이드
├── apple-iap-subscription-06-free-trial.feature      # Epic 6: 무료 체험
└── apple-iap-subscription-07-history.feature         # Epic 7: 구독 이력
```

---

## 다음 단계

1. **Step Definition 작성**: 각 시나리오에 대한 Step Definition 구현
2. **TDD 구현**: `/tdd` 명령어로 도메인 구현
3. **검증**: `/verify` 명령어로 최종 검증

---

## 검증 체크리스트

- [x] 모든 Must Have 요구사항이 시나리오로 커버됨
- [x] Happy Path 17개 시나리오 작성
- [x] Edge Cases 39개 시나리오 작성
- [x] Epic별 Feature 파일 분리 (7개)
- [x] 각 Feature별 태그 일관성 유지
- [x] context.json 업데이트
