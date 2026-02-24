package com.example.e2e.step;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * 공통 Step Definitions.
 *
 * <p>모든 Feature에서 공통으로 사용하는 Step들을 정의한다.
 */
public class CommonStepDefinitions {

    @LocalServerPort
    private int port;

    protected Response response;

    @Before
    public void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Given("데이터베이스가 초기화되어 있다")
    public void databaseIsInitialized() {
        // @DataJpaTest의 기본 동작으로 자동 초기화됨
        // 필요시 여기서 추가 초기화 로직 구현
    }

    @Then("상태 코드 {int}를 받는다")
    public void verifyStatusCode(int statusCode) {
        response.then().statusCode(statusCode);
    }

    @Then("응답의 {string} 필드는 {string}이다")
    public void verifyResponseFieldString(String field, String value) {
        response.then().body(field, equalTo(value));
    }

    @Then("응답의 {string} 필드는 {int}이다")
    public void verifyResponseFieldInt(String field, int value) {
        response.then().body(field, equalTo(value));
    }

    @Then("응답의 {string} 필드에 {string}가 포함된다")
    public void verifyResponseFieldContains(String field, String value) {
        response.then().body(field, containsString(value));
    }

    @Then("응답의 {string} 필드가 존재한다")
    public void verifyFieldExists(String field) {
        response.then().body(field, notNullValue());
    }

    @Then("응답의 {string} 필드가 제거된다")
    public void verifyFieldNotExists(String field) {
        response.then().body(field, nullValue());
    }

    @Then("응답의 {string} 배열 크기는 {int}이다")
    public void verifyArraySize(String field, int size) {
        response.then().body(field + ".size()", equalTo(size));
    }

    @Then("응답의 {string} 배열은 비어있다")
    public void verifyArrayIsEmpty(String field) {
        response.then().body(field + ".size()", equalTo(0));
    }
}
