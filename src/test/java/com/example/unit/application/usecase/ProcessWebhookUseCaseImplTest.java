package com.example.unit.application.usecase;

import com.example.subscription.application.usecase.ProcessWebhookUseCase;
import com.example.subscription.domain.entity.Subscription;
import com.example.subscription.domain.entity.Transaction;
import com.example.subscription.domain.repository.SubscriptionRepository;
import com.example.subscription.domain.repository.TransactionRepository;
import com.example.subscription.domain.vo.Period;
import com.example.subscription.domain.vo.ProductId;
import com.example.subscription.domain.vo.SubscriptionStatus;
import com.example.subscription.domain.vo.WebhookEvent;
import com.example.subscription.infrastructure.parser.AppleWebhookParser;
import com.example.subscription.infrastructure.parser.JwsVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * ProcessWebhookUseCase 구현체 단위 테스트.
 */
@DisplayName("ProcessWebhookUseCase 테스트")
@ExtendWith(MockitoExtension.class)
class ProcessWebhookUseCaseImplTest {

    @Mock
    private JwsVerifier jwsVerifier;

    @Mock
    private AppleWebhookParser webhookParser;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private ProcessWebhookUseCase processWebhookUseCase;

    @BeforeEach
    void setUp() {
        processWebhookUseCase = new com.example.subscription.application.usecase.impl.ProcessWebhookUseCaseImpl(
                jwsVerifier, webhookParser, subscriptionRepository, transactionRepository);
    }

    @Nested
    @DisplayName("process 메서드")
    class Process {

        @Test
        @DisplayName("신규 구독 SUBSCRIBED 이벤트 처리")
        void processNewSubscriptionSubscribedEvent() {
            // given
            String signedPayload = "valid_jws_payload";
            String verifiedJson = "{\"notificationType\":\"SUBSCRIBED\"}";

            WebhookEvent event = createWebhookEvent("SUBSCRIBED", "tx-new-123", "orig-new-123");

            when(jwsVerifier.verify(signedPayload)).thenReturn(verifiedJson);
            when(webhookParser.parseVerifiedJson(verifiedJson)).thenReturn(event);
            when(subscriptionRepository.findByOriginalTransactionId("orig-new-123"))
                    .thenReturn(Optional.empty());
            when(subscriptionRepository.save(any(Subscription.class)))
                    .thenAnswer(inv -> {
                        Subscription saved = inv.getArgument(0);
                        setSubscriptionId(saved, 1L);
                        return saved;
                    });
            when(transactionRepository.existsByTransactionId("tx-new-123")).thenReturn(false);
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // when
            WebhookEvent result = processWebhookUseCase.process(signedPayload);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getNotificationType()).isEqualTo("SUBSCRIBED");
            verify(subscriptionRepository).save(any(Subscription.class));
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("기존 구독 DID_RENEW 이벤트 처리")
        void processExistingSubscriptionRenewEvent() {
            // given
            String signedPayload = "valid_jws_payload";
            String verifiedJson = "{\"notificationType\":\"DID_RENEW\"}";

            WebhookEvent event = new WebhookEvent(
                    "DID_RENEW", "tx-renew-123", "orig-123", "basic_001",
                    Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS), "PRODUCTION");

            Subscription existingSubscription = Subscription.create(
                    "orig-123", 1L, new ProductId("basic_001"),
                    new Period(Instant.now().minus(30, ChronoUnit.DAYS), Instant.now()));
            setSubscriptionId(existingSubscription, 1L);

            when(jwsVerifier.verify(signedPayload)).thenReturn(verifiedJson);
            when(webhookParser.parseVerifiedJson(verifiedJson)).thenReturn(event);
            when(subscriptionRepository.findByOriginalTransactionId("orig-123"))
                    .thenReturn(Optional.of(existingSubscription));
            when(transactionRepository.existsByTransactionId("tx-renew-123")).thenReturn(false);
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // when
            WebhookEvent result = processWebhookUseCase.process(signedPayload);

            // then
            assertThat(result).isNotNull();
            assertThat(existingSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("EXPIRED 이벤트 처리")
        void processExpiredEvent() {
            // given
            String signedPayload = "valid_jws_payload";
            String verifiedJson = "{\"notificationType\":\"EXPIRED\"}";

            WebhookEvent event = createWebhookEvent("EXPIRED", "tx-exp-123", "orig-123");

            Subscription existingSubscription = Subscription.create(
                    "orig-123", 1L, new ProductId("basic_001"),
                    new Period(Instant.now().minus(30, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS)));
            setSubscriptionId(existingSubscription, 1L);

            when(jwsVerifier.verify(signedPayload)).thenReturn(verifiedJson);
            when(webhookParser.parseVerifiedJson(verifiedJson)).thenReturn(event);
            when(subscriptionRepository.findByOriginalTransactionId("orig-123"))
                    .thenReturn(Optional.of(existingSubscription));
            when(transactionRepository.existsByTransactionId("tx-exp-123")).thenReturn(false);
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // when
            WebhookEvent result = processWebhookUseCase.process(signedPayload);

            // then
            assertThat(result).isNotNull();
            assertThat(existingSubscription.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        }

        @Test
        @DisplayName("REFUND 이벤트 처리")
        void processRefundEvent() {
            // given
            String signedPayload = "valid_jws_payload";
            String verifiedJson = "{\"notificationType\":\"REFUND\"}";

            WebhookEvent event = createWebhookEvent("REFUND", "tx-refund-123", "orig-123");

            Subscription existingSubscription = Subscription.create(
                    "orig-123", 1L, new ProductId("basic_001"),
                    new Period(Instant.now().minus(30, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS)));
            setSubscriptionId(existingSubscription, 1L);

            when(jwsVerifier.verify(signedPayload)).thenReturn(verifiedJson);
            when(webhookParser.parseVerifiedJson(verifiedJson)).thenReturn(event);
            when(subscriptionRepository.findByOriginalTransactionId("orig-123"))
                    .thenReturn(Optional.of(existingSubscription));
            when(transactionRepository.existsByTransactionId("tx-refund-123")).thenReturn(false);
            when(transactionRepository.save(any(Transaction.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // when
            WebhookEvent result = processWebhookUseCase.process(signedPayload);

            // then
            assertThat(result).isNotNull();
            assertThat(existingSubscription.getStatus()).isEqualTo(SubscriptionStatus.REFUNDED);
        }

        @Test
        @DisplayName("JWS 서명 검증 실패 시 SecurityException 발생")
        void throwSecurityExceptionWhenJwsVerificationFails() {
            // given
            String signedPayload = "invalid_jws_payload";
            when(jwsVerifier.verify(signedPayload)).thenThrow(new SecurityException("Invalid signature"));

            // when & then
            assertThatThrownBy(() -> processWebhookUseCase.process(signedPayload))
                    .isInstanceOf(SecurityException.class)
                    .hasMessage("Invalid signature");

            verify(webhookParser, never()).parseVerifiedJson(anyString());
            verify(subscriptionRepository, never()).save(any());
        }

        @Test
        @DisplayName("동일 transactionId 중복 처리 시 멱등성 보장")
        void ensureIdempotencyForDuplicateTransaction() {
            // given
            String signedPayload = "valid_jws_payload";
            String verifiedJson = "{\"notificationType\":\"DID_RENEW\"}";

            WebhookEvent event = createWebhookEvent("DID_RENEW", "tx-dup-123", "orig-123");

            when(jwsVerifier.verify(signedPayload)).thenReturn(verifiedJson);
            when(webhookParser.parseVerifiedJson(verifiedJson)).thenReturn(event);
            // 중복 트랜잭션이 감지되면 findByOriginalTransactionId는 호출되지 않음
            when(transactionRepository.existsByTransactionId("tx-dup-123")).thenReturn(true);

            // when
            WebhookEvent result = processWebhookUseCase.process(signedPayload);

            // then
            assertThat(result).isNotNull();
            verify(transactionRepository, never()).save(any(Transaction.class));
        }

        @Test
        @DisplayName("지원하지 않는 이벤트 타입은 무시하고 로그만 기록")
        void ignoreUnsupportedEventType() {
            // given
            String signedPayload = "valid_jws_payload";
            String verifiedJson = "{\"notificationType\":\"UNKNOWN_TYPE\"}";

            WebhookEvent event = createWebhookEvent("UNKNOWN_TYPE", "tx-unknown-123", "orig-123");

            when(jwsVerifier.verify(signedPayload)).thenReturn(verifiedJson);
            when(webhookParser.parseVerifiedJson(verifiedJson)).thenReturn(event);

            // when
            WebhookEvent result = processWebhookUseCase.process(signedPayload);

            // then
            assertThat(result).isNotNull();
            verify(subscriptionRepository, never()).save(any());
            verify(transactionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("verifyOnly 메서드")
    class VerifyOnly {

        @Test
        @DisplayName("서명 검증만 수행하고 페이로드 반환")
        void verifyOnlyReturnsPayload() {
            // given
            String signedPayload = "valid_jws_payload";
            String expectedPayload = "{\"verified\":true}";

            when(jwsVerifier.verify(signedPayload)).thenReturn(expectedPayload);

            // when
            String result = processWebhookUseCase.verifyOnly(signedPayload);

            // then
            assertThat(result).isEqualTo(expectedPayload);
            verify(webhookParser, never()).parseVerifiedJson(anyString());
            verify(subscriptionRepository, never()).save(any());
        }

        @Test
        @DisplayName("서명 검증 실패 시 SecurityException 발생")
        void throwSecurityExceptionOnVerifyOnlyFailure() {
            // given
            String signedPayload = "invalid_jws_payload";
            when(jwsVerifier.verify(signedPayload)).thenThrow(new SecurityException("Invalid signature"));

            // when & then
            assertThatThrownBy(() -> processWebhookUseCase.verifyOnly(signedPayload))
                    .isInstanceOf(SecurityException.class)
                    .hasMessage("Invalid signature");
        }
    }

    // Helper methods

    private WebhookEvent createWebhookEvent(String notificationType, String transactionId, String originalTransactionId) {
        return new WebhookEvent(
                notificationType,
                transactionId,
                originalTransactionId,
                "basic_001",
                Instant.now(),
                Instant.now().plus(30, ChronoUnit.DAYS),
                "PRODUCTION");
    }

    private void setSubscriptionId(Subscription subscription, Long id) {
        try {
            Field idField = Subscription.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(subscription, id);
        } catch (Exception e) {
            // ignore for test setup
        }
    }
}
