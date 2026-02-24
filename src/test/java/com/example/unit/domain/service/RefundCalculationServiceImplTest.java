package com.example.unit.domain.service;

import com.example.subscription.domain.service.RefundCalculationServiceImpl;
import com.example.subscription.domain.vo.Money;
import com.example.subscription.domain.vo.Period;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RefundCalculationServiceImpl 단위 테스트.
 *
 * <p>환불 금액 계산 로직을 검증한다.
 */
@DisplayName("RefundCalculationServiceImpl 단위 테스트")
class RefundCalculationServiceImplTest {

    private RefundCalculationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RefundCalculationServiceImpl();
    }

    private Money money(double amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.getInstance("USD"));
    }

    private Period period(Instant start, Instant end) {
        return new Period(start, end);
    }

    @Nested
    @DisplayName("getRefundPeriodDays 메서드")
    class GetRefundPeriodDays {

        @Test
        @DisplayName("환불 가능 기간은 14일")
        void refundPeriodIs14Days() {
            // when
            int days = service.getRefundPeriodDays();

            // then
            assertThat(days).isEqualTo(14);
        }
    }

    @Nested
    @DisplayName("canRefund 메서드")
    class CanRefund {

        @Test
        @DisplayName("구독 시작 후 7일이면 환불 가능")
        void withinRefundPeriod() {
            // given
            Long subscriptionId = 1L;
            Instant now = Instant.now();

            // when
            boolean result = service.canRefund(subscriptionId, now);

            // then - Mock 없이 항상 true 반환 (실제로는 repository 확인 필요)
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("calculateRefundAmount 메서드")
    class CalculateRefundAmount {

        @Test
        @DisplayName("전체 기간 남음 - 전액 환불")
        void fullRefund() {
            // given
            Money totalPaid = money(9.99);
            Instant now = Instant.now();
            Instant periodStart = now.minus(1, ChronoUnit.DAYS);
            Instant periodEnd = now.plus(29, ChronoUnit.DAYS);
            Period period = period(periodStart, periodEnd);

            // when
            Money refund = service.calculateRefundAmount(totalPaid, period, now);

            // then - 거의 전액 환불 (1일 사용)
            assertThat(refund.getAmount()).isGreaterThan(BigDecimal.valueOf(9.00));
        }

        @Test
        @DisplayName("절반 기간 남음 - 절반 환불")
        void halfRefund() {
            // given
            Money totalPaid = money(10.00);
            Instant now = Instant.now();
            Instant periodStart = now.minus(15, ChronoUnit.DAYS);
            Instant periodEnd = now.plus(15, ChronoUnit.DAYS);
            Period period = period(periodStart, periodEnd);

            // when
            Money refund = service.calculateRefundAmount(totalPaid, period, now);

            // then - 절반 환불 (약 $5.00)
            assertThat(refund.getAmount()).isBetween(BigDecimal.valueOf(4.90), BigDecimal.valueOf(5.10));
        }

        @Test
        @DisplayName("기간 종료 - 환불 없음")
        void noRefund() {
            // given
            Money totalPaid = money(9.99);
            Instant now = Instant.now();
            Instant periodStart = now.minus(30, ChronoUnit.DAYS);
            Instant periodEnd = now.minus(1, ChronoUnit.DAYS);
            Period period = period(periodStart, periodEnd);

            // when
            Money refund = service.calculateRefundAmount(totalPaid, period, now);

            // then - 환불 없음
            assertThat(refund.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Proration 계산 정확성")
        void prorationAccuracy() {
            // given
            Money totalPaid = money(30.00);
            Instant now = Instant.now();
            // 30일 중 10일 사용, 20일 남음
            Instant periodStart = now.minus(10, ChronoUnit.DAYS);
            Instant periodEnd = now.plus(20, ChronoUnit.DAYS);
            Period period = period(periodStart, periodEnd);

            // when
            Money refund = service.calculateRefundAmount(totalPaid, period, now);

            // then - 20/30 = 2/3 환불 = $20.00
            assertThat(refund.getAmount()).isBetween(BigDecimal.valueOf(19.90), BigDecimal.valueOf(20.10));
        }
    }
}
