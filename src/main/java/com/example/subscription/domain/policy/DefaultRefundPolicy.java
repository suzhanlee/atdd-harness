package com.example.subscription.domain.policy;

import org.springframework.stereotype.Component;

/**
 * 기본 환불 정책 구현체.
 *
 * <p>Apple IAP 환불 정책을 기반으로 구현한다.
 */
@Component
public class DefaultRefundPolicy implements RefundPolicy {

    /** 환불 가능 기간 (일) - Apple 기본 정책 14일 */
    private static final int REFUND_PERIOD_DAYS = 14;

    /** 부분 환불 허용 여부 */
    private static final boolean PARTIAL_REFUND_ALLOWED = true;

    /** 환불 수수료율 - 0% (Apple 정책 기반) */
    private static final double REFUND_FEE_RATE = 0.0;

    @Override
    public int getRefundPeriodDays() {
        return REFUND_PERIOD_DAYS;
    }

    @Override
    public boolean isPartialRefundAllowed() {
        return PARTIAL_REFUND_ALLOWED;
    }

    @Override
    public double getRefundFeeRate() {
        return REFUND_FEE_RATE;
    }

    @Override
    public boolean isRefundableForReason(String reasonCode) {
        if (reasonCode == null) {
            return false;
        }
        // Apple에서 지원하는 환불 사유 코드
        return switch (reasonCode.toUpperCase()) {
            case "UNINTENDED_PURCHASE",
                    "APPLICATION_ISSUE",
                    "SERVICE_ISSUE",
                    "ACCIDENTAL_PURCHASE",
                    "OTHER" -> true;
            default -> false;
        };
    }

    @Override
    public double getRefundRate(int daysElapsed, int totalDays) {
        if (daysElapsed <= 0) {
            return 1.0; // 전액 환불
        }
        if (daysElapsed >= totalDays) {
            return 0.0; // 환불 불가
        }
        // 비례 환불 (Proration)
        double usedRatio = (double) daysElapsed / totalDays;
        return Math.max(0, 1.0 - usedRatio);
    }
}
