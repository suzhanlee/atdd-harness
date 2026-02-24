package com.example.subscription.domain.entity;

import com.example.subscription.domain.vo.Money;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * 환불 정보 Entity.
 *
 * <p>Apple에서 환불 처리된 구독에 대한 환불 정보를 관리한다. 환불 사유, 금액, 처리 일시 등을 추적한다.
 *
 * <p>불변식:
 *
 * <ul>
 *   <li>subscriptionId는 null이 아니어야 함
 *   <li>refundAmount는 null이 아니어야 함
 *   <li>refundedAt은 null이 아니어야 함
 * </ul>
 *
 * @see Subscription
 * @see com.example.subscription.domain.policy.RefundPolicy
 */
@Entity
@Table(name = "refunds", indexes = {
    @Index(name = "idx_refund_subscription_id", columnList = "subscriptionId"),
    @Index(name = "idx_refund_refunded_at", columnList = "refundedAt")
})
public class Refund {

    /** 환불 사유 */
    public enum Reason {
        /** 사용자 요청 */
        USER_REQUEST,
        /** Apple 승인 */
        APPLE_APPROVED,
        /** 결제 문제 */
        PAYMENT_ISSUE,
        /** 기타 */
        OTHER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 구독 ID */
    @Column(nullable = false)
    private Long subscriptionId;

    /** Apple 원본 트랜잭션 ID */
    @Column(nullable = false, length = 100)
    private String originalTransactionId;

    /** 환불 금액 */
    @Embedded
    private Money refundAmount;

    /** 환불 사유 */
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Reason reason;

    /** Apple 환불 사유 코드 */
    @Column(length = 50)
    private String appleReasonCode;

    /** 환불 처리 일시 */
    @Column(nullable = false)
    private Instant refundedAt;

    /** 생성 일시 */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /** JPA 기본 생성자 */
    protected Refund() {}

    /**
     * 환불 정보를 생성한다.
     *
     * @param subscriptionId 구독 ID
     * @param originalTransactionId Apple 원본 트랜잭션 ID
     * @param refundAmount 환불 금액
     * @param reason 환불 사유
     * @param appleReasonCode Apple 환불 사유 코드
     * @param refundedAt 환불 처리 일시
     * @return 생성된 환불 정보
     */
    public static Refund create(
            Long subscriptionId,
            String originalTransactionId,
            Money refundAmount,
            Reason reason,
            String appleReasonCode,
            Instant refundedAt) {
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public String getOriginalTransactionId() {
        return originalTransactionId;
    }

    public Money getRefundAmount() {
        return refundAmount;
    }

    public Reason getReason() {
        return reason;
    }

    public String getAppleReasonCode() {
        return appleReasonCode;
    }

    public Instant getRefundedAt() {
        return refundedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
