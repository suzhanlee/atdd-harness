# Design Critique Report

## 개요

| 항목 | 내용 |
|------|------|
| 검토 일시 | 2026-02-24 |
| 검토 대상 | Subscription, Transaction, Refund, SubscriptionHistory |
| 검토 방법 | RRAIRU (6관점 DDD 비평) |
| 전체 위험도 | **MEDIUM** |

---

## Self-Reflection Questions

> "왜 이렇게 설계했나요?" - Self-Explanation 질문을 통한 설계 역량 강화

1. **ProductId.isHigherTierThan()**이 VO에 있는데, 등급 정보는 어디서 가져오나요? 외부 설정인가요, 하드코딩인가요?

2. **무료 체험 기간(isTrialPeriod)**을 어떻게 추적할 건가요? S1 요구사항에서 언급되었지만 Subscription에 필드가 없습니다.

3. **Transaction이 별도 Aggregate인가요, Subscription과 같은 Aggregate인가요?** ID 참조와 FK 제약조건이 혼재되어 있습니다.

4. **upgrade() 호출 시 productId 검증**을 누가 책임지나요? Entity인가요, Service인가요?

5. **expiresAt과 currentPeriodEnd**의 관계는 무엇인가요? 항상 `expiresAt >= currentPeriodEnd`인가요?

---

## 이슈 목록 (RRAIRU 분석)

### 1. Responsibility (책임 분배)

#### RESP-1: ProductId 등급 비교 책임
| 항목 | 내용 |
|------|------|
| **심각도** | MEDIUM |
| **위치** | `ProductId.isHigherTierThan()` |
| **문제** | VO 내부에 등급 비교 로직이 있으나, 실제 등급 정보는 외부 설정(ProductCatalog 등)에 있음. VO가 외부 의존성을 가져야 하는 구조적 문제 |
| **영향** | VO가 Domain Service에 의존하게 되거나, 하드코딩된 등급 매핑이 필요 |

```java
// 현재 설계 (구현 미완)
public boolean isHigherTierThan(ProductId other) {
    throw new UnsupportedOperationException("TODO: TDD에서 구현 - 등급 비교 로직 필요");
}
```

**제안**:
- Option A: ProductId는 식별자만 유지, `UpgradeService.compareTier()`에서 비교
- Option B: ProductTier VO를 별도로 정의하고 ProductId와 연결

---

### 2. Requirements Fit (요구사항 적합성)

#### REQ-1: 무료 체험 플래그 누락
| 항목 | 내용 |
|------|------|
| **심각도** | MEDIUM |
| **요구사항** | S1 - 무료 체험 시작 |
| **문제** | Subscription에 `isTrialPeriod` 필드가 없어 무료 체험 기간 추적 불가 |
| **영향** | 무료 체험 종료 후 유료 전환 시점 판단 불가, 분석 리포트 생성 불가 |

**제안**:
```java
@Column(nullable = false)
private boolean isTrialPeriod; // 무료 체험 여부
```

#### REQ-2: 갱신 취소 상태 누락
| 항목 | 내용 |
|------|------|
| **심각도** | LOW |
| **요구사항** | DID_CHANGE_RENEWAL_STATUS 이벤트 처리 |
| **문제** | "자동 갱신 취소" 상태를 추적하는 필드가 없음 |
| **영향** | 사용자가 갱신을 취소해도 만료일까지는 서비스 이용 가능해야 함 |

**제안**:
```java
private boolean autoRenewEnabled = true; // 자동 갱신 여부
```

---

### 3. Aggregate Boundary (Aggregate 경계)

#### AGG-1: Transaction이 별도 Aggregate인지 불명확
| 항목 | 내용 |
|------|------|
| **심각도** | MEDIUM |
| **위치** | Transaction Entity |
| **문제** | ERD에서는 FK로 연결되어 있으나, DDD 관점에서 Transaction이 Subscription과 같은 Aggregate인지 별도 Aggregate인지 불명확 |
| **영향** | 트랜잭션 경계 설계에 영향, 동시성 제어 방식 결정 필요 |

**현재 설계**:
```java
@Entity
public class Transaction {
    @Column(nullable = false)
    private Long subscriptionId; // ID 참조 (별도 Aggregate?)
    // ...
}
```

**제안**:
- 같은 Aggregate라면: `Subscription` 내부 `List<Transaction>` 컬렉션
- 별도 Aggregate라면: 현재처럼 ID 참조 + 별도 Repository
- **권장**: 별도 Aggregate (트랜잭션 수가 무제한으로 늘어날 수 있음)

#### AGG-2: SubscriptionHistory와 Refund의 경계
| 항목 | 내용 |
|------|------|
| **심각도** | LOW |
| **결과** | 적절히 설계됨 - 둘 다 Subscription에 ID로 참조됨 |

---

### 4. Invariants (불변식 완전성)

#### INV-1: expiresAt < currentPeriodEnd 불변식 누락
| 항목 | 내용 |
|------|------|
| **심각도** | HIGH |
| **위치** | Subscription Entity |
| **문제** | `expiresAt`과 `currentPeriodEnd` 간의 관계 규칙이 정의되지 않음 |
| **영향** | 데이터 일관성 보장 불가 |

**제안 불변식**:
```
expiresAt >= currentPeriodEnd (항상)
currentPeriodStart < currentPeriodEnd (항상)
```

#### INV-2: upgrade() 시 productId 검증 불변식 누락
| 항목 | 내용 |
|------|------|
| **심각도** | HIGH |
| **위치** | Subscription.upgrade() |
| **문제** | `newProductId > currentProductId` 검증을 UpgradeService에 위임. Entity에도 불변식으로 정의되어야 함 |
| **영향** | Service 계층을 우회한 직접 호출 시 비즈니스 규칙 위반 가능 |

**현재 설계**:
```java
public void upgrade(ProductId newProductId) {
    throw new UnsupportedOperationException("TODO: TDD에서 구현");
    // newProductId.isHigherTierThan(this.productId) 검증 필요
}
```

**제안**:
```java
public void upgrade(ProductId newProductId, ProductTierRegistry tierRegistry) {
    if (!tierRegistry.isHigher(newProductId, this.productId)) {
        throw new IllegalArgumentException("하위 등급으로 변경할 수 없습니다");
    }
    this.productId = newProductId;
    // ...
}
```

#### INV-3: Money 음수 허용 여부
| 항목 | 내용 |
|------|------|
| **심각도** | MEDIUM |
| **위치** | Money VO |
| **문제** | 주석에는 `amount >= 0`이나, 환불 계산 시 음수 필요할 수 있음 |
| **현재 상태** | TODO 주석으로 남겨짐 |

```java
// 불변식: amount ≥ 0, currency ≠ null
// (환불 계산 시 음수 허용 고려 필요)
```

**제안**:
- Money는 항상 양수 유지
- 환불 금액은 별도 `RefundAmount` VO로 표현 (양수)
- 또는 계산 결과만 음수 허용, 저장은 양수로

---

### 5. Relationships (연관관계 설계)

#### REL-1: Subscription → Transaction 양방향 연관관계 없음
| 항목 | 내용 |
|------|------|
| **심각도** | LOW |
| **결과** | 적절함 - ID 참조만 사용하여 느슨한 결합 유지 |

#### REL-2: User Entity와의 관계 미정의
| 항목 | 내용 |
|------|------|
| **심각도** | LOW |
| **결과** | 적절함 - userId만 보유, User Aggregate와의 경계 명확 (ID 참조) |

---

### 6. Ubiquitous Language (보편 언어)

#### UBIQ-1: UPSELL vs UPGRADE 용어 혼용
| 항목 | 내용 |
|------|------|
| **심각도** | LOW |
| **위치** | Transaction.Type vs Subscription.upgrade() |
| **문제** | `Transaction.Type.UPSELL` vs `Subscription.upgrade()` - Apple 용어와 비즈니스 용어 혼재 |

**현재 설계**:
```java
// Transaction.Type
enum Type { PURCHASE, RENEWAL, REFUND, UPSELL }

// Subscription 메서드
public void upgrade(ProductId newProductId) { ... }
```

**제안**: 용어 통일 또는 명시적 매핑 문서화

#### UBIQ-2: 상태명 EXPIRED vs CANCELLED
| 항목 | 내용 |
|------|------|
| **심각도** | LOW |
| **문제** | Apple은 EXPIRED 사용, 일반적으로는 CANCELLED 혼용 |
| **결과** | Apple 용어 기준(EXPIRED)으로 통일됨 - 적절 |

---

## 이슈 요약

| ID | 관점 | 심각도 | 이슈 |
|----|------|--------|------|
| INV-1 | Invariants | **HIGH** | expiresAt < currentPeriodEnd 불변식 누락 |
| INV-2 | Invariants | **HIGH** | upgrade() 시 productId 검증 불변식 누락 |
| RESP-1 | Responsibility | MEDIUM | ProductId 등급 비교 책임 |
| REQ-1 | Requirements | MEDIUM | 무료 체험 플래그 누락 |
| AGG-1 | Aggregate | MEDIUM | Transaction이 별도 Aggregate인지 불명확 |
| INV-3 | Invariants | MEDIUM | Money 음수 허용 여부 |
| REQ-2 | Requirements | LOW | 갱신 취소 상태 누락 |
| REL-1 | Relationships | LOW | (적절함) |
| REL-2 | Relationships | LOW | (적절함) |
| UBIQ-1 | Language | LOW | UPSELL vs UPGRADE 용어 혼용 |
| UBIQ-2 | Language | LOW | (적절함) |

---

## 반영 방향 작성 테이블

| 이슈 ID | 심각도 | 반영 방향 | 결정 |
|---------|--------|-----------|------|
| INV-1 | HIGH | Subscription 불변식에 `expiresAt >= currentPeriodEnd` 추가 | ☐ ACCEPT / DEFER / REJECT |
| INV-2 | HIGH | Entity 내부에도 productId 검증 로직 추가 | ☐ ACCEPT / DEFER / REJECT |
| RESP-1 | MEDIUM | ProductId는 식별자만 유지, UpgradeService에서 비교 | ☐ ACCEPT / DEFER / REJECT |
| REQ-1 | MEDIUM | Subscription에 `isTrialPeriod` 필드 추가 | ☐ ACCEPT / DEFER / REJECT |
| AGG-1 | MEDIUM | Transaction을 별도 Aggregate로 명시 | ☐ ACCEPT / DEFER / REJECT |
| INV-3 | MEDIUM | Money는 양수 유지, 환불은 별도 VO | ☐ ACCEPT / DEFER / REJECT |
| REQ-2 | LOW | `autoRenewEnabled` 필드 추가 | ☐ ACCEPT / DEFER / REJECT |
| UBIQ-1 | LOW | 용어 가이드 문서화 (UPSELL = upgrade) | ☐ ACCEPT / DEFER / REJECT |

---

## Contrastive Cases (안티패턴 vs 권장 패턴)

### Case 1: 불변식 위치

| 안티패턴 | 권장 패턴 |
|----------|----------|
| Service에서만 검증 | Entity 내부 + Service 이중 검증 |
| `UpgradeService.upgrade()`에서만 등급 검증 | `Subscription.upgrade()` 내부에서도 검증 |

### Case 2: VO 책임

| 안티패턴 | 권장 패턴 |
|----------|----------|
| VO가 외부 의존성(DB, API) 필요 | VO는 순수하게 유지, 비교는 Domain Service |
| `ProductId.isHigherTierThan()`이 DB 조회 | `ProductTierRegistry.isHigher(a, b)` |

### Case 3: Aggregate 경계

| 안티패턴 | 권장 패턴 |
|----------|----------|
| 모든 것을 한 Aggregate에 | 수명주기와 변경 빈도 기준 분리 |
| Subscription 내부 `List<Transaction>` | Transaction은 별도 Aggregate |

---

## 다음 단계

1. 사용자가 각 이슈에 대해 ACCEPT/DEFER/REJECT 결정
2. 결정 내용을 `decisions.md`에 기록
3. DEFER 항목은 `backlog.md`에 추가
4. ACCEPT 항목은 domain-model.md, ERD 업데이트
5. `/compound` 실행하여 Episode 생성
