package com.example.unit.domain.entity;

import static org.assertj.core.api.Assertions.*;

import com.example.subscription.domain.entity.Transaction;
import com.example.subscription.domain.vo.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Transaction Entity 테스트")
class TransactionTest {

    private static final Long SUBSCRIPTION_ID = 1L;
    private static final String TRANSACTION_ID = "txn_12345";
    private static final Money AMOUNT = new Money(new BigDecimal("10000"), Currency.getInstance("KRW"));

    @Nested
    @DisplayName("create 정적 팩토리 메서드는")
    class Create {

        @Test
        @DisplayName("유효한 파라미터로 트랜잭션을 생성한다")
        void create_withValidParameters() {
            // given
            Instant purchasedAt = Instant.now();

            // when
            Transaction transaction = Transaction.create(
                    SUBSCRIPTION_ID, TRANSACTION_ID, Transaction.Type.PURCHASE, AMOUNT, purchasedAt);

            // then
            assertThat(transaction.getSubscriptionId()).isEqualTo(SUBSCRIPTION_ID);
            assertThat(transaction.getTransactionId()).isEqualTo(TRANSACTION_ID);
            assertThat(transaction.getType()).isEqualTo(Transaction.Type.PURCHASE);
            assertThat(transaction.getAmount()).isEqualTo(AMOUNT);
            assertThat(transaction.getPurchasedAt()).isEqualTo(purchasedAt);
            assertThat(transaction.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("RENEWAL 타입으로 생성한다")
        void create_renewalType() {
            // given
            Instant purchasedAt = Instant.now();

            // when
            Transaction transaction = Transaction.create(
                    SUBSCRIPTION_ID, TRANSACTION_ID, Transaction.Type.RENEWAL, AMOUNT, purchasedAt);

            // then
            assertThat(transaction.getType()).isEqualTo(Transaction.Type.RENEWAL);
        }

        @Test
        @DisplayName("REFUND 타입으로 생성한다")
        void create_refundType() {
            // given
            Instant purchasedAt = Instant.now();

            // when
            Transaction transaction = Transaction.create(
                    SUBSCRIPTION_ID, TRANSACTION_ID, Transaction.Type.REFUND, AMOUNT, purchasedAt);

            // then
            assertThat(transaction.getType()).isEqualTo(Transaction.Type.REFUND);
        }

        @Test
        @DisplayName("UPSELL 타입으로 생성한다")
        void create_upsellType() {
            // given
            Instant purchasedAt = Instant.now();

            // when
            Transaction transaction = Transaction.create(
                    SUBSCRIPTION_ID, TRANSACTION_ID, Transaction.Type.UPSELL, AMOUNT, purchasedAt);

            // then
            assertThat(transaction.getType()).isEqualTo(Transaction.Type.UPSELL);
        }

        @Test
        @DisplayName("null subscriptionId로 생성 시 예외를 던진다")
        void create_withNullSubscriptionId_throwsException() {
            // when & then
            assertThatThrownBy(() -> Transaction.create(
                            null, TRANSACTION_ID, Transaction.Type.PURCHASE, AMOUNT, Instant.now()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("subscriptionId must not be null");
        }

        @Test
        @DisplayName("null transactionId로 생성 시 예외를 던진다")
        void create_withNullTransactionId_throwsException() {
            // when & then
            assertThatThrownBy(() -> Transaction.create(
                            SUBSCRIPTION_ID, null, Transaction.Type.PURCHASE, AMOUNT, Instant.now()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("transactionId must not be null");
        }

        @Test
        @DisplayName("null type으로 생성 시 예외를 던진다")
        void create_withNullType_throwsException() {
            // when & then
            assertThatThrownBy(() -> Transaction.create(
                            SUBSCRIPTION_ID, TRANSACTION_ID, null, AMOUNT, Instant.now()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("type must not be null");
        }

        @Test
        @DisplayName("null purchasedAt으로 생성 시 예외를 던진다")
        void create_withNullPurchasedAt_throwsException() {
            // when & then
            assertThatThrownBy(() -> Transaction.create(
                            SUBSCRIPTION_ID, TRANSACTION_ID, Transaction.Type.PURCHASE, AMOUNT, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("purchasedAt must not be null");
        }
    }

    @Nested
    @DisplayName("isRefund 메서드는")
    class IsRefund {

        @Test
        @DisplayName("REFUND 타입이면 true를 반환한다")
        void isRefund_refundType_returnsTrue() {
            // given
            Transaction transaction = Transaction.create(
                    SUBSCRIPTION_ID, TRANSACTION_ID, Transaction.Type.REFUND, AMOUNT, Instant.now());

            // when & then
            assertThat(transaction.isRefund()).isTrue();
        }

        @Test
        @DisplayName("PURCHASE 타입이면 false를 반환한다")
        void isRefund_purchaseType_returnsFalse() {
            // given
            Transaction transaction = Transaction.create(
                    SUBSCRIPTION_ID, TRANSACTION_ID, Transaction.Type.PURCHASE, AMOUNT, Instant.now());

            // when & then
            assertThat(transaction.isRefund()).isFalse();
        }

        @Test
        @DisplayName("RENEWAL 타입이면 false를 반환한다")
        void isRefund_renewalType_returnsFalse() {
            // given
            Transaction transaction = Transaction.create(
                    SUBSCRIPTION_ID, TRANSACTION_ID, Transaction.Type.RENEWAL, AMOUNT, Instant.now());

            // when & then
            assertThat(transaction.isRefund()).isFalse();
        }

        @Test
        @DisplayName("UPSELL 타입이면 false를 반환한다")
        void isRefund_upsellType_returnsFalse() {
            // given
            Transaction transaction = Transaction.create(
                    SUBSCRIPTION_ID, TRANSACTION_ID, Transaction.Type.UPSELL, AMOUNT, Instant.now());

            // when & then
            assertThat(transaction.isRefund()).isFalse();
        }
    }
}
