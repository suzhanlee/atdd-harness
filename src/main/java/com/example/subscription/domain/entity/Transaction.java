package com.example.subscription.domain.entity;

import com.example.subscription.domain.vo.Money;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * 결제/갱신 트랜잭션 Entity.
 *
 * <p>Apple IAP에서 발생하는 모든 결제 관련 트랜잭션을 기록한다. 구독 갱신, 환불, 업그레이드 등의 이력을 추적한다.
 *
 * <p>불변식:
 *
 * <ul>
 *   <li>subscriptionId는 null이 아니어야 함
 *   <li>transactionId는 null이 아니어야 함 (Apple 제공)
 *   <li>type은 null이 아니어야 함
 *   <li>purchasedAt은 null이 아니어야 함
 * </ul>
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_tx_id", columnList = "transactionId", unique = true),
    @Index(name = "idx_transaction_subscription_id", columnList = "subscriptionId"),
    @Index(name = "idx_transaction_type", columnList = "type"),
    @Index(name = "idx_transaction_purchased_at", columnList = "purchasedAt")
})
public class Transaction {

    /** 트랜잭션 유형 */
    public enum Type {
        /** 최초 구매 */
        PURCHASE,
        /** 구독 갱신 */
        RENEWAL,
        /** 환불 */
        REFUND,
        /** 업셀 (업그레이드) */
        UPSELL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 구독 ID */
    @Column(nullable = false)
    private Long subscriptionId;

    /** Apple 제공 트랜잭션 ID */
    @Column(nullable = false, unique = true, length = 100)
    private String transactionId;

    /** 트랜잭션 유형 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Type type;

    /** 결제 금액 */
    @Embedded
    private Money amount;

    /** 구매/결제 일시 */
    @Column(nullable = false)
    private Instant purchasedAt;

    /** 생성 일시 */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /** JPA 기본 생성자 */
    protected Transaction() {}

    /**
     * 트랜잭션을 생성한다.
     *
     * @param subscriptionId 구독 ID
     * @param transactionId Apple 트랜잭션 ID
     * @param type 트랜잭션 유형
     * @param amount 결제 금액
     * @param purchasedAt 구매 일시
     * @return 생성된 트랜잭션
     */
    public static Transaction create(
            Long subscriptionId, String transactionId, Type type, Money amount, Instant purchasedAt) {
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Type getType() {
        return type;
    }

    public Money getAmount() {
        return amount;
    }

    public Instant getPurchasedAt() {
        return purchasedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * 환불 트랜잭션인지 확인한다.
     *
     * @return 환불이면 true
     */
    public boolean isRefund() {
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }
}
