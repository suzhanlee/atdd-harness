package com.example.subscription.domain.event;

import java.time.Instant;

/**
 * 유예 기간 진입 이벤트.
 *
 * <p>구독 결제 실패로 유예 기간에 진입했을 때 발행되는 도메인 이벤트이다. 결제 실패 알림, 유예 기간 안내 등에 활용된다.
 *
 * <p>Apple은 결제 실패 시 일정 기간 동안 서비스를 유지하며 자동으로 결제를 재시도한다.
 *
 * <p>불변식:
 *
 * <ul>
 *   <li>subscriptionId는 null이 아니어야 함
 *   <li>userId는 null이 아니어야 함
 *   <li>gracePeriodEnd는 null이 아니어야 함
 *   <li>occurredAt은 null이 아니어야 함
 * </ul>
 */
public class GracePeriodEnteredEvent {

    private final Long subscriptionId;
    private final String originalTransactionId;
    private final Long userId;
    private final String productId;
    private final Instant gracePeriodEnd;
    private final String billingFailureReason;
    private final Instant occurredAt;

    /**
     * 유예 기간 진입 이벤트를 생성한다.
     *
     * @param subscriptionId 구독 ID
     * @param originalTransactionId Apple 원본 트랜잭션 ID
     * @param userId 사용자 ID
     * @param productId 제품 ID
     * @param gracePeriodEnd 유예 기간 종료 일시
     * @param billingFailureReason 결제 실패 사유
     * @param occurredAt 이벤트 발생 시간
     */
    public GracePeriodEnteredEvent(
            Long subscriptionId,
            String originalTransactionId,
            Long userId,
            String productId,
            Instant gracePeriodEnd,
            String billingFailureReason,
            Instant occurredAt) {
        // TODO: TDD에서 불변식 검증 구현
        this.subscriptionId = subscriptionId;
        this.originalTransactionId = originalTransactionId;
        this.userId = userId;
        this.productId = productId;
        this.gracePeriodEnd = gracePeriodEnd;
        this.billingFailureReason = billingFailureReason;
        this.occurredAt = occurredAt;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public String getOriginalTransactionId() {
        return originalTransactionId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getProductId() {
        return productId;
    }

    public Instant getGracePeriodEnd() {
        return gracePeriodEnd;
    }

    public String getBillingFailureReason() {
        return billingFailureReason;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
