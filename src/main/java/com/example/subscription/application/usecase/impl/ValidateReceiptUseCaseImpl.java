package com.example.subscription.application.usecase.impl;

import com.example.subscription.application.usecase.ValidateReceiptUseCase;
import com.example.subscription.domain.entity.Subscription;
import com.example.subscription.domain.repository.SubscriptionRepository;
import com.example.subscription.domain.vo.Period;
import com.example.subscription.domain.vo.ProductId;
import com.example.subscription.domain.vo.SubscriptionStatus;
import com.example.subscription.infrastructure.client.AppleAppStoreClient;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 영수증 검증 Use Case 구현체.
 *
 * <p>Apple App Store Server API를 통해 영수증을 검증한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ValidateReceiptUseCaseImpl implements ValidateReceiptUseCase {

    private final AppleAppStoreClient appleClient;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public Optional<Subscription> validate(String receiptData, Long userId) {
        ValidationResult result = validateWithDetails(receiptData);

        if (!result.valid()) {
            return Optional.empty();
        }

        // 기존 구독 확인
        Optional<Subscription> existing = subscriptionRepository.findByOriginalTransactionId(result.originalTransactionId());

        if (existing.isPresent()) {
            // 기존 구독이 있으면 업데이트
            Subscription subscription = existing.get();
            if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
                return Optional.of(subscription);
            }
            return Optional.empty();
        }

        // 신규 구독 생성
        Period period = new Period(
                Instant.now(),
                Instant.parse(result.expiresDate()));

        Subscription subscription = Subscription.create(
                result.originalTransactionId(),
                userId,
                new ProductId(result.productId()),
                period);

        return Optional.of(subscriptionRepository.save(subscription));
    }

    @Override
    public ValidationResult validateWithDetails(String receiptData) {
        if (receiptData == null || receiptData.isBlank()) {
            return ValidationResult.invalid("INVALID_RECEIPT", "영수증 데이터가 필요합니다");
        }

        // Base64 검증
        try {
            Base64.getDecoder().decode(receiptData);
        } catch (IllegalArgumentException e) {
            return ValidationResult.invalid("INVALID_FORMAT", "잘못된 영수증 형식");
        }

        // Apple API 호출
        try {
            AppleAppStoreClient.ReceiptVerificationResult response = appleClient.verifyReceipt(receiptData);

            if (!response.isValid()) {
                return ValidationResult.invalid(
                        response.errorCode() != null ? response.errorCode() : "VERIFICATION_FAILED",
                        response.errorMessage() != null ? response.errorMessage() : "구매 확인 실패, 고객센터 문의");
            }

            String expiresDate = response.expiresDateMs() != null
                    ? Instant.ofEpochMilli(response.expiresDateMs()).toString()
                    : Instant.now().plus(30, ChronoUnit.DAYS).toString();

            return ValidationResult.valid(
                    response.originalTransactionId(),
                    response.productId(),
                    null, // userId는 별도 설정
                    expiresDate);

        } catch (Exception e) {
            log.error("Apple receipt validation failed", e);
            return ValidationResult.invalid("SERVICE_UNAVAILABLE", "일시적 오류, 잠시 후 재시도");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Subscription> findById(Long id) {
        return subscriptionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<Subscription> findByUserId(Long userId) {
        return subscriptionRepository.findByUserId(userId);
    }
}
