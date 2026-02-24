package com.example.subscription.domain.event;

import com.example.subscription.domain.vo.Money;
import java.time.Instant;

/**
 * 구독 환불 이벤트.
 *
 * <p>구독이 환불 처리되었을 때 발행되는 도메인 이벤트이다. 환불 알림, 서비스 접근 즉시 제한, 정산 처리 등에 활용된다.
 *
 * <p>불변식:
 *
 * <ul>
 *   <li>subscriptionId는 null이 아니어야 함
 *   <li>userId는 null이 아니어야 함
 *   <li>refundAmount는 null이 아니어야 함
 *   <li>refundedAt은 null이 아니어야 함
 *   <li>occurredAt은 null이 아니어야 함
 * </ul>
 */
public class SubscriptionRefundedEvent {

    private final Long subscriptionId;
    private final String originalTransactionId;
    private final Long userId;
    private final String productId;
    private final Money refundAmount;
    private final String refundReason;
    private final Instant refundedAt;
    private final Instant occurredAt;

    /**
     * 구독 환불 이벤트를 생성한다.
     *
     * @param subscriptionId 구독 ID
     * @param originalTransactionId Apple 원본 트랜잭션 ID
     * @param userId 사용자 ID
     * @param productId 제품 ID
     * @param refundAmount 환불 금액
     * @param refundReason 환불 사유
     * @param refundedAt 환불 처리 일시
     * @param occurredAt 이벤트 발생 시간
     */
    public SubscriptionRefundedEvent(
            Long subscriptionId,
            String originalTransactionId,
            Long userId,
            String productId,
            Money refundAmount,
            String refundReason,
            Instant refundedAt,
            Instant occurredAt) {
        // TODO: TDD에서 불변식 검증 구현
        this.subscriptionId = subscriptionId;
        this.originalTransactionId = originalTransactionId;
        this.userId = userId;
        this.productId = productId;
        this.refundAmount = refundAmount;
        this.refundReason = refundReason;
        this.refundedAt = refundedAt;
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

    public Money getRefundAmount() {
        return refundAmount;
    }

    public String getRefundReason() {
        return refundReason;
    }

    public Instant getRefundedAt() {
        return refundedAt;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
