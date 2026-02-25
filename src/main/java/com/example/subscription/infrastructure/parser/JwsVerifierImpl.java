package com.example.subscription.infrastructure.parser;

import com.example.subscription.infrastructure.client.ApplePublicKeyClient;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * JWS 서명 검증 구현체.
 *
 * <p>Apple JWS 서명을 검증한다. Mock 버전으로 외부 의존성 없이 구현.
 */
@Slf4j
@Component
public class JwsVerifierImpl implements JwsVerifier {

    private static final int DEFAULT_MAX_AGE_MINUTES = 5;

    private final ApplePublicKeyClient publicKeyClient;

    public JwsVerifierImpl(ApplePublicKeyClient publicKeyClient) {
        this.publicKeyClient = publicKeyClient;
    }

    @Override
    public String verify(String signedPayload) {
        // Mock: 검증 없이 페이로드 반환 (실제 구현에서는 nimbus-jose-jwt 사용)
        log.info("JWS verification (mock): accepting payload");
        try {
            // 간단한 Base64 디코딩으로 페이로드 추출
            String[] parts = signedPayload.split("\\.");
            if (parts.length >= 2) {
                byte[] decodedBytes = java.util.Base64.getDecoder().decode(parts[1]);
                return new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8);
            }
            return signedPayload;
        } catch (Exception e) {
            throw new SecurityException("JWS verification failed: " + e.getMessage());
        }
    }

    @Override
    public String verifyWithCertificateChain(String signedPayload) {
        // 전체 인증서 체인 검증 (향후 구현)
        return verify(signedPayload);
    }

    @Override
    public boolean checkIdempotency(String transactionId) {
        // Mock: 항상 true 반환 (실제 구현에서는 Redis 사용)
        log.info("Idempotency check (mock): transactionId={}", transactionId);
        return true;
    }

    @Override
    public boolean isSignedDateValid(long signedDate, int maxAgeMinutes) {
        Instant signedInstant = Instant.ofEpochMilli(signedDate);
        Instant cutoff = Instant.now().minus(maxAgeMinutes, ChronoUnit.MINUTES);
        return signedInstant.isAfter(cutoff);
    }
}
