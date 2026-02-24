# 도메인 모델

## Bounded Context

### Subscription Context
구독 생성, 갱신, 만료, 환불, 업그레이드를 관리하는 핵심 컨텍스트

---

## Aggregate

### Subscription (Aggregate Root)

**식별자**: `originalTransactionId` (Apple 제공 비즈니스 키)

#### 속성

| 속성 | 타입 | 설명 |
|------|------|------|
| id | Long | 내부 식별자 |
| originalTransactionId | String | Apple 원본 트랜잭션 ID |
| userId | Long | 사용자 ID |
| productId | ProductId (VO) | Apple 제품 ID |
| status | SubscriptionStatus (Enum) | 구독 상태 |
| expiresAt | Instant | 만료 일시 |
| currentPeriodStart | Instant | 현재 기간 시작 |
| currentPeriodEnd | Instant | 현재 기간 종료 |
| version | Long | 낙관적 락 버전 |

#### 행동

| 메서드 | 설명 | 불변식 |
|--------|------|--------|
| `create()` | 구독 생성 | originalTransactionId 필수, status=ACTIVE |
| `renew()` | 구독 갱신 | status=ACTIVE|GRACE_PERIOD|BILLING_RETRY |
| `expire()` | 만료 처리 | status=ACTIVE|GRACE_PERIOD|BILLING_RETRY |
| `refund()` | 환불 처리 | status=ACTIVE |
| `upgrade()` | 업그레이드 | status=ACTIVE, newProductId > currentProductId |
| `enterGracePeriod()` | 유예 기간 진입 | status=ACTIVE |
| `enterBillingRetry()` | 결제 재시도 진입 | status=GRACE_PERIOD |
| `revoke()` | 가족 공유 취소 | status=ACTIVE |
| `changeStatus()` | 상태 변경 | SubscriptionStatusSpec 준수 |

#### 불변식

1. `originalTransactionId`는 null이 아니어야 함
2. `userId`는 null이 아니어야 함
3. `status`는 null이 아니어야 함
4. `expiresAt`은 null이 아니어야 함
5. 상태 전이는 `SubscriptionStatusSpec` 규칙을 따라야 함
6. 업그레이드는 상위 등급으로만 가능

#### 발행 이벤트

| 이벤트 | 발생 시점 |
|--------|----------|
| SubscriptionRenewedEvent | 갱신 성공 시 |
| SubscriptionExpiredEvent | 만료 처리 시 |
| SubscriptionRefundedEvent | 환불 처리 시 |
| SubscriptionUpgradedEvent | 업그레이드 시 |
| GracePeriodEnteredEvent | 유예 기간 진입 시 |

---

## Entity

### Transaction

**식별자**: `transactionId` (Apple 제공)

#### 속성

| 속성 | 타입 | 설명 |
|------|------|------|
| id | Long | 내부 식별자 |
| subscriptionId | Long | 구독 ID |
| transactionId | String | Apple 트랜잭션 ID |
| type | Type (Enum) | PURCHASE, RENEWAL, REFUND, UPSELL |
| amount | Money (VO) | 결제 금액 |
| purchasedAt | Instant | 구매 일시 |

#### 행동

| 메서드 | 설명 |
|--------|------|
| `create()` | 트랜잭션 생성 (정적 팩토리) |
| `isRefund()` | 환불 트랜잭션 여부 |

---

### SubscriptionHistory

**식별자**: `id` (자동 생성)

#### 속성

| 속성 | 타입 | 설명 |
|------|------|------|
| id | Long | 내부 식별자 |
| subscriptionId | Long | 구독 ID |
| fromStatus | SubscriptionStatus | 변경 전 상태 |
| toStatus | SubscriptionStatus | 변경 후 상태 |
| reason | String | 변경 사유 |
| transactionId | String | 관련 트랜잭션 ID |
| changedAt | Instant | 변경 일시 |
| note | String | 비고 |

#### 행동

| 메서드 | 설명 |
|--------|------|
| `create()` | 이력 생성 (정적 팩토리) |

---

### Refund

**식별자**: `id` (자동 생성)

#### 속성

| 속성 | 타입 | 설명 |
|------|------|------|
| id | Long | 내부 식별자 |
| subscriptionId | Long | 구독 ID |
| originalTransactionId | String | Apple 원본 트랜잭션 ID |
| refundAmount | Money (VO) | 환불 금액 |
| reason | Reason (Enum) | USER_REQUEST, APPLE_APPROVED 등 |
| appleReasonCode | String | Apple 환불 코드 |
| refundedAt | Instant | 환불 일시 |

#### 행동

| 메서드 | 설명 |
|--------|------|
| `create()` | 환불 생성 (정적 팩토리) |

---

## Value Object

### SubscriptionStatus (Enum)

| 값 | 설명 | 활성 여부 |
|----|------|----------|
| ACTIVE | 활성 구독 | ✅ |
| EXPIRED | 만료 | ❌ |
| GRACE_PERIOD | 유예 기간 | ✅ |
| BILLING_RETRY | 결제 재시도 | ✅ |
| REFUNDED | 환불 완료 | ❌ |
| REVOKED | 가족 공유 취소 | ❌ |

---

### Money

| 속성 | 타입 | 설명 |
|------|------|------|
| amount | BigDecimal | 금액 |
| currency | Currency | 통화 |

**행동**: `add()`, `subtract()`, `multiply()`, `isGreaterThan()`, `isZero()`

**불변식**: amount ≥ 0, currency ≠ null

---

### Period

| 속성 | 타입 | 설명 |
|------|------|------|
| startAt | Instant | 시작 시간 |
| endAt | Instant | 종료 시간 |

**행동**: `contains()`, `overlaps()`, `getDurationInSeconds()`

**불변식**: startAt < endAt

---

### ProductId

| 속성 | 타입 | 설명 |
|------|------|------|
| value | String | Apple Product ID |

**행동**: `isHigherTierThan()`

**불변식**: value ≠ null, value ≠ ""

---

### WebhookEvent

| 속성 | 타입 | 설명 |
|------|------|------|
| notificationType | String | Apple 이벤트 타입 |
| transactionId | String | 트랜잭션 ID |
| originalTransactionId | String | 원본 트랜잭션 ID |
| productId | String | 제품 ID |
| signedDate | Instant | 서명 일시 |
| expiresDate | Instant | 만료 일시 |
| environment | String | 환경 |

**행동**: `isSandbox()`, `isRecent()`

---

## Domain Service

### UpgradeService

| 메서드 | 설명 |
|--------|------|
| `calculateUpgradePrice()` | 업그레이드 추가 금액 계산 |
| `calculateNewPeriod()` | 새 구독 기간 계산 |
| `canUpgrade()` | 업그레이드 가능 여부 |
| `compareTier()` | 제품 등급 비교 |

---

### RefundCalculationService

| 메서드 | 설명 |
|--------|------|
| `calculateRefundAmount()` | 환불 금액 계산 |
| `canRefund()` | 환불 가능 여부 |
| `getRefundPeriodDays()` | 환불 가능 기간 |

---

## Specification

### SubscriptionStatusSpec

상태 전이 규칙 검증

```
ACTIVE → EXPIRED, GRACE_PERIOD, BILLING_RETRY, REFUNDED, REVOKED
GRACE_PERIOD → ACTIVE, EXPIRED, BILLING_RETRY
BILLING_RETRY → ACTIVE, EXPIRED
EXPIRED → (종료 상태)
REFUNDED → (종료 상태)
REVOKED → (종료 상태)
```

| 메서드 | 설명 |
|--------|------|
| `canTransition()` | 전이 가능 여부 |
| `validateTransition()` | 전이 검증 (예외 발생) |
| `getTargetStatus()` | Apple 이벤트 → 목표 상태 매핑 |
| `isTerminalStatus()` | 종료 상태 여부 |
| `isActiveStatus()` | 활성 상태 여부 |

---

## Policy

### RefundPolicy

| 메서드 | 설명 |
|--------|------|
| `getRefundPeriodDays()` | 환불 가능 기간 |
| `isPartialRefundAllowed()` | 부분 환불 허용 여부 |
| `getRefundFeeRate()` | 환불 수수료율 |
| `isRefundableForReason()` | 사유별 환불 가능 여부 |
| `getRefundRate()` | 경과 기간별 환불 비율 |

---

## Repository

### SubscriptionRepository

| 메서드 | 설명 |
|--------|------|
| `findByOriginalTransactionId()` | 비즈니스 키로 조회 |
| `findActiveByUserId()` | 사용자 활성 구독 조회 |
| `findExpiringBetween()` | 만료 예정 구독 조회 |
| `findByStatus()` | 상태별 조회 |
| `existsByOriginalTransactionId()` | 존재 여부 |

---

### TransactionRepository

| 메서드 | 설명 |
|--------|------|
| `findByTransactionId()` | Apple TX ID로 조회 |
| `findBySubscriptionId()` | 구독별 트랜잭션 |
| `existsByTransactionId()` | 멱등성 검사 |
| `findLastRenewalBySubscriptionId()` | 마지막 갱신 조회 |

---

### RefundRepository

| 메서드 | 설명 |
|--------|------|
| `findBySubscriptionId()` | 구독별 환불 조회 |
| `findByOriginalTransactionId()` | Apple TX ID로 조회 |
| `existsBySubscriptionId()` | 환불 여부 |

---

## 협력 흐름

### Webhook 처리 흐름

```
Apple Server
    │
    ▼
WebhookController.handleAppleWebhook()
    │
    ▼
JwsVerifier.verify() ──────────────→ SecurityException (검증 실패)
    │
    ▼
AppleWebhookParser.parse()
    │
    ▼
ProcessWebhookUseCase.process()
    │
    ├──→ SubscriptionRepository.findByOriginalTransactionId()
    │         │
    │         ├── 신규 → Subscription.create()
    │         │
    │         └── 기존 → Subscription.renew() / expire() / refund() / upgrade()
    │
    ▼
TransactionRepository.existsByTransactionId() (멱등성)
    │
    ▼
Transaction.create()
    │
    ▼
SubscriptionHistory.create()
    │
    ▼
Domain Event 발행 (SubscriptionRenewedEvent 등)
```

### 업그레이드 처리 흐름

```
Client
    │
    ▼
SubscriptionController.getUpgradeQuote()
    │
    ▼
UpgradeSubscriptionUseCase.getQuote()
    │
    ├──→ UpgradeService.canUpgrade()
    │
    └──→ UpgradeService.calculateUpgradePrice()
    │
    ▼
[사용자 승인]
    │
    ▼
SubscriptionController.upgradeSubscription()
    │
    ▼
UpgradeSubscriptionUseCase.upgrade()
    │
    ├──→ Subscription.upgrade()
    │
    ├──→ Transaction.create(type=UPSELL)
    │
    └──→ SubscriptionHistory.create()
    │
    ▼
SubscriptionUpgradedEvent 발행
```
