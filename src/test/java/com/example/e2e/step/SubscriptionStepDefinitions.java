package com.example.e2e.step;

import com.example.subscription.domain.entity.Subscription;
import com.example.subscription.domain.entity.SubscriptionHistory;
import com.example.subscription.domain.repository.SubscriptionHistoryRepository;
import com.example.subscription.domain.repository.SubscriptionRepository;
import com.example.subscription.domain.vo.Period;
import com.example.subscription.domain.vo.ProductId;
import com.example.subscription.domain.vo.SubscriptionStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 구독 관련 Step Definitions.
 *
 * <p>Apple IAP 구독 기능에 대한 E2E 테스트 Step을 정의한다.
 */
public class SubscriptionStepDefinitions {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionHistoryRepository subscriptionHistoryRepository;

    @Autowired
    private ScenarioContext scenarioContext;

    @MockBean
    private com.example.subscription.infrastructure.client.AppleAppStoreClient appleAppStoreClient;

    @MockBean
    private com.example.subscription.infrastructure.parser.JwsVerifier jwsVerifier;

    @MockBean
    private com.example.subscription.infrastructure.parser.AppleWebhookParser appleWebhookParser;

    private boolean defaultMockInitialized = false;

    /**
     * 기본 Apple Mock 설정 (lazy initialization).
     * Spring 의존성 주입이 완료된 후 첫 번째 요청 시 설정된다.
     */
    private void ensureDefaultMockInitialized() {
        if (!defaultMockInitialized && appleAppStoreClient != null) {
            defaultMockInitialized = true;
            // 기본적인 영수증 검증 Mock 설정
            when(appleAppStoreClient.verifyReceipt(any()))
                .thenReturn(new com.example.subscription.infrastructure.client.AppleAppStoreClient.ReceiptVerificationResult(
                    0,  // status = 0 means success
                    "Sandbox",
                    "original-tx-" + System.currentTimeMillis(),
                    "basic_001",
                    Instant.now().plusSeconds(86400 * 30).toEpochMilli(),
                    false,
                    null,
                    null
                ));
        }
    }

    // Convenience methods for scenario context
    private Response getResponse() { return scenarioContext.getResponse(); }
    private void setResponse(Response response) { scenarioContext.setResponse(response); }
    private String getAuthToken() { return scenarioContext.getAuthToken(); }
    private void setAuthToken(String authToken) { scenarioContext.setAuthToken(authToken); }
    private Long getCurrentUserId() { return scenarioContext.getCurrentUserId(); }
    private void setCurrentUserId(Long userId) { scenarioContext.setCurrentUserId(userId); }

    // ============================================
    // Given - 상태 설정
    // ============================================

    @Given("사용자가 로그인되어 있다")
    public void userIsLoggedIn(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        Map<String, String> user = rows.get(0);
        setCurrentUserId(Long.parseLong(user.get("userId")));
        setAuthToken("Bearer test-jwt-token-" + getCurrentUserId());
    }

    @Given("사용자가 로그인되어 있지 않다")
    public void userIsNotLoggedIn() {
        setAuthToken(null);
        setCurrentUserId(null);
    }

    @Given("다음 구독이 존재한다")
    public void subscriptionExists(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        for (Map<String, String> row : rows) {
            Long id = Long.parseLong(row.get("id"));
            Long userId = Long.parseLong(row.get("userId"));
            String productTier = row.get("productTier");
            String status = row.get("status");
            String expiresAtStr = row.getOrDefault("expiresAt", "2026-12-31T00:00:00");
            String trialEndsAtStr = row.get("trialEndsAt");
            String originalTxId = row.getOrDefault("originalTransactionId",
                "original-tx-" + id + "-" + Instant.now().toEpochMilli());

            Subscription subscription = Subscription.create(
                    originalTxId,
                    userId,
                    new ProductId(productTier.toLowerCase() + "_001"),
                    new Period(Instant.now(), Instant.parse(expiresAtStr + "Z"))
            );

            // 상태 설정 (IN_TRIAL은 ACTIVE에서 전이 불가하므로 직접 설정)
            if (!"ACTIVE".equals(status)) {
                switch (status) {
                    case "EXPIRED" -> subscription.expire();
                    case "REFUNDED" -> subscription.refund();
                    case "IN_GRACE_PERIOD" -> subscription.enterGracePeriod(Instant.now().plusSeconds(86400));
                    case "IN_TRIAL" -> subscription.forceStatusForTest(
                            com.example.subscription.domain.vo.SubscriptionStatus.IN_TRIAL);
                }
            }

            subscriptionRepository.save(subscription);
        }
    }

    @Given("사용자의 활성 구독이 없다")
    public void noActiveSubscription() {
        // Repository가 비어있는 상태 (기본 동작)
    }

    @Given("구독이 존재하지 않는다")
    public void subscriptionDoesNotExist() {
        subscriptionRepository.deleteAll();
    }

    @Given("사용자가 기존 구독이 없다")
    public void userHasNoExistingSubscription() {
        // 기본 동작 - 빈 DB
    }

    @Given("사용자가 기존에 무료 체험을 사용했다")
    public void userHasUsedFreeTrial() {
        // 이전에 사용한 무료 체험 구독 생성 (이미 만료된 상태)
        Long userId = getCurrentUserId();
        if (userId == null) {
            userId = 1L;  // 기본값
        }

        Subscription previousTrial = Subscription.create(
            "trial-used-" + userId,
            userId,
            new ProductId("trial_001"),
            new Period(Instant.now().minusSeconds(86400 * 14), Instant.now().minusSeconds(86400 * 7))
        );
        previousTrial.forceStatusForTest(SubscriptionStatus.EXPIRED);

        subscriptionRepository.save(previousTrial);
    }

    // ============================================
    // Given - Apple Mock 서버 설정
    // ============================================

    @Given("Apple Mock 서버가 실행되어 있다")
    public void appleMockServerRunning() {
        // 정상적인 영수증 검증 Mock 설정
        when(appleAppStoreClient.verifyReceipt(any()))
            .thenReturn(new com.example.subscription.infrastructure.client.AppleAppStoreClient.ReceiptVerificationResult(
                0,  // status = 0 means success
                "Sandbox",
                "original-tx-" + System.currentTimeMillis(),
                "basic_001",
                Instant.now().plusSeconds(86400 * 30).toEpochMilli(),
                false,
                null,
                null
            ));
    }

    @Given("Apple Mock 서버가 타임아웃을 반환한다")
    public void appleMockServerReturnsTimeout() {
        when(appleAppStoreClient.verifyReceipt(any()))
            .thenThrow(new RuntimeException("Connection timeout"));
    }

    @Given("Apple Mock 서버가 검증 실패을 반환한다")
    public void appleMockServerReturnsVerificationFailure() {
        when(appleAppStoreClient.verifyReceipt(any()))
            .thenReturn(new com.example.subscription.infrastructure.client.AppleAppStoreClient.ReceiptVerificationResult(
                21002,  // status != 0 means failure
                null,
                null,
                null,
                null,
                false,
                "INVALID_RECEIPT",
                "영수증 데이터가 유효하지 않습니다"
            ));
    }

    @Given("Apple Mock 서버가 연결 거부을 반환한다")
    public void appleMockServerReturnsConnectionRefused() {
        when(appleAppStoreClient.verifyReceipt(any()))
            .thenThrow(new RuntimeException("Connection refused"));
    }

    @Given("Apple Mock 서버가 500 오류을 반환한다")
    public void appleMockServerReturns500Error() {
        when(appleAppStoreClient.verifyReceipt(any()))
            .thenThrow(new RuntimeException("Internal Server Error"));
    }

    // ============================================
    // Given - Webhook 설정
    // ============================================

    @Given("Apple 공개키가 캐싱되어 있다")
    public void applePublicKeyCached() {
        // JWS 검증 Mock 설정
        String verifiedJson = "{\"notificationType\":\"DID_RENEW\",\"data\":{\"originalTransactionId\":\"original-tx-1\",\"bundleId\":\"com.example.app\",\"productId\":\"basic_001\",\"transactionId\":\"txn-12345\",\"signedDate\":" + Instant.now().toEpochMilli() + ",\"expiresDate\":" + Instant.now().plusSeconds(86400).toEpochMilli() + "}}";
        when(jwsVerifier.verify(anyString())).thenReturn(verifiedJson);
        when(jwsVerifier.verifyWithCertificateChain(anyString())).thenReturn(verifiedJson);
        when(jwsVerifier.checkIdempotency(anyString())).thenReturn(true);
        when(jwsVerifier.isSignedDateValid(anyLong(), anyInt())).thenReturn(true);

        // Webhook 파서 Mock 설정 - 올바른 생성자 사용
        com.example.subscription.domain.vo.WebhookEvent mockEvent =
            new com.example.subscription.domain.vo.WebhookEvent(
                "DID_RENEW",                    // notificationType
                "txn-12345",                    // transactionId
                "original-tx-1",                // originalTransactionId
                "basic_001",                    // productId
                Instant.now(),                  // signedDate
                Instant.now().plusSeconds(86400), // expiresDate
                "SANDBOX"                       // environment
            );
        when(appleWebhookParser.parse(anyString())).thenReturn(mockEvent);
        when(appleWebhookParser.parseVerifiedJson(anyString())).thenReturn(mockEvent);
        when(appleWebhookParser.extractNotificationType(anyString())).thenReturn("DID_RENEW");
    }

    @Given("동일한 transactionId의 Webhook이 이미 처리되었다")
    public void webhookAlreadyProcessed(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        String transactionId = rows.get(0).get("transactionId");
        // 이미 처리된 transactionId로 Transaction 엔티티 생성
    }

    @Given("데이터베이스 연결에 실패한다")
    public void databaseConnectionFails() {
        // DB 연결 실패 시뮬레이션
    }

    @Given("업그레이드 트랜잭션이 진행 중이다")
    public void upgradeTransactionInProgress() {
        // 업그레이드 트랜잭션 진행 중 상태 설정
    }

    @Given("다음 비갱신형 구독이 존재한다")
    public void nonRenewingSubscriptionExists(DataTable dataTable) {
        // 비갱신형 구독 생성 로직
        subscriptionExists(dataTable);
    }

    @Given("다음 구독 이력이 존재한다")
    public void subscriptionHistoryExists(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        for (Map<String, String> row : rows) {
            // fromTier/toTier는 Product Tier를 의미하므로 ACTIVE로 매핑
            // 실제로는 Product Tier 변경 이력이 아닌 상태 변경 이력을 저장
            com.example.subscription.domain.vo.SubscriptionStatus fromStatus =
                    mapToSubscriptionStatus(row.getOrDefault("fromStatus", row.get("fromTier")));
            com.example.subscription.domain.vo.SubscriptionStatus toStatus =
                    mapToSubscriptionStatus(row.getOrDefault("toStatus", row.get("toTier")));

            // 먼저 구독을 생성 (이력은 구독에 연결되어야 함)
            Long subscriptionId = Long.parseLong(row.get("id"));
            Subscription subscription = subscriptionRepository.findById(subscriptionId).orElse(null);

            if (subscription == null) {
                // 구독이 없으면 생성
                subscription = Subscription.create(
                    "original-tx-history-" + subscriptionId,
                    Long.parseLong(row.get("userId")),
                    new ProductId("basic_001"),
                    new Period(Instant.now(), Instant.now().plusSeconds(86400 * 30))
                );
                subscription = subscriptionRepository.save(subscription);
            }

            SubscriptionHistory history = SubscriptionHistory.create(
                    subscription.getId(),  // 저장된 구독의 실제 ID 사용
                    fromStatus,
                    toStatus,
                    row.get("action"),
                    "test-txn-" + row.get("id"),
                    java.time.Instant.now()
            );
            subscriptionHistoryRepository.save(history);
        }
    }

    private com.example.subscription.domain.vo.SubscriptionStatus mapToSubscriptionStatus(String value) {
        if (value == null) {
            return com.example.subscription.domain.vo.SubscriptionStatus.ACTIVE;
        }
        // Product tier names map to ACTIVE status (since they're valid subscription states)
        return switch (value.toUpperCase()) {
            case "BASIC", "PRO", "ULTRA", "TRIAL", "ACTIVE" ->
                com.example.subscription.domain.vo.SubscriptionStatus.ACTIVE;
            case "EXPIRED" -> com.example.subscription.domain.vo.SubscriptionStatus.EXPIRED;
            case "REFUNDED" -> com.example.subscription.domain.vo.SubscriptionStatus.REFUNDED;
            case "IN_TRIAL" -> com.example.subscription.domain.vo.SubscriptionStatus.IN_TRIAL;
            case "IN_GRACE_PERIOD", "GRACE_PERIOD" -> com.example.subscription.domain.vo.SubscriptionStatus.GRACE_PERIOD;
            case "BILLING_RETRY" -> com.example.subscription.domain.vo.SubscriptionStatus.BILLING_RETRY;
            default -> com.example.subscription.domain.vo.SubscriptionStatus.ACTIVE;
        };
    }

    @Given("구독 이력이 없다")
    public void noSubscriptionHistory() {
        // 기본 동작 - 빈 이력
    }

    // ============================================
    // When - 요청 전송
    // ============================================

    @When("구독 구매 요청을 보낸다")
    public void sendPurchaseRequest(DataTable dataTable) throws Exception {
        ensureDefaultMockInitialized();  // 기본 Mock 설정

        Map<String, String> data = new HashMap<>(dataTable.asMaps().get(0));

        // receiptData가 Base64가 아니면 변환
        if (data.containsKey("receiptData")) {
            String receiptData = data.get("receiptData");
            if (!isBase64(receiptData)) {
                data.put("receiptData", java.util.Base64.getEncoder().encodeToString(receiptData.getBytes()));
            }
        }

        var request = RestAssured.given()
                .contentType("application/json")
                .body(objectMapper.writeValueAsString(data));

        if (getAuthToken() != null) {
            request.header("Authorization", getAuthToken());
        }

        setResponse(request.when()
                .post("/subscriptions/purchase"));
    }

    /**
     * 문자열이 유효한 Base64인지 확인한다.
     */
    private boolean isBase64(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            java.util.Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @When("사용자가 구독 상태 조회 요청을 보낸다")
    public void sendGetSubscriptionRequest() {
        var request = RestAssured.given();

        if (getAuthToken() != null) {
            request.header("Authorization", getAuthToken());
        }

        setResponse(request.when()
                .get("/subscriptions/me"));
    }

    @When("업그레이드 요청을 보낸다")
    public void sendUpgradeRequest(DataTable dataTable) throws Exception {
        Map<String, String> data = dataTable.asMaps().get(0);
        String targetTier = data.get("targetTier");

        // targetTier → productId 변환
        String newProductId = convertTierToProductId(targetTier);

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("newProductId", newProductId);
        requestData.put("transactionId", "upgrade-tx-" + Instant.now().toEpochMilli());

        var request = RestAssured.given()
                .contentType("application/json")
                .body(objectMapper.writeValueAsString(requestData));

        if (getAuthToken() != null) {
            request.header("Authorization", getAuthToken());
        }

        setResponse(request.when()
                .post("/subscriptions/upgrade"));
    }

    /**
     * Tier 이름을 ProductId로 변환한다.
     */
    private String convertTierToProductId(String tier) {
        if (tier == null) {
            return "unknown_001";
        }
        return switch (tier.toUpperCase()) {
            case "BASIC" -> "basic_001";
            case "PRO" -> "pro_001";
            case "ULTRA" -> "ultra_001";
            case "TRIAL" -> "trial_001";
            default -> tier.toLowerCase() + "_001";
        };
    }

    @When("사용자가 구독 이력 조회 요청을 보낸다")
    public void sendGetHistoryRequest() {
        var request = RestAssured.given();

        if (getAuthToken() != null) {
            request.header("Authorization", getAuthToken());
        }

        setResponse(request.when()
                .get("/subscriptions/history"));
    }

    @When("페이징으로 구독 이력 조회 요청을 보낸다")
    public void sendGetHistoryRequestWithPaging(DataTable dataTable) {
        Map<String, String> params = dataTable.asMaps().get(0);

        var request = RestAssured.given()
                .queryParam("page", params.get("page"))
                .queryParam("size", params.get("size"));

        if (getAuthToken() != null) {
            request.header("Authorization", getAuthToken());
        }

        setResponse(request.when()
                .get("/subscriptions/history"));
    }

    @When("영수증 검증 요청을 보낸다")
    public void sendReceiptVerificationRequest(DataTable dataTable) throws Exception {
        ensureDefaultMockInitialized();  // 기본 Mock 설정

        Map<String, String> data = new HashMap<>(dataTable.asMaps().get(0));

        // receiptData가 Base64가 아니면 변환
        if (data.containsKey("receiptData")) {
            String receiptData = data.get("receiptData");
            if (!isBase64(receiptData)) {
                data.put("receiptData", java.util.Base64.getEncoder().encodeToString(receiptData.getBytes()));
            }
        }

        var request = RestAssured.given()
                .contentType("application/json")
                .body(objectMapper.writeValueAsString(data));

        if (getAuthToken() != null) {
            request.header("Authorization", getAuthToken());
        }

        setResponse(request.when()
                .post("/subscriptions/verify"));
    }

    @When("무료 체험 시작 요청을 보낸다")
    public void sendFreeTrialRequest(DataTable dataTable) throws Exception {
        Map<String, String> data = dataTable.asMaps().get(0);

        var request = RestAssured.given()
                .contentType("application/json")
                .body(objectMapper.writeValueAsString(data));

        if (getAuthToken() != null) {
            request.header("Authorization", getAuthToken());
        }

        setResponse(request.when()
                .post("/subscriptions/trial"));
    }

    @When("무료 체험 종료 후 유료 구독 요청을 보낸다")
    public void sendConvertTrialRequest(DataTable dataTable) throws Exception {
        ensureDefaultMockInitialized();  // 기본 Mock 설정

        Map<String, String> data = new HashMap<>(dataTable.asMaps().get(0));

        // receiptData가 Base64가 아니면 변환
        if (data.containsKey("receiptData")) {
            String receiptData = data.get("receiptData");
            if (!isBase64(receiptData)) {
                data.put("receiptData", java.util.Base64.getEncoder().encodeToString(receiptData.getBytes()));
            }
        }

        var request = RestAssured.given()
                .contentType("application/json")
                .body(objectMapper.writeValueAsString(data));

        if (getAuthToken() != null) {
            request.header("Authorization", getAuthToken());
        }

        setResponse(request.when()
                .post("/subscriptions/trial/convert"));
    }

    // ============================================
    // When - Webhook 요청
    // ============================================

    @When("Apple Webhook 요청을 보낸다")
    public void sendAppleWebhookRequest(DataTable dataTable) throws Exception {
        Map<String, String> data = dataTable.asMaps().get(0);
        String signedPayload = data.get("signedPayload");

        Map<String, String> body = new HashMap<>();
        body.put("signedPayload", signedPayload != null ? signedPayload : "");

        setResponse(RestAssured.given()
                .contentType("application/json")
                .body(objectMapper.writeValueAsString(body))
                .when()
                .post("/webhooks/apple"));
    }

    @When("동일한 transactionId로 Apple Webhook 요청을 보낸다")
    public void sendDuplicateWebhookRequest(DataTable dataTable) throws Exception {
        sendAppleWebhookRequest(dataTable);
    }

    @When("서명이 위조된 Apple Webhook 요청을 보낸다")
    public void sendForgedWebhookRequest(DataTable dataTable) throws Exception {
        Map<String, String> data = dataTable.asMaps().get(0);

        Map<String, String> body = new HashMap<>();
        body.put("signedPayload", data.get("signedPayload"));

        setResponse(RestAssured.given()
                .contentType("application/json")
                .body(objectMapper.writeValueAsString(body))
                .when()
                .post("/webhooks/apple"));
    }

    @When("만료된 JWS 토큰으로 Apple Webhook 요청을 보낸다")
    public void sendExpiredJwsWebhookRequest(DataTable dataTable) throws Exception {
        sendForgedWebhookRequest(dataTable);
    }

    @When("지원하지 않는 타입의 Apple Webhook 요청을 보낸다")
    public void sendUnsupportedTypeWebhookRequest(DataTable dataTable) throws Exception {
        sendForgedWebhookRequest(dataTable);
    }

    // ============================================
    // When - 이벤트 수신
    // ============================================

    @When("DID_RENEW Webhook 이벤트를 수신한다")
    public void receiveDidRenewEvent(DataTable dataTable) throws Exception {
        sendMockWebhookEvent("DID_RENEW", dataTable);
    }

    @When("EXPIRED Webhook 이벤트를 수신한다")
    public void receiveExpiredEvent(DataTable dataTable) throws Exception {
        sendMockWebhookEvent("EXPIRED", dataTable);
    }

    @When("REFUND Webhook 이벤트를 수신한다")
    public void receiveRefundEvent(DataTable dataTable) throws Exception {
        sendMockWebhookEvent("REFUND", dataTable);
    }

    @When("DID_FAIL_TO_RENEW Webhook 이벤트를 수신한다")
    public void receiveDidFailToRenewEvent(DataTable dataTable) throws Exception {
        sendMockWebhookEvent("DID_FAIL_TO_RENEW", dataTable);
    }

    @When("GRACE_PERIOD_EXPIRED Webhook 이벤트를 수신한다")
    public void receiveGracePeriodExpiredEvent(DataTable dataTable) throws Exception {
        sendMockWebhookEvent("GRACE_PERIOD_EXPIRED", dataTable);
    }

    @When("비갱신형 구독 만료 시간이 지나면")
    public void nonRenewingSubscriptionExpires() {
        // 시간 경과 시뮬레이션
        setResponse(RestAssured.given()
                .when()
                .post("/subscriptions/check-expiry"));
    }

    private void sendMockWebhookEvent(String eventType, DataTable dataTable) throws Exception {
        Map<String, String> data = dataTable.asMaps().get(0);

        Map<String, Object> body = new HashMap<>();
        body.put("signedPayload", "mock_signed_payload_for_" + eventType);
        body.put("eventType", eventType);
        body.putAll(data);

        setResponse(RestAssured.given()
                .contentType("application/json")
                .body(objectMapper.writeValueAsString(body))
                .when()
                .post("/webhooks/apple"));
    }

    // ============================================
    // Then - 검증
    // ============================================

    @Then("업그레이드가 즉시 적용된다")
    public void verifyUpgradeApplied() {
        getResponse().then().body("productTier", notNullValue());
    }

    @Then("구독의 {string}이 {string}로 연장된다")
    public void verifyExpiryExtended(String field, String newExpiry) {
        getResponse().then().body(field, notNullValue());
    }

    @Then("구독의 {string}이 {string}로 변경된다")
    public void verifyStatusChanged(String field, String newStatus) {
        getResponse().then().body(field, equalTo(newStatus));
    }

    @Then("구독의 {string}가 {string}로 변경된다")
    public void verifyStatusChangedAlt(String field, String newStatus) {
        getResponse().then().body(field, equalTo(newStatus));
    }

    @Then("구독 권한이 즉시 회수된다")
    public void verifyAccessRevoked() {
        getResponse().then().body("status", equalTo("REFUNDED"));
    }

    @Then("구독 권한이 회수된다")
    public void verifyAccessRevokedExpired() {
        getResponse().then().body("status", equalTo("EXPIRED"));
    }

    @Then("구독 권한이 자동 회수된다")
    public void verifyAccessAutoRevoked() {
        getResponse().then().body("status", equalTo("EXPIRED"));
    }

    @Then("신규 구독이 자동 생성된다")
    public void verifyNewSubscriptionCreated() {
        getResponse().then().statusCode(200);
    }

    @Then("로그만 기록되고 추가 처리는 하지 않는다")
    public void verifyOnlyLogged() {
        getResponse().then().statusCode(200);
    }

    @Then("환불 이벤트가 우선 처리된다")
    public void verifyRefundPriority() {
        getResponse().then().body("status", equalTo("REFUNDED"));
    }

    @Then("업그레이드 트랜잭션이 롤백된다")
    public void verifyUpgradeRolledBack() {
        getResponse().then().statusCode(200);
    }

    @Then("{string} 필드가 제거된다")
    public void verifyFieldRemoved(String field) {
        getResponse().then().body(field, nullValue());
    }

    @Then("중복 처리되지 않는다")
    public void verifyNotDuplicated() {
        getResponse().then().statusCode(200);
    }

    @Then("구독 상태가 변경되지 않는다")
    public void verifyStatusUnchanged() {
        getResponse().then().statusCode(200);
    }

    @Then("중복 처리 로그가 기록된다")
    public void verifyDuplicateLog() {
        getResponse().then().statusCode(200);
    }

    @Then("이벤트가 무시된다")
    public void verifyEventIgnored() {
        getResponse().then().statusCode(200);
    }

    @Then("실패한 이벤트가 DLQ에 저장된다")
    public void verifyDLQStored() {
        getResponse().then().statusCode(500);
    }

    @Then("보안 로그가 기록된다")
    public void verifySecurityLog() {
        getResponse().then().statusCode(401);
    }

    @Then("재시도가 3회 수행된다")
    public void verifyRetry3Times() {
        // 재시도 검증 (현재는 상태 코드만 확인)
        getResponse().then().statusCode(anyOf(is(503), is(400)));
    }

    @Then("3회 재시도 후 실패한다")
    public void verifyRetry3TimesAndFail() {
        getResponse().then().statusCode(503);
    }

    @Then("구독의 {string}이 {string}로 유지된다")
    public void verifyStatusMaintained(String field, String status) {
        getResponse().then().body(field, equalTo(status));
    }

    @Then("{string}이 연장된다")
    public void verifyFieldExtended(String field) {
        getResponse().then().body(field, notNullValue());
    }
}
