package com.example.subscription.domain.service;

import com.example.subscription.domain.vo.Money;
import com.example.subscription.domain.vo.Period;
import java.time.Instant;

/**
 * 환불 금액 계산 도메인 서비스.
 *
 * <p>환불 정책에 따른 환불 금액을 계산한다. 구독 상태, 경과 기간 등을 고려하여 정확한 환불 금액을 산출한다.
 *
 * @see com.example.subscription.domain.policy.RefundPolicy
 */
public interface RefundCalculationService {

    /**
     * 환불 금액을 계산한다.
     *
     * <p>Proration 방식: (총 결제 금액 * 남은 기간 / 총 기간)
     *
     * @param totalPaid 총 결제 금액
     * @param subscriptionPeriod 구독 기간
     * @param refundAt 환불 시점
     * @return 환불 금액
     */
    Money calculateRefundAmount(Money totalPaid, Period subscriptionPeriod, Instant refundAt);

    /**
     * 환불 가능 여부를 확인한다.
     *
     * @param subscriptionId 구독 ID
     * @param refundAt 환불 시점
     * @return 환불 가능하면 true
     */
    boolean canRefund(Long subscriptionId, Instant refundAt);

    /**
     * 환불 가능 기간(일)을 반환한다.
     *
     * <p>Apple 정책 또는 서비스 정책에 따른 환불 가능 기간
     *
     * @return 환불 가능 기간 (일)
     */
    int getRefundPeriodDays();
}
