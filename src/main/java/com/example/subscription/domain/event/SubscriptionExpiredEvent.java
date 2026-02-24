package com.example.subscription.domain.event;

import java.time.Instant;

/**
 * 구독 만료 이벤트.
 *
 * <p>구독이 만료되었을 때 발행되는 도메인 이벤트이다. 만료 알림, 서비스 접근 제한 등의 후속 처리에 활용된다.
 *
 * <p>불변식:
 *
 * <ul>
 *   <li>subscriptionId는 null이 아니어야 함
 *   <li>userId는 null이 아니어야 함
 *   <li>expiredAt은 null이 아니어야 함
 *   <li>occurredAt은 null이 아니어야 함
 * </ul>
 */
public class SubscriptionExpiredEvent {

    private final Long subscriptionId;
    private final String originalTransactionId;
    private final Long userId;
    private final String productId;
    private final Instant expiredAt;
    private final String expiryReason;
    private final Instant occurredAt;

    /**
     * 구독 만료 이벤트를 생성한다.
     *
     * @param subscriptionId 구독 ID
     * @param originalTransactionId Apple 원본 트랜잭션 ID
     * @param userId 사용자 ID
     * @param productId 제품 ID
     * @param expiredAt 만료 일시
     * @param expiryReason 만료 사유 (NATURAL, BILLING_FAILURE 등)
     * @param occurredAt 이벤트 발생 시간
     */
    public SubscriptionExpiredEvent(
            Long subscriptionId,
            String originalTransactionId,
            Long userId,
            String productId,
            Instant expiredAt,
            String expiryReason,
            Instant occurredAt) {
        // TODO: TDD에서 불변식 검증 구현
        this.subscriptionId = subscriptionId;
        this.originalTransactionId = originalTransactionId;
        this.userId = userId;
        this.productId = productId;
        this.expiredAt = expiredAt;
        this.expiryReason = expiryReason;
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

    public Instant getExpiredAt() {
        return expiredAt;
    }

    public String getExpiryReason() {
        return expiryReason;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
