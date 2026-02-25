package com.example.unit.interfaces.controller;

import com.example.subscription.application.usecase.RefundSubscriptionUseCase;
import com.example.subscription.application.usecase.UpgradeSubscriptionUseCase;
import com.example.subscription.application.usecase.ValidateReceiptUseCase;
import com.example.subscription.domain.entity.Subscription;
import com.example.subscription.domain.repository.SubscriptionHistoryRepository;
import com.example.subscription.domain.repository.SubscriptionRepository;
import com.example.subscription.domain.vo.Money;
import com.example.subscription.domain.vo.Period;
import com.example.subscription.domain.vo.ProductId;
import com.example.subscription.domain.vo.SubscriptionStatus;
import com.example.subscription.interfaces.controller.SubscriptionController;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SubscriptionController 단위 테스트.
 *
 * <p>구독 관리 API 엔드포인트를 검증한다.
 */
@WebMvcTest(SubscriptionController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("SubscriptionController 단위 테스트")
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ValidateReceiptUseCase validateReceiptUseCase;

    @MockBean
    private UpgradeSubscriptionUseCase upgradeSubscriptionUseCase;

    @MockBean
    private RefundSubscriptionUseCase refundSubscriptionUseCase;

    @MockBean
    private SubscriptionRepository subscriptionRepository;

    @MockBean
    private SubscriptionHistoryRepository subscriptionHistoryRepository;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private Subscription mockSubscription;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();
        Period period = new Period(now, now.plusSeconds(2592000));
        mockSubscription = Subscription.create(
            "originalTx123",
            1L,
            new ProductId("com.example.basic"),
            period
        );
    }

    @Nested
    @DisplayName("validateReceipt()")
    class ValidateReceipt {

        @Test
        @DisplayName("정상적인 영수증 검증 - 200 OK")
        void validateReceiptSuccessfully() throws Exception {
            // given
            SubscriptionController.ValidateReceiptRequest request =
                new SubscriptionController.ValidateReceiptRequest("base64_receipt", 1L);

            when(validateReceiptUseCase.validate(any(), any())).thenReturn(java.util.Optional.of(mockSubscription));

            // when & then
            mockMvc.perform(post("/api/v1/subscriptions/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalTransactionId").value("originalTx123"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

            verify(validateReceiptUseCase).validate(any(), any());
        }

        @Test
        @DisplayName("receiptData 누락 시 - 400 Bad Request")
        void validateReceiptWithMissingReceiptData() throws Exception {
            // given
            SubscriptionController.ValidateReceiptRequest request =
                new SubscriptionController.ValidateReceiptRequest(null, 1L);

            // when & then
            mockMvc.perform(post("/api/v1/subscriptions/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("getSubscription()")
    class GetSubscription {

        @Test
        @DisplayName("정상적인 구독 조회 - 200 OK")
        void getSubscriptionSuccessfully() throws Exception {
            // given
            // Using validateReceiptUseCase as repository accessor for now
            // In real implementation, this would use a separate query service
            when(validateReceiptUseCase.findById(1L)).thenReturn(java.util.Optional.of(mockSubscription));

            // when & then
            mockMvc.perform(get("/api/v1/subscriptions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalTransactionId").value("originalTx123"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

            verify(validateReceiptUseCase).findById(1L);
        }

        @Test
        @DisplayName("존재하지 않는 구독 ID - 404 Not Found")
        void getSubscriptionNotFound() throws Exception {
            // given
            when(validateReceiptUseCase.findById(999L)).thenReturn(java.util.Optional.empty());

            // when & then
            mockMvc.perform(get("/api/v1/subscriptions/999"))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("getUserSubscriptions()")
    class GetUserSubscriptions {

        @Test
        @DisplayName("사용자 구독 목록 조회 - 200 OK")
        void getUserSubscriptionsSuccessfully() throws Exception {
            // given
            List<Subscription> subscriptions = List.of(mockSubscription);
            when(validateReceiptUseCase.findByUserId(1L)).thenReturn(subscriptions);

            // when & then
            mockMvc.perform(get("/api/v1/subscriptions/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptions").isArray())
                .andExpect(jsonPath("$.count").value(1));

            verify(validateReceiptUseCase).findByUserId(1L);
        }
    }

    @Nested
    @DisplayName("getUpgradeQuote()")
    class GetUpgradeQuote {

        @Test
        @DisplayName("업그레이드 견적 조회 - 200 OK")
        void getUpgradeQuoteSuccessfully() throws Exception {
            // given
            UpgradeSubscriptionUseCase.UpgradeQuote quote =
                new UpgradeSubscriptionUseCase.UpgradeQuote(
                    1L,
                    "com.example.basic",
                    "com.example.pro",
                    new Money(BigDecimal.valueOf(5000), java.util.Currency.getInstance("USD")),
                    Instant.now().plusSeconds(5184000).toString(),
                    true,
                    null
                );

            when(upgradeSubscriptionUseCase.getQuote(1L, "com.example.pro")).thenReturn(quote);

            // when & then
            mockMvc.perform(get("/api/v1/subscriptions/1/upgrade/quote")
                    .param("newProductId", "com.example.pro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionId").value(1))
                .andExpect(jsonPath("$.currentProductId").value("com.example.basic"))
                .andExpect(jsonPath("$.newProductId").value("com.example.pro"))
                .andExpect(jsonPath("$.upgradePossible").value(true));

            verify(upgradeSubscriptionUseCase).getQuote(1L, "com.example.pro");
        }
    }

    @Nested
    @DisplayName("upgradeSubscription()")
    class UpgradeSubscription {

        @Test
        @DisplayName("정상적인 업그레이드 - 200 OK")
        void upgradeSubscriptionSuccessfully() throws Exception {
            // given
            SubscriptionController.UpgradeRequest request =
                new SubscriptionController.UpgradeRequest("com.example.pro", "tx456", 1L);

            Instant now = Instant.now();
            Period period = new Period(now, now.plusSeconds(5184000));
            Subscription upgradedSubscription = Subscription.create(
                "originalTx123",
                1L,
                new ProductId("com.example.pro"),
                period
            );

            when(upgradeSubscriptionUseCase.upgrade(any(UpgradeSubscriptionUseCase.UpgradeCommand.class)))
                .thenReturn(upgradedSubscription);

            // when & then
            mockMvc.perform(post("/api/v1/subscriptions/1/upgrade")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("com.example.pro"));

            verify(upgradeSubscriptionUseCase).upgrade(any(UpgradeSubscriptionUseCase.UpgradeCommand.class));
        }

        @Test
        @DisplayName("업그레이드 불가능한 경우 - 400 Bad Request")
        void upgradeSubscriptionNotPossible() throws Exception {
            // given
            SubscriptionController.UpgradeRequest request =
                new SubscriptionController.UpgradeRequest("com.example.basic", "tx456", 1L);

            when(upgradeSubscriptionUseCase.upgrade(any(UpgradeSubscriptionUseCase.UpgradeCommand.class)))
                .thenThrow(new IllegalArgumentException("Cannot upgrade to the same product"));

            // when & then
            mockMvc.perform(post("/api/v1/subscriptions/1/upgrade")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("getRefundEstimate()")
    class GetRefundEstimate {

        @Test
        @DisplayName("환불 예상 금액 조회 - 200 OK")
        void getRefundEstimateSuccessfully() throws Exception {
            // given
            RefundSubscriptionUseCase.RefundEstimate estimate =
                new RefundSubscriptionUseCase.RefundEstimate(
                    1L,
                    new Money(BigDecimal.valueOf(9000), java.util.Currency.getInstance("USD")),
                    Instant.now().plusSeconds(2592000).toString(),
                    true
                );

            when(refundSubscriptionUseCase.estimateRefund(1L)).thenReturn(estimate);

            // when & then
            mockMvc.perform(get("/api/v1/subscriptions/1/refund/estimate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionId").value(1))
                .andExpect(jsonPath("$.eligible").value(true));

            verify(refundSubscriptionUseCase).estimateRefund(1L);
        }
    }
}
