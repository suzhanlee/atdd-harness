package com.example.unit.domain.vo;

import static org.assertj.core.api.Assertions.*;

import com.example.subscription.domain.vo.ProductId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ProductId Value Object 테스트")
class ProductIdTest {

    // 제품 등급: BASIC < PRO < ULTRA
    private static final ProductId BASIC = new ProductId("basic_001");
    private static final ProductId PRO = new ProductId("pro_001");
    private static final ProductId ULTRA = new ProductId("ultra_001");

    @Nested
    @DisplayName("생성자는")
    class Constructor {

        @Test
        @DisplayName("유효한 값으로 생성한다")
        void constructor_withValidValue() {
            // given
            String value = "basic_001";

            // when
            ProductId productId = new ProductId(value);

            // then
            assertThat(productId.getValue()).isEqualTo(value);
        }

        @Test
        @DisplayName("null 값으로 생성 시 예외를 던진다")
        void constructor_withNullValue_throwsException() {
            // when & then
            assertThatThrownBy(() -> new ProductId(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("value must not be null");
        }

        @Test
        @DisplayName("빈 문자열로 생성 시 예외를 던진다")
        void constructor_withEmptyValue_throwsException() {
            // when & then
            assertThatThrownBy(() -> new ProductId(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("value must not be blank");
        }

        @Test
        @DisplayName("공백만 있는 문자열로 생성 시 예외를 던진다")
        void constructor_withBlankValue_throwsException() {
            // when & then
            assertThatThrownBy(() -> new ProductId("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("value must not be blank");
        }
    }

    @Nested
    @DisplayName("isHigherTierThan 메서드는")
    class IsHigherTierThan {

        @Test
        @DisplayName("ULTRA는 PRO보다 높은 등급이다")
        void isHigherTierThan_ultraVsPro_returnsTrue() {
            // when & then
            assertThat(ULTRA.isHigherTierThan(PRO)).isTrue();
        }

        @Test
        @DisplayName("ULTRA는 BASIC보다 높은 등급이다")
        void isHigherTierThan_ultraVsBasic_returnsTrue() {
            // when & then
            assertThat(ULTRA.isHigherTierThan(BASIC)).isTrue();
        }

        @Test
        @DisplayName("PRO는 BASIC보다 높은 등급이다")
        void isHigherTierThan_proVsBasic_returnsTrue() {
            // when & then
            assertThat(PRO.isHigherTierThan(BASIC)).isTrue();
        }

        @Test
        @DisplayName("BASIC은 PRO보다 낮은 등급이다")
        void isHigherTierThan_basicVsPro_returnsFalse() {
            // when & then
            assertThat(BASIC.isHigherTierThan(PRO)).isFalse();
        }

        @Test
        @DisplayName("PRO는 ULTRA보다 낮은 등급이다")
        void isHigherTierThan_proVsUltra_returnsFalse() {
            // when & then
            assertThat(PRO.isHigherTierThan(ULTRA)).isFalse();
        }

        @Test
        @DisplayName("동일한 등급이면 false를 반환한다")
        void isHigherTierThan_sameTier_returnsFalse() {
            // given
            ProductId basic2 = new ProductId("basic_001");

            // when & then
            assertThat(BASIC.isHigherTierThan(basic2)).isFalse();
        }
    }

    @Nested
    @DisplayName("동등성 비교")
    class Equality {

        @Test
        @DisplayName("같은 값의 ProductId는 동등하다")
        void equality_sameValue() {
            // given
            ProductId productId1 = new ProductId("basic_001");
            ProductId productId2 = new ProductId("basic_001");

            // then
            assertThat(productId1).isEqualTo(productId2);
            assertThat(productId1.hashCode()).isEqualTo(productId2.hashCode());
        }

        @Test
        @DisplayName("다른 값의 ProductId는 동등하지 않다")
        void equality_differentValue() {
            // given
            ProductId productId1 = new ProductId("basic_001");
            ProductId productId2 = new ProductId("pro_001");

            // then
            assertThat(productId1).isNotEqualTo(productId2);
        }
    }

    @Nested
    @DisplayName("toString 메서드는")
    class ToString {

        @Test
        @DisplayName("값을 문자열로 반환한다")
        void toString_returnsValue() {
            // given
            ProductId productId = new ProductId("basic_001");

            // when & then
            assertThat(productId.toString()).isEqualTo("basic_001");
        }
    }
}
