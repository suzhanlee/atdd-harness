package com.example.subscription.domain.vo;

import java.time.Instant;
import java.util.Objects;

/**
 * Apple Webhook 이벤트 정보를 표현하는 불변 값 객체.
 *
 * <p>Apple App Store Server Notifications V2에서 수신한 이벤트의 핵심 정보를 캡슐화한다. JWS 서명 검증 후
 * 파싱된 데이터를 담는다.
 *
 * <p>불변식:
 *
 * <ul>
 *   <li>notificationType은 null이 아니어야 함
 *   <li>transactionId는 null이 아니어야 함
 *   <li>signedDate는 null이 아니어야 함
 * </ul>
 *
 * @see com.example.subscription.infrastructure.parser.AppleWebhookParser
 */
public class WebhookEvent {

    private final String notificationType;
    private final String transactionId;
    private final String originalTransactionId;
    private final String productId;
    private final Instant signedDate;
    private final Instant expiresDate;
    private final String environment;

    /**
     * WebhookEvent 객체를 생성한다.
     *
     * @param notificationType 알림 유형 (SUBSCRIBED, DID_RENEW 등)
     * @param transactionId 트랜잭션 ID
     * @param originalTransactionId 원본 트랜잭션 ID (구독 그룹 식별용)
     * @param productId 제품 ID
     * @param signedDate 서명 일시
     * @param expiresDate 만료 일시
     * @param environment 환경 (SANDBOX, PRODUCTION)
     */
    public WebhookEvent(
            String notificationType,
            String transactionId,
            String originalTransactionId,
            String productId,
            Instant signedDate,
            Instant expiresDate,
            String environment) {
        // TODO: TDD에서 불변식 검증 구현
        this.notificationType = notificationType;
        this.transactionId = transactionId;
        this.originalTransactionId = originalTransactionId;
        this.productId = productId;
        this.signedDate = signedDate;
        this.expiresDate = expiresDate;
        this.environment = environment;
    }

    /**
     * 알림 유형을 반환한다.
     *
     * @return 알림 유형
     */
    public String getNotificationType() {
        return notificationType;
    }

    /**
     * 트랜잭션 ID를 반환한다.
     *
     * @return 트랜잭션 ID
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * 원본 트랜잭션 ID를 반환한다.
     *
     * <p>구독 그룹을 식별하는 비즈니스 키로 사용된다.
     *
     * @return 원본 트랜잭션 ID
     */
    public String getOriginalTransactionId() {
        return originalTransactionId;
    }

    /**
     * 제품 ID를 반환한다.
     *
     * @return 제품 ID
     */
    public String getProductId() {
        return productId;
    }

    /**
     * 서명 일시를 반환한다.
     *
     * @return 서명 일시
     */
    public Instant getSignedDate() {
        return signedDate;
    }

    /**
     * 만료 일시를 반환한다.
     *
     * @return 만료 일시
     */
    public Instant getExpiresDate() {
        return expiresDate;
    }

    /**
     * 환경을 반환한다.
     *
     * @return 환경 (SANDBOX, PRODUCTION)
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * 샌드박스 환경인지 확인한다.
     *
     * @return 샌드박스 환경이면 true
     */
    public boolean isSandbox() {
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }

    /**
     * 이벤트가 Replay Attack 방지 시간 윈도우 내에 있는지 확인한다.
     *
     * @param maxAgeMinutes 허용되는 최대 경과 시간 (분)
     * @return 유효하면 true
     */
    public boolean isRecent(int maxAgeMinutes) {
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebhookEvent that = (WebhookEvent) o;
        return Objects.equals(notificationType, that.notificationType)
                && Objects.equals(transactionId, that.transactionId)
                && Objects.equals(originalTransactionId, that.originalTransactionId)
                && Objects.equals(productId, that.productId)
                && Objects.equals(signedDate, that.signedDate)
                && Objects.equals(expiresDate, that.expiresDate)
                && Objects.equals(environment, that.environment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                notificationType, transactionId, originalTransactionId, productId, signedDate, expiresDate, environment);
    }

    @Override
    public String toString() {
        return String.format(
                "WebhookEvent[type=%s, txId=%s, product=%s]", notificationType, transactionId, productId);
    }
}
