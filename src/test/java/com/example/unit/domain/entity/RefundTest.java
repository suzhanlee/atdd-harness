package com.example.unit.domain.entity;

import static org.assertj.core.api.Assertions.*;

import com.example.subscription.domain.entity.Refund;
import com.example.subscription.domain.vo.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Refund Entity 테스트")
class RefundTest {

    private static final Long SUBSCRIPTION_ID = 1L;
    private static final String ORIGINAL_TX_ID = "txn_original_001";
    private static final Money REFUND_AMOUNT = new Money(new BigDecimal("10000"), Currency.getInstance("KRW"));

    @Nested
    @DisplayName("create 정적 팩토리 메서드는")
    class Create {

        @Test
        @DisplayName("유효한 파라미터로 환불 정보를 생성한다")
        void create_withValidParameters() {
            // given
            Instant refundedAt = Instant.now();

            // when
            Refund refund = Refund.create(
                    SUBSCRIPTION_ID,
                    ORIGINAL_TX_ID,
                    REFUND_AMOUNT,
                    Refund.Reason.USER_REQUEST,
                    "APPLE_REASON_001",
                    refundedAt);

            // then
            assertThat(refund.getSubscriptionId()).isEqualTo(SUBSCRIPTION_ID);
            assertThat(refund.getOriginalTransactionId()).isEqualTo(ORIGINAL_TX_ID);
            assertThat(refund.getRefundAmount()).isEqualTo(REFUND_AMOUNT);
            assertThat(refund.getReason()).isEqualTo(Refund.Reason.USER_REQUEST);
            assertThat(refund.getAppleReasonCode()).isEqualTo("APPLE_REASON_001");
            assertThat(refund.getRefundedAt()).isEqualTo(refundedAt);
            assertThat(refund.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("APPLE_APPROVED 사유로 생성한다")
        void create_appleApprovedReason() {
            // given
            Instant refundedAt = Instant.now();

            // when
            Refund refund = Refund.create(
                    SUBSCRIPTION_ID,
                    ORIGINAL_TX_ID,
                    REFUND_AMOUNT,
                    Refund.Reason.APPLE_APPROVED,
                    null,
                    refundedAt);

            // then
            assertThat(refund.getReason()).isEqualTo(Refund.Reason.APPLE_APPROVED);
            assertThat(refund.getAppleReasonCode()).isNull();
        }

        @Test
        @DisplayName("PAYMENT_ISSUE 사유로 생성한다")
        void create_paymentIssueReason() {
            // given
            Instant refundedAt = Instant.now();

            // when
            Refund refund = Refund.create(
                    SUBSCRIPTION_ID,
                    ORIGINAL_TX_ID,
                    REFUND_AMOUNT,
                    Refund.Reason.PAYMENT_ISSUE,
                    null,
                    refundedAt);

            // then
            assertThat(refund.getReason()).isEqualTo(Refund.Reason.PAYMENT_ISSUE);
        }

        @Test
        @DisplayName("OTHER 사유로 생성한다")
        void create_otherReason() {
            // given
            Instant refundedAt = Instant.now();

            // when
            Refund refund = Refund.create(
                    SUBSCRIPTION_ID,
                    ORIGINAL_TX_ID,
                    REFUND_AMOUNT,
                    Refund.Reason.OTHER,
                    null,
                    refundedAt);

            // then
            assertThat(refund.getReason()).isEqualTo(Refund.Reason.OTHER);
        }

        @Test
        @DisplayName("null subscriptionId로 생성 시 예외를 던진다")
        void create_withNullSubscriptionId_throwsException() {
            // when & then
            assertThatThrownBy(() -> Refund.create(
                            null,
                            ORIGINAL_TX_ID,
                            REFUND_AMOUNT,
                            Refund.Reason.USER_REQUEST,
                            null,
                            Instant.now()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("subscriptionId must not be null");
        }

        @Test
        @DisplayName("null originalTransactionId로 생성 시 예외를 던진다")
        void create_withNullOriginalTransactionId_throwsException() {
            // when & then
            assertThatThrownBy(() -> Refund.create(
                            SUBSCRIPTION_ID,
                            null,
                            REFUND_AMOUNT,
                            Refund.Reason.USER_REQUEST,
                            null,
                            Instant.now()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("originalTransactionId must not be null");
        }

        @Test
        @DisplayName("null refundAmount로 생성 시 예외를 던진다")
        void create_withNullRefundAmount_throwsException() {
            // when & then
            assertThatThrownBy(() -> Refund.create(
                            SUBSCRIPTION_ID,
                            ORIGINAL_TX_ID,
                            null,
                            Refund.Reason.USER_REQUEST,
                            null,
                            Instant.now()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("refundAmount must not be null");
        }

        @Test
        @DisplayName("null refundedAt으로 생성 시 예외를 던진다")
        void create_withNullRefundedAt_throwsException() {
            // when & then
            assertThatThrownBy(() -> Refund.create(
                            SUBSCRIPTION_ID,
                            ORIGINAL_TX_ID,
                            REFUND_AMOUNT,
                            Refund.Reason.USER_REQUEST,
                            null,
                            null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("refundedAt must not be null");
        }
    }
}
