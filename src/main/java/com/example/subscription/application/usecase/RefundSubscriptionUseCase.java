package com.example.subscription.application.usecase;

import com.example.subscription.domain.entity.Refund;
import com.example.subscription.domain.entity.Subscription;
import com.example.subscription.domain.vo.Money;

/**
 * 환불 처리 Use Case.
 *
 * <p>Apple에서 발생한 환불 이벤트를 처리한다. 환불 금액 계산, 구독 상태 변경, 환불 이력 기록을 수행한다.
 *
 * <p>처리 흐름:
 *
 * <ol>
 *   <li>환불 이벤트 수신 (Webhook)
 *   <li>구독 정보 조회
 *   <li>환불 금액 계산
 *   <li>구독 상태를 REFUNDED로 변경
 *   <li>환불 정보 저장
 *   <li>환불 이벤트 발행
 * </ol>
 *
 * @see com.example.subscription.domain.service.RefundCalculationService
 * @see com.example.subscription.domain.policy.RefundPolicy
 */
public interface RefundSubscriptionUseCase {

    /**
     * 환불을 처리한다.
     *
     * <p>Apple Webhook REFUND 이벤트 수신 시 호출
     *
     * @param command 환불 처리 명령
     * @return 환불 정보
     * @throws IllegalArgumentException 구독을 찾을 수 없는 경우
     * @throws IllegalStateException 이미 환불된 구독인 경우
     */
    Refund processRefund(RefundCommand command);

    /**
     * 환불 예상 금액을 계산한다.
     *
     * <p>사용자 요청에 의한 환불 검토 시 사용
     *
     * @param subscriptionId 구독 ID
     * @return 환불 예상 금액
     */
    RefundEstimate estimateRefund(Long subscriptionId);

    /**
     * 환불 가능 여부를 확인한다.
     *
     * @param subscriptionId 구독 ID
     * @return 환불 가능 여부와 사유
     */
    RefundEligibility checkEligibility(Long subscriptionId);

    /** 환불 처리 명령 */
    record RefundCommand(
            String originalTransactionId,
            String transactionId,
            String reasonCode,
            Money refundAmount,
            String appleReasonCode) {}

    /** 환불 예상 금액 */
    record RefundEstimate(Long subscriptionId, Money estimatedAmount, String expiresAt, boolean eligible) {}

    /** 환불 가능 여부 */
    record RefundEligibility(Long subscriptionId, boolean eligible, String reason, int daysRemaining) {}
}
