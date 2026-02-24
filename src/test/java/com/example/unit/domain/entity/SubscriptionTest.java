package com.example.unit.domain.entity;

import static org.assertj.core.api.Assertions.*;

import com.example.subscription.domain.entity.Subscription;
import com.example.subscription.domain.vo.Period;
import com.example.subscription.domain.vo.ProductId;
import com.example.subscription.domain.vo.SubscriptionStatus;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Subscription Entity 테스트")
class SubscriptionTest {

    private static final String ORIGINAL_TX_ID = "txn_original_001";
    private static final Long USER_ID = 1L;
    private static final ProductId BASIC_PRODUCT = new ProductId("basic_001");
    private static final ProductId PRO_PRODUCT = new ProductId("pro_001");
    private static final ProductId ULTRA_PRODUCT = new ProductId("ultra_001");

    @Nested
    @DisplayName("create 정적 팩토리 메서드는")
    class Create {

        @Test
        @DisplayName("유효한 파라미터로 구독을 생성한다")
        void create_withValidParameters() {
            // given
            Instant now = Instant.now();
            Period period = new Period(now, now.plus(30, ChronoUnit.DAYS));

            // when
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, period);

            // then
            assertThat(subscription.getOriginalTransactionId()).isEqualTo(ORIGINAL_TX_ID);
            assertThat(subscription.getUserId()).isEqualTo(USER_ID);
            assertThat(subscription.getProductId()).isEqualTo(BASIC_PRODUCT);
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            assertThat(subscription.getExpiresAt()).isEqualTo(period.getEndAt());
            assertThat(subscription.getCurrentPeriodStart()).isEqualTo(period.getStartAt());
            assertThat(subscription.getCurrentPeriodEnd()).isEqualTo(period.getEndAt());
            assertThat(subscription.getCreatedAt()).isNotNull();
            assertThat(subscription.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("null originalTransactionId로 생성 시 예외를 던진다")
        void create_withNullTransactionId_throwsException() {
            // given
            Period period = new Period(Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS));

            // when & then
            assertThatThrownBy(() -> Subscription.create(null, USER_ID, BASIC_PRODUCT, period))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("originalTransactionId must not be null");
        }

        @Test
        @DisplayName("null userId로 생성 시 예외를 던진다")
        void create_withNullUserId_throwsException() {
            // given
            Period period = new Period(Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS));

            // when & then
            assertThatThrownBy(() -> Subscription.create(ORIGINAL_TX_ID, null, BASIC_PRODUCT, period))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("userId must not be null");
        }

        @Test
        @DisplayName("null productId로 생성 시 예외를 던진다")
        void create_withNullProductId_throwsException() {
            // given
            Period period = new Period(Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS));

            // when & then
            assertThatThrownBy(() -> Subscription.create(ORIGINAL_TX_ID, USER_ID, null, period))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("productId must not be null");
        }

        @Test
        @DisplayName("null period로 생성 시 예외를 던진다")
        void create_withNullPeriod_throwsException() {
            // when & then
            assertThatThrownBy(() -> Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("period must not be null");
        }
    }

    @Nested
    @DisplayName("renew 메서드는")
    class Renew {

        @Test
        @DisplayName("ACTIVE 상태에서 갱신한다")
        void renew_fromActive() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);

            Instant newPeriodStart = now.plus(30, ChronoUnit.DAYS);
            Instant newPeriodEnd = newPeriodStart.plus(30, ChronoUnit.DAYS);
            Instant newExpiresAt = newPeriodEnd;

            // when
            subscription.renew(newExpiresAt, newPeriodStart, newPeriodEnd);

            // then
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            assertThat(subscription.getExpiresAt()).isEqualTo(newExpiresAt);
            assertThat(subscription.getCurrentPeriodStart()).isEqualTo(newPeriodStart);
            assertThat(subscription.getCurrentPeriodEnd()).isEqualTo(newPeriodEnd);
        }

        @Test
        @DisplayName("GRACE_PERIOD 상태에서 갱신 시 ACTIVE로 변경된다")
        void renew_fromGracePeriod_toActive() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);
            subscription.enterGracePeriod(now.plus(16, ChronoUnit.DAYS));

            Instant newPeriodStart = now.plus(16, ChronoUnit.DAYS);
            Instant newPeriodEnd = newPeriodStart.plus(30, ChronoUnit.DAYS);

            // when
            subscription.renew(newPeriodEnd, newPeriodStart, newPeriodEnd);

            // then
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        }

        @Test
        @DisplayName("EXPIRED 상태에서 갱신 시 예외를 던진다")
        void renew_fromExpired_throwsException() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);
            subscription.expire();

            // when & then
            assertThatThrownBy(() -> subscription.renew(now.plus(60, ChronoUnit.DAYS), now, now.plus(30, ChronoUnit.DAYS)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot renew from status");
        }

        @Test
        @DisplayName("만료일이 현재 기간 종료일보다 이전이면 예외를 던진다")
        void renew_withInvalidExpiresAt_throwsException() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);

            Instant newPeriodStart = now.plus(30, ChronoUnit.DAYS);
            Instant newPeriodEnd = newPeriodStart.plus(30, ChronoUnit.DAYS);
            // expiresAt이 currentPeriodEnd보다 이전
            Instant invalidExpiresAt = newPeriodStart.minus(1, ChronoUnit.DAYS);

            // when & then
            assertThatThrownBy(() -> subscription.renew(invalidExpiresAt, newPeriodStart, newPeriodEnd))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("expiresAt must be >= currentPeriodEnd");
        }
    }

    @Nested
    @DisplayName("expire 메서드는")
    class Expire {

        @Test
        @DisplayName("ACTIVE 상태에서 만료 처리한다")
        void expire_fromActive() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);

            // when
            subscription.expire();

            // then
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        }

        @Test
        @DisplayName("GRACE_PERIOD 상태에서 만료 처리한다")
        void expire_fromGracePeriod() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);
            subscription.enterGracePeriod(now.plus(16, ChronoUnit.DAYS));

            // when
            subscription.expire();

            // then
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        }

        @Test
        @DisplayName("BILLING_RETRY 상태에서 만료 처리한다")
        void expire_fromBillingRetry() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);
            subscription.enterGracePeriod(now.plus(16, ChronoUnit.DAYS));
            subscription.enterBillingRetry();

            // when
            subscription.expire();

            // then
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        }

        @Test
        @DisplayName("이미 EXPIRED 상태면 예외를 던진다")
        void expire_fromExpired_throwsException() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);
            subscription.expire();

            // when & then
            assertThatThrownBy(subscription::expire)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("refund 메서드는")
    class Refund {

        @Test
        @DisplayName("ACTIVE 상태에서 환불 처리한다")
        void refund_fromActive() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);

            // when
            subscription.refund();

            // then
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.REFUNDED);
        }

        @Test
        @DisplayName("EXPIRED 상태에서 환불 처리 시 예외를 던진다")
        void refund_fromExpired_throwsException() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);
            subscription.expire();

            // when & then
            assertThatThrownBy(subscription::refund)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("enterGracePeriod 메서드는")
    class EnterGracePeriod {

        @Test
        @DisplayName("ACTIVE 상태에서 유예 기간에 진입한다")
        void enterGracePeriod_fromActive() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);
            Instant gracePeriodEnd = now.plus(16, ChronoUnit.DAYS);

            // when
            subscription.enterGracePeriod(gracePeriodEnd);

            // then
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.GRACE_PERIOD);
        }

        @Test
        @DisplayName("EXPIRED 상태에서 진입 시 예외를 던진다")
        void enterGracePeriod_fromExpired_throwsException() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);
            subscription.expire();

            // when & then
            assertThatThrownBy(() -> subscription.enterGracePeriod(now.plus(16, ChronoUnit.DAYS)))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("enterBillingRetry 메서드는")
    class EnterBillingRetry {

        @Test
        @DisplayName("GRACE_PERIOD 상태에서 결제 재시도에 진입한다")
        void enterBillingRetry_fromGracePeriod() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);
            subscription.enterGracePeriod(now.plus(16, ChronoUnit.DAYS));

            // when
            subscription.enterBillingRetry();

            // then
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.BILLING_RETRY);
        }

        @Test
        @DisplayName("ACTIVE 상태에서 진입 시 예외를 던진다")
        void enterBillingRetry_fromActive_throwsException() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);

            // when & then
            assertThatThrownBy(subscription::enterBillingRetry)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("upgrade 메서드는")
    class Upgrade {

        @Test
        @DisplayName("BASIC에서 PRO로 업그레이드한다")
        void upgrade_basicToPro() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);

            // when
            subscription.upgrade(PRO_PRODUCT);

            // then
            assertThat(subscription.getProductId()).isEqualTo(PRO_PRODUCT);
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        }

        @Test
        @DisplayName("BASIC에서 ULTRA로 업그레이드한다")
        void upgrade_basicToUltra() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);

            // when
            subscription.upgrade(ULTRA_PRODUCT);

            // then
            assertThat(subscription.getProductId()).isEqualTo(ULTRA_PRODUCT);
        }

        @Test
        @DisplayName("동일 등급으로 업그레이드 시 예외를 던진다")
        void upgrade_sameTier_throwsException() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, PRO_PRODUCT, initialPeriod);

            // when & then
            assertThatThrownBy(() -> subscription.upgrade(PRO_PRODUCT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot upgrade to same or lower tier");
        }

        @Test
        @DisplayName("다운그레이드 시 예외를 던진다")
        void upgrade_downgrade_throwsException() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, PRO_PRODUCT, initialPeriod);

            // when & then
            assertThatThrownBy(() -> subscription.upgrade(BASIC_PRODUCT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot upgrade to same or lower tier");
        }

        @Test
        @DisplayName("EXPIRED 상태에서 업그레이드 시 예외를 던진다")
        void upgrade_fromExpired_throwsException() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);
            subscription.expire();

            // when & then
            assertThatThrownBy(() -> subscription.upgrade(PRO_PRODUCT))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot upgrade from status");
        }
    }

    @Nested
    @DisplayName("revoke 메서드는")
    class Revoke {

        @Test
        @DisplayName("ACTIVE 상태에서 가족 공유 취소 처리한다")
        void revoke_fromActive() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);

            // when
            subscription.revoke();

            // then
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.REVOKED);
        }

        @Test
        @DisplayName("EXPIRED 상태에서 취소 시 예외를 던진다")
        void revoke_fromExpired_throwsException() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);
            subscription.expire();

            // when & then
            assertThatThrownBy(subscription::revoke)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("isActive 메서드는")
    class IsActive {

        @Test
        @DisplayName("ACTIVE 상태면 true를 반환한다")
        void isActive_activeStatus_returnsTrue() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);

            // when & then
            assertThat(subscription.isActive()).isTrue();
        }

        @Test
        @DisplayName("EXPIRED 상태면 false를 반환한다")
        void isActive_expiredStatus_returnsFalse() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);
            subscription.expire();

            // when & then
            assertThat(subscription.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("isExpired 메서드는")
    class IsExpired {

        @Test
        @DisplayName("EXPIRED 상태면 true를 반환한다")
        void isExpired_expiredStatus_returnsTrue() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);
            subscription.expire();

            // when & then
            assertThat(subscription.isExpired()).isTrue();
        }

        @Test
        @DisplayName("ACTIVE 상태면 false를 반환한다")
        void isExpired_activeStatus_returnsFalse() {
            // given
            Instant now = Instant.now();
            Period initialPeriod = new Period(now, now.plus(30, ChronoUnit.DAYS));
            Subscription subscription = Subscription.create(ORIGINAL_TX_ID, USER_ID, BASIC_PRODUCT, initialPeriod);

            // when & then
            assertThat(subscription.isExpired()).isFalse();
        }
    }
}
