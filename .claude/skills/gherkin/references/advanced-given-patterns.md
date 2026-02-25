# Advanced Given Patterns

## 개요

TestDataManager 기반의 고급 Gherkin Given 패턴. 복잡한 FK 관계, 상태 기반, 시간 기반, 복합 조건의 데이터 셋업을 자동화한다.

---

## 1. 단일 엔티티 생성 패턴

### 기본 패턴

| Gherkin 패턴 | TestDataManager 메서드 |
|-------------|----------------------|
| `다음 {Entity}가 존재한다` | `createFromDataTable(DataTable)` |
| `기본 {Entity}가 존재한다` | `createDefault()` |
| `{email} {Entity}가 존재한다` | `createByEmail(String)` |

### 예시

```gherkin
# DataTable 기반
Given 다음 사용자가 존재한다
  | email         | name   | status |
  | test@test.com | 테스터 | ACTIVE |

# 기본값 사용
Given 기본 사용자가 존재한다

# 특정 식별자로 생성
Given test@test.com 사용자가 존재한다
```

### Step Definition

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

## 2. FK 관계 Given 패턴

### 패턴

| Gherkin 패턴 | 설명 |
|-------------|------|
| `사용자 {email}의 {Entity}가 존재한다` | FK로 사용자 참조 |
| `{email} 사용자에게 {Entity}가 존재한다` | 사용자-엔티티 관계 |
| `다음 {Entity}가 존재한다 (userEmail 컬럼)` | DataTable 내 FK 참조 |

### 예시 1: FK 관계를 문장으로 표현

```gherkin
Given test@test.com 사용자가 존재한다
And 사용자 test@test.com의 주문이 존재한다
  | amount | status  |
  | 10000  | PENDING |
```

```java
@Given("사용자 {string}의 주문이 존재한다")
public void orderForUserExists(String email, DataTable dataTable) {
    // 1. 사용자 조회 또는 생성
    User user = userDataManager.findByEmailOrCreate(email);

    // 2. 사용자와 연관된 주문 생성
    List<Map<String, String>> rows = dataTable.asMaps();
    for (Map<String, String> row : rows) {
        Order order = Order.builder()
                .user(user)
                .amount(Integer.parseInt(row.get("amount")))
                .status(OrderStatus.valueOf(row.get("status")))
                .build();
        orderDataManager.save(order);
    }
}
```

### 예시 2: DataTable 내 FK 참조

```gherkin
Given 다음 주문이 존재한다
  | userEmail     | amount | status  |
  | test@test.com | 10000  | PENDING |
  | user@test.com | 20000  | COMPLETED |
```

```java
@Given("다음 주문이 존재한다")
public void ordersExist(DataTable dataTable) {
    // TestDataManager가 FK 자동 처리
    orderDataManager.createFromDataTable(dataTable);
}

// OrderTestDataManager 구현
private Order createOrderFromMap(Map<String, String> row) {
    // userEmail로 User 자동 조회/생성
    User user = userDataManager.findByEmailOrCreate(row.get("userEmail"));

    return Order.builder()
            .user(user)
            .amount(Integer.parseInt(row.get("amount")))
            .status(OrderStatus.valueOf(row.getOrDefault("status", "PENDING")))
            .build();
}
```

### 예시 3: 다중 FK 관계

```gherkin
Given 다음 결제 실패 로그가 존재한다
  | paymentEventId | failureCode | failureMessage |
  | 1              | CARD_ERROR  | 카드 오류      |
```

```java
// PaymentEventFailureLogDataManager
private PaymentEventFailureLog createFromMap(Map<String, String> row) {
    Long eventId = Long.parseLong(row.get("paymentEventId"));

    // FK 자동 처리: PaymentEvent 조회 또는 기본 생성
    PaymentEvent event = paymentEventDataManager.findById(eventId)
            .orElseGet(() -> paymentEventDataManager.createDefault(eventId));

    return PaymentEventFailureLog.builder()
            .paymentEvent(event)
            .failureCode(row.get("failureCode"))
            .failureMessage(row.get("failureMessage"))
            .build();
}
```

---

## 3. 상태 기반 Given 패턴

### 패턴

| Gherkin 패턴 | TestDataManager 메서드 |
|-------------|----------------------|
| `상태가 {status}인 {Entity}가 존재한다` | `createWithStatus(Status)` |
| `{status} 상태의 {Entity}가 존재한다` | `createWithStatus(Status)` |

### 예시

```gherkin
# 활성 사용자
Given 상태가 ACTIVE인 사용자가 존재한다
  | email         | name   |
  | test@test.com | 테스터 |

# 삭제된 사용자
Given 상태가 DELETED인 사용자가 존재한다
  | email            | name     |
  | deleted@test.com | 삭제자   |

# 완료된 주문
Given 상태가 COMPLETED인 주문이 존재한다
  | userEmail     | amount |
  | test@test.com | 10000  |
```

### Step Definition

```java
@Given("상태가 {string}인 사용자가 존재한다")
public void userWithStatusExists(String status, DataTable dataTable) {
    UserStatus userStatus = UserStatus.valueOf(status);
    List<Map<String, String>> rows = dataTable.asMaps();

    for (Map<String, String> row : rows) {
        userDataManager.createWithStatus(row.get("email"), userStatus);
    }
}

@Given("상태가 {string}인 주문이 존재한다")
public void orderWithStatusExists(String status, DataTable dataTable) {
    OrderStatus orderStatus = OrderStatus.valueOf(status);
    List<Map<String, String>> rows = dataTable.asMaps();

    for (Map<String, String> row : rows) {
        User user = userDataManager.findByEmailOrCreate(row.get("userEmail"));
        orderDataManager.createWithStatus(user, orderStatus);
    }
}
```

---

## 4. 시간 기반 Given 패턴

### 패턴

| Gherkin 패턴 | TestDataManager 메서드 |
|-------------|----------------------|
| `{n}일 전에 생성된 {Entity}가 존재한다` | `createDaysAgo(int)` |
| `{n}시간 전에 생성된 {Entity}가 존재한다` | `createHoursAgo(int)` |
| `{date}에 생성된 {Entity}가 존재한다` | `createAtDate(LocalDate)` |

### 예시

```gherkin
# 7일 전 생성
Given 7일 전에 생성된 사용자가 존재한다
  | email       | name    |
  | old@test.com | 오래된  |

# 30일 전 생성된 주문
Given 30일 전에 생성된 주문이 존재한다
  | userEmail     | amount |
  | old@test.com  | 5000   |

# 만료 예정 구독
Given 3일 후에 만료되는 구독이 존재한다
  | userEmail     | plan  |
  | test@test.com | PRO   |
```

### Step Definition

```java
@Given("{int}일 전에 생성된 사용자가 존재한다")
public void userCreatedDaysAgo(int daysAgo, DataTable dataTable) {
    List<Map<String, String>> rows = dataTable.asMaps();
    for (Map<String, String> row : rows) {
        userDataManager.createDaysAgo(row.get("email"), daysAgo);
    }
}

@Given("{int}일 전에 생성된 주문이 존재한다")
public void orderCreatedDaysAgo(int daysAgo, DataTable dataTable) {
    List<Map<String, String>> rows = dataTable.asMaps();
    for (Map<String, String> row : rows) {
        User user = userDataManager.findByEmailOrCreate(row.get("userEmail"));
        orderDataManager.createDaysAgo(user, daysAgo);
    }
}

@Given("{int}일 후에 만료되는 구독이 존재한다")
public void subscriptionExpiresInDays(int days, DataTable dataTable) {
    List<Map<String, String>> rows = dataTable.asMaps();
    for (Map<String, String> row : rows) {
        User user = userDataManager.findByEmailOrCreate(row.get("userEmail"));
        subscriptionDataManager.createExpiringInDays(user, days);
    }
}
```

---

## 5. 복합 조건 Given 패턴

### 패턴: 상태 + 시간

```gherkin
Given 7일 전에 생성되고 상태가 PENDING인 주문이 존재한다
  | userEmail     | amount |
  | test@test.com | 10000  |
```

```java
@Given("{int}일 전에 생성되고 상태가 {string}인 주문이 존재한다")
public void orderWithStatusCreatedDaysAgo(int daysAgo, String status, DataTable dataTable) {
    OrderStatus orderStatus = OrderStatus.valueOf(status);
    List<Map<String, String>> rows = dataTable.asMaps();

    for (Map<String, String> row : rows) {
        User user = userDataManager.findByEmailOrCreate(row.get("userEmail"));
        Order order = orderDataManager.createWithStatusAndDaysAgo(user, orderStatus, daysAgo);
    }
}
```

### 패턴: FK + 상태

```gherkin
Given test@test.com 사용자의 상태가 COMPLETED인 주문이 존재한다
  | amount |
  | 10000  |
```

```java
@Given("{string} 사용자의 상태가 {string}인 주문이 존재한다")
public void orderForUserWithStatus(String email, String status, DataTable dataTable) {
    User user = userDataManager.findByEmailOrCreate(email);
    OrderStatus orderStatus = OrderStatus.valueOf(status);
    List<Map<String, String>> rows = dataTable.asMaps();

    for (Map<String, String> row : rows) {
        orderDataManager.createWithStatus(user, orderStatus);
    }
}
```

### 패턴: 수량 지정

```gherkin
Given 3개의 주문이 존재한다
  | userEmail     | amount |
  | test@test.com | 10000  |
```

```java
@Given("{int}개의 주문이 존재한다")
public void multipleOrdersExist(int count, DataTable dataTable) {
    List<Map<String, String>> rows = dataTable.asMaps();
    Map<String, String> row = rows.get(0);

    User user = userDataManager.findByEmailOrCreate(row.get("userEmail"));
    int amount = Integer.parseInt(row.get("amount"));

    for (int i = 0; i < count; i++) {
        orderDataManager.createWithAmount(user, amount);
    }
}
```

---

## 6. TestDataManager 메서드 네이밍 규칙

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

## 7. 커스텀 Given 패턴 정의 방법

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

    @Autowired
    private PaymentEventTestDataManager paymentEventDataManager;

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

## 8. 전체 예시: E2E 테스트

### Feature 파일

```gherkin
Feature: 주문 취소

  Background:
    Given 기본 사용자가 존재한다

  Scenario: 대기 상태 주문 취소 성공
    Given 상태가 PENDING인 주문이 존재한다
      | userEmail     | amount |
      | test@test.com | 10000  |
    When 주문 취소 요청을 보낸다: 1
    Then 상태 코드 200를 받는다
    And 주문 상태는 CANCELLED이다

  Scenario: 완료된 주문 취소 실패
    Given 상태가 COMPLETED인 주문이 존재한다
      | userEmail     | amount |
      | test@test.com | 10000  |
    When 주문 취소 요청을 보낸다: 1
    Then 상태 코드 400를 받는다
    And 에러 메시지는 "완료된 주문은 취소할 수 없습니다"이다

  Scenario: 7일 지난 주문 취소 실패
    Given 7일 전에 생성된 상태가 PENDING인 주문이 존재한다
      | userEmail     | amount |
      | test@test.com | 10000  |
    When 주문 취소 요청을 보낸다: 1
    Then 상태 코드 400를 받는다
    And 에러 메시지는 "7일이 지난 주문은 취소할 수 없습니다"이다
```

### Step Definitions

```java
public class OrderStepDefinitions {

    @Autowired
    private UserTestDataManager userDataManager;

    @Autowired
    private OrderTestDataManager orderDataManager;

    @Before
    public void setUp() {
        orderDataManager.deleteAll();
        userDataManager.deleteAll();
    }

    @Given("기본 사용자가 존재한다")
    public void defaultUserExists() {
        userDataManager.createDefault();
    }

    @Given("상태가 {string}인 주문이 존재한다")
    public void orderWithStatusExists(String status, DataTable dataTable) {
        OrderStatus orderStatus = OrderStatus.valueOf(status);
        Map<String, String> row = dataTable.asMaps().get(0);

        User user = userDataManager.findByEmailOrCreate(row.get("userEmail"));
        orderDataManager.createWithStatus(user, orderStatus);
    }

    @Given("{int}일 전에 생성된 상태가 {string}인 주문이 존재한다")
    public void orderCreatedDaysAgoWithStatus(int days, String status, DataTable dataTable) {
        OrderStatus orderStatus = OrderStatus.valueOf(status);
        Map<String, String> row = dataTable.asMaps().get(0);

        User user = userDataManager.findByEmailOrCreate(row.get("userEmail"));
        orderDataManager.createWithStatusAndDaysAgo(user, orderStatus, days);
    }

    @When("주문 취소 요청을 보낸다: {long}")
    public void cancelOrder(Long orderId) {
        response = RestAssured.given()
                .when()
                .post("/api/v1/orders/" + orderId + "/cancel");
    }

    @Then("주문 상태는 {string}이다")
    public void verifyOrderStatus(String status) {
        response.then().body("status", equalTo(status));
    }
}
```

---

## 9. 패턴 선택 가이드

| 상황 | 추천 패턴 |
|------|----------|
| 단순 엔티티 생성 | `다음 {Entity}가 존재한다` |
| FK 관계 있는 엔티티 | DataTable 내 `userEmail` 컬럼 사용 |
| 특정 상태 필요 | `상태가 {status}인 {Entity}가 존재한다` |
| 시간 조건 필요 | `{n}일 전에 생성된 {Entity}가 존재한다` |
| 여러 조건 조합 | 복합 조건 패턴 사용 |
| 빠른 셋업 | `기본 {Entity}가 존재한다` |

---

## 10. 주의사항

1. **FK 순서 준수**: 독립 엔티티 → 종속 엔티티 순서로 Given 작성
2. **재사용성**: `findByXxxOrCreate` 패턴으로 중복 생성 방지
3. **명확성**: 패턴 이름만으로 의도가 드러나야 함
4. **일관성**: 프로젝트 내에서 동일한 패턴 사용
5. **유지보수**: TestDataManager에 공통 로직 캡슐화

---

## 관련 문서

- [Step 네이밍 컨벤션](step-naming-convention.md)
- [TestDataManager 템플릿](../tdd/references/test-data-manager-template.md)
- [E2E 테스트 템플릿](../tdd/references/e2e-test-template.md)
- [계층별 TDD 가이드](../tdd/references/layered-tdd-guide.md)
