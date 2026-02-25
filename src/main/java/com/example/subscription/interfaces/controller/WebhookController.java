package com.example.subscription.interfaces.controller;

import com.example.subscription.application.usecase.ProcessWebhookUseCase;
import com.example.subscription.domain.vo.WebhookEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Apple Webhook 수신 Controller.
 *
 * <p>Apple App Store Server Notifications V2 Webhook을 수신하는 엔드포인트를 제공한다.
 *
 * <p>Endpoints:
 *
 * <ul>
 *   <li>POST /api/v1/webhooks/apple - Apple Webhook 수신
 * </ul>
 *
 * <p>보안:
 *
 * <ul>
 *   <li>JWS 서명 검증 필수
 *   <li>Replay Attack 방지
 * </ul>
 *
 * @see com.example.subscription.application.usecase.ProcessWebhookUseCase
 */
@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private final ProcessWebhookUseCase processWebhookUseCase;

    public WebhookController(ProcessWebhookUseCase processWebhookUseCase) {
        this.processWebhookUseCase = processWebhookUseCase;
    }

    /**
     * Apple Webhook을 수신하고 처리한다.
     *
     * <p>Apple App Store Server Notifications V2 형식:
     *
     * <pre>
     * {
     *   "signedPayload": "eyJ..."
     * }
     * </pre>
     *
     * @param request Webhook 요청
     * @return 처리 결과 (200 OK 또는 에러)
     */
    @PostMapping("/apple")
    public ResponseEntity<WebhookResponse> handleAppleWebhook(@RequestBody WebhookRequest request) {
        // 1. signedPayload 추출 및 검증
        if (request.signedPayload() == null || request.signedPayload().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(WebhookResponse.failure("signedPayload is required"));
        }

        try {
            // 2. ProcessWebhookUseCase.process() 호출
            WebhookEvent event = processWebhookUseCase.process(request.signedPayload());

            // 3. 결과 반환 (Apple은 200 OK만 확인)
            return ResponseEntity.ok(WebhookResponse.success(event.getNotificationType()));

        } catch (SecurityException e) {
            // 서명 검증 실패
            return ResponseEntity.status(401)
                .body(WebhookResponse.failure("JWS verification failed: " + e.getMessage()));

        } catch (Exception e) {
            // 기타 예외
            return ResponseEntity.status(500)
                .body(WebhookResponse.failure("Webhook processing failed: " + e.getMessage()));
        }
    }

    /**
     * Webhook 상태 확인 (Health Check).
     *
     * @return 상태
     */
    @PostMapping("/apple/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    /** Apple Webhook 요청 */
    public record WebhookRequest(String signedPayload) {}

    /** Webhook 처리 응답 */
    public record WebhookResponse(boolean success, String message, String notificationType) {
        public static WebhookResponse success(String notificationType) {
            return new WebhookResponse(true, "Webhook processed successfully", notificationType);
        }

        public static WebhookResponse failure(String message) {
            return new WebhookResponse(false, message, null);
        }
    }
}
