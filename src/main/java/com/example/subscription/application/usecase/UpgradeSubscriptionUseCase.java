package com.example.subscription.application.usecase;

import com.example.subscription.domain.entity.Subscription;
import com.example.subscription.domain.vo.Money;

/**
 * 구독 업그레이드 Use Case.
 *
 * <p>기존 구독을 상위 등급으로 업그레이드한다. 추가 결제 금액 계산 및 새 구독 기간 설정을 포함한다.
 *
 * <p>처리 흐름:
 *
 * <ol>
 *   <li>현재 구독 상태 확인
 *   <li>업그레이드 가능 여부 검증
 *   <li>추가 결제 금액 계산
 *   <li>Apple In-App Purchase 처리
 *   <li>구독 정보 업데이트
 *   <li>업그레이드 이벤트 발행
 * </ol>
 *
 * @see com.example.subscription.domain.service.UpgradeService
 */
public interface UpgradeSubscriptionUseCase {

    /**
     * 구독 업그레이드 견적을 계산한다.
     *
     * <p>실제 업그레이드 전 사용자에게 보여줄 견적 정보
     *
     * @param subscriptionId 구독 ID
     * @param newProductId 새 제품 ID
     * @return 업그레이드 견적
     */
    UpgradeQuote getQuote(Long subscriptionId, String newProductId);

    /**
     * 구독을 업그레이드한다.
     *
     * @param command 업그레이드 명령
     * @return 업그레이드된 구독
     * @throws IllegalArgumentException 잘못된 요청인 경우
     * @throws IllegalStateException 업그레이드 불가능한 상태인 경우
     */
    Subscription upgrade(UpgradeCommand command);

    /** 업그레이드 견적 */
    record UpgradeQuote(
            Long subscriptionId,
            String currentProductId,
            String newProductId,
            Money additionalPrice,
            String newExpiresAt,
            boolean upgradePossible,
            String impossibleReason) {}

    /** 업그레이드 명령 */
    record UpgradeCommand(Long subscriptionId, String newProductId, String transactionId, Long userId) {}
}
