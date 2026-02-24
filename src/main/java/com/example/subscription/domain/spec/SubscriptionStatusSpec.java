package com.example.subscription.domain.spec;

import com.example.subscription.domain.vo.SubscriptionStatus;

/**
 * 구독 상태 전이 규칙 Specification.
 *
 * <p>구독 상태 간 전이가 유효한지 검증하는 규칙을 정의한다. Apple IAP 이벤트 타입과 구독 상태 전이를 매핑한다.
 *
 * <p>상태 전이 매트릭스:
 *
 * <pre>
 * FROM           TO
 * ACTIVE         EXPIRED, GRACE_PERIOD, BILLING_RETRY, REFUNDED, REVOKED
 * GRACE_PERIOD   ACTIVE, EXPIRED, BILLING_RETRY
 * BILLING_RETRY  ACTIVE, EXPIRED
 * EXPIRED        (종료 상태 - 전이 불가)
 * REFUNDED       (종료 상태 - 전이 불가)
 * REVOKED        (종료 상태 - 전이 불가)
 * </pre>
 *
 * @see com.example.subscription.domain.vo.SubscriptionStatus
 * @see com.example.subscription.domain.entity.Subscription
 */
public final class SubscriptionStatusSpec {

    private SubscriptionStatusSpec() {
        // Utility class
    }

    /**
     * 상태 전이가 유효한지 검증한다.
     *
     * @param from 현재 상태
     * @param to 새 상태
     * @return 전이가 유효하면 true
     */
    public static boolean canTransition(SubscriptionStatus from, SubscriptionStatus to) {
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }

    /**
     * 상태 전이가 유효한지 검증하고, 유효하지 않으면 예외를 발생시킨다.
     *
     * @param from 현재 상태
     * @param to 새 상태
     * @throws IllegalStateException 유효하지 않은 상태 전이인 경우
     */
    public static void validateTransition(SubscriptionStatus from, SubscriptionStatus to) {
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }

    /**
     * Apple 이벤트 타입에 따른 목표 상태를 반환한다.
     *
     * @param eventType Apple 이벤트 타입 (SUBSCRIBED, DID_RENEW 등)
     * @return 목표 상태
     */
    public static SubscriptionStatus getTargetStatus(String eventType) {
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }

    /**
     * 종료 상태인지 확인한다.
     *
     * <p>종료 상태에서는 다른 상태로 전이할 수 없다.
     *
     * @param status 확인할 상태
     * @return 종료 상태이면 true
     */
    public static boolean isTerminalStatus(SubscriptionStatus status) {
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }

    /**
     * 활성 상태인지 확인한다.
     *
     * <p>활성 상태는 서비스 이용이 가능한 상태이다.
     *
     * @param status 확인할 상태
     * @return 활성 상태이면 true
     */
    public static boolean isActiveStatus(SubscriptionStatus status) {
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }
}
