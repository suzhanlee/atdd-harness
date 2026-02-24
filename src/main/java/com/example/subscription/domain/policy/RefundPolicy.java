package com.example.subscription.domain.policy;

/**
 * 환불 정책 인터페이스.
 *
 * <p>환불 처리와 관련된 정책을 정의한다. Apple 정책과 서비스 자체 정책을 조합하여 구현한다.
 *
 * <p>Apple 환불 정책:
 *
 * <ul>
 *   <li>사용자 요청 환불: Apple 지원팀 승인 필요
 *   <li>결제 문제 환불: 자동 처리 가능
 *   <li>환불 기간: Apple 정책에 따름 (보통 구매 후 14일 이내)
 * </ul>
 *
 * @see com.example.subscription.domain.service.RefundCalculationService
 */
public interface RefundPolicy {

    /**
     * 환불 가능 기간(일)을 반환한다.
     *
     * @return 환불 가능 기간 (일)
     */
    int getRefundPeriodDays();

    /**
     * 부분 환불 허용 여부를 반환한다.
     *
     * @return 부분 환불이 허용되면 true
     */
    boolean isPartialRefundAllowed();

    /**
     * 환불 수수료율을 반환한다.
     *
     * @return 환불 수수료율 (0.0 ~ 1.0)
     */
    double getRefundFeeRate();

    /**
     * 특정 사유로 환불이 가능한지 확인한다.
     *
     * @param reasonCode 환불 사유 코드
     * @return 환불 가능하면 true
     */
    boolean isRefundableForReason(String reasonCode);

    /**
     * 구독 시작 후 경과 일수에 따른 환불 비율을 반환한다.
     *
     * <p>Proration 방식
     *
     * @param daysElapsed 경과 일수
     * @param totalDays 총 구독 기간 (일)
     * @return 환불 비율 (0.0 ~ 1.0)
     */
    double getRefundRate(int daysElapsed, int totalDays);
}
