package com.example.subscription.domain.entity;

import com.example.subscription.domain.vo.SubscriptionStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * 구독 상태 변경 이력 Entity.
 *
 * <p>구독의 모든 상태 변경을 추적하여 감사 및 CS 대응에 활용한다. DDD Aggregate 내부의 이력 테이블로 관리된다.
 *
 * <p>불변식:
 *
 * <ul>
 *   <li>subscriptionId는 null이 아니어야 함
 *   <li>fromStatus는 null이 아니어야 함
 *   <li>toStatus는 null이 아니어야 함
 *   <li>changedAt은 null이 아니어야 함
 * </ul>
 *
 * @see Subscription
 */
@Entity
@Table(name = "subscription_histories", indexes = {
    @Index(name = "idx_history_subscription_id", columnList = "subscriptionId"),
    @Index(name = "idx_history_changed_at", columnList = "changedAt")
})
public class SubscriptionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 구독 ID */
    @Column(nullable = false)
    private Long subscriptionId;

    /** 변경 전 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus fromStatus;

    /** 변경 후 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus toStatus;

    /** 변경 사유 (Apple 이벤트 타입 등) */
    @Column(length = 50)
    private String reason;

    /** 관련 트랜잭션 ID */
    @Column(length = 100)
    private String transactionId;

    /** 변경 일시 */
    @Column(nullable = false)
    private Instant changedAt;

    /** 비고 */
    @Column(length = 500)
    private String note;

    /** JPA 기본 생성자 */
    protected SubscriptionHistory() {}

    /**
     * 구독 상태 변경 이력을 생성한다.
     *
     * @param subscriptionId 구독 ID
     * @param fromStatus 변경 전 상태
     * @param toStatus 변경 후 상태
     * @param reason 변경 사유
     * @param transactionId 관련 트랜잭션 ID
     * @param changedAt 변경 일시
     * @return 생성된 이력
     */
    public static SubscriptionHistory create(
            Long subscriptionId,
            SubscriptionStatus fromStatus,
            SubscriptionStatus toStatus,
            String reason,
            String transactionId,
            Instant changedAt) {
        Objects.requireNonNull(subscriptionId, "subscriptionId must not be null");
        Objects.requireNonNull(fromStatus, "fromStatus must not be null");
        Objects.requireNonNull(toStatus, "toStatus must not be null");
        Objects.requireNonNull(changedAt, "changedAt must not be null");

        SubscriptionHistory history = new SubscriptionHistory();
        history.subscriptionId = subscriptionId;
        history.fromStatus = fromStatus;
        history.toStatus = toStatus;
        history.reason = reason;
        history.transactionId = transactionId;
        history.changedAt = changedAt;

        return history;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public SubscriptionStatus getFromStatus() {
        return fromStatus;
    }

    public SubscriptionStatus getToStatus() {
        return toStatus;
    }

    public String getReason() {
        return reason;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public String getNote() {
        return note;
    }
}
