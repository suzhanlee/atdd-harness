package com.example.unit.domain.vo;

import com.example.subscription.domain.vo.WebhookEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * WebhookEvent VO 단위 테스트.
 */
@DisplayName("WebhookEvent VO 테스트")
class WebhookEventTest {

    @Nested
    @DisplayName("isSandbox 메서드")
    class IsSandbox {

        @Test
        @DisplayName("SANDBOX 환경이면 true를 반환한다")
        void returnsTrueForSandboxEnvironment() {
            // given
            WebhookEvent event = createWebhookEvent("SANDBOX");

            // when
            boolean result = event.isSandbox();

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("PRODUCTION 환경이면 false를 반환한다")
        void returnsFalseForProductionEnvironment() {
            // given
            WebhookEvent event = createWebhookEvent("PRODUCTION");

            // when
            boolean result = event.isSandbox();

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("환경이 null이면 false를 반환한다")
        void returnsFalseForNullEnvironment() {
            // given
            WebhookEvent event = new WebhookEvent(
                    "DID_RENEW", "tx123", "orig123", "prod1",
                    Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS), null);

            // when
            boolean result = event.isSandbox();

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("isRecent 메서드")
    class IsRecent {

        @Test
        @DisplayName("signedDate가 허용 시간 내이면 true를 반환한다")
        void returnsTrueWhenSignedDateIsRecent() {
            // given
            Instant recentSignedDate = Instant.now().minus(3, ChronoUnit.MINUTES);
            WebhookEvent event = createWebhookEventWithSignedDate(recentSignedDate);

            // when
            boolean result = event.isRecent(5);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("signedDate가 허용 시간을 초과하면 false를 반환한다")
        void returnsFalseWhenSignedDateIsTooOld() {
            // given
            Instant oldSignedDate = Instant.now().minus(10, ChronoUnit.MINUTES);
            WebhookEvent event = createWebhookEventWithSignedDate(oldSignedDate);

            // when
            boolean result = event.isRecent(5);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("signedDate가 null이면 false를 반환한다")
        void returnsFalseWhenSignedDateIsNull() {
            // given
            WebhookEvent event = new WebhookEvent(
                    "DID_RENEW", "tx123", "orig123", "prod1",
                    null, Instant.now().plus(30, ChronoUnit.DAYS), "PRODUCTION");

            // when
            boolean result = event.isRecent(5);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("signedDate가 현재 시간이면 true를 반환한다")
        void returnsTrueWhenSignedDateIsNow() {
            // given
            WebhookEvent event = createWebhookEventWithSignedDate(Instant.now());

            // when
            boolean result = event.isRecent(5);

            // then
            assertThat(result).isTrue();
        }
    }

    // Helper methods

    private WebhookEvent createWebhookEvent(String environment) {
        return new WebhookEvent(
                "DID_RENEW",
                "tx123",
                "orig123",
                "prod1",
                Instant.now(),
                Instant.now().plus(30, ChronoUnit.DAYS),
                environment);
    }

    private WebhookEvent createWebhookEventWithSignedDate(Instant signedDate) {
        return new WebhookEvent(
                "DID_RENEW",
                "tx123",
                "orig123",
                "prod1",
                signedDate,
                Instant.now().plus(30, ChronoUnit.DAYS),
                "PRODUCTION");
    }
}
