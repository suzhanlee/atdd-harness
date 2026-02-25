package com.example.subscription.domain.vo;

/**
 * 구독 상태를 나타내는 열거형.
 *
 * <p>Apple IAP 구독 상태와 매핑되며, 상태 전이 규칙은 {@link
 * com.example.subscription.domain.spec.SubscriptionStatusSpec}에서 관리한다.
 *
 * <p>상태 전이 매트릭스:
 *
 * <ul>
 *   <li>ACTIVE → EXPIRED, GRACE_PERIOD, BILLING_RETRY, REFUNDED, REVOKED
 *   <li>GRACE_PERIOD → ACTIVE, EXPIRED, BILLING_RETRY
 *   <li>BILLING_RETRY → ACTIVE, EXPIRED
 *   <li>IN_TRIAL → ACTIVE, EXPIRED
 *   <li>EXPIRED → (종료 상태)
 *   <li>REFUNDED → (종료 상태)
 *   <li>REVOKED → (종료 상태)
 * </ul>
 *
 * @see com.example.subscription.domain.spec.SubscriptionStatusSpec
 */
public enum SubscriptionStatus {
    /** 활성 구독 상태 - 정상적으로 결제되고 서비스 이용 중 */
    ACTIVE,

    /** 무료 체험 상태 - 무료 체험 기간 중 */
    IN_TRIAL,

    /** 만료 상태 - 구독 기간 종료 또는 갱신 실패로 인한 만료 */
    EXPIRED,

    /** 유예 기간 상태 - 결제 실패 후 Apple이 제공하는 유예 기간 중 */
    GRACE_PERIOD,

    /** 결제 재시도 상태 - 유예 기간 종료 후 Apple이 자동으로 결제 재시도 중 */
    BILLING_RETRY,

    /** 환불 완료 상태 - 사용자 요청 또는 Apple 승인에 의한 환불 */
    REFUNDED,

    /** 가족 공유 취소 상태 - 가족 공유 구독이 취소됨 */
    REVOKED
}
