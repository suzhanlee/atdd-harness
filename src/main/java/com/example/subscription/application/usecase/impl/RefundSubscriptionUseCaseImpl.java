package com.example.subscription.application.usecase.impl;

import com.example.subscription.application.usecase.RefundSubscriptionUseCase;
import com.example.subscription.domain.entity.Refund;
import com.example.subscription.domain.entity.Subscription;
import com.example.subscription.domain.policy.RefundPolicy;
import com.example.subscription.domain.repository.RefundRepository;
import com.example.subscription.domain.repository.SubscriptionRepository;
import com.example.subscription.domain.service.RefundCalculationService;
import com.example.subscription.domain.vo.Money;
import com.example.subscription.domain.vo.Period;
import com.example.subscription.domain.vo.SubscriptionStatus;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 환불 처리 Use Case 구현체.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundSubscriptionUseCaseImpl implements RefundSubscriptionUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final RefundRepository refundRepository;
    private final RefundCalculationService refundCalculationService;
    private final RefundPolicy refundPolicy;

    @Override
    @Transactional
    public Refund processRefund(RefundCommand command) {
        Subscription subscription = subscriptionRepository
                .findByOriginalTransactionId(command.originalTransactionId())
                .orElseThrow(() -> new NoSuchElementException("구독을 찾을 수 없습니다: " + command.originalTransactionId()));

        if (subscription.getStatus() == SubscriptionStatus.REFUNDED) {
            throw new IllegalStateException("이미 환불된 구독입니다");
        }

        Instant refundAt = Instant.now();

        // 구독 상태 변경
        subscription.refund();

        // 환불 정보 저장
        Refund refund = Refund.create(
                subscription.getId(),
                command.originalTransactionId(),
                command.refundAmount(),
                Refund.Reason.USER_REQUEST,
                command.appleReasonCode(),
                refundAt);

        Refund saved = refundRepository.save(refund);

        log.info("Refund processed: subscriptionId={}, amount={}",
                subscription.getId(), command.refundAmount());

        return saved;
    }

    @Override
    public RefundEstimate estimateRefund(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new NoSuchElementException("구독을 찾을 수 없습니다: " + subscriptionId));

        Instant refundAt = Instant.now();
        boolean eligible = refundCalculationService.canRefund(subscriptionId, refundAt);

        // 기본 환불 금액 (실제로는 결제 내역에서 조회해야 함)
        Period period = new Period(subscription.getCurrentPeriodStart(), subscription.getCurrentPeriodEnd());
        Money estimatedAmount = refundCalculationService.calculateRefundAmount(
                new Money(java.math.BigDecimal.valueOf(9.99), java.util.Currency.getInstance("USD")),
                period,
                refundAt);

        int daysRemaining = (int) Instant.now()
                .until(subscription.getExpiresAt(), ChronoUnit.DAYS);

        return new RefundEstimate(
                subscriptionId,
                estimatedAmount,
                subscription.getExpiresAt().toString(),
                eligible);
    }

    @Override
    public RefundEligibility checkEligibility(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new NoSuchElementException("구독을 찾을 수 없습니다: " + subscriptionId));

        Instant refundAt = Instant.now();
        boolean eligible = refundCalculationService.canRefund(subscriptionId, refundAt);
        String reason = eligible ? "환불 가능" : "환불 기간 만료";

        int daysRemaining = (int) Instant.now()
                .until(subscription.getExpiresAt(), ChronoUnit.DAYS);

        return new RefundEligibility(subscriptionId, eligible, reason, Math.max(0, daysRemaining));
    }
}
