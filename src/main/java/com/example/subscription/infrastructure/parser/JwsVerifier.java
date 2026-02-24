package com.example.subscription.infrastructure.parser;

/**
 * JWS 서명 검증 인터페이스.
 *
 * <p>Apple App Store Server Notifications V2의 JWS 서명을 검증한다. Apple 공개키를 사용하여 서명을 확인하고
 * Replay Attack을 방지한다.
 *
 * <p>ADR-001에 따른 구현:
 *
 * <ul>
 *   <li>라이브러리: nimbus-jose-jwt
 *   <li>공개키 캐싱: Redis (24시간 TTL)
 *   <li>Fallback: Redis 장애 시 Apple API 직접 호출
 *   <li>Replay Attack 방지: signedDate 검증 (5분 이내) + Redis 중복 방지
 * </ul>
 *
 * @see com.example.subscription.infrastructure.client.ApplePublicKeyClient
 */
public interface JwsVerifier {

    /**
     * JWS 서명을 검증한다.
     *
     * @param signedPayload JWS로 서명된 페이로드
     * @return 검증된 JSON 페이로드
     * @throws SecurityException 서명 검증 실패
     * @throws IllegalArgumentException 잘못된 형식의 페이로드
     */
    String verify(String signedPayload);

    /**
     * JWS 서명을 검증하고 x5c 인증서 체인도 검증한다.
     *
     * <p>Apple Root CA 기반 전체 체인 검증을 수행한다.
     *
     * @param signedPayload JWS로 서명된 페이로드
     * @return 검증된 JSON 페이로드
     * @throws SecurityException 서명 검증 실패 또는 인증서 체인 검증 실패
     */
    String verifyWithCertificateChain(String signedPayload);

    /**
     * Replay Attack 방지를 위한 중복 검사를 수행한다.
     *
     * <p>Redis에 transactionId를 저장하여 중복 처리를 방지한다.
     *
     * @param transactionId 트랜잭션 ID
     * @return 중복이 아니면 true (처리 계속)
     */
    boolean checkIdempotency(String transactionId);

    /**
     * 서명 시간이 유효한지 확인한다.
     *
     * <p>Replay Attack 방지를 위해 signedDate가 지정된 시간 내인지 확인한다.
     *
     * @param signedDate 서명 시간 (Unix timestamp)
     * @param maxAgeMinutes 허용되는 최대 경과 시간 (분)
     * @return 유효하면 true
     */
    boolean isSignedDateValid(long signedDate, int maxAgeMinutes);
}
