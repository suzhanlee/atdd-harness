package com.example.subscription.domain.entity;

import com.example.subscription.domain.spec.SubscriptionStatusSpec;
import com.example.subscription.domain.vo.Period;
import com.example.subscription.domain.vo.ProductId;
import com.example.subscription.domain.vo.SubscriptionStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * 구독 Aggregate Root.
 *
 * <p>Apple IAP 구독의 핵심 엔티티로, 구독 상태와 관련 정보를 관리한다. DDD Aggregate Root로서 구독 관련 모든 상태
 * 변경의 진입점이다.
 *
 * <p>불변식:
 *
 * <ul>
 *   <li>originalTransactionId는 null이 아니어야 함 (비즈니스 키)
 *   <li>userId는 null이 아니어야 함
 *   <li>status는 null이 아니어야 함
 *   <li>expiresAt은 null이 아니어야 함
 *   <li>상태 전이는 SubscriptionStatusSpec을 따라야 함
 * </ul>
 *
 * @see com.example.subscription.domain.spec.SubscriptionStatusSpec
 * @see com.example.subscription.domain.vo.SubscriptionStatus
 */
@Entity
@Table(name = "subscriptions", indexes = {
    @Index(name = "idx_subscription_original_tx_id", columnList = "originalTransactionId", unique = true),
    @Index(name = "idx_subscription_user_id", columnList = "userId"),
    @Index(name = "idx_subscription_status", columnList = "status"),
    @Index(name = "idx_subscription_expires_at", columnList = "expiresAt")
})
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Apple 제공 원본 트랜잭션 ID - 비즈니스 키 */
    @Column(nullable = false, unique = true, length = 100)
    private String originalTransactionId;

    /** 사용자 ID */
    @Column(nullable = false)
    private Long userId;

    /** 제품 ID (Embedded VO) */
    @Embedded
    private ProductId productId;

    /** 구독 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    /** 구독 만료 일시 */
    @Column(nullable = false)
    private Instant expiresAt;

    /** 현재 구독 기간 시작 일시 */
    @Column(nullable = false)
    private Instant currentPeriodStart;

    /** 현재 구독 기간 종료 일시 */
    @Column(nullable = false)
    private Instant currentPeriodEnd;

    /** 낙관적 락 버전 */
    @Version
    private Long version;

    /** 생성 일시 */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /** 수정 일시 */
    @Column(nullable = false)
    private Instant updatedAt;

    /** JPA 기본 생성자 */
    protected Subscription() {}

    /**
     * 구독을 생성한다.
     *
     * @param originalTransactionId Apple 원본 트랜잭션 ID
     * @param userId 사용자 ID
     * @param productId 제품 ID
     * @param period 초기 구독 기간
     * @return 생성된 구독
     */
    public static Subscription create(
            String originalTransactionId, Long userId, ProductId productId, Period period) {
        Objects.requireNonNull(originalTransactionId, "originalTransactionId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(productId, "productId must not be null");
        Objects.requireNonNull(period, "period must not be null");

        Subscription subscription = new Subscription();
        subscription.originalTransactionId = originalTransactionId;
        subscription.userId = userId;
        subscription.productId = productId;
        subscription.status = SubscriptionStatus.ACTIVE;
        subscription.expiresAt = period.getEndAt();
        subscription.currentPeriodStart = period.getStartAt();
        subscription.currentPeriodEnd = period.getEndAt();
        subscription.createdAt = Instant.now();
        subscription.updatedAt = Instant.now();

        return subscription;
    }

    /**
     * 구독을 갱신한다.
     *
     * <p>갱신 성공 시 상태를 ACTIVE로 변경하고 만료일을 연장한다.
     *
     * @param newExpiresAt 새 만료 일시
     * @param newPeriodStart 새 기간 시작 일시
     * @param newPeriodEnd 새 기간 종료 일시
     * @throws IllegalStateException 갱신 불가능한 상태인 경우
     */
    public void renew(Instant newExpiresAt, Instant newPeriodStart, Instant newPeriodEnd) {
        // 불변식: expiresAt >= currentPeriodEnd
        if (newExpiresAt.isBefore(newPeriodEnd)) {
            throw new IllegalArgumentException("expiresAt must be >= currentPeriodEnd");
        }

        // 상태 전이 검증
        if (!SubscriptionStatusSpec.isActiveStatus(status)) {
            throw new IllegalStateException("Cannot renew from status: " + status);
        }

        this.expiresAt = newExpiresAt;
        this.currentPeriodStart = newPeriodStart;
        this.currentPeriodEnd = newPeriodEnd;
        this.status = SubscriptionStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * 구독을 만료시킨다.
     *
     * <p>만료일 도달 또는 갱신 실패로 인한 만료 처리.
     *
     * @throws IllegalStateException 이미 만료된 상태인 경우
     */
    public void expire() {
        SubscriptionStatusSpec.validateTransition(status, SubscriptionStatus.EXPIRED);
        this.status = SubscriptionStatus.EXPIRED;
        this.updatedAt = Instant.now();
    }

    /**
     * 구독을 환불 처리한다.
     *
     * <p>환불 완료 시 상태를 REFUNDED로 변경한다.
     *
     * @throws IllegalStateException 환불 불가능한 상태인 경우
     */
    public void refund() {
        SubscriptionStatusSpec.validateTransition(status, SubscriptionStatus.REFUNDED);
        this.status = SubscriptionStatus.REFUNDED;
        this.updatedAt = Instant.now();
    }

    /**
     * 구독 상태를 변경한다.
     *
     * <p>상태 전이 규칙은 SubscriptionStatusSpec에서 검증한다.
     *
     * @param newStatus 새 상태
     * @throws IllegalArgumentException 유효하지 않은 상태 전이인 경우
     */
    public void changeStatus(SubscriptionStatus newStatus) {
        SubscriptionStatusSpec.validateTransition(status, newStatus);
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    /**
     * 유예 기간에 진입한다.
     *
     * <p>결제 실패 시 Apple이 제공하는 유예 기간으로 진입한다.
     *
     * @param gracePeriodEnd 유예 기간 종료 일시
     * @throws IllegalStateException 유예 기간 진입 불가능한 상태인 경우
     */
    public void enterGracePeriod(Instant gracePeriodEnd) {
        SubscriptionStatusSpec.validateTransition(status, SubscriptionStatus.GRACE_PERIOD);
        this.status = SubscriptionStatus.GRACE_PERIOD;
        this.updatedAt = Instant.now();
    }

    /**
     * 구독을 업그레이드한다.
     *
     * <p>상위 등급 제품으로 변경한다.
     *
     * @param newProductId 새 제품 ID
     * @throws IllegalArgumentException 하위 등급으로 변경 시도한 경우
     * @throws IllegalStateException 업그레이드 불가능한 상태인 경우
     */
    public void upgrade(ProductId newProductId) {
        if (status != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Cannot upgrade from status: " + status);
        }

        // 동일 등급 또는 하위 등급으로는 업그레이드 불가
        if (!newProductId.isHigherTierThan(this.productId)) {
            throw new IllegalArgumentException("Cannot upgrade to same or lower tier");
        }

        this.productId = newProductId;
        this.updatedAt = Instant.now();
    }

    /**
     * 구독을 취소한다 (가족 공유).
     *
     * <p>가족 공유 구독이 취소된 경우 호출한다.
     *
     * @throws IllegalStateException 취소 불가능한 상태인 경우
     */
    public void revoke() {
        SubscriptionStatusSpec.validateTransition(status, SubscriptionStatus.REVOKED);
        this.status = SubscriptionStatus.REVOKED;
        this.updatedAt = Instant.now();
    }

    /**
     * 결제 재시도 상태로 진입한다.
     *
     * <p>유예 기간 종료 후 Apple이 자동으로 결제를 재시도하는 상태.
     *
     * @throws IllegalStateException 진입 불가능한 상태인 경우
     */
    public void enterBillingRetry() {
        SubscriptionStatusSpec.validateTransition(status, SubscriptionStatus.BILLING_RETRY);
        this.status = SubscriptionStatus.BILLING_RETRY;
        this.updatedAt = Instant.now();
    }

    // Getters

    public Long getId() {
        return id;
    }

    public String getOriginalTransactionId() {
        return originalTransactionId;
    }

    public Long getUserId() {
        return userId;
    }

    public ProductId getProductId() {
        return productId;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCurrentPeriodStart() {
        return currentPeriodStart;
    }

    public Instant getCurrentPeriodEnd() {
        return currentPeriodEnd;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 구독이 활성 상태인지 확인한다.
     *
     * @return 활성 상태이면 true
     */
    public boolean isActive() {
        return SubscriptionStatusSpec.isActiveStatus(status);
    }

    /**
     * 구독이 만료되었는지 확인한다.
     *
     * @return 만료되었으면 true
     */
    public boolean isExpired() {
        return status == SubscriptionStatus.EXPIRED;
    }
}
