package com.example.subscription.domain.spec;

import com.example.subscription.domain.vo.SubscriptionStatus;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    /** 상태 전이 매트릭스: FROM 상태 -> 가능한 TO 상태 목록 */
    private static final Map<SubscriptionStatus, Set<SubscriptionStatus>> TRANSITIONS = new HashMap<>();

    /** Apple 이벤트 타입 -> 구독 상태 매핑 */
    private static final Map<String, SubscriptionStatus> EVENT_STATUS_MAP = new HashMap<>();

    /** 종료 상태 목록 */
    private static final Set<SubscriptionStatus> TERMINAL_STATUSES =
            EnumSet.of(SubscriptionStatus.EXPIRED, SubscriptionStatus.REFUNDED, SubscriptionStatus.REVOKED);

    /** 활성 상태 목록 (서비스 이용 가능) */
    private static final Set<SubscriptionStatus> ACTIVE_STATUSES =
            EnumSet.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.GRACE_PERIOD, SubscriptionStatus.BILLING_RETRY, SubscriptionStatus.IN_TRIAL);

    static {
        // ACTIVE에서 가능한 전이
        // Note: ACTIVE → BILLING_RETRY is NOT allowed. Billing retry only after GRACE_PERIOD.
        TRANSITIONS.put(
                SubscriptionStatus.ACTIVE,
                EnumSet.of(
                        SubscriptionStatus.EXPIRED,
                        SubscriptionStatus.GRACE_PERIOD,
                        SubscriptionStatus.REFUNDED,
                        SubscriptionStatus.REVOKED));

        // IN_TRIAL에서 가능한 전이
        TRANSITIONS.put(
                SubscriptionStatus.IN_TRIAL,
                EnumSet.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.EXPIRED));

        // GRACE_PERIOD에서 가능한 전이
        TRANSITIONS.put(
                SubscriptionStatus.GRACE_PERIOD,
                EnumSet.of(
                        SubscriptionStatus.ACTIVE, SubscriptionStatus.EXPIRED, SubscriptionStatus.BILLING_RETRY));

        // BILLING_RETRY에서 가능한 전이
        TRANSITIONS.put(
                SubscriptionStatus.BILLING_RETRY, EnumSet.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.EXPIRED));

        // 종료 상태는 전이 불가
        TRANSITIONS.put(SubscriptionStatus.EXPIRED, EnumSet.noneOf(SubscriptionStatus.class));
        TRANSITIONS.put(SubscriptionStatus.REFUNDED, EnumSet.noneOf(SubscriptionStatus.class));
        TRANSITIONS.put(SubscriptionStatus.REVOKED, EnumSet.noneOf(SubscriptionStatus.class));

        // Apple 이벤트 타입 매핑
        EVENT_STATUS_MAP.put("SUBSCRIBED", SubscriptionStatus.ACTIVE);
        EVENT_STATUS_MAP.put("DID_RENEW", SubscriptionStatus.ACTIVE);
        EVENT_STATUS_MAP.put("DID_FAIL_TO_RENEW", SubscriptionStatus.GRACE_PERIOD);
        EVENT_STATUS_MAP.put("EXPIRED", SubscriptionStatus.EXPIRED);
        EVENT_STATUS_MAP.put("GRACE_PERIOD_EXPIRED", SubscriptionStatus.EXPIRED);
        EVENT_STATUS_MAP.put("REFUND", SubscriptionStatus.REFUNDED);
        EVENT_STATUS_MAP.put("REVOKE", SubscriptionStatus.REVOKED);
        EVENT_STATUS_MAP.put("PRICE_CHANGE", SubscriptionStatus.ACTIVE);
        EVENT_STATUS_MAP.put("BILLING_RECOVERY", SubscriptionStatus.ACTIVE);
        EVENT_STATUS_MAP.put("START_TRIAL", SubscriptionStatus.IN_TRIAL);
    }

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
        Set<SubscriptionStatus> allowedTargets = TRANSITIONS.get(from);
        return allowedTargets != null && allowedTargets.contains(to);
    }

    /**
     * 상태 전이가 유효한지 검증하고, 유효하지 않으면 예외를 발생시킨다.
     *
     * @param from 현재 상태
     * @param to 새 상태
     * @throws IllegalStateException 유효하지 않은 상태 전이인 경우
     */
    public static void validateTransition(SubscriptionStatus from, SubscriptionStatus to) {
        if (!canTransition(from, to)) {
            throw new IllegalStateException(
                    String.format("Invalid status transition: %s -> %s", from, to));
        }
    }

    /**
     * Apple 이벤트 타입에 따른 목표 상태를 반환한다.
     *
     * @param eventType Apple 이벤트 타입 (SUBSCRIBED, DID_RENEW 등)
     * @return 목표 상태, 알 수 없는 이벤트면 null
     */
    public static SubscriptionStatus getTargetStatus(String eventType) {
        if (eventType == null) {
            return null;
        }
        return EVENT_STATUS_MAP.get(eventType.toUpperCase());
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
        return TERMINAL_STATUSES.contains(status);
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
        return ACTIVE_STATUSES.contains(status);
    }
}
