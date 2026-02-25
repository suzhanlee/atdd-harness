# TestDataManager Template

## 개요

EntityManager를 래핑하여 테스트 데이터 셋업을 자동화하는 유틸리티 패턴. E2E 테스트와 Repository 테스트에서 Given 절의 데이터 준비를 간소화한다.

---

## BaseTestDataManager 추상 클래스

```java
package com.example.test.fixture;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 테스트 데이터 관리를 위한 베이스 클래스
 *
 * @param <E> 엔티티 타입
 * @param <ID> 식별자 타입
 */
public abstract class BaseTestDataManager<E, ID> {

    @PersistenceContext
    protected EntityManager entityManager;

    protected final Class<E> entityClass;

    protected BaseTestDataManager(Class<E> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * 엔티티 저장 및 flush
     */
    protected E save(E entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    /**
     * 엔티티 저장 후 영속성 컨텍스트 초기화
     */
    protected E saveAndClear(E entity) {
        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();
        return entity;
    }

    /**
     * ID로 조회
     */
    protected Optional<E> findById(ID id) {
        return Optional.ofNullable(entityManager.find(entityClass, id));
    }

    /**
     * 전체 조회
     */
    protected List<E> findAll() {
        String jpql = String.format("SELECT e FROM %s e", entityClass.getSimpleName());
        return entityManager.createQuery(jpql, entityClass).getResultList();
    }

    /**
     * 조건으로 조회 (단일)
     */
    protected Optional<E> findByField(String fieldName, Object value) {
        String jpql = String.format("SELECT e FROM %s e WHERE e.%s = :value",
                entityClass.getSimpleName(), fieldName);
        try {
            E result = entityManager.createQuery(jpql, entityClass)
                    .setParameter("value", value)
                    .getSingleResult();
            return Optional.of(result);
        } catch (jakarta.persistence.NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * DB 클린업 (테스트 간 격리)
     */
    public void deleteAll() {
        String jpql = String.format("DELETE FROM %s", entityClass.getSimpleName());
        entityManager.createQuery(jpql).executeUpdate();
        entityManager.flush();
    }

    /**
     * 영속성 컨텍스트 초기화
     */
    public void clear() {
        entityManager.clear();
    }
}
```

---

## Entity별 TestDataManager 구현 예시

### 1. User TestDataManager

```java
package com.example.test.fixture;

import com.example.domain.entity.User;
import com.example.domain.entity.UserStatus;
import com.example.domain.vo.Email;
import io.cucumber.datatable.DataTable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@Transactional
public class UserTestDataManager extends BaseTestDataManager<User, Long> {

    public UserTestDataManager() {
        super(User.class);
    }

    /**
     * DataTable에서 User 생성
     *
     * Gherkin:
     * Given 다음 사용자가 존재한다
     *   | email         | name   | status  |
     *   | test@test.com | 테스터 | ACTIVE  |
     */
    public List<User> createFromDataTable(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        return rows.stream()
                .map(this::createUserFromMap)
                .map(this::save)
                .toList();
    }

    /**
     * 기본 사용자 생성 (빠른 셋업)
     */
    public User createDefault() {
        return createByEmail("test@test.com");
    }

    /**
     * 이메일로 기본 사용자 생성
     */
    public User createByEmail(String email) {
        User user = User.builder()
                .email(new Email(email))
                .name("테스터")
                .status(UserStatus.ACTIVE)
                .build();
        return save(user);
    }

    /**
     * 이메일로 조회하거나 없으면 생성
     */
    public User findByEmailOrCreate(String email) {
        return findByField("email.value", email)
                .orElseGet(() -> createByEmail(email));
    }

    /**
     * 상태가 있는 사용자 생성
     */
    public User createWithStatus(String email, UserStatus status) {
        User user = User.builder()
                .email(new Email(email))
                .name("테스터")
                .status(status)
                .build();
        return save(user);
    }

    /**
     * N일 전에 생성된 사용자
     */
    public User createDaysAgo(String email, int daysAgo) {
        User user = User.builder()
                .email(new Email(email))
                .name("테스터")
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(daysAgo))
                .build();
        return save(user);
    }

    private User createUserFromMap(Map<String, String> row) {
        return User.builder()
                .email(new Email(row.get("email")))
                .name(row.getOrDefault("name", "테스터"))
                .status(UserStatus.valueOf(row.getOrDefault("status", "ACTIVE")))
                .build();
    }
}
```

---

### 2. Order TestDataManager (FK 관계 포함)

```java
package com.example.test.fixture;

import com.example.domain.entity.Order;
import com.example.domain.entity.OrderStatus;
import com.example.domain.entity.User;
import io.cucumber.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@Transactional
public class OrderTestDataManager extends BaseTestDataManager<Order, Long> {

    @Autowired
    private UserTestDataManager userDataManager;

    public OrderTestDataManager() {
        super(Order.class);
    }

    /**
     * DataTable에서 Order 생성 (FK 관계 자동 처리)
     *
     * Gherkin:
     * Given 다음 주문이 존재한다
     *   | userEmail     | amount | status   |
     *   | test@test.com | 10000  | PENDING  |
     */
    public List<Order> createFromDataTable(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        return rows.stream()
                .map(this::createOrderFromMap)
                .map(this::save)
                .toList();
    }

    /**
     * 사용자의 기본 주문 생성
     */
    public Order createDefault(User user) {
        Order order = Order.builder()
                .user(user)
                .amount(10000)
                .status(OrderStatus.PENDING)
                .build();
        return save(order);
    }

    /**
     * 이메일로 사용자를 찾아 주문 생성 (FK 자동 처리)
     */
    public Order createForUser(String userEmail) {
        User user = userDataManager.findByEmailOrCreate(userEmail);
        return createDefault(user);
    }

    /**
     * 상태가 있는 주문 생성
     */
    public Order createWithStatus(User user, OrderStatus status) {
        Order order = Order.builder()
                .user(user)
                .amount(10000)
                .status(status)
                .build();
        return save(order);
    }

    /**
     * N일 전에 생성된 주문
     */
    public Order createDaysAgo(User user, int daysAgo) {
        Order order = Order.builder()
                .user(user)
                .amount(10000)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now().minusDays(daysAgo))
                .build();
        return save(order);
    }

    private Order createOrderFromMap(Map<String, String> row) {
        // FK 관계: userEmail로 User 조회 또는 생성
        User user = userDataManager.findByEmailOrCreate(row.get("userEmail"));

        return Order.builder()
                .user(user)
                .amount(Integer.parseInt(row.get("amount")))
                .status(OrderStatus.valueOf(row.getOrDefault("status", "PENDING")))
                .build();
    }
}
```

---

### 3. PaymentEventFailureLog TestDataManager (복잡한 FK)

```java
package com.example.test.fixture;

import com.example.domain.entity.PaymentEvent;
import com.example.domain.entity.PaymentEventFailureLog;
import com.example.domain.entity.PaymentEventStatus;
import io.cucumber.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@Transactional
public class PaymentEventFailureLogTestDataManager extends BaseTestDataManager<PaymentEventFailureLog, Long> {

    @Autowired
    private PaymentEventTestDataManager paymentEventDataManager;

    public PaymentEventFailureLogTestDataManager() {
        super(PaymentEventFailureLog.class);
    }

    /**
     * DataTable에서 생성
     *
     * Gherkin:
     * Given 다음 결제 실패 로그가 존재한다
     *   | paymentEventId | failureCode | failureMessage |
     *   | 1              | CARD_ERROR  | 카드 오류      |
     */
    public List<PaymentEventFailureLog> createFromDataTable(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        return rows.stream()
                .map(this::createFromMap)
                .map(this::save)
                .toList();
    }

    /**
     * PaymentEvent에 대한 실패 로그 생성
     */
    public PaymentEventFailureLog createForEvent(PaymentEvent event, String failureCode, String message) {
        PaymentEventFailureLog log = PaymentEventFailureLog.builder()
                .paymentEvent(event)
                .failureCode(failureCode)
                .failureMessage(message)
                .build();
        return save(log);
    }

    /**
     * 이벤트 ID로 조회하여 실패 로그 생성
     */
    public PaymentEventFailureLog createForEventId(Long eventId, String failureCode, String message) {
        PaymentEvent event = paymentEventDataManager.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("PaymentEvent not found: " + eventId));
        return createForEvent(event, failureCode, message);
    }

    private PaymentEventFailureLog createFromMap(Map<String, String> row) {
        Long eventId = Long.parseLong(row.get("paymentEventId"));
        PaymentEvent event = paymentEventDataManager.findById(eventId)
                .orElseGet(() -> paymentEventDataManager.createDefault(eventId));

        return PaymentEventFailureLog.builder()
                .paymentEvent(event)
                .failureCode(row.get("failureCode"))
                .failureMessage(row.get("failureMessage"))
                .build();
    }
}
```

---

## DataTable 변환 유틸리티

```java
package com.example.test.fixture;

import io.cucumber.datatable.DataTable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

/**
 * DataTable 변환 공통 유틸리티
 */
public final class DataTableConverter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DataTableConverter() {}

    public static String getString(Map<String, String> row, String key) {
        return row.get(key);
    }

    public static String getStringOrDefault(Map<String, String> row, String key, String defaultValue) {
        return row.getOrDefault(key, defaultValue);
    }

    public static Integer getInteger(Map<String, String> row, String key) {
        return Optional.ofNullable(row.get(key))
                .map(Integer::parseInt)
                .orElse(null);
    }

    public static Integer getIntegerOrDefault(Map<String, String> row, String key, Integer defaultValue) {
        return Optional.ofNullable(row.get(key))
                .map(Integer::parseInt)
                .orElse(defaultValue);
    }

    public static Long getLong(Map<String, String> row, String key) {
        return Optional.ofNullable(row.get(key))
                .map(Long::parseLong)
                .orElse(null);
    }

    public static Boolean getBoolean(Map<String, String> row, String key) {
        return Optional.ofNullable(row.get(key))
                .map(v -> "true".equalsIgnoreCase(v) || "1".equals(v))
                .orElse(null);
    }

    public static Boolean getBooleanOrDefault(Map<String, String> row, String key, Boolean defaultValue) {
        return Optional.ofNullable(row.get(key))
                .map(v -> "true".equalsIgnoreCase(v) || "1".equals(v))
                .orElse(defaultValue);
    }

    public static LocalDateTime getDateTime(Map<String, String> row, String key) {
        return Optional.ofNullable(row.get(key))
                .map(v -> LocalDateTime.parse(v, DATE_TIME_FORMATTER))
                .orElse(null);
    }

    public static <T extends Enum<T>> T getEnum(Map<String, String> row, String key, Class<T> enumClass) {
        return Optional.ofNullable(row.get(key))
                .map(v -> Enum.valueOf(enumClass, v))
                .orElse(null);
    }

    public static <T extends Enum<T>> T getEnumOrDefault(Map<String, String> row, String key, Class<T> enumClass, T defaultValue) {
        return Optional.ofNullable(row.get(key))
                .map(v -> Enum.valueOf(enumClass, v))
                .orElse(defaultValue);
    }
}
```

---

## FK 관계 처리 패턴

### 1. 단계적 생성 (계층형)

```java
// 1단계: 독립 엔티티 먼저 생성
User user = userDataManager.createByEmail("user@test.com");

// 2단계: User를 참조하는 Order 생성
Order order = orderDataManager.createDefault(user);

// 3단계: Order를 참조하는 Payment 생성
Payment payment = paymentDataManager.createForOrder(order);
```

### 2. 자동 조회/생성 (편의 메서드)

```java
// userEmail로 자동 User 조회 또는 생성 후 Order 생성
Order order = orderDataManager.createForUser("user@test.com");
```

### 3. DataTable 내 FK 참조

```gherkin
Given 다음 주문이 존재한다
  | userEmail     | amount | status   |
  | test@test.com | 10000  | PENDING  |  # userEmail로 User 자동 조회/생성
```

```java
// OrderDataManager에서 자동 처리
private Order createOrderFromMap(Map<String, String> row) {
    User user = userDataManager.findByEmailOrCreate(row.get("userEmail"));
    // ...
}
```

---

## 메서드 네이밍 규칙

| 메서드 패턴 | 용도 | 예시 |
|------------|------|------|
| `createFromDataTable(DataTable)` | Gherkin Given절에서 사용 | `createFromDataTable(table)` |
| `createDefault()` | 기본값으로 빠른 생성 | `createDefault()` |
| `createByXxx(value)` | 특정 필드 값으로 생성 | `createByEmail("test@test.com")` |
| `createWithXxx(value)` | 특정 속성을 지정하여 생성 | `createWithStatus(UserStatus.DELETED)` |
| `findByXxxOrCreate(value)` | 조회 후 없으면 생성 | `findByEmailOrCreate("test@test.com")` |
| `createForXxx(xxx)` | FK 관계 엔티티 지정 | `createForUser(user)` |
| `createDaysAgo(xxx, n)` | N일 전 생성일 설정 | `createDaysAgo(user, 7)` |

---

## 검증 체크리스트

### ✅ 컴파일 확인
```bash
./gradlew compileTestJava
```

### ✅ 실행 확인
```bash
# 단위 테스트
./gradlew test

# 통합 테스트
./gradlew integrationTest
```

### ✅ 테스트 통과 확인
```bash
./gradlew check
```

---

## 주의사항

1. **@Transactional 필수**: 모든 TestDataManager 메서드는 트랜잭션 내에서 실행되어야 함
2. **flush()/clear() 적절히 사용**: 테스트 격리와 영속성 컨텍스트 동기화
3. **FK 순서 준수**: 독립 엔티티 → 종속 엔티티 순서로 생성
4. **재사용성 고려**: findByXxxOrCreate 패턴으로 중복 생성 방지
5. **불변성 유지**: 생성된 엔티티의 상태 변경은 도메인 메서드 사용

---

## 관련 문서

- [E2E 테스트 템플릿](e2e-test-template.md)
- [Repository 테스트 템플릿](repository-test-template.md)
- [계층별 TDD 가이드](layered-tdd-guide.md)
- [고급 Given 패턴](../gherkin/references/advanced-given-patterns.md)
