package com.example.subscription.infrastructure.parser;

import com.example.subscription.domain.vo.WebhookEvent;

/**
 * Apple Webhook 파서 인터페이스.
 *
 * <p>JWS로 서명된 Apple App Store Server Notifications V2 Webhook 페이로드를 파싱하여 도메인 객체로 변환한다.
 *
 * <p>Apple Webhook V2 페이로드 구조:
 *
 * <pre>
 * {
 *   "signedPayload": "eyJ..."  // JWS로 서명된 JSON
 * }
 * </pre>
 *
 * <p>디코딩된 페이로드:
 *
 * <pre>
 * {
 *   "notificationType": "DID_RENEW",
 *   "subtype": "RESUBSCRIBE",
 *   "data": {
 *     "transactionId": "...",
 *     "originalTransactionId": "...",
 *     "productId": "...",
 *     "signedDate": 1234567890000,
 *     "expiresDate": 1234567890000,
 *     "environment": "PRODUCTION"
 *   }
 * }
 * </pre>
 *
 * @see com.example.subscription.domain.vo.WebhookEvent
 * @see JwsVerifier
 */
public interface AppleWebhookParser {

    /**
     * JWS 서명된 페이로드를 파싱한다.
     *
     * <p>서명 검증 없이 파싱만 수행한다. 서명 검증이 필요한 경우 {@link JwsVerifier}를 먼저 사용해야 한다.
     *
     * @param signedPayload JWS로 서명된 페이로드
     * @return 파싱된 Webhook 이벤트
     * @throws IllegalArgumentException 잘못된 형식의 페이로드인 경우
     */
    WebhookEvent parse(String signedPayload);

    /**
     * 검증된 JSON 페이로드를 파싱한다.
     *
     * <p>JwsVerifier에서 검증된 JSON 문자열을 WebhookEvent로 변환한다.
     *
     * @param verifiedJson 검증된 JSON 문자열
     * @return 파싱된 Webhook 이벤트
     * @throws IllegalArgumentException 잘못된 형식의 JSON인 경우
     */
    WebhookEvent parseVerifiedJson(String verifiedJson);

    /**
     * 알림 유형을 추출한다.
     *
     * <p>전체 파싱 없이 알림 유형만 빠르게 확인할 때 사용한다.
     *
     * @param signedPayload JWS로 서명된 페이로드
     * @return 알림 유형 (SUBSCRIBED, DID_RENEW 등)
     */
    String extractNotificationType(String signedPayload);
}
