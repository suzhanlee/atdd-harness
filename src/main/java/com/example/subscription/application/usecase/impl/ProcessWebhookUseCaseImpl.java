package com.example.subscription.application.usecase.impl;

import com.example.subscription.application.usecase.ProcessWebhookUseCase;
import com.example.subscription.domain.entity.Subscription;
import com.example.subscription.domain.entity.Transaction;
import com.example.subscription.domain.repository.SubscriptionRepository;
import com.example.subscription.domain.repository.TransactionRepository;
import com.example.subscription.domain.spec.SubscriptionStatusSpec;
import com.example.subscription.domain.vo.Period;
import com.example.subscription.domain.vo.ProductId;
import com.example.subscription.domain.vo.SubscriptionStatus;
import com.example.subscription.domain.vo.WebhookEvent;
import com.example.subscription.infrastructure.parser.AppleWebhookParser;
import com.example.subscription.infrastructure.parser.JwsVerifier;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Webhook 처리 Use Case 구현체.
 *
 * <p>Apple App Store Server Notifications V2 Webhook을 처리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessWebhookUseCaseImpl implements ProcessWebhookUseCase {

    private final JwsVerifier jwsVerifier;
    private final AppleWebhookParser webhookParser;
    private final SubscriptionRepository subscriptionRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public WebhookEvent process(String signedPayload) {
        // 1. JWS 서명 검증
        String verifiedJson = jwsVerifier.verify(signedPayload);

        // 2. Webhook 이벤트 파싱
        WebhookEvent event = webhookParser.parseVerifiedJson(verifiedJson);

        // 3. 이벤트 타입에 따른 목표 상태 결정
        SubscriptionStatus targetStatus = SubscriptionStatusSpec.getTargetStatus(event.getNotificationType());

        if (targetStatus == null) {
            log.info("Unsupported webhook event type: {}", event.getNotificationType());
            return event;
        }

        // 4. 멱등성 검사
        if (transactionRepository.existsByTransactionId(event.getTransactionId())) {
            log.info("Duplicate transaction detected: {}", event.getTransactionId());
            return event;
        }

        // 5. 구독 조회 또는 생성
        Optional<Subscription> existingSubscription =
                subscriptionRepository.findByOriginalTransactionId(event.getOriginalTransactionId());

        Subscription subscription;
        if (existingSubscription.isPresent()) {
            subscription = existingSubscription.get();
            updateSubscriptionStatus(subscription, targetStatus, event);
        } else {
            // 신규 구독 생성
            subscription = createNewSubscription(event);
            subscription = subscriptionRepository.save(subscription);
        }

        // 6. 트랜잭션 기록 (subscriptionId는 null이 아님)
        if (subscription.getId() != null) {
            Transaction transaction = Transaction.create(
                    subscription.getId(),
                    event.getTransactionId(),
                    determineTransactionType(event.getNotificationType()),
                    null, // amount는 Webhook에서 제공되지 않을 수 있음
                    Instant.now());
            transactionRepository.save(transaction);
        }

        log.info("Webhook processed successfully: type={}, txId={}, subscriptionId={}",
                event.getNotificationType(), event.getTransactionId(), subscription.getId());

        return event;
    }

    @Override
    public String verifyOnly(String signedPayload) {
        return jwsVerifier.verify(signedPayload);
    }

    private Subscription createNewSubscription(WebhookEvent event) {
        Period period = new Period(
                event.getSignedDate() != null ? event.getSignedDate() : Instant.now(),
                event.getExpiresDate() != null ? event.getExpiresDate() : Instant.now().plusSeconds(2592000)); // 기본 30일

        // Webhook에서 userId를 직접 얻을 수 없으므로 기본값 사용
        // 실제 운영에서는 Webhook 페이로드에서 사용자 정보를 추출하거나
        // 별도의 사용자 매핑 로직이 필요함
        Long defaultUserId = 1L;

        return Subscription.create(
                event.getOriginalTransactionId(),
                defaultUserId,
                new ProductId(event.getProductId()),
                period);
    }

    private void updateSubscriptionStatus(Subscription subscription, SubscriptionStatus targetStatus, WebhookEvent event) {
        switch (targetStatus) {
            case ACTIVE:
                // 갱신 또는 복구
                if (event.getExpiresDate() != null) {
                    Instant newPeriodStart = subscription.getCurrentPeriodEnd();
                    subscription.renew(
                            event.getExpiresDate(),
                            newPeriodStart,
                            event.getExpiresDate());
                }
                break;
            case EXPIRED:
                subscription.expire();
                break;
            case REFUNDED:
                subscription.refund();
                break;
            case GRACE_PERIOD:
                subscription.enterGracePeriod(event.getExpiresDate());
                break;
            case REVOKED:
                subscription.revoke();
                break;
            default:
                log.warn("Unhandled target status: {}", targetStatus);
        }
    }

    private Transaction.Type determineTransactionType(String notificationType) {
        return switch (notificationType.toUpperCase()) {
            case "SUBSCRIBED" -> Transaction.Type.PURCHASE;
            case "DID_RENEW", "BILLING_RECOVERY" -> Transaction.Type.RENEWAL;
            case "REFUND" -> Transaction.Type.REFUND;
            case "DID_CHANGE_RENEWAL_STATUS", "PRICE_CHANGE" -> Transaction.Type.UPSELL;
            default -> Transaction.Type.PURCHASE;
        };
    }
}
