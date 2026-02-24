package com.example.subscription.interfaces.controller;

import com.example.subscription.application.usecase.RefundSubscriptionUseCase;
import com.example.subscription.application.usecase.UpgradeSubscriptionUseCase;
import com.example.subscription.application.usecase.ValidateReceiptUseCase;
import com.example.subscription.domain.entity.Subscription;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 구독 관리 API Controller.
 *
 * <p>구독 조회, 업그레이드, 환불 관련 API를 제공한다.
 *
 * <p>Endpoints:
 *
 * <ul>
 *   <li>POST /api/v1/subscriptions/validate - 영수증 검증
 *   <li>GET /api/v1/subscriptions/{id} - 구독 조회
 *   <li>GET /api/v1/subscriptions/user/{userId} - 사용자 구독 목록
 *   <li>POST /api/v1/subscriptions/{id}/upgrade - 구독 업그레이드
 *   <li>POST /api/v1/subscriptions/{id}/refund/estimate - 환불 예상 금액
 * </ul>
 *
 * @see com.example.subscription.application.usecase.ValidateReceiptUseCase
 * @see com.example.subscription.application.usecase.UpgradeSubscriptionUseCase
 * @see com.example.subscription.application.usecase.RefundSubscriptionUseCase
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final ValidateReceiptUseCase validateReceiptUseCase;
    private final UpgradeSubscriptionUseCase upgradeSubscriptionUseCase;
    private final RefundSubscriptionUseCase refundSubscriptionUseCase;

    public SubscriptionController(
            ValidateReceiptUseCase validateReceiptUseCase,
            UpgradeSubscriptionUseCase upgradeSubscriptionUseCase,
            RefundSubscriptionUseCase refundSubscriptionUseCase) {
        this.validateReceiptUseCase = validateReceiptUseCase;
        this.upgradeSubscriptionUseCase = upgradeSubscriptionUseCase;
        this.refundSubscriptionUseCase = refundSubscriptionUseCase;
    }

    /**
     * 영수증을 검증하고 구독 정보를 반환한다.
     *
     * @param request 영수증 검증 요청
     * @return 검증된 구독 정보
     */
    @PostMapping("/validate")
    public ResponseEntity<SubscriptionResponse> validateReceipt(@RequestBody ValidateReceiptRequest request) {
        // TODO: TDD에서 구현
        // 1. ValidateReceiptUseCase.validate() 호출
        // 2. 결과를 SubscriptionResponse로 변환
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }

    /**
     * 구독 정보를 조회한다.
     *
     * @param id 구독 ID
     * @return 구독 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> getSubscription(@PathVariable Long id) {
        // TODO: TDD에서 구현
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }

    /**
     * 사용자의 활성 구독 목록을 조회한다.
     *
     * @param userId 사용자 ID
     * @return 구독 목록
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<SubscriptionListResponse> getUserSubscriptions(@PathVariable Long userId) {
        // TODO: TDD에서 구현
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }

    /**
     * 구독 업그레이드 견적을 조회한다.
     *
     * @param id 구독 ID
     * @param newProductId 새 제품 ID
     * @return 업그레이드 견적
     */
    @GetMapping("/{id}/upgrade/quote")
    public ResponseEntity<UpgradeQuoteResponse> getUpgradeQuote(
            @PathVariable Long id, @RequestParam String newProductId) {
        // TODO: TDD에서 구현
        // 1. UpgradeSubscriptionUseCase.getQuote() 호출
        // 2. 결과 반환
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }

    /**
     * 구독을 업그레이드한다.
     *
     * @param id 구독 ID
     * @param request 업그레이드 요청
     * @return 업그레이드된 구독 정보
     */
    @PostMapping("/{id}/upgrade")
    public ResponseEntity<SubscriptionResponse> upgradeSubscription(
            @PathVariable Long id, @RequestBody UpgradeRequest request) {
        // TODO: TDD에서 구현
        // 1. UpgradeSubscriptionUseCase.upgrade() 호출
        // 2. 결과 반환
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }

    /**
     * 환불 예상 금액을 조회한다.
     *
     * @param id 구독 ID
     * @return 환불 예상 금액
     */
    @GetMapping("/{id}/refund/estimate")
    public ResponseEntity<RefundEstimateResponse> getRefundEstimate(@PathVariable Long id) {
        // TODO: TDD에서 구현
        // 1. RefundSubscriptionUseCase.estimateRefund() 호출
        // 2. 결과 반환
        throw new UnsupportedOperationException("TODO: TDD에서 구현");
    }

    // Request/Response DTOs

    /** 영수증 검증 요청 */
    public record ValidateReceiptRequest(String receiptData, Long userId) {}

    /** 구독 응답 */
    public record SubscriptionResponse(
            Long id,
            String originalTransactionId,
            Long userId,
            String productId,
            String status,
            String expiresAt,
            String currentPeriodStart,
            String currentPeriodEnd) {

        public static SubscriptionResponse from(Subscription subscription) {
            throw new UnsupportedOperationException("TODO: TDD에서 구현");
        }
    }

    /** 구독 목록 응답 */
    public record SubscriptionListResponse(java.util.List<SubscriptionResponse> subscriptions, int count) {}

    /** 업그레이드 요청 */
    public record UpgradeRequest(String newProductId, String transactionId, Long userId) {}

    /** 업그레이드 견적 응답 */
    public record UpgradeQuoteResponse(
            Long subscriptionId,
            String currentProductId,
            String newProductId,
            String additionalPrice,
            String newExpiresAt,
            boolean upgradePossible,
            String impossibleReason) {}

    /** 환불 예상 금액 응답 */
    public record RefundEstimateResponse(
            Long subscriptionId, String estimatedAmount, String expiresAt, boolean eligible) {}
}
