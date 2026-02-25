package com.example.subscription.infrastructure.parser;

import com.example.subscription.domain.vo.WebhookEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Apple Webhook 파서 구현체.
 *
 * <p>JWS로 서명된 Apple App Store Server Notifications V2 Webhook 페이로드를 파싱한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppleWebhookParserImpl implements AppleWebhookParser {

    private final ObjectMapper objectMapper;

    @Override
    public WebhookEvent parse(String signedPayload) {
        try {
            // JWS 페이로드 추출 (서명 검증 없이)
            String[] parts = signedPayload.split("\\.");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid JWS format");
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            return parseVerifiedJson(payloadJson);

        } catch (Exception e) {
            log.error("Failed to parse webhook payload", e);
            throw new IllegalArgumentException("Failed to parse webhook: " + e.getMessage());
        }
    }

    @Override
    public WebhookEvent parseVerifiedJson(String verifiedJson) {
        try {
            JsonNode root = objectMapper.readTree(verifiedJson);

            String notificationType = getTextOrNull(root, "notificationType");
            String transactionId = null;
            String originalTransactionId = null;
            String productId = null;
            Long signedDate = null;
            Long expiresDate = null;
            String environment = null;

            // data 객체에서 트랜잭션 정보 추출
            JsonNode dataNode = root.get("data");
            if (dataNode != null) {
                transactionId = getTextOrNull(dataNode, "transactionId");
                originalTransactionId = getTextOrNull(dataNode, "originalTransactionId");
                productId = getTextOrNull(dataNode, "productId");
                signedDate = getLongOrNull(dataNode, "signedDate");
                expiresDate = getLongOrNull(dataNode, "expiresDate");
                environment = getTextOrNull(dataNode, "environment");
            }

            return new WebhookEvent(
                    notificationType,
                    transactionId,
                    originalTransactionId,
                    productId,
                    signedDate != null ? Instant.ofEpochMilli(signedDate) : null,
                    expiresDate != null ? Instant.ofEpochMilli(expiresDate) : null,
                    environment);

        } catch (Exception e) {
            log.error("Failed to parse verified JSON", e);
            throw new IllegalArgumentException("Failed to parse JSON: " + e.getMessage());
        }
    }

    @Override
    public String extractNotificationType(String signedPayload) {
        try {
            String[] parts = signedPayload.split("\\.");
            if (parts.length < 2) {
                return null;
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode root = objectMapper.readTree(payloadJson);
            return getTextOrNull(root, "notificationType");

        } catch (Exception e) {
            log.warn("Failed to extract notification type", e);
            return null;
        }
    }

    private String getTextOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : null;
    }

    private Long getLongOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asLong() : null;
    }
}
