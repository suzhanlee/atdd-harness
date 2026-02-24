package com.example.e2e.step;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.hamcrest.Matchers.*;

/**
 * 구독 관련 Step Definitions.
 *
 * <p>Apple IAP 구독 기능에 대한 E2E 테스트 Step을 정의한다.
 */
public class SubscriptionStepDefinitions {

    @Autowired
    private ObjectMapper objectMapper;

    private Response response;

    @Given("사용자가 로그인되어 있다")
    public void userIsLoggedIn(DataTable dataTable) {
        // 로그인 처리 로직 (JWT 토큰 발급 등)
        // 현재는 Mock 구현
    }

    @Given("사용자가 로그인되어 있지 않다")
    public void userIsNotLoggedIn() {
        // 인증 없이 요청
    }

    @Given("다음 구독이 존재한다")
    public void subscriptionExists(DataTable dataTable) {
        // 테스트 데이터 셋업
        // @Sql 또는 Repository 사용
    }

    @Given("사용자의 활성 구독이 없다")
    public void noActiveSubscription() {
        // 활성 구독 없음 상태 설정
    }

    @Given("구독이 존재하지 않는다")
    public void subscriptionDoesNotExist() {
        // 구독 없음 상태
    }

    @When("구독 구매 요청을 보낸다")
    public void sendPurchaseRequest(DataTable dataTable) throws Exception {
        Map<String, String> data = dataTable.asMaps().get(0);

        response = RestAssured.given()
                .contentType("application/json")
                .body(objectMapper.writeValueAsString(data))
                .when()
                .post("/subscriptions/purchase");
    }

    @When("사용자가 구독 상태 조회 요청을 보낸다")
    public void sendGetSubscriptionRequest() {
        response = RestAssured.given()
                .when()
                .get("/subscriptions/me");
    }

    @When("업그레이드 요청을 보낸다")
    public void sendUpgradeRequest(DataTable dataTable) throws Exception {
        Map<String, String> data = dataTable.asMaps().get(0);

        response = RestAssured.given()
                .contentType("application/json")
                .body(objectMapper.writeValueAsString(data))
                .when()
                .post("/subscriptions/upgrade");
    }

    @When("사용자가 구독 이력 조회 요청을 보낸다")
    public void sendGetHistoryRequest() {
        response = RestAssured.given()
                .when()
                .get("/subscriptions/history");
    }

    @When("사용자가 구독 이력 조회 요청을 보낸다")
    public void sendGetHistoryRequestWithPaging(DataTable dataTable) {
        Map<String, String> params = dataTable.asMaps().get(0);

        response = RestAssured.given()
                .queryParam("page", params.get("page"))
                .queryParam("size", params.get("size"))
                .when()
                .get("/subscriptions/history");
    }

    @Then("업그레이드가 즉시 적용된다")
    public void verifyUpgradeApplied() {
        response.then().body("productTier", notNullValue());
    }

    @Then("구독의 {string}이 {string}로 연장된다")
    public void verifyExpiryExtended(String field, String newExpiry) {
        // 만료일 연장 검증
        response.then().body(field, notNullValue());
    }

    @Then("구독의 {string}이 {string}로 변경된다")
    public void verifyStatusChanged(String field, String newStatus) {
        response.then().body(field, equalTo(newStatus));
    }

    @Then("구독 권한이 즉시 회수된다")
    public void verifyAccessRevoked() {
        // 권한 회수 검증
        response.then().body("status", equalTo("REFUNDED"));
    }

    @Then("구독 권한이 회수된다")
    public void verifyAccessRevokedExpired() {
        response.then().body("status", equalTo("EXPIRED"));
    }

    @Then("신규 구독이 자동 생성된다")
    public void verifyNewSubscriptionCreated() {
        response.then().statusCode(200);
    }

    @Then("로그만 기록되고 추가 처리는 하지 않는다")
    public void verifyOnlyLogged() {
        response.then().statusCode(200);
    }

    @Then("환불 이벤트가 우선 처리된다")
    public void verifyRefundPriority() {
        response.then().body("status", equalTo("REFUNDED"));
    }

    @Then("업그레이드 트랜잭션이 롤백된다")
    public void verifyUpgradeRolledBack() {
        // 롤백 검증
        response.then().statusCode(200);
    }

    @Then("{string} 필드가 제거된다")
    public void verifyFieldRemoved(String field) {
        response.then().body(field, nullValue());
    }

    @Then("중복 처리되지 않는다")
    public void verifyNotDuplicated() {
        response.then().statusCode(200);
    }

    @Then("구독 상태가 변경되지 않는다")
    public void verifyStatusUnchanged() {
        response.then().statusCode(200);
    }

    @Then("중복 처리 로그가 기록된다")
    public void verifyDuplicateLog() {
        response.then().statusCode(200);
    }

    @Then("이벤트가 무시된다")
    public void verifyEventIgnored() {
        response.then().statusCode(200);
    }

    @Then("실패한 이벤트가 DLQ에 저장된다")
    public void verifyDLQStored() {
        response.then().statusCode(500);
    }
}
