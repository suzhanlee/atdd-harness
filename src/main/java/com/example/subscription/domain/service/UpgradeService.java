package com.example.subscription.domain.service;

import com.example.subscription.domain.vo.Money;
import com.example.subscription.domain.vo.Period;
import com.example.subscription.domain.vo.ProductId;
import java.time.Instant;

/**
 * 구독 업그레이드 계산 도메인 서비스.
 *
 * <p>업그레이드 시 발생하는 비용 계산 및 새 만료일 계산을 담당한다. 여러 Aggregate에 걸친 계산 로직을 캡슐화한다.
 *
 * <p>Apple의 업그레이드 정책에 따라:
 *
 * <ul>
 *   <li>Proration: 기존 구독의 남은 기간에 대한 비용을 새 구독에 적용
 *   <li>Time Extension: 업그레이드 시점부터 새 만료일 계산
 * </ul>
 */
public interface UpgradeService {

    /**
     * 업그레이드 시 추가 결제 금액을 계산한다.
     *
     * <p>Proration 방식: (새 상품 가격 - 기존 상품 잔여 가치)
     *
     * @param currentProductId 현재 제품 ID
     * @param newProductId 새 제품 ID
     * @param currentPeriodEnd 현재 구독 기간 종료일
     * @param upgradeAt 업그레이드 시점
     * @return 추가 결제 금액 (음수면 크레딧 발생)
     */
    Money calculateUpgradePrice(
            ProductId currentProductId, ProductId newProductId, Instant currentPeriodEnd, Instant upgradeAt);

    /**
     * 업그레이드 후 새 구독 기간을 계산한다.
     *
     * <p>Apple 정책에 따라 새 기간 계산 (보통 업그레이드 시점 + 새 상품 구독 기간)
     *
     * @param newProductId 새 제품 ID
     * @param upgradeAt 업그레이드 시점
     * @return 새 구독 기간
     */
    Period calculateNewPeriod(ProductId newProductId, Instant upgradeAt);

    /**
     * 업그레이드 가능 여부를 확인한다.
     *
     * @param currentProductId 현재 제품 ID
     * @param newProductId 새 제품 ID
     * @return 업그레이드 가능하면 true
     */
    boolean canUpgrade(ProductId currentProductId, ProductId newProductId);

    /**
     * 제품 등급을 비교한다.
     *
     * @param productId1 첫 번째 제품 ID
     * @param productId2 두 번째 제품 ID
     * @return 양수면 productId1이 높은 등급, 음수면 productId2가 높은 등급, 0이면 동일
     */
    int compareTier(ProductId productId1, ProductId productId2);
}
