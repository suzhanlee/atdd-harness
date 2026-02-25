# Layered TDD Guide

## 개요

Inside-Out 방식으로 계층별 TDD를 수행하는 가이드. 도메인 코어부터 API 레이어까지 테스트 주도로 개발한다.

---

## Inside-Out TDD 개발 순서

```
┌─────────────────────────────────────────────────────────────────┐
│                          API Layer                               │
│                    (Controller, E2E Test)                        │
│    4단계: Cucumber E2E 테스트 → Controller 구현                   │
├─────────────────────────────────────────────────────────────────┤
│                      Application Layer                           │
│                    (Service, Facade)                             │
│    3단계: Service 단위 테스트 → Service 구현                       │
├─────────────────────────────────────────────────────────────────┤
│                      Repository Layer                            │
│              (Repository, @DataJpaTest)                          │
│    2단계: Repository 통합 테스트 → Repository 구현                 │
├─────────────────────────────────────────────────────────────────┤
│                        Domain Layer                              │
│                  (Entity, VO, Domain Service)                    │
│    1단계: Domain 단위 테스트 → Entity/VO 구현                      │
└─────────────────────────────────────────────────────────────────┘
```

---

## 1단계: Domain Layer (도메인 단위 테스트)

### 특징
- **순수 Java 테스트**: Spring 컨텍스트 없이 실행
- **Classist 방식**: Mock 사용하지 않음
- **빠른 피드백**: 밀리초 단위 실행
- **불변성 검증**: Entity, VO의 불변 조건 테스트

### 테스트 구조

```java
package com.example.domain.entity;

import com.example.domain.vo.Email;
import com.example.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Order 도메인 테스트")
class OrderTest {

    @Test
    @DisplayName("주문 생성 - 정상")
    void createOrder() {
        // given
        User user = User.create("test@test.com", "name");
        Money amount = Money.wons(10000);

        // when
        Order order = Order.create(user, amount);

        // then
        assertThat(order.getUser()).isEqualTo(user);
        assertThat(order.getAmount()).isEqualTo(amount);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("주문 생성 - 금액이 0 이하면 예외")
    void createOrder_invalidAmount() {
        // given
        User user = User.create("test@test.com", "name");
        Money invalidAmount = Money.wons(0);

        // when & then
        assertThatThrownBy(() -> Order.create(user, invalidAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 금액은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("주문 취소 - 대기 상태에서만 가능")
    void cancel() {
        // given
        Order order = Order.create(user, Money.wons(10000));

        // when
        order.cancel();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("주문 취소 - 완료 상태면 예외")
    void cancel_alreadyCompleted() {
        // given
        Order order = Order.create(user, Money.wons(10000));
        order.complete();

        // when & then
        assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("완료된 주문은 취소할 수 없습니다");
    }
}
```

### 테스트 실행 명령어

```bash
# Domain 단위 테스트만 실행
./gradlew :{module}:test --tests "*.domain.*"

# 예시: kokpick 도메인 테스트
./gradlew :kokpick-domains:test
```

---

## 2단계: Repository Layer (통합 테스트)

### 특징
- **@DataJpaTest**: JPA 관련 빈만 로드
- **TestEntityManager**: 테스트용 EntityManager
- **실제 DB 테스트**: H2/MySQL Mode 또는 TestContainers
- **TestDataManager 사용**: 데이터 셋업 자동화

### 테스트 구조 (TestDataManager 활용)

```java
package com.example.integration.repository;

import com.example.domain.entity.Order;
import com.example.domain.entity.OrderStatus;
import com.example.domain.entity.User;
import com.example.test.fixture.OrderTestDataManager;
import com.example.test.fixture.UserTestDataManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({UserTestDataManager.class, OrderTestDataManager.class})
@DisplayName("OrderRepository 통합 테스트")
class OrderRepositoryTest {

    @Autowired
    private OrderTestDataManager orderDataManager;

    @Autowired
    private UserTestDataManager userDataManager;

    @Autowired
    private OrderRepository orderRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        orderDataManager.deleteAll();
        userDataManager.deleteAll();
        testUser = userDataManager.createDefault();
    }

    @Test
    @DisplayName("사용자별 주문 조회")
    void findByUser() {
        // given - TestDataManager로 데이터 준비
        orderDataManager.createDefault(testUser);
        orderDataManager.createDefault(testUser);
        orderDataManager.clear();

        // when
        List<Order> orders = orderRepository.findByUser(testUser);

        // then
        assertThat(orders).hasSize(2);
    }

    @Test
    @DisplayName("상태별 주문 조회 - 페이징")
    void findByStatusWithPaging() {
        // given
        for (int i = 0; i < 15; i++) {
            Order order = orderDataManager.createDefault(testUser);
            if (i < 5) {
                order.complete();
            }
        }
        orderDataManager.clear();

        // when
        Page<Order> page = orderRepository.findByStatus(
                OrderStatus.COMPLETED,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(page.getContent()).hasSize(5);
        assertThat(page.getTotalElements()).isEqualTo(5);
    }

    @Test
    @DisplayName("사용자 이메일로 주문 조회 - QueryDSL")
    void findByUserEmail() {
        // given
        User user = userDataManager.createByEmail("custom@test.com");
        orderDataManager.createDefault(user);
        orderDataManager.clear();

        // when
        List<Order> orders = orderRepository.findByUserEmail("custom@test.com");

        // then
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getUser().getEmail().getValue())
                .isEqualTo("custom@test.com");
    }
}
```

### 테스트 실행 명령어

```bash
# Repository 통합 테스트 실행
./gradlew integrationTest

# 특정 Repository 테스트만
./gradlew integrationTest --tests "*OrderRepository*"
```

---

## 3단계: Application Layer (Service 단위 테스트)

### 특징
- **Mockito 사용**: Repository 등 의존성 Mock
- **비즈니스 로직 검증**: Service 레이어의 로직
- **트랜잭션 경계**: @Transactional 동작 검증
- **TestDataManager 미사용**: Mock 기반이므로 DB 불필요

### 테스트 구조

```java
package com.example.application.service;

import com.example.application.dto.OrderRequest;
import com.example.application.dto.OrderResponse;
import com.example.domain.entity.Order;
import com.example.domain.entity.User;
import com.example.domain.repository.OrderRepository;
import com.example.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 단위 테스트")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.create(1L, "test@test.com", "테스터");
    }

    @Test
    @DisplayName("주문 생성 - 정상")
    void createOrder() {
        // given
        OrderRequest request = new OrderRequest(1L, 10000);
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(orderRepository.save(any(Order.class))).willAnswer(inv -> {
            Order order = inv.getArgument(0);
            return order;
        });

        // when
        OrderResponse response = orderService.create(request);

        // then
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getAmount()).isEqualTo(10000);
        then(orderRepository).should().save(any(Order.class));
    }

    @Test
    @DisplayName("주문 생성 - 사용자 없으면 예외")
    void createOrder_userNotFound() {
        // given
        OrderRequest request = new OrderRequest(999L, 10000);
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다");

        then(orderRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("주문 취소 - 정상")
    void cancelOrder() {
        // given
        Order order = Order.create(testUser, Money.wons(10000));
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        // when
        orderService.cancel(1L);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }
}
```

### 테스트 실행 명령어

```bash
# Service 단위 테스트 실행
./gradlew test --tests "*.application.*"

# 특정 Service 테스트만
./gradlew test --tests "*OrderServiceTest*"
```

---

## 4단계: API Layer (E2E 테스트)

### 특징
- **Cucumber + RestAssured**: BDD 스타일 E2E 테스트
- **TestDataManager 필수**: Given 절 데이터 셋업
- **전체 스택 테스트**: HTTP → Controller → Service → Repository → DB
- **실제 DB 사용**: @SpringBootTest

### Feature 파일

```gherkin
Feature: 주문 관리

  Scenario: 주문 생성 성공
    Given 다음 사용자가 존재한다
      | email         | name   |
      | test@test.com | 테스터 |
    When 주문 생성 요청을 보낸다
      | userEmail     | amount |
      | test@test.com | 10000  |
    Then 상태 코드 201를 받는다
    And 응답의 "amount" 필드는 10000이다

  Scenario: 존재하지 않는 사용자 주문 생성 실패
    When 주문 생성 요청을 보낸다
      | userEmail      | amount |
      | unknown@te.com | 10000  |
    Then 상태 코드 400를 받는다
    And 에러 메시지는 "사용자를 찾을 수 없습니다"이다
```

### Step Definition (TestDataManager 활용)

```java
package com.example.e2e.step;

import com.example.test.fixture.OrderTestDataManager;
import com.example.test.fixture.UserTestDataManager;
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

import static org.hamcrest.Matchers.*;

public class OrderStepDefinitions {

    @LocalServerPort
    private int port;

    @Autowired
    private UserTestDataManager userDataManager;

    @Autowired
    private OrderTestDataManager orderDataManager;

    private Response response;

    @Before
    public void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
        // DB 클린업
        orderDataManager.deleteAll();
        userDataManager.deleteAll();
    }

    @Given("다음 사용자가 존재한다")
    public void userExists(DataTable dataTable) {
        // TestDataManager로 자동 데이터 셋업
        userDataManager.createFromDataTable(dataTable);
    }

    @When("주문 생성 요청을 보낸다")
    public void sendCreateOrderRequest(DataTable dataTable) throws Exception {
        Map<String, String> request = dataTable.asMaps().get(0);

        response = RestAssured.given()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/orders");
    }

    @Then("상태 코드 {int}를 받는다")
    public void verifyStatusCode(int statusCode) {
        response.then().statusCode(statusCode);
    }

    @Then("응답의 {string} 필드는 {int}이다")
    public void verifyIntField(String field, int value) {
        response.then().body(field, equalTo(value));
    }

    @Then("에러 메시지는 {string}이다")
    public void verifyErrorMessage(String message) {
        response.then().body("message", equalTo(message));
    }
}
```

### 테스트 실행 명령어

```bash
# E2E 테스트 실행
./gradlew cucumber

# 특정 Feature만 실행
./gradlew cucumber -Dcucumber.filter.tags="@order"
```

---

## TestDataManager 사용 시나리오

### 사용하는 계층
| 계층 | TestDataManager 사용 | 이유 |
|------|---------------------|------|
| Domain | ❌ 사용 안 함 | 순수 Java, DB 없음 |
| Repository | ✅ 사용 | DB 데이터 준비 |
| Application | ❌ 사용 안 함 | Mock 기반, DB 없음 |
| API (E2E) | ✅ 사용 | Given 절 데이터 셋업 |

### Repository 테스트에서 사용

```java
@DataJpaTest
@Import({UserTestDataManager.class, OrderTestDataManager.class})
class OrderRepositoryTest {

    @Autowired
    private OrderTestDataManager orderDataManager;

    @Autowired
    private UserTestDataManager userDataManager;

    @BeforeEach
    void setUp() {
        orderDataManager.deleteAll();
        userDataManager.deleteAll();
    }

    @Test
    void findByStatus() {
        // given - TestDataManager로 간단히 셋업
        User user = userDataManager.createDefault();
        orderDataManager.createWithStatus(user, OrderStatus.COMPLETED);
        orderDataManager.createWithStatus(user, OrderStatus.PENDING);

        // when & then
        ...
    }
}
```

### E2E 테스트에서 사용

```java
@Given("다음 주문이 존재한다")
public void orderExists(DataTable dataTable) {
    // FK 관계가 있어도 TestDataManager가 자동 처리
    orderDataManager.createFromDataTable(dataTable);
}
```

---

## 테스트 실행 명령어 요약

| 명령어 | 설명 | 계층 |
|--------|------|------|
| `./gradlew test --tests "*.domain.*"` | Domain 단위 테스트 | Domain |
| `./gradlew integrationTest` | Repository 통합 테스트 | Repository |
| `./gradlew test --tests "*.application.*"` | Service 단위 테스트 | Application |
| `./gradlew cucumber` | E2E 테스트 | API |
| `./gradlew check` | 모든 테스트 실행 | 전체 |
| `./gradlew jacocoTestReport` | 커버리지 리포트 | 전체 |

---

## 검증 체크리스트

### Domain Layer
```bash
./gradlew test --tests "*.domain.*"
# ✅ 모든 테스트 통과 확인
```

### Repository Layer
```bash
./gradlew integrationTest
# ✅ 모든 테스트 통과 확인
# ✅ TestDataManager 정상 동작 확인
```

### Application Layer
```bash
./gradlew test --tests "*.application.*"
# ✅ 모든 테스트 통과 확인
# ✅ Mock 동작 확인
```

### API Layer
```bash
./gradlew cucumber
# ✅ 모든 시나리오 통과 확인
# ✅ TestDataManager Given 절 동작 확인
```

### 전체 검증
```bash
./gradlew check
# ✅ 모든 테스트 통과 (0 failures)
# ✅ 컴파일 에러 없음
```

---

## Best Practices

1. **Inside-Out 순서 준수**: Domain → Repository → Application → API
2. **테스트 격리**: 각 테스트는 독립적으로 실행 가능해야 함
3. **TestDataManager 재사용**: Repository/E2E 테스트에서 공통 사용
4. **명확한 Given/When/Then**: 테스트 의도가 명확해야 함
5. **커버리지 목표**: Domain 85%+, 전체 80%+
6. **빠른 피드백**: Domain → 순차 실행으로 빠른 실패 확인

---

## 관련 문서

- [TestDataManager 템플릿](test-data-manager-template.md)
- [E2E 테스트 템플릿](e2e-test-template.md)
- [Repository 테스트 템플릿](repository-test-template.md)
- [고급 Given 패턴](../gherkin/references/advanced-given-patterns.md)
