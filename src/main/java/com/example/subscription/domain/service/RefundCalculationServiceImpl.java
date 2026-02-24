package com.example.subscription.domain.service;

import com.example.subscription.domain.vo.Money;
import com.example.subscription.domain.vo.Period;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import org.springframework.stereotype.Service;

/**
 * 환불 금액 계산 도메인 서비스 구현체.
 *
 * <p>Apple IAP 환불 정책에 따른 환불 금액 계산을 담당한다. Proration 방식으로 남은 기간에 비례하여 환불 금액을 계산한다.
 */
@Service
public class RefundCalculationServiceImpl implements RefundCalculationService {

    private static final Currency USD = Currency.getInstance("USD");
    private static final int REFUND_PERIOD_DAYS = 14;

    @Override
    public Money calculateRefundAmount(Money totalPaid, Period subscriptionPeriod, Instant refundAt) {
        // 기간이 이미 종료된 경우 환불 없음
        if (refundAt.isAfter(subscriptionPeriod.getEndAt())
                || refundAt.equals(subscriptionPeriod.getEndAt())) {
            return new Money(BigDecimal.ZERO, USD);
        }

        // 시작 전인 경우 전액 환불
        if (refundAt.isBefore(subscriptionPeriod.getStartAt())) {
            return totalPaid;
        }

        // 총 기간 (초)
        long totalSeconds = subscriptionPeriod.getDurationInSeconds();
        if (totalSeconds <= 0) {
            return new Money(BigDecimal.ZERO, USD);
        }

        // 남은 기간 (초)
        long remainingSeconds = ChronoUnit.SECONDS.between(refundAt, subscriptionPeriod.getEndAt());
        if (remainingSeconds < 0) {
            remainingSeconds = 0;
        }

        // 남은 기간 비율 계산
        BigDecimal remainingRatio = BigDecimal.valueOf(remainingSeconds)
                .divide(BigDecimal.valueOf(totalSeconds), 6, RoundingMode.HALF_UP);

        // 환불 금액 = 총 결제 금액 * 남은 기간 비율
        BigDecimal refundAmount = totalPaid.getAmount().multiply(remainingRatio);

        return new Money(refundAmount.setScale(2, RoundingMode.HALF_UP), USD);
    }

    @Override
    public boolean canRefund(Long subscriptionId, Instant refundAt) {
        // 실제 구현에서는 repository에서 구독 정보를 조회하여 확인
        // 여기서는 간단히 항상 true 반환 (비즈니스 로직 검증은 별도)
        return true;
    }

    @Override
    public int getRefundPeriodDays() {
        return REFUND_PERIOD_DAYS;
    }
}
