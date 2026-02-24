package com.example.unit.domain.spec;

import static org.assertj.core.api.Assertions.*;

import com.example.subscription.domain.spec.SubscriptionStatusSpec;
import com.example.subscription.domain.vo.SubscriptionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("SubscriptionStatusSpec 테스트")
class SubscriptionStatusSpecTest {

    @Nested
    @DisplayName("canTransition 메서드는")
    class CanTransition {

        @Test
        @DisplayName("ACTIVE → GRACE_PERIOD 전이는 가능하다")
        void canTransition_activeToGracePeriod_returnsTrue() {
            // when & then
            assertThat(SubscriptionStatusSpec.canTransition(SubscriptionStatus.ACTIVE, SubscriptionStatus.GRACE_PERIOD))
                    .isTrue();
        }

        @Test
        @DisplayName("ACTIVE → BILLING_RETRY 전이는 불가능하다 (GRACE_PERIOD를 거쳐야 함)")
        void canTransition_activeToBillingRetry_returnsFalse() {
            // when & then
            assertThat(SubscriptionStatusSpec.canTransition(SubscriptionStatus.ACTIVE, SubscriptionStatus.BILLING_RETRY))
                    .isFalse();
        }

        @Test
        @DisplayName("ACTIVE → EXPIRED 전이는 가능하다")
        void canTransition_activeToExpired_returnsTrue() {
            // when & then
            assertThat(SubscriptionStatusSpec.canTransition(SubscriptionStatus.ACTIVE, SubscriptionStatus.EXPIRED))
                    .isTrue();
        }

        @Test
        @DisplayName("ACTIVE → REFUNDED 전이는 가능하다")
        void canTransition_activeToRefunded_returnsTrue() {
            // when & then
            assertThat(SubscriptionStatusSpec.canTransition(SubscriptionStatus.ACTIVE, SubscriptionStatus.REFUNDED))
                    .isTrue();
        }

        @Test
        @DisplayName("ACTIVE → REVOKED 전이는 가능하다")
        void canTransition_activeToRevoked_returnsTrue() {
            // when & then
            assertThat(SubscriptionStatusSpec.canTransition(SubscriptionStatus.ACTIVE, SubscriptionStatus.REVOKED))
                    .isTrue();
        }

        @Test
        @DisplayName("GRACE_PERIOD → ACTIVE 전이는 가능하다")
        void canTransition_gracePeriodToActive_returnsTrue() {
            // when & then
            assertThat(SubscriptionStatusSpec.canTransition(SubscriptionStatus.GRACE_PERIOD, SubscriptionStatus.ACTIVE))
                    .isTrue();
        }

        @Test
        @DisplayName("GRACE_PERIOD → EXPIRED 전이는 가능하다")
        void canTransition_gracePeriodToExpired_returnsTrue() {
            // when & then
            assertThat(SubscriptionStatusSpec.canTransition(SubscriptionStatus.GRACE_PERIOD, SubscriptionStatus.EXPIRED))
                    .isTrue();
        }

        @Test
        @DisplayName("GRACE_PERIOD → BILLING_RETRY 전이는 가능하다")
        void canTransition_gracePeriodToBillingRetry_returnsTrue() {
            // when & then
            assertThat(SubscriptionStatusSpec.canTransition(SubscriptionStatus.GRACE_PERIOD, SubscriptionStatus.BILLING_RETRY))
                    .isTrue();
        }

        @Test
        @DisplayName("BILLING_RETRY → ACTIVE 전이는 가능하다")
        void canTransition_billingRetryToActive_returnsTrue() {
            // when & then
            assertThat(SubscriptionStatusSpec.canTransition(SubscriptionStatus.BILLING_RETRY, SubscriptionStatus.ACTIVE))
                    .isTrue();
        }

        @Test
        @DisplayName("BILLING_RETRY → EXPIRED 전이는 가능하다")
        void canTransition_billingRetryToExpired_returnsTrue() {
            // when & then
            assertThat(SubscriptionStatusSpec.canTransition(SubscriptionStatus.BILLING_RETRY, SubscriptionStatus.EXPIRED))
                    .isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = SubscriptionStatus.class, mode = Mode.EXCLUDE, names = {"EXPIRED", "REFUNDED", "REVOKED"})
        @DisplayName("종료 상태(EXPIRED, REFUNDED, REVOKED)에서는 다른 상태로 전이할 수 없다")
        void canTransition_terminalStatus_returnsFalse(SubscriptionStatus targetStatus) {
            // when & then
            assertThat(SubscriptionStatusSpec.canTransition(SubscriptionStatus.EXPIRED, targetStatus)).isFalse();
            assertThat(SubscriptionStatusSpec.canTransition(SubscriptionStatus.REFUNDED, targetStatus)).isFalse();
            assertThat(SubscriptionStatusSpec.canTransition(SubscriptionStatus.REVOKED, targetStatus)).isFalse();
        }

        @Test
        @DisplayName("GRACE_PERIOD → REFUNDED 전이는 불가능하다")
        void canTransition_gracePeriodToRefunded_returnsFalse() {
            // when & then
            assertThat(SubscriptionStatusSpec.canTransition(SubscriptionStatus.GRACE_PERIOD, SubscriptionStatus.REFUNDED))
                    .isFalse();
        }

        @Test
        @DisplayName("BILLING_RETRY → REFUNDED 전이는 불가능하다")
        void canTransition_billingRetryToRefunded_returnsFalse() {
            // when & then
            assertThat(SubscriptionStatusSpec.canTransition(SubscriptionStatus.BILLING_RETRY, SubscriptionStatus.REFUNDED))
                    .isFalse();
        }
    }

    @Nested
    @DisplayName("validateTransition 메서드는")
    class ValidateTransition {

        @Test
        @DisplayName("유효한 전이 시 예외를 던지지 않는다")
        void validateTransition_validTransition_noException() {
            // when & then - should not throw
            assertThatCode(() ->
                    SubscriptionStatusSpec.validateTransition(SubscriptionStatus.ACTIVE, SubscriptionStatus.GRACE_PERIOD))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("유효하지 않은 전이 시 예외를 던진다")
        void validateTransition_invalidTransition_throwsException() {
            // when & then
            assertThatThrownBy(() ->
                    SubscriptionStatusSpec.validateTransition(SubscriptionStatus.EXPIRED, SubscriptionStatus.ACTIVE))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid status transition");
        }
    }

    @Nested
    @DisplayName("getTargetStatus 메서드는")
    class GetTargetStatus {

        @Test
        @DisplayName("SUBSCRIBED 이벤트는 ACTIVE 상태를 반환한다")
        void getTargetStatus_subscribed_returnsActive() {
            // when
            SubscriptionStatus status = SubscriptionStatusSpec.getTargetStatus("SUBSCRIBED");

            // then
            assertThat(status).isEqualTo(SubscriptionStatus.ACTIVE);
        }

        @Test
        @DisplayName("DID_RENEW 이벤트는 ACTIVE 상태를 반환한다")
        void getTargetStatus_didRenew_returnsActive() {
            // when
            SubscriptionStatus status = SubscriptionStatusSpec.getTargetStatus("DID_RENEW");

            // then
            assertThat(status).isEqualTo(SubscriptionStatus.ACTIVE);
        }

        @Test
        @DisplayName("DID_FAIL_TO_RENEW 이벤트는 GRACE_PERIOD 상태를 반환한다")
        void getTargetStatus_didFailToRenew_returnsGracePeriod() {
            // when
            SubscriptionStatus status = SubscriptionStatusSpec.getTargetStatus("DID_FAIL_TO_RENEW");

            // then
            assertThat(status).isEqualTo(SubscriptionStatus.GRACE_PERIOD);
        }

        @Test
        @DisplayName("EXPIRED 이벤트는 EXPIRED 상태를 반환한다")
        void getTargetStatus_expired_returnsExpired() {
            // when
            SubscriptionStatus status = SubscriptionStatusSpec.getTargetStatus("EXPIRED");

            // then
            assertThat(status).isEqualTo(SubscriptionStatus.EXPIRED);
        }

        @Test
        @DisplayName("GRACE_PERIOD_EXPIRED 이벤트는 EXPIRED 상태를 반환한다")
        void getTargetStatus_gracePeriodExpired_returnsExpired() {
            // when
            SubscriptionStatus status = SubscriptionStatusSpec.getTargetStatus("GRACE_PERIOD_EXPIRED");

            // then
            assertThat(status).isEqualTo(SubscriptionStatus.EXPIRED);
        }

        @Test
        @DisplayName("REFUND 이벤트는 REFUNDED 상태를 반환한다")
        void getTargetStatus_refund_returnsRefunded() {
            // when
            SubscriptionStatus status = SubscriptionStatusSpec.getTargetStatus("REFUND");

            // then
            assertThat(status).isEqualTo(SubscriptionStatus.REFUNDED);
        }

        @Test
        @DisplayName("REVOKE 이벤트는 REVOKED 상태를 반환한다")
        void getTargetStatus_revoke_returnsRevoked() {
            // when
            SubscriptionStatus status = SubscriptionStatusSpec.getTargetStatus("REVOKE");

            // then
            assertThat(status).isEqualTo(SubscriptionStatus.REVOKED);
        }

        @Test
        @DisplayName("PRICE_CHANGE 이벤트는 ACTIVE 상태를 반환한다")
        void getTargetStatus_priceChange_returnsActive() {
            // when
            SubscriptionStatus status = SubscriptionStatusSpec.getTargetStatus("PRICE_CHANGE");

            // then
            assertThat(status).isEqualTo(SubscriptionStatus.ACTIVE);
        }

        @Test
        @DisplayName("BILLING_RECOVERY 이벤트는 ACTIVE 상태를 반환한다")
        void getTargetStatus_billingRecovery_returnsActive() {
            // when
            SubscriptionStatus status = SubscriptionStatusSpec.getTargetStatus("BILLING_RECOVERY");

            // then
            assertThat(status).isEqualTo(SubscriptionStatus.ACTIVE);
        }

        @Test
        @DisplayName("알 수 없는 이벤트는 null을 반환한다")
        void getTargetStatus_unknownEvent_returnsNull() {
            // when
            SubscriptionStatus status = SubscriptionStatusSpec.getTargetStatus("UNKNOWN_EVENT");

            // then
            assertThat(status).isNull();
        }
    }

    @Nested
    @DisplayName("isTerminalStatus 메서드는")
    class IsTerminalStatus {

        @Test
        @DisplayName("EXPIRED는 종료 상태이다")
        void isTerminalStatus_expired_returnsTrue() {
            // when & then
            assertThat(SubscriptionStatusSpec.isTerminalStatus(SubscriptionStatus.EXPIRED)).isTrue();
        }

        @Test
        @DisplayName("REFUNDED는 종료 상태이다")
        void isTerminalStatus_refunded_returnsTrue() {
            // when & then
            assertThat(SubscriptionStatusSpec.isTerminalStatus(SubscriptionStatus.REFUNDED)).isTrue();
        }

        @Test
        @DisplayName("REVOKED는 종료 상태이다")
        void isTerminalStatus_revoked_returnsTrue() {
            // when & then
            assertThat(SubscriptionStatusSpec.isTerminalStatus(SubscriptionStatus.REVOKED)).isTrue();
        }

        @Test
        @DisplayName("ACTIVE는 종료 상태가 아니다")
        void isTerminalStatus_active_returnsFalse() {
            // when & then
            assertThat(SubscriptionStatusSpec.isTerminalStatus(SubscriptionStatus.ACTIVE)).isFalse();
        }

        @Test
        @DisplayName("GRACE_PERIOD는 종료 상태가 아니다")
        void isTerminalStatus_gracePeriod_returnsFalse() {
            // when & then
            assertThat(SubscriptionStatusSpec.isTerminalStatus(SubscriptionStatus.GRACE_PERIOD)).isFalse();
        }

        @Test
        @DisplayName("BILLING_RETRY는 종료 상태가 아니다")
        void isTerminalStatus_billingRetry_returnsFalse() {
            // when & then
            assertThat(SubscriptionStatusSpec.isTerminalStatus(SubscriptionStatus.BILLING_RETRY)).isFalse();
        }
    }

    @Nested
    @DisplayName("isActiveStatus 메서드는")
    class IsActiveStatus {

        @Test
        @DisplayName("ACTIVE는 활성 상태이다")
        void isActiveStatus_active_returnsTrue() {
            // when & then
            assertThat(SubscriptionStatusSpec.isActiveStatus(SubscriptionStatus.ACTIVE)).isTrue();
        }

        @Test
        @DisplayName("GRACE_PERIOD는 활성 상태이다")
        void isActiveStatus_gracePeriod_returnsTrue() {
            // when & then
            assertThat(SubscriptionStatusSpec.isActiveStatus(SubscriptionStatus.GRACE_PERIOD)).isTrue();
        }

        @Test
        @DisplayName("BILLING_RETRY는 활성 상태이다")
        void isActiveStatus_billingRetry_returnsTrue() {
            // when & then
            assertThat(SubscriptionStatusSpec.isActiveStatus(SubscriptionStatus.BILLING_RETRY)).isTrue();
        }

        @Test
        @DisplayName("EXPIRED는 활성 상태가 아니다")
        void isActiveStatus_expired_returnsFalse() {
            // when & then
            assertThat(SubscriptionStatusSpec.isActiveStatus(SubscriptionStatus.EXPIRED)).isFalse();
        }

        @Test
        @DisplayName("REFUNDED는 활성 상태가 아니다")
        void isActiveStatus_refunded_returnsFalse() {
            // when & then
            assertThat(SubscriptionStatusSpec.isActiveStatus(SubscriptionStatus.REFUNDED)).isFalse();
        }

        @Test
        @DisplayName("REVOKED는 활성 상태가 아니다")
        void isActiveStatus_revoked_returnsFalse() {
            // when & then
            assertThat(SubscriptionStatusSpec.isActiveStatus(SubscriptionStatus.REVOKED)).isFalse();
        }
    }
}
