package com.example.subscription.infrastructure.client;

import java.util.Optional;

/**
 * Apple App Store API 클라이언트 인터페이스.
 *
 * <p>Apple App Store Server API와의 통신을 담당한다. 영수증 검증 및 구독 상태 조회에 사용된다.
 *
 * <p>API Endpoints:
 *
 * <ul>
 *   <li>POST /verifyReceipt - 영수증 검증
 *   <li>GET /subscriptions/{transactionId} - 구독 상태 조회
 * </ul>
 */
public interface AppleAppStoreClient {

    /**
     * 영수증을 검증한다.
     *
     * <p>Apple verifyReceipt API를 호출하여 영수증 유효성을 확인한다.
     *
     * @param receiptData Base64 인코딩된 영수증 데이터
     * @return 검증 결과
     */
    ReceiptVerificationResult verifyReceipt(String receiptData);

    /**
     * 구독 상태를 조회한다.
     *
     * <p>Apple App Store Server API를 통해 특정 구독의 상태를 조회한다.
     *
     * @param originalTransactionId Apple 원본 트랜잭션 ID
     * @return 구독 상태 정보
     */
    Optional<SubscriptionStatusResponse> getSubscriptionStatus(String originalTransactionId);

    /**
     * 환불 내역을 조회한다.
     *
     * <p>특정 구독의 환불 내역을 조회한다.
     *
     * @param transactionId 트랜잭션 ID
     * @return 환불 내역
     */
    Optional<RefundResponse> getRefundHistory(String transactionId);

    /** 영수증 검증 결과 */
    record ReceiptVerificationResult(
            int status,
            String environment,
            String originalTransactionId,
            String productId,
            Long expiresDateMs,
            boolean isTrialPeriod,
            String errorCode,
            String errorMessage) {

        public boolean isValid() {
            return status == 0;
        }
    }

    /** 구독 상태 응답 */
    record SubscriptionStatusResponse(
            String originalTransactionId,
            String productId,
            String status,
            Long expiresDateMs,
            String environment) {}

    /** 환불 응답 */
    record RefundResponse(String transactionId, Long refundedDateMs, String reason) {}
}
