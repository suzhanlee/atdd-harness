package com.example.subscription.application.usecase;

import com.example.subscription.domain.entity.Subscription;
import java.util.Optional;

/**
 * 영수증 검증 Use Case.
 *
 * <p>클라이언트에서 수신한 Apple 영수증을 검증하고 구독 정보를 조회한다. 앱 시작 시 또는 구매 직후 영수증 검증에
 * 활용된다.
 *
 * <p>처리 흐름:
 *
 * <ol>
 *   <li>영수증 Base64 디코딩
 *   <li>Apple App Store API 호출 (verifyReceipt)
 *   <li>응답 파싱 및 구독 정보 추출
 *   <li>구독 생성 또는 업데이트
 * </ol>
 */
public interface ValidateReceiptUseCase {

    /**
     * 영수증을 검증한다.
     *
     * @param receiptData Base64 인코딩된 영수증 데이터
     * @param userId 사용자 ID
     * @return 검증된 구독 정보 (유효하지 않으면 empty)
     */
    Optional<Subscription> validate(String receiptData, Long userId);

    /**
     * 영수증을 검증하고 상세 정보를 반환한다.
     *
     * @param receiptData Base64 인코딩된 영수증 데이터
     * @return 검증 결과
     */
    ValidationResult validateWithDetails(String receiptData);

    /** 영수증 검증 결과 */
    record ValidationResult(
            boolean valid,
            String originalTransactionId,
            String productId,
            Long userId,
            String expiresDate,
            String errorCode,
            String errorMessage) {

        public static ValidationResult invalid(String errorCode, String errorMessage) {
            return new ValidationResult(false, null, null, null, null, errorCode, errorMessage);
        }

        public static ValidationResult valid(
                String originalTransactionId, String productId, Long userId, String expiresDate) {
            return new ValidationResult(true, originalTransactionId, productId, userId, expiresDate, null, null);
        }
    }
}
