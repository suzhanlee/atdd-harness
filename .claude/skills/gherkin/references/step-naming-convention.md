# Step Naming Convention

## 목적
TDD Step Definition 자동 생성을 위한 표준 Step 패턴 정의

---

## Given 패턴

| 패턴 | 파라미터 | Step Definition |
|------|----------|-----------------|
| `{Entity}가 존재한다` | DataTable | `@Given("{Entity}가 존재한다")` |
| `데이터베이스가 초기화되어 있다` | 없음 | `@Given("데이터베이스가 초기화되어 있다")` |
| `사용자가 로그인되어 있다` | 없음 | `@Given("사용자가 로그인되어 있다")` |

### 예시
```gherkin
Given 다음 사용자가 존재한다
  | id | email         | name |
  | 1  | test@test.com | 테스터 |
```

---

## When 패턴

| 패턴 | 파라미터 | Step Definition |
|------|----------|-----------------|
| `{Entity} 생성 요청을 보낸다` | DataTable | `@When("{Entity} 생성 요청을 보낸다")` |
| `{Entity} 조회 요청을 보낸다: {id}` | id | `@When("{Entity} 조회 요청을 보낸다: {int}")` |
| `{Entity} 수정 요청을 보낸다: {id}` | id, DataTable | `@When("{Entity} 수정 요청을 보낸다: {int}")` |
| `{Entity} 삭제 요청을 보낸다: {id}` | id | `@When("{Entity} 삭제 요청을 보낸다: {int}")` |
| `{action} 요청을 보낸다` | DataTable | `@When("{action} 요청을 보낸다")` |

### 예시
```gherkin
When 회원가입 요청을 보낸다
  | email         | password     | name   |
  | test@test.com | password123! | 테스터 |

When 사용자 조회 요청을 보낸다: 1
```

---

## Then 패턴

| 패턴 | 파라미터 | Step Definition |
|------|----------|-----------------|
| `상태 코드 {int}를 받는다` | int | `@Then("상태 코드 {int}를 받는다")` |
| `응답의 "{field}" 필드는 "{value}"이다` | field, value | `@Then("응답의 {string} 필드는 {string}이다")` |
| `응답의 "{field}" 필드는 {int}이다` | field, int | `@Then("응답의 {string} 필드는 {int}이다")` |
| `응답에 "{field}" 필드가 존재한다` | field | `@Then("응답에 {string} 필드가 존재한다")` |
| `에러 메시지는 "{message}"이다` | message | `@Then("에러 메시지는 {string}이다")` |

### 예시
```gherkin
Then 상태 코드 201를 받는다
And 응답의 "email" 필드는 "test@test.com"이다
And 응답의 "id" 필드는 1이다
```

---

## 변환 규칙

### 자유 형식 → 정형화 규칙

| 사용자 입력 | 변환 결과 |
|-------------|-----------|
| `유저를 만든다` | `사용자 생성 요청을 보낸다` |
| `회원가입 한다` | `회원가입 요청을 보낸다` |
| `성공한다` | `상태 코드 200를 받는다` |
| `실패한다` | `상태 코드 400를 받는다` (문맥에 따라) |
| `201 반환` | `상태 코드 201를 받는다` |

### HTTP 메서드 매핑

| 키워드 | HTTP 메서드 | 패턴 |
|--------|-------------|------|
| 생성, 등록, 추가 | POST | `{Entity} 생성 요청을 보낸다` |
| 조회, 검색, 찾기 | GET | `{Entity} 조회 요청을 보낸다` |
| 수정, 변경, 업데이트 | PUT/PATCH | `{Entity} 수정 요청을 보낸다: {id}` |
| 삭제, 제거 | DELETE | `{Entity} 삭제 요청을 보낸다: {id}` |

---

## Data Table 형식

### 요청 본문 (When)
```gherkin
When 회원가입 요청을 보낸다
  | email         | password     | name   |
  | test@test.com | password123! | 테스터 |
```

### 엔티티 데이터 (Given)
```gherkin
Given 다음 사용자가 존재한다
  | id | email         | name   |
  | 1  | test@test.com | 테스터 |
```

### 응답 검증 (Then)
```gherkin
Then 응답 본문은 다음과 같다
  | field  | value        |
  | email  | test@test.com|
  | name   | 테스터       |
```

---

## Step Definition 구현 가이드

### Given Step 예시
```java
@Given("{string}가 존재한다")
public void entityExists(String entityName, DataTable dataTable) {
    List<Map<String, String>> rows = dataTable.asMaps();
    // 엔티티 생성 및 저장 로직
}

@Given("데이터베이스가 초기화되어 있다")
public void databaseInitialized() {
    databaseCleanup.execute();
}
```

### When Step 예시
```java
@When("{string} 생성 요청을 보낸다")
public void sendCreateRequest(String entityName, DataTable dataTable) {
    Map<String, String> request = dataTable.asMaps().get(0);
    response = RestAssured.given()
        .contentType(ContentType.JSON)
        .body(request)
        .post("/api/v1/" + entityName.toLowerCase());
}

@When("{string} 조회 요청을 보낸다: {int}")
public void sendGetRequest(String entityName, int id) {
    response = RestAssured.given()
        .get("/api/v1/" + entityName.toLowerCase() + "/" + id);
}

@When("{string} 수정 요청을 보낸다: {int}")
public void sendUpdateRequest(String entityName, int id, DataTable dataTable) {
    Map<String, String> request = dataTable.asMaps().get(0);
    response = RestAssured.given()
        .contentType(ContentType.JSON)
        .body(request)
        .put("/api/v1/" + entityName.toLowerCase() + "/" + id);
}

@When("{string} 삭제 요청을 보낸다: {int}")
public void sendDeleteRequest(String entityName, int id) {
    response = RestAssured.given()
        .delete("/api/v1/" + entityName.toLowerCase() + "/" + id);
}
```

### Then Step 예시
```java
@Then("상태 코드 {int}를 받는다")
public void verifyStatusCode(int statusCode) {
    response.then().statusCode(statusCode);
}

@Then("응답의 {string} 필드는 {string}이다")
public void verifyStringField(String field, String value) {
    response.then().body(field, equalTo(value));
}

@Then("응답의 {string} 필드는 {int}이다")
public void verifyIntField(String field, int value) {
    response.then().body(field, equalTo(value));
}

@Then("응답에 {string} 필드가 존재한다")
public void verifyFieldExists(String field) {
    response.then().body(field, notNullValue());
}

@Then("에러 메시지는 {string}이다")
public void verifyErrorMessage(String message) {
    response.then().body("message", equalTo(message));
}
```

---

## 품질 검증 체크리스트

| 항목 | 검증 내용 | 합격 기준 |
|------|-----------|-----------|
| Step 패턴 | TDD 인식 가능한 패턴 사용 | 100% 준수 |
| Data Table | 올바른 형식의 테이블 | 필수 필드 포함 |
| 상태 코드 | `{int}` 파라미터 사용 | 모든 Then에 명시 |
| 중복 Step | 동일 의미의 다른 표현 | 없음 |

---

## 주의사항

1. **일관성**: 동일한 작업에는 항상 동일한 Step 패턴 사용
2. **명확성**: 모호한 표현보다는 구체적인 패턴 사용
3. **재사용성**: Scenario Outline과 Examples를 활용하여 중복 최소화
4. **매개변수**: 하드코딩보다는 파라미터화된 Step 사용

---

## 관련 문서

- [고급 Given 패턴](advanced-given-patterns.md) - FK, 상태, 시간 기반, TestDataManager 패턴 상세 가이드
- [TestDataManager 템플릿](../tdd/references/test-data-manager-template.md) - 구현 가이드
- [E2E 테스트 템플릿](../tdd/references/e2e-test-template.md) - Step Definition 작성
