# E2E Test Template (Cucumber + RestAssured)

## Step Definition 기본 구조

```java
package com.example.e2e;

import com.example.application.dto.EntityNameRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;

public class EntityNameStepDefinitions {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private Response response;

    @Before
    public void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
    }
}
```

---

## @Sql 어노테이션 패턴

### 단일 SQL 파일 실행

```java
import org.springframework.test.context.jdbc.Sql;

@Sql("/testdata/users.sql")
public class UserStepDefinitions {
    // users.sql이 테스트 실행 전 자동으로 실행됨
}
```

### 다중 SQL 파일 실행 (순서 중요!)

```java
@Sql({
    "/testdata/users.sql",      // 1순위: 독립 엔티티
    "/testdata/products.sql",   // 2순위: 독립 엔티티
    "/testdata/orders.sql"      // 3순위: users를 참조하는 FK
})
public class OrderStepDefinitions {
    // FK 제약조건을 고려한 순서로 실행
}
```

### 실행 시점 제어

```java
@Sql(scripts = "/testdata/cleanup.sql",
     executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CleanupStepDefinitions {
    // 테스트 완료 후 자동 정리
}
```

### @Before/@After 훅과 조합

```java
public class DatabaseHooks {

    @Before
    public void cleanDatabase() {
        // jdbcTemplate으로 FK 역순 삭제
        // 또는 TRUNCATE 사용
    }

    @After
    public void verifyCleanup() {
        // 테스트 후 상태 검증
    }
}
```

> **참고:** SQL 작성 가이드는 [sql-data-guide.md](sql-data-guide.md) 참조

---

## REST API Step Patterns

### GET 요청

```java
@When("Entity 조회 요청을 보낸다: {long}")
public void sendFindByIdRequest(Long id) {
    response = RestAssured.given()
            .when()
            .get("/entities/" + id);
}

@When("Entity 목록 조회 요청을 보낸다")
public void sendFindAllRequest() {
    response = RestAssured.given()
            .queryParam("page", 0)
            .queryParam("size", 10)
            .when()
            .get("/entities");
}
```

### POST 요청

```java
@When("Entity 생성 요청을 보낸다")
public void sendCreateRequest(DataTable dataTable) throws Exception {
    Map<String, String> data = dataTable.asMaps().get(0);

    response = RestAssured.given()
            .contentType("application/json")
            .body(objectMapper.writeValueAsString(data))
            .when()
            .post("/entities");
}
```

### PUT 요청

```java
@When("Entity 수정 요청을 보낸다: {long}")
public void sendUpdateRequest(Long id, DataTable dataTable) throws Exception {
    Map<String, String> data = dataTable.asMaps().get(0);

    response = RestAssured.given()
            .contentType("application/json")
            .body(objectMapper.writeValueAsString(data))
            .when()
            .put("/entities/" + id);
}
```

### DELETE 요청

```java
@When("Entity 삭제 요청을 보낸다: {long}")
public void sendDeleteRequest(Long id) {
    response = RestAssured.given()
            .when()
            .delete("/entities/" + id);
}
```

### 인증 헤더 처리

```java
@When("인증된 사용자가 Entity 생성 요청을 보낸다")
public void sendAuthenticatedCreateRequest(DataTable dataTable) throws Exception {
    Map<String, String> data = dataTable.asMaps().get(0);
    String token = getAuthToken(); // 인증 토큰 획득

    response = RestAssured.given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + token)
            .body(objectMapper.writeValueAsString(data))
            .when()
            .post("/entities");
}
```

---

## Database Setup Steps

### @Given 데이터베이스 초기화

```java
@Given("데이터베이스가 초기화되어 있다")
public void databaseIsInitialized() {
    // 방법 1: Repository 사용
    // userRepository.deleteAll();

    // 방법 2: @Sql 사용 권장
    // 클래스 레벨에 @Sql 어노테이션 선언
}
```

### @Given 기존 데이터 존재

```java
@Given("다음 Entity가 존재한다")
public void entityExists(DataTable dataTable) {
    // DataTable에서 데이터 읽어 생성
    for (Map<String, String> row : dataTable.asMaps()) {
        EntityNameRequest request = new EntityNameRequest(row.get("fieldName"));
        // repository.save(EntityName.create(request.getFieldName()));
    }
}
```

> **권장:** 복잡한 데이터는 @Sql로 SQL 파일을 사용하세요.
> 상세 가이드: [sql-data-guide.md](sql-data-guide.md)

---

## 응답 검증 Steps

### 상태 코드 검증

```java
@Then("상태 코드 {int}를 받는다")
public void verifyStatusCode(int statusCode) {
    response.then().statusCode(statusCode);
}
```

### 필드 값 검증

```java
@Then("응답의 {string} 필드는 {string}이다")
public void verifyResponseField(String field, String value) {
    response.then().body(field, equalTo(value));
}

@Then("응답의 {string} 필드는 {int}이다")
public void verifyResponseFieldInt(String field, int value) {
    response.then().body(field, equalTo(value));
}
```

### 필드 존재 검증

```java
@Then("응답에 {string} 필드가 존재한다")
public void verifyFieldExists(String field) {
    response.then().body(field, notNullValue());
}
```

### 에러 메시지 검증

```java
@Then("에러 메시지는 {string}이다")
public void verifyErrorMessage(String message) {
    response.then().body("message", containsString(message));
}
```

### 목록 검증

```java
@Then("응답 목록의 크기는 {int}이다")
public void verifyListSize(int size) {
    response.then().body("size()", equalTo(size));
}

@Then("응답 목록의 {int}번째 항목의 {string} 필드는 {string}이다")
public void verifyListItem(int index, String field, String value) {
    response.then().body("[" + index + "]." + field, equalTo(value));
}
```

---

## Test Naming Convention

| Step | Naming Pattern | Example |
|------|---------------|---------|
| Given | `{entity}가 존재한다` | `사용자가 존재한다` |
| When | `{action} 요청을 보낸다` | `회원가입 요청을 보낸다` |
| Then | `상태 코드 {code}를 받는다` | `상태 코드 201를 받는다` |
| Then | `응답의 {field} 필드는 {value}이다` | `응답의 email 필드는 test@test.com이다` |

---

## Best Practices

1. **Step 재사용성** - 공통 Step Definition을 별도 클래스로 분리하여 재사용
2. **한글 Step 작성** - 비개발자(PO, 기획자)도 이해할 수 있는 한글 Step 사용
3. **DataTable 활용** - 구체적인 테스트 데이터는 DataTable로 표현
4. **@Sql로 복잡한 데이터 셋업** - 복잡한 FK 관계는 @Sql과 SQL 파일로 관리
5. **독립적인 테스트** - 각 시나리오가 다른 시나리오에 영향을 주지 않도록 격리
6. **명확한 검증** - 상태 코드, 응답 본문, 에러 메시지를 명확히 검증

---

## 관련 문서

- [SQL 데이터 가이드](sql-data-guide.md) - INSERT SQL 작성법
- [단위 테스트 템플릿](unit-test-template.md) - Service/Entity 테스트
- [통합 테스트 템플릿](integration-test-template.md) - Repository 테스트
