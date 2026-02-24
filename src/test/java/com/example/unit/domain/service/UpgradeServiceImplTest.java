package com.example.unit.domain.service;

import com.example.subscription.domain.service.UpgradeServiceImpl;
import com.example.subscription.domain.vo.Money;
import com.example.subscription.domain.vo.Period;
import com.example.subscription.domain.vo.ProductId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UpgradeServiceImpl 단위 테스트.
 *
 * <p>구독 업그레이드 계산 로직을 검증한다.
 */
@DisplayName("UpgradeServiceImpl 단위 테스트")
class UpgradeServiceImplTest {

    private UpgradeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UpgradeServiceImpl();
    }

    private Money money(double amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.getInstance("USD"));
    }

    private ProductId productId(String value) {
        return new ProductId(value);
    }

    @Nested
    @DisplayName("canUpgrade 메서드")
    class CanUpgrade {

        @Test
        @DisplayName("BASIC -> PRO 업그레이드 가능")
        void basicToPro() {
            // given
            ProductId basic = productId("basic_001");
            ProductId pro = productId("pro_001");

            // when
            boolean result = service.canUpgrade(basic, pro);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("BASIC -> ULTRA 업그레이드 가능")
        void basicToUltra() {
            // given
            ProductId basic = productId("basic_001");
            ProductId ultra = productId("ultra_001");

            // when
            boolean result = service.canUpgrade(basic, ultra);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("PRO -> ULTRA 업그레이드 가능")
        void proToUltra() {
            // given
            ProductId pro = productId("pro_001");
            ProductId ultra = productId("ultra_001");

            // when
            boolean result = service.canUpgrade(pro, ultra);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("동일 등급 업그레이드 불가")
        void sameTier() {
            // given
            ProductId pro = productId("pro_001");
            ProductId pro2 = productId("pro_002");

            // when
            boolean result = service.canUpgrade(pro, pro2);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("다운그레이드 불가 (PRO -> BASIC)")
        void downgrade() {
            // given
            ProductId pro = productId("pro_001");
            ProductId basic = productId("basic_001");

            // when
            boolean result = service.canUpgrade(pro, basic);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("compareTier 메서드")
    class CompareTier {

        @Test
        @DisplayName("BASIC < PRO")
        void basicVsPro() {
            // given
            ProductId basic = productId("basic_001");
            ProductId pro = productId("pro_001");

            // when
            int result = service.compareTier(basic, pro);

            // then
            assertThat(result).isNegative();
        }

        @Test
        @DisplayName("PRO < ULTRA")
        void proVsUltra() {
            // given
            ProductId pro = productId("pro_001");
            ProductId ultra = productId("ultra_001");

            // when
            int result = service.compareTier(pro, ultra);

            // then
            assertThat(result).isNegative();
        }

        @Test
        @DisplayName("ULTRA > BASIC")
        void ultraVsBasic() {
            // given
            ProductId ultra = productId("ultra_001");
            ProductId basic = productId("basic_001");

            // when
            int result = service.compareTier(ultra, basic);

            // then
            assertThat(result).isPositive();
        }

        @Test
        @DisplayName("동일 등급은 0")
        void sameTier() {
            // given
            ProductId pro1 = productId("pro_001");
            ProductId pro2 = productId("pro_002");

            // when
            int result = service.compareTier(pro1, pro2);

            // then
            assertThat(result).isZero();
        }
    }

    @Nested
    @DisplayName("calculateNewPeriod 메서드")
    class CalculateNewPeriod {

        @Test
        @DisplayName("업그레이드 시 새 기간은 30일")
        void newPeriodIs30Days() {
            // given
            ProductId pro = productId("pro_001");
            Instant upgradeAt = Instant.now();

            // when
            Period period = service.calculateNewPeriod(pro, upgradeAt);

            // then
            assertThat(period.getStartAt()).isEqualTo(upgradeAt);
            assertThat(period.getDurationInSeconds()).isEqualTo(30L * 24 * 60 * 60);
        }
    }

    @Nested
    @DisplayName("calculateUpgradePrice 메서드")
    class CalculateUpgradePrice {

        @Test
        @DisplayName("BASIC -> PRO 업그레이드 가격 계산")
        void basicToProPrice() {
            // given
            ProductId basic = productId("basic_001");
            ProductId pro = productId("pro_001");
            Instant now = Instant.now();
            Instant currentPeriodEnd = now.plus(20, ChronoUnit.DAYS); // 20일 남음

            // when
            Money upgradePrice = service.calculateUpgradePrice(basic, pro, currentPeriodEnd, now);

            // then
            // Proration: PRO 가격 - (BASIC 남은 기간 비율 * BASIC 가격)
            // PRO($19.99) - (20/30 * BASIC($9.99)) ≈ $13.33
            assertThat(upgradePrice.getAmount()).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("업그레이드 불가능한 경우 예외")
        void invalidUpgrade() {
            // given
            ProductId pro = productId("pro_001");
            ProductId basic = productId("basic_001");
            Instant now = Instant.now();
            Instant currentPeriodEnd = now.plus(20, ChronoUnit.DAYS);

            // when & then
            assertThatThrownBy(() -> service.calculateUpgradePrice(pro, basic, currentPeriodEnd, now))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot upgrade");
        }
    }
}
