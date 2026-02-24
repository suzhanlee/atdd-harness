package com.example.unit.domain.entity;

import static org.assertj.core.api.Assertions.*;

import com.example.subscription.domain.entity.SubscriptionHistory;
import com.example.subscription.domain.vo.SubscriptionStatus;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SubscriptionHistory Entity 테스트")
class SubscriptionHistoryTest {

    private static final Long SUBSCRIPTION_ID = 1L;
    private static final String TRANSACTION_ID = "txn_12345";

    @Nested
    @DisplayName("create 정적 팩토리 메서드는")
    class Create {

        @Test
        @DisplayName("유효한 파라미터로 이력을 생성한다")
        void create_withValidParameters() {
            // given
            Instant changedAt = Instant.now();

            // when
            SubscriptionHistory history = SubscriptionHistory.create(
                    SUBSCRIPTION_ID,
                    SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.GRACE_PERIOD,
                    "DID_FAIL_TO_RENEW",
                    TRANSACTION_ID,
                    changedAt);

            // then
            assertThat(history.getSubscriptionId()).isEqualTo(SUBSCRIPTION_ID);
            assertThat(history.getFromStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            assertThat(history.getToStatus()).isEqualTo(SubscriptionStatus.GRACE_PERIOD);
            assertThat(history.getReason()).isEqualTo("DID_FAIL_TO_RENEW");
            assertThat(history.getTransactionId()).isEqualTo(TRANSACTION_ID);
            assertThat(history.getChangedAt()).isEqualTo(changedAt);
        }

        @Test
        @DisplayName("노트와 함께 이력을 생성한다")
        void create_withNote() {
            // given
            Instant changedAt = Instant.now();

            // when
            SubscriptionHistory history = SubscriptionHistory.create(
                    SUBSCRIPTION_ID,
                    SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.EXPIRED,
                    "EXPIRED",
                    TRANSACTION_ID,
                    changedAt);

            // then
            assertThat(history.getFromStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            assertThat(history.getToStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        }

        @Test
        @DisplayName("null subscriptionId로 생성 시 예외를 던진다")
        void create_withNullSubscriptionId_throwsException() {
            // when & then
            assertThatThrownBy(() -> SubscriptionHistory.create(
                            null,
                            SubscriptionStatus.ACTIVE,
                            SubscriptionStatus.EXPIRED,
                            "EXPIRED",
                            TRANSACTION_ID,
                            Instant.now()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("subscriptionId must not be null");
        }

        @Test
        @DisplayName("null fromStatus로 생성 시 예외를 던진다")
        void create_withNullFromStatus_throwsException() {
            // when & then
            assertThatThrownBy(() -> SubscriptionHistory.create(
                            SUBSCRIPTION_ID,
                            null,
                            SubscriptionStatus.EXPIRED,
                            "EXPIRED",
                            TRANSACTION_ID,
                            Instant.now()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("fromStatus must not be null");
        }

        @Test
        @DisplayName("null toStatus로 생성 시 예외를 던진다")
        void create_withNullToStatus_throwsException() {
            // when & then
            assertThatThrownBy(() -> SubscriptionHistory.create(
                            SUBSCRIPTION_ID,
                            SubscriptionStatus.ACTIVE,
                            null,
                            "EXPIRED",
                            TRANSACTION_ID,
                            Instant.now()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("toStatus must not be null");
        }

        @Test
        @DisplayName("null changedAt으로 생성 시 예외를 던진다")
        void create_withNullChangedAt_throwsException() {
            // when & then
            assertThatThrownBy(() -> SubscriptionHistory.create(
                            SUBSCRIPTION_ID,
                            SubscriptionStatus.ACTIVE,
                            SubscriptionStatus.EXPIRED,
                            "EXPIRED",
                            TRANSACTION_ID,
                            null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("changedAt must not be null");
        }
    }
}
