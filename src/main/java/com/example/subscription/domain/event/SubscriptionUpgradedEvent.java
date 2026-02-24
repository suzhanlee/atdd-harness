package com.example.subscription.domain.event;

import java.time.Instant;

/**
 * 구독 업그레이드 이벤트.
 *
 * <p>구독이 상위 등급으로 업그레이드되었을 때 발행되는 도메인 이벤트이다. 서비스 권한 확장, 비용 정산 등에 활용된다.
 *
 * <p>불변식:
 *
 * <ul>
 *   <li>subscriptionId는 null이 아니어야 함
 *   <li>userId는 null이 아니어야 함
 *   <li>fromProductId는 null이 아니어야 함
 *   <li>toProductId는 null이 아니어야 함
 *   <li>occurredAt은 null이 아니어야 함
 * </ul>
 */
public class SubscriptionUpgradedEvent {

    private final Long subscriptionId;
    private final String originalTransactionId;
    private final Long userId;
    private final String fromProductId;
    private final String toProductId;
    private final Instant newExpiresAt;
    private final Instant occurredAt;

    /**
     * 구독 업그레이드 이벤트를 생성한다.
     *
     * @param subscriptionId 구독 ID
     * @param originalTransactionId Apple 원본 트랜잭션 ID
     * @param userId 사용자 ID
     * @param fromProductId 변경 전 제품 ID
     * @param toProductId 변경 후 제품 ID
     * @param newExpiresAt 새 만료 일시
     * @param occurredAt 이벤트 발생 시간
     */
    public SubscriptionUpgradedEvent(
            Long subscriptionId,
            String originalTransactionId,
            Long userId,
            String fromProductId,
            String toProductId,
            Instant newExpiresAt,
            Instant occurredAt) {
        // TODO: TDD에서 불변식 검증 구현
        this.subscriptionId = subscriptionId;
        this.originalTransactionId = originalTransactionId;
        this.userId = userId;
        this.fromProductId = fromProductId;
        this.toProductId = toProductId;
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

    public String getFromProductId() {
        return fromProductId;
    }

    public String getToProductId() {
        return toProductId;
    }

    public Instant getNewExpiresAt() {
        return newExpiresAt;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
