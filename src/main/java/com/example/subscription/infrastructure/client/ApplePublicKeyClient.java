package com.example.subscription.infrastructure.client;

import java.security.interfaces.ECPublicKey;
import java.util.List;

/**
 * Apple 공개키 조회 클라이언트 인터페이스.
 *
 * <p>Apple의 JWS 서명 검증에 필요한 공개키를 조회한다. ADR-001에 따라 Redis 캐싱을 적용한다.
 *
 * <p>공개키 소스:
 *
 * <ul>
 *   <li>Apple Root CA에서 발급한 중간 인증서
 *   <li>JWS 헤더의 x5c 인증서 체인에서 추출
 * </ul>
 *
 * <p>캐싱 전략:
 *
 * <ul>
 *   <li>TTL: 24시간
 *   <li>Fallback: Redis 장애 시 Apple API 직접 호출
 *   <li>키 로테이션 대응: 검증 실패 시 캐시 무효화 후 재조회
 * </ul>
 */
public interface ApplePublicKeyClient {

    /**
     * 공개키를 조회한다.
     *
     * <p>캐시에서 먼저 조회하고, 없으면 Apple에서 조회한다.
     *
     * @param keyId 키 식별자 (JWS 헤더의 kid)
     * @return 공개키
     * @throws IllegalArgumentException 해당 키 ID에 대한 키가 없는 경우
     */
    ECPublicKey getPublicKey(String keyId);

    /**
     * 모든 활성 공개키를 조회한다.
     *
     * <p>Apple의 현재 공개키 목록을 조회한다.
     *
     * @return 공개키 목록
     */
    List<ApplePublicKey> getAllPublicKeys();

    /**
     * 캐시를 무효화한다.
     *
     * <p>키 로테이션 감지 시 호출하여 캐시된 키를 삭제한다.
     *
     * @param keyId 무효화할 키 ID (null이면 전체 무효화)
     */
    void invalidateCache(String keyId);

    /**
     * 캐시를 갱신한다.
     *
     * <p>Apple에서 최신 공개키를 조회하여 캐시를 업데이트한다.
     */
    void refreshCache();

    /** Apple 공개키 정보 */
    record ApplePublicKey(String keyId, ECPublicKey publicKey, String algorithm, Long fetchedAt) {}
}
