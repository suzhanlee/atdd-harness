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

## TestDataManager 친화적 Given 패턴

### 단일 엔티티 생성

| 패턴 | TestDataManager 메서드 | 설명 |
|------|----------------------|------|
| `다음 {Entity}가 존재한다` | `createFromDataTable(DataTable)` | DataTable 기반 생성 |
| `기본 {Entity}가 존재한다` | `createDefault()` | 기본값으로 빠른 생성 |
| `{email} {Entity}가 존재한다` | `createByEmail(String)` | 특정 식별자로 생성 |

#### 예시
```gherkin
# DataTable 기반
Given 다음 사용자가 존재한다
  | email         | name   | status |
  | test@test.com | 테스터 | ACTIVE |

# 기본값 사용
Given 기본 사용자가 존재한다

# 특정 이메일
Given test@test.com 사용자가 존재한다
```

#### Step Definition
```java
@Given("다음 사용자가 존재한다")
public void userExists(DataTable dataTable) {
    userDataManager.createFromDataTable(dataTable);
}

@Given("기본 사용자가 존재한다")
public void defaultUserExists() {
    userDataManager.createDefault();
}

@Given("{string} 사용자가 존재한다")
public void userWithEmailExists(String email) {
    userDataManager.createByEmail(email);
}
```

---

### FK 관계가 있는 엔티티

| 패턴 | 설명 |
|------|------|
| `사용자 {email}의 {Entity}가 존재한다` | FK로 사용자 참조 |
| `{email} 사용자에게 {Entity}가 존재한다` | 사용자-엔티티 관계 |
| DataTable 내 `userEmail` 컬럼 | FK 자동 참조 |

#### 예시 1: 문장으로 FK 표현
```gherkin
Given test@test.com 사용자가 존재한다
And 사용자 test@test.com의 주문이 존재한다
  | amount | status  |
  | 10000  | PENDING |
```

#### 예시 2: DataTable 내 FK 컬럼
```gherkin
Given 다음 주문이 존재한다
  | userEmail     | amount | status  |
  | test@test.com | 10000  | PENDING |
  | user@test.com | 20000  | COMPLETED |
```

#### Step Definition
```java
@Given("사용자 {string}의 주문이 존재한다")
public void orderForUserExists(String email, DataTable dataTable) {
    User user = userDataManager.findByEmailOrCreate(email);
    orderDataManager.createForUser(user, dataTable);
}

@Given("다음 주문이 존재한다")
public void ordersExist(DataTable dataTable) {
    // OrderDataManager가 userEmail로 User 자동 조회/생성
    orderDataManager.createFromDataTable(dataTable);
}
```

---

### 상태 기반 엔티티

| 패턴 | TestDataManager 메서드 |
|------|----------------------|
| `상태가 {status}인 {Entity}가 존재한다` | `createWithStatus(Status)` |
| `{status} 상태의 {Entity}가 존재한다` | `createWithStatus(Status)` |

#### 예시
```gherkin
Given 상태가 COMPLETED인 주문이 존재한다
  | userEmail     | amount |
  | test@test.com | 10000  |

Given 상태가 DELETED인 사용자가 존재한다
  | email            | name   |
  | deleted@test.com | 삭제자 |
```

#### Step Definition
```java
@Given("상태가 {string}인 주문이 존재한다")
public void orderWithStatusExists(String status, DataTable dataTable) {
    OrderStatus orderStatus = OrderStatus.valueOf(status);
    Map<String, String> row = dataTable.asMaps().get(0);

    User user = userDataManager.findByEmailOrCreate(row.get("userEmail"));
    orderDataManager.createWithStatus(user, orderStatus);
}
```

---

### 시간 기반 엔티티

| 패턴 | TestDataManager 메서드 |
|------|----------------------|
| `{n}일 전에 생성된 {Entity}가 존재한다` | `createDaysAgo(int)` |
| `{n}시간 전에 생성된 {Entity}가 존재한다` | `createHoursAgo(int)` |
| `{n}일 후에 만료되는 {Entity}가 존재한다` | `createExpiringInDays(int)` |

#### 예시
```gherkin
Given 7일 전에 생성된 주문이 존재한다
  | userEmail     | amount |
  | test@test.com | 10000  |

Given 30일 전에 생성된 사용자가 존재한다
  | email       | name    |
  | old@test.com | 오래된  |

Given 3일 후에 만료되는 구독이 존재한다
  | userEmail     | plan |
  | test@test.com | PRO  |
```

#### Step Definition
```java
@Given("{int}일 전에 생성된 주문이 존재한다")
public void orderCreatedDaysAgo(int daysAgo, DataTable dataTable) {
    Map<String, String> row = dataTable.asMaps().get(0);

    User user = userDataManager.findByEmailOrCreate(row.get("userEmail"));
    orderDataManager.createDaysAgo(user, daysAgo);
}
```

---

### 복합 조건

| 패턴 | 설명 |
|------|------|
| `{n}일 전에 생성되고 상태가 {status}인 {Entity}가 존재한다` | 시간 + 상태 |
| `{email} 사용자의 상태가 {status}인 {Entity}가 존재한다` | FK + 상태 |
| `{n}개의 {Entity}가 존재한다` | 수량 지정 |

#### 예시
```gherkin
# 시간 + 상태
Given 7일 전에 생성되고 상태가 PENDING인 주문이 존재한다
  | userEmail     | amount |
  | test@test.com | 10000  |

# FK + 상태
Given test@test.com 사용자의 상태가 COMPLETED인 주문이 존재한다
  | amount |
  | 10000  |

# 수량 지정
Given 3개의 주문이 존재한다
  | userEmail     | amount |
  | test@test.com | 10000  |
```

#### Step Definition
```java
@Given("{int}일 전에 생성되고 상태가 {string}인 주문이 존재한다")
public void orderWithStatusAndDaysAgo(int daysAgo, String status, DataTable dataTable) {
    OrderStatus orderStatus = OrderStatus.valueOf(status);
    Map<String, String> row = dataTable.asMaps().get(0);

    User user = userDataManager.findByEmailOrCreate(row.get("userEmail"));
    orderDataManager.createWithStatusAndDaysAgo(user, orderStatus, daysAgo);
}

@Given("{int}개의 주문이 존재한다")
public void multipleOrdersExist(int count, DataTable dataTable) {
    Map<String, String> row = dataTable.asMaps().get(0);

    User user = userDataManager.findByEmailOrCreate(row.get("userEmail"));
    for (int i = 0; i < count; i++) {
        orderDataManager.createDefault(user);
    }
}
```

---

## 커스텀 Given 패턴 정의 방법

### Step 1: Gherkin Feature에 패턴 작성

```gherkin
Feature: 결제 실패 로그 관리

  Scenario: 결제 실패 로그 생성
    Given 결제 이벤트 1번이 존재한다
    And 결제 이벤트 1번의 실패 로그가 존재한다
      | failureCode | failureMessage |
      | CARD_ERROR  | 카드 한도 초과 |
```

### Step 2: TestDataManager에 편의 메서드 추가

```java
@Component
public class PaymentEventFailureLogTestDataManager extends BaseTestDataManager<PaymentEventFailureLog, Long> {

    /**
     * 이벤트 ID로 실패 로그 생성
     */
    public PaymentEventFailureLog createForEventId(Long eventId, String code, String message) {
        PaymentEvent event = paymentEventDataManager.findById(eventId)
                .orElseGet(() -> paymentEventDataManager.createDefault(eventId));
        return createForEvent(event, code, message);
    }
}
```

### Step 3: Step Definition 구현

```java
@Given("결제 이벤트 {long}번이 존재한다")
public void paymentEventExists(Long eventId) {
    paymentEventDataManager.findById(eventId)
            .orElseGet(() -> paymentEventDataManager.createDefault(eventId));
}

@Given("결제 이벤트 {long}번의 실패 로그가 존재한다")
public void failureLogForEventExists(Long eventId, DataTable dataTable) {
    List<Map<String, String>> rows = dataTable.asMaps();
    for (Map<String, String> row : rows) {
        failureLogDataManager.createForEventId(
                eventId,
                row.get("failureCode"),
                row.get("failureMessage")
        );
    }
}
```

---

## TestDataManager 메서드 네이밍 규칙

| 패턴 | 메서드명 | 설명 |
|------|---------|------|
| DataTable 변환 | `createFromDataTable(DataTable)` | Gherkin Given절 기본 |
| 기본 생성 | `createDefault()` | 빠른 셋업용 |
| 필드 지정 생성 | `createByXxx(value)` | 특정 필드 값으로 생성 |
| 상태 지정 생성 | `createWithStatus(status)` | 상태 기반 |
| 시간 지정 생성 | `createDaysAgo(days)` / `createHoursAgo(hours)` | 시간 기반 |
| FK 관계 생성 | `createForXxx(xxx)` | FK 엔티티 지정 |
| 조회 또는 생성 | `findByXxxOrCreate(value)` | 중복 방지 |
| 복합 조건 생성 | `createWithStatusAndDaysAgo(...)` | 상태 + 시간 |

---

## 관련 문서

- [고급 Given 패턴](advanced-given-patterns.md) - FK, 상태, 시간 기반 상세 가이드
- [TestDataManager 템플릿](../tdd/references/test-data-manager-template.md) - 구현 가이드
- [E2E 테스트 템플릿](../tdd/references/e2e-test-template.md) - Step Definition 작성
