package com.example.unit.interfaces.controller;

import com.example.subscription.application.usecase.ProcessWebhookUseCase;
import com.example.subscription.domain.vo.WebhookEvent;
import com.example.subscription.interfaces.controller.WebhookController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebhookController 단위 테스트.
 *
 * <p>Apple Webhook 수신 및 처리를 검증한다.
 */
@WebMvcTest(WebhookController.class)
@AutoConfigureMockMvc(addFilters = false)  // Security 필터 비활성화
@DisplayName("WebhookController 단위 테스트")
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProcessWebhookUseCase processWebhookUseCase;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;  // JPA 컨텍스트 Mock

    private WebhookEvent mockWebhookEvent;

    @BeforeEach
    void setUp() {
        mockWebhookEvent = new WebhookEvent(
            "SUBSCRIBED",
            "tx123",
            "originalTx123",
            "com.example.basic",
            java.time.Instant.now(),
            java.time.Instant.now().plusSeconds(2592000),
            "Sandbox"
        );
    }

    @Nested
    @DisplayName("handleAppleWebhook()")
    class HandleAppleWebhook {

        @Test
        @DisplayName("정상적인 Webhook 수신 및 처리 - 200 OK")
        void handleValidWebhook() throws Exception {
            // given
            String signedPayload = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJub3RpZmljYXRpb25UeXBlIjoiU1VCU0NSSUJFRCJ9.signature";
            WebhookController.WebhookRequest request = new WebhookController.WebhookRequest(signedPayload);

            when(processWebhookUseCase.process(any(String.class)))
                .thenReturn(mockWebhookEvent);

            // when & then
            mockMvc.perform(post("/api/v1/webhooks/apple")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.notificationType").value("SUBSCRIBED"));

            verify(processWebhookUseCase).process(any(String.class));
        }

        @Test
        @DisplayName("signedPayload 누락 시 - 400 Bad Request")
        void handleMissingPayload() throws Exception {
            // given
            WebhookController.WebhookRequest request = new WebhookController.WebhookRequest(null);

            // when & then
            mockMvc.perform(post("/api/v1/webhooks/apple")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("빈 signedPayload 전송 시 - 400 Bad Request")
        void handleEmptyPayload() throws Exception {
            // given
            WebhookController.WebhookRequest request = new WebhookController.WebhookRequest("");

            // when & then
            mockMvc.perform(post("/api/v1/webhooks/apple")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("UseCase 예외 발생 시 - 500 Internal Server Error")
        void handleUseCaseException() throws Exception {
            // given
            String signedPayload = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJub3RpZmljYXRpb25UeXBlIjoiU1VCU0NSSUJFRCJ9.signature";
            WebhookController.WebhookRequest request = new WebhookController.WebhookRequest(signedPayload);

            when(processWebhookUseCase.process(any(String.class)))
                .thenThrow(new RuntimeException("Webhook processing failed"));

            // when & then
            mockMvc.perform(post("/api/v1/webhooks/apple")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("SecurityException 발생 시 (서명 검증 실패) - 401 Unauthorized")
        void handleSecurityException() throws Exception {
            // given
            String signedPayload = "invalid.signature.payload";
            WebhookController.WebhookRequest request = new WebhookController.WebhookRequest(signedPayload);

            when(processWebhookUseCase.process(any(String.class)))
                .thenThrow(new SecurityException("JWS verification failed"));

            // when & then
            mockMvc.perform(post("/api/v1/webhooks/apple")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("healthCheck()")
    class HealthCheck {

        @Test
        @DisplayName("Health Check 정상 응답 - 200 OK")
        void healthCheckReturnsOk() throws Exception {
            // when & then
            mockMvc.perform(post("/api/v1/webhooks/apple/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
        }
    }
}
