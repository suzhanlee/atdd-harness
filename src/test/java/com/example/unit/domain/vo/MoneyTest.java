package com.example.unit.domain.vo;

import static org.assertj.core.api.Assertions.*;

import com.example.subscription.domain.vo.Money;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Money Value Object 테스트")
class MoneyTest {

    private static final Currency KRW = Currency.getInstance("KRW");
    private static final Currency USD = Currency.getInstance("USD");

    @Nested
    @DisplayName("생성자는")
    class Constructor {

        @Test
        @DisplayName("유효한 금액과 통화로 생성한다")
        void constructor_withValidAmount() {
            // given
            BigDecimal amount = new BigDecimal("10000");

            // when
            Money money = new Money(amount, KRW);

            // then
            assertThat(money.getAmount()).isEqualByComparingTo(amount);
            assertThat(money.getCurrency()).isEqualTo(KRW);
        }

        @Test
        @DisplayName("0원으로 생성할 수 있다")
        void constructor_withZero() {
            // given
            BigDecimal amount = BigDecimal.ZERO;

            // when
            Money money = new Money(amount, KRW);

            // then
            assertThat(money.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("null amount로 생성 시 예외를 던진다")
        void constructor_withNullAmount_throwsException() {
            // when & then
            assertThatThrownBy(() -> new Money(null, KRW))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("amount must not be null");
        }

        @Test
        @DisplayName("null currency로 생성 시 예외를 던진다")
        void constructor_withNullCurrency_throwsException() {
            // when & then
            assertThatThrownBy(() -> new Money(BigDecimal.TEN, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("currency must not be null");
        }

        @Test
        @DisplayName("음수 금액으로 생성 시 예외를 던진다")
        void constructor_withNegativeAmount_throwsException() {
            // given
            BigDecimal negativeAmount = new BigDecimal("-1000");

            // when & then
            assertThatThrownBy(() -> new Money(negativeAmount, KRW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("amount must be non-negative");
        }
    }

    @Nested
    @DisplayName("add 메서드는")
    class Add {

        @Test
        @DisplayName("같은 통화의 금액을 더한다")
        void add_sameCurrency() {
            // given
            Money money1 = new Money(new BigDecimal("10000"), KRW);
            Money money2 = new Money(new BigDecimal("5000"), KRW);

            // when
            Money result = money1.add(money2);

            // then
            assertThat(result.getAmount()).isEqualByComparingTo("15000");
            assertThat(result.getCurrency()).isEqualTo(KRW);
        }

        @Test
        @DisplayName("다른 통화의 금액을 더할 때 예외를 던진다")
        void add_differentCurrency_throwsException() {
            // given
            Money money1 = new Money(new BigDecimal("10000"), KRW);
            Money money2 = new Money(new BigDecimal("10"), USD);

            // when & then
            assertThatThrownBy(() -> money1.add(money2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Currency mismatch");
        }
    }

    @Nested
    @DisplayName("subtract 메서드는")
    class Subtract {

        @Test
        @DisplayName("같은 통화의 금액을 뺀다")
        void subtract_sameCurrency() {
            // given
            Money money1 = new Money(new BigDecimal("10000"), KRW);
            Money money2 = new Money(new BigDecimal("3000"), KRW);

            // when
            Money result = money1.subtract(money2);

            // then
            assertThat(result.getAmount()).isEqualByComparingTo("7000");
        }

        @Test
        @DisplayName("다른 통화의 금액을 뺄 때 예외를 던진다")
        void subtract_differentCurrency_throwsException() {
            // given
            Money money1 = new Money(new BigDecimal("10000"), KRW);
            Money money2 = new Money(new BigDecimal("10"), USD);

            // when & then
            assertThatThrownBy(() -> money1.subtract(money2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Currency mismatch");
        }
    }

    @Nested
    @DisplayName("multiply 메서드는")
    class Multiply {

        @Test
        @DisplayName("금액에 비율을 곱한다")
        void multiply_positiveMultiplier() {
            // given
            Money money = new Money(new BigDecimal("10000"), KRW);
            BigDecimal multiplier = new BigDecimal("1.1");

            // when
            Money result = money.multiply(multiplier);

            // then
            assertThat(result.getAmount()).isEqualByComparingTo("11000");
        }

        @Test
        @DisplayName("0을 곱하면 0원이 된다")
        void multiply_zeroMultiplier() {
            // given
            Money money = new Money(new BigDecimal("10000"), KRW);

            // when
            Money result = money.multiply(BigDecimal.ZERO);

            // then
            assertThat(result.getAmount()).isEqualByComparingTo("0");
        }
    }

    @Nested
    @DisplayName("isGreaterThan 메서드는")
    class IsGreaterThan {

        @Test
        @DisplayName("더 큰 금액이면 true를 반환한다")
        void isGreaterThan_smallerAmount_returnsTrue() {
            // given
            Money money1 = new Money(new BigDecimal("10000"), KRW);
            Money money2 = new Money(new BigDecimal("5000"), KRW);

            // when & then
            assertThat(money1.isGreaterThan(money2)).isTrue();
        }

        @Test
        @DisplayName("더 작은 금액이면 false를 반환한다")
        void isGreaterThan_largerAmount_returnsFalse() {
            // given
            Money money1 = new Money(new BigDecimal("5000"), KRW);
            Money money2 = new Money(new BigDecimal("10000"), KRW);

            // when & then
            assertThat(money1.isGreaterThan(money2)).isFalse();
        }

        @Test
        @DisplayName("같은 금액이면 false를 반환한다")
        void isGreaterThan_sameAmount_returnsFalse() {
            // given
            Money money1 = new Money(new BigDecimal("10000"), KRW);
            Money money2 = new Money(new BigDecimal("10000"), KRW);

            // when & then
            assertThat(money1.isGreaterThan(money2)).isFalse();
        }
    }

    @Nested
    @DisplayName("isZero 메서드는")
    class IsZero {

        @Test
        @DisplayName("0원이면 true를 반환한다")
        void isZero_zeroAmount_returnsTrue() {
            // given
            Money money = new Money(BigDecimal.ZERO, KRW);

            // when & then
            assertThat(money.isZero()).isTrue();
        }

        @Test
        @DisplayName("0원이 아니면 false를 반환한다")
        void isZero_nonZeroAmount_returnsFalse() {
            // given
            Money money = new Money(new BigDecimal("10000"), KRW);

            // when & then
            assertThat(money.isZero()).isFalse();
        }
    }

    @Nested
    @DisplayName("동등성 비교")
    class Equality {

        @Test
        @DisplayName("같은 값의 Money는 동등하다")
        void equality_sameValue() {
            // given
            Money money1 = new Money(new BigDecimal("10000"), KRW);
            Money money2 = new Money(new BigDecimal("10000"), KRW);

            // then
            assertThat(money1).isEqualTo(money2);
            assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
        }

        @Test
        @DisplayName("다른 값의 Money는 동등하지 않다")
        void equality_differentValue() {
            // given
            Money money1 = new Money(new BigDecimal("10000"), KRW);
            Money money2 = new Money(new BigDecimal("5000"), KRW);

            // then
            assertThat(money1).isNotEqualTo(money2);
        }
    }
}
