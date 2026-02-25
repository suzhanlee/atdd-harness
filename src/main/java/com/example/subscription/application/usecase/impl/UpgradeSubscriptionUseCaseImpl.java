package com.example.subscription.application.usecase.impl;

import com.example.subscription.application.usecase.UpgradeSubscriptionUseCase;
import com.example.subscription.domain.entity.Subscription;
import com.example.subscription.domain.entity.Transaction;
import com.example.subscription.domain.repository.SubscriptionRepository;
import com.example.subscription.domain.repository.TransactionRepository;
import com.example.subscription.domain.service.UpgradeService;
import com.example.subscription.domain.vo.Money;
import com.example.subscription.domain.vo.Period;
import com.example.subscription.domain.vo.ProductId;
import com.example.subscription.domain.vo.SubscriptionStatus;
import java.time.Instant;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 구독 업그레이드 Use Case 구현체.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpgradeSubscriptionUseCaseImpl implements UpgradeSubscriptionUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final TransactionRepository transactionRepository;
    private final UpgradeService upgradeService;

    @Override
    public UpgradeQuote getQuote(Long subscriptionId, String newProductId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new NoSuchElementException("구독을 찾을 수 없습니다: " + subscriptionId));

        ProductId newProduct = new ProductId(newProductId);
        boolean canUpgrade = upgradeService.canUpgrade(subscription.getProductId(), newProduct);

        if (!canUpgrade) {
            return new UpgradeQuote(
                    subscriptionId,
                    subscription.getProductId().getValue(),
                    newProductId,
                    Money.zero("USD"),
                    null,
                    false,
                    "상위 등급으로만 업그레이드할 수 있습니다");
        }

        Instant upgradeAt = Instant.now();
        Money additionalPrice = upgradeService.calculateUpgradePrice(
                subscription.getProductId(),
                newProduct,
                subscription.getCurrentPeriodEnd(),
                upgradeAt);
        Period newPeriod = upgradeService.calculateNewPeriod(newProduct, upgradeAt);

        return new UpgradeQuote(
                subscriptionId,
                subscription.getProductId().getValue(),
                newProductId,
                additionalPrice,
                newPeriod.getEndAt().toString(),
                true,
                null);
    }

    @Override
    @Transactional
    public Subscription upgrade(UpgradeCommand command) {
        Subscription subscription = subscriptionRepository.findById(command.subscriptionId())
                .orElseThrow(() -> new NoSuchElementException("구독을 찾을 수 없습니다: " + command.subscriptionId()));

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("활성 구독만 업그레이드할 수 있습니다");
        }

        ProductId newProductId = new ProductId(command.newProductId());
        Instant upgradeAt = Instant.now();

        // 업그레이드 수행
        subscription.upgrade(newProductId);

        // 트랜잭션 기록
        Transaction transaction = Transaction.create(
                subscription.getId(),
                command.transactionId(),
                Transaction.Type.UPSELL,
                upgradeService.calculateUpgradePrice(
                        subscription.getProductId(),
                        newProductId,
                        subscription.getCurrentPeriodEnd(),
                        upgradeAt),
                upgradeAt);

        transactionRepository.save(transaction);

        log.info("Subscription upgraded: subscriptionId={}, newProductId={}",
                subscription.getId(), command.newProductId());

        return subscription;
    }
}
