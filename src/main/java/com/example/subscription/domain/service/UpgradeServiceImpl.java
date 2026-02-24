package com.example.subscription.domain.service;

import com.example.subscription.domain.vo.Money;
import com.example.subscription.domain.vo.Period;
import com.example.subscription.domain.vo.ProductId;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * 구독 업그레이드 계산 도메인 서비스 구현체.
 *
 * <p>Apple IAP 업그레이드 정책에 따른 가격 및 기간 계산을 담당한다.
 */
@Service
public class UpgradeServiceImpl implements UpgradeService {

    /** 제품별 월 구독 가격 (USD) */
    private static final Map<String, BigDecimal> PRODUCT_PRICES = Map.of(
            "basic", BigDecimal.valueOf(9.99),
            "pro", BigDecimal.valueOf(19.99),
            "ultra", BigDecimal.valueOf(29.99));

    private static final Currency USD = Currency.getInstance("USD");
    private static final int SUBSCRIPTION_DAYS = 30;

    @Override
    public Money calculateUpgradePrice(
            ProductId currentProductId, ProductId newProductId, Instant currentPeriodEnd, Instant upgradeAt) {
        if (!canUpgrade(currentProductId, newProductId)) {
            throw new IllegalArgumentException("Cannot upgrade from " + currentProductId + " to " + newProductId);
        }

        // 현재 제품의 남은 기간 가치 계산
        long remainingDays = ChronoUnit.DAYS.between(upgradeAt, currentPeriodEnd);
        if (remainingDays < 0) {
            remainingDays = 0;
        }

        BigDecimal currentPrice = getPrice(currentProductId);
        BigDecimal newPrice = getPrice(newProductId);

        // 남은 기간 비율 계산
        BigDecimal remainingRatio = BigDecimal.valueOf(remainingDays)
                .divide(BigDecimal.valueOf(SUBSCRIPTION_DAYS), 4, RoundingMode.HALF_UP);

        // 현재 제품의 남은 가치
        BigDecimal remainingValue = currentPrice.multiply(remainingRatio);

        // 업그레이드 가격 = 새 제품 가격 - 현재 제품 남은 가치
        BigDecimal upgradePrice = newPrice.subtract(remainingValue);

        // 음수면 0으로 처리 (크레딧은 별도 처리)
        if (upgradePrice.compareTo(BigDecimal.ZERO) < 0) {
            upgradePrice = BigDecimal.ZERO;
        }

        return new Money(upgradePrice.setScale(2, RoundingMode.HALF_UP), USD);
    }

    @Override
    public Period calculateNewPeriod(ProductId newProductId, Instant upgradeAt) {
        Instant newEnd = upgradeAt.plus(SUBSCRIPTION_DAYS, ChronoUnit.DAYS);
        return new Period(upgradeAt, newEnd);
    }

    @Override
    public boolean canUpgrade(ProductId currentProductId, ProductId newProductId) {
        return newProductId.isHigherTierThan(currentProductId);
    }

    @Override
    public int compareTier(ProductId productId1, ProductId productId2) {
        int tier1 = getTierLevel(productId1);
        int tier2 = getTierLevel(productId2);
        return Integer.compare(tier1, tier2);
    }

    private int getTierLevel(ProductId productId) {
        String value = productId.getValue().toLowerCase();
        if (value.startsWith("ultra")) {
            return 3;
        } else if (value.startsWith("pro")) {
            return 2;
        } else if (value.startsWith("basic")) {
            return 1;
        }
        return 1; // 기본값
    }

    private BigDecimal getPrice(ProductId productId) {
        String tier = getTier(productId);
        return PRODUCT_PRICES.getOrDefault(tier, BigDecimal.valueOf(9.99));
    }

    private String getTier(ProductId productId) {
        String value = productId.getValue().toLowerCase();
        if (value.startsWith("ultra")) {
            return "ultra";
        } else if (value.startsWith("pro")) {
            return "pro";
        }
        return "basic";
    }
}
