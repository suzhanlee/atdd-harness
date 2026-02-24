package com.example.subscription.domain.event;

import java.time.Instant;

/**
 * 구독 갱신 이벤트.
 *
 * <p>구독이 성공적으로 갱신되었을 때 발행되는 도메인 이벤트이다. 알림 발송, 통계 집계 등의 후속 처리에 활용된다.
 *
 * <p>불변식:
 *
 * <ul>
 *   <li>subscriptionId는 null이 아니어야 함
 *   <li>userId는 null이 아니어야 함
 *   <li>newExpiresAt은 null이 아니어야 함
 *   <li>occurredAt은 null이 아니어야 함
 * </ul>
 */
public class SubscriptionRenewedEvent {

    private final Long subscriptionId;
    private final String originalTransactionId;
    private final Long userId;
    private final String productId;
    private final Instant newExpiresAt;
    private final Instant occurredAt;

    /**
     * 구독 갱신 이벤트를 생성한다.
     *
     * @param subscriptionId 구독 ID
     * @param originalTransactionId Apple 원본 트랜잭션 ID
     * @param userId 사용자 ID
     * @param productId 제품 ID
     * @param newExpiresAt 새 만료 일시
     * @param occurredAt 이벤트 발생 시간
     */
    public SubscriptionRenewedEvent(
            Long subscriptionId,
            String originalTransactionId,
            Long userId,
            String productId,
            Instant newExpiresAt,
            Instant occurredAt) {
        // TODO: TDD에서 불변식 검증 구현
        this.subscriptionId = subscriptionId;
        this.originalTransactionId = originalTransactionId;
        this.userId = userId;
        this.productId = productId;
        this.newExpiresAt = newExpiresAt;
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

    public Instant getNewExpiresAt() {
        return newExpiresAt;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
