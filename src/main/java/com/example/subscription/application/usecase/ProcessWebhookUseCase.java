package com.example.subscription.application.usecase;

import com.example.subscription.domain.vo.WebhookEvent;

/**
 * Webhook 처리 Use Case.
 *
 * <p>Apple App Store Server Notifications V2 Webhook을 처리한다. JWS 검증, 이벤트 파싱, 구독 상태 변경을
 * 순차적으로 수행한다.
 *
 * <p>처리 흐름:
 *
 * <ol>
 *   <li>JWS 서명 검증
 *   <li>Webhook 이벤트 파싱
 *   <li>멱등성 검사 (transactionId 기반)
 *   <li>구독 상태 변경
 *   <li>도메인 이벤트 발행
 * </ol>
 *
 * @see com.example.subscription.infrastructure.parser.JwsVerifier
 * @see com.example.subscription.infrastructure.parser.AppleWebhookParser
 */
public interface ProcessWebhookUseCase {

    /**
     * Webhook을 처리한다.
     *
     * @param signedPayload JWS로 서명된 Webhook 페이로드
     * @return 처리된 이벤트 정보
     * @throws IllegalArgumentException 잘못된 페이로드인 경우
     * @throws SecurityException 서명 검증 실패
     */
    WebhookEvent process(String signedPayload);

    /**
     * Webhook 서명을 검증만 수행한다.
     *
     * <p>검증된 페이로드를 반환하며 상태 변경은 수행하지 않는다.
     *
     * @param signedPayload JWS로 서명된 Webhook 페이로드
     * @return 검증된 페이로드 (JSON 문자열)
     * @throws SecurityException 서명 검증 실패
     */
    String verifyOnly(String signedPayload);
}
