# 요구사항-도메인 추적 매트릭스

## Must Have 요구사항 매핑

| ID | 요구사항 | Entity/VO | 메서드/속성 | 상태 |
|----|----------|-----------|-------------|------|
| M1 | 구독 구매 | Subscription | `Subscription.create()` | ✅ |
| M2 | 영수증 검증 | ValidateReceiptUseCase | `validate()` | ✅ |
| M3 | 구매 검증 후 권한 부여 | Subscription | `status = ACTIVE` | ✅ |
| M4 | 중복 구매 방지 | SubscriptionRepository | `existsByOriginalTransactionId()` | ✅ |
| M5 | 업그레이드 | Subscription | `upgrade()` | ✅ |
| M6 | 업그레이드 즉시 적용 | Subscription | `upgrade()` 내 상태 변경 | ✅ |
| M7 | 다운그레이드 미지원 | UpgradeService | `canUpgrade()` (false for downgrade) | ✅ |
| M8 | 환불 이벤트 수신 | WebhookController | `handleAppleWebhook()` | ✅ |
| M9 | 환불 시 권한 회수 | Subscription | `refund()` | ✅ |
| M10 | 비갱신형 만료 | Subscription | `expire()` | ✅ |
| M11 | 갱신 이벤트 수신 | WebhookController | `handleAppleWebhook()` | ✅ |
| M12 | 갱신 성공 시 기간 연장 | Subscription | `renew()` | ✅ |
| M13 | 갱신 실패 처리 | Subscription | `enterGracePeriod()`, `enterBillingRetry()` | ✅ |

**커버리지: 13/13 (100%)** ✅

---

## Should Have 요구사항 매핑

| ID | 요구사항 | Entity/VO | 메서드/속성 | 상태 |
|----|----------|-----------|-------------|------|
| S1 | 무료 체험 시작 | Subscription | `create()` (isTrialPeriod 플래그) | ✅ |
| S2 | 무료 체험 후 유료 전환 | Subscription | `renew()` | ✅ |
| S3 | 구독 상태 조회 | SubscriptionController | `getSubscription()` | ✅ |
| S4 | 구독 상태 값 | SubscriptionStatus | Enum 값 정의 | ✅ |

**커버리지: 4/4 (100%)** ✅

---

## Could Have 요구사항 매핑

| ID | 요구사항 | Entity/VO | 메서드/속성 | 상태 |
|----|----------|-----------|-------------|------|
| C1 | 구독 이력 조회 | SubscriptionHistory | Entity + Repository | ✅ |

**커버리지: 1/1 (100%)** ✅

---

## 비기능 요구사항 매핑

| ID | 요구사항 | 계층 | 구현 요소 | 상태 |
|----|----------|------|-----------|------|
| NF1 | 서버 사이드 영수증 검증 | Application | ValidateReceiptUseCase | ✅ |
| NF2 | Webhook 무결성 검증 | Infrastructure | JwsVerifier | ✅ |
| NF3 | 재시도 메커니즘 | Infrastructure | (설정 필요) | ⚠️ |
| NF4 | 멱등성 보장 | Domain | transactionId 기반 검사 | ✅ |
| NF5 | 새로운 상품 추가 용이 | Domain | ProductId VO, 확장 가능한 구조 | ✅ |
| NF6 | JWS 검증 라이브러리 | Infrastructure | nimbus-jose-jwt (의존성 추가 필요) | ⚠️ |
| NF7 | 공개키 캐싱 | Infrastructure | ApplePublicKeyClient + Redis | ⚠️ |

**커버리지: 4/7 구현, 3/7 인프라 설정 필요**

---

## Apple 이벤트 타입 매핑

| Apple 이벤트 | SubscriptionStatusSpec.getTargetStatus() | Subscription 메서드 | 상태 |
|-------------|-----------------------------------------|---------------------|------|
| SUBSCRIBED | ACTIVE | `create()` | ✅ |
| DID_RENEW | ACTIVE | `renew()` | ✅ |
| DID_FAIL_TO_RENEW | GRACE_PERIOD / BILLING_RETRY | `enterGracePeriod()` / `enterBillingRetry()` | ✅ |
| EXPIRED | EXPIRED | `expire()` | ✅ |
| GRACE_PERIOD_EXPIRED | EXPIRED | `expire()` | ✅ |
| REFUND | REFUNDED | `refund()` | ✅ |
| REVOKE | REVOKED | `revoke()` | ✅ |
| OFFER_REDEEMED | ACTIVE | `create()` / `renew()` | ✅ |
| PRICE_CHANGE | (상태 유지) | - | ✅ |
| DID_CHANGE_RENEWAL_STATUS | (상태 유지) | - | ✅ |
| REFUND_DECLINED | (상태 유지) | - | ✅ |
| RENEWAL_EXTENDED | (만료일 연장) | `renew()` | ✅ |

**커버리지: 12/12 (100%)** ✅

---

## 무결성 검증

### NOT NULL 준수

| 테이블 | 검증 결과 |
|--------|----------|
| subscriptions | ✅ 모든 필수 필드 NOT NULL |
| transactions | ✅ 모든 필수 필드 NOT NULL |
| subscription_histories | ✅ 모든 필수 필드 NOT NULL |
| refunds | ✅ 모든 필수 필드 NOT NULL |

### UNIQUE 준수

| 제약조건 | 검증 결과 |
|----------|----------|
| subscriptions.original_transaction_id | ✅ UNIQUE |
| transactions.transaction_id | ✅ UNIQUE |

### FK 무결성

| 관계 | 검증 결과 |
|------|----------|
| transactions.subscription_id → subscriptions.id | ✅ |
| subscription_histories.subscription_id → subscriptions.id | ✅ |
| refunds.subscription_id → subscriptions.id | ✅ |

---

## 검증 요약

| 카테고리 | 기준 | 결과 | 상태 |
|----------|------|------|------|
| Must Have 매핑 | 100% | 100% (13/13) | ✅ |
| Should Have 매핑 | 80% 이상 | 100% (4/4) | ✅ |
| Could Have 매핑 | - | 100% (1/1) | ✅ |
| Apple 이벤트 매핑 | 100% | 100% (12/12) | ✅ |
| Entity 불변식 | 100% | 정의됨 | ✅ |
| VO 유효성 검증 | 100% | 정의됨 | ✅ |
| NOT NULL 준수 | 100% | 100% | ✅ |
| UNIQUE 준수 | 100% | 100% | ✅ |
| FK 무결성 | 100% | 100% | ✅ |

---

## 다음 단계

설계 검증 완료 ✅

다음 단계: `/redteam-design` 실행
