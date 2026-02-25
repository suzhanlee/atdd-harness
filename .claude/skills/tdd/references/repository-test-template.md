# Repository Test Template (@DataJpaTest)

## Repository Test Template

```java
package com.example.integration;

import com.example.domain.entity.EntityName;
import com.example.domain.repository.EntityNameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("EntityNameRepository 테스트")
class EntityNameRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EntityNameRepository repository;

    @BeforeEach
    void setUp() {
        entityManager.clear();
    }

    @Test
    @DisplayName("Entity 저장 및 ID로 조회")
    void saveAndFindById() {
        // given
        EntityName entity = EntityName.create("test");
        EntityName saved = entityManager.persist(entity);
        entityManager.flush();

        // when
        Optional<EntityName> found = repository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getFieldName()).isEqualTo("test");
    }

    @Test
    @DisplayName("필드명으로 조회")
    void findByFieldName() {
        // given
        EntityName entity = EntityName.create("test");
        entityManager.persist(entity);
        entityManager.flush();

        // when
        Optional<EntityName> found = repository.findByFieldName("test");

        // then
        assertThat(found).isPresent();
    }
}
```

---

## Query Method Test

### 기본 Query Method

```java
@Test
@DisplayName("이메일로 사용자 조회")
void findByEmail() {
    // given
    User user = User.create("test@example.com", "password", "name");
    entityManager.persist(user);
    entityManager.flush();

    // when
    Optional<User> found = repository.findByEmail("test@example.com");

    // then
    assertThat(found).isPresent();
    assertThat(found.get().getEmail()).isEqualTo("test@example.com");
}

@Test
@DisplayName("활성 상태인 사용자만 조회")
void findByActiveTrue() {
    // given
    User activeUser = User.create("active@example.com", "password", "name");
    User deletedUser = User.create("deleted@example.com", "password", "name");
    deletedUser.delete();

    entityManager.persist(activeUser);
    entityManager.persist(deletedUser);
    entityManager.flush();

    // when
    List<User> activeUsers = repository.findByActiveTrue();

    // then
    assertThat(activeUsers).hasSize(1);
    assertThat(activeUsers.get(0).getEmail()).isEqualTo("active@example.com");
}
```

### @Query 테스트

```java
@Test
@DisplayName("JPQL @Query로 조회")
void findByCustomQuery() {
    // given
    EntityName entity = EntityName.create("test");
    entityManager.persist(entity);
    entityManager.flush();

    // when
    List<EntityName> results = repository.findByCustomQuery("test");

    // then
    assertThat(results).hasSize(1);
}

@Test
@DisplayName("Native Query로 조회")
void findByNativeQuery() {
    // given
    EntityName entity = EntityName.create("test");
    entityManager.persist(entity);
    entityManager.flush();

    // when
    List<EntityName> results = repository.findByNativeQuery("test");

    // then
    assertThat(results).hasSize(1);
}
```

### 페이징/정렬 테스트

```java
@Test
@DisplayName("페이징 조회")
void findAllWithPaging() {
    // given
    for (int i = 0; i < 15; i++) {
        entityManager.persist(EntityName.create("test" + i));
    }
    entityManager.flush();

    // when
    Page<EntityName> page = repository.findAll(PageRequest.of(0, 10));

    // then
    assertThat(page.getContent()).hasSize(10);
    assertThat(page.getTotalElements()).isEqualTo(15);
    assertThat(page.getTotalPages()).isEqualTo(2);
    assertThat(page.isFirst()).isTrue();
    assertThat(page.hasNext()).isTrue();
}

@Test
@DisplayName("정렬 조회")
void findAllWithSorting() {
    // given
    entityManager.persist(EntityName.create("charlie"));
    entityManager.persist(EntityName.create("alpha"));
    entityManager.persist(EntityName.create("bravo"));
    entityManager.flush();

    // when
    List<EntityName> results = repository.findAllByOrderByFieldNameAsc();

    // then
    assertThat(results).extracting("fieldName")
            .containsExactly("alpha", "bravo", "charlie");
}
```

---

## Auditing Test

```java
@DataJpaTest
@Import(JpaAuditingConfiguration.class)  // Auditing 활성화
@DisplayName("Auditing 테스트")
class AuditingTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EntityNameRepository repository;

    @Test
    @DisplayName("생성일자 자동 설정")
    void createdDateAutoSet() {
        // given
        EntityName entity = EntityName.create("test");
        entityManager.persist(entity);
        entityManager.flush();

        // when
        EntityName found = repository.findById(entity.getId()).orElseThrow();

        // then
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("수정일자 자동 갱신")
    void lastModifiedDateAutoUpdated() throws InterruptedException {
        // given
        EntityName entity = EntityName.create("test");
        entityManager.persist(entity);
        entityManager.flush();
        LocalDateTime createdAt = entity.getCreatedAt();

        Thread.sleep(100); // 시간 차이 생성

        // when
        entity.updateFieldName("updated");
        entityManager.flush();
        entityManager.clear();
        EntityName found = repository.findById(entity.getId()).orElseThrow();

        // then
        assertThat(found.getLastModifiedAt()).isAfterOrEqualTo(createdAt);
    }
}
```

---

## Transaction Test

### @Transactional 동작 검증

```java
@Test
@DisplayName("트랜잭션 롤백 테스트")
void transactionRollback() {
    // given
    EntityName entity = EntityName.create("test");

    // when
    EntityName saved = repository.save(entity);
    assertThat(repository.findById(saved.getId())).isPresent();

    // then - @DataJpaTest는 기본적으로 rollback
    // 테스트 종료 후 데이터가 롤백됨
}

@Test
@DisplayName("flush 후 영속성 컨텍스트 초기화")
void flushAndClear() {
    // given
    EntityName entity = EntityName.create("test");
    entityManager.persist(entity);

    // when
    entityManager.flush();
    entityManager.clear();

    // then - DB에서 다시 조회
    EntityName found = entityManager.find(EntityName.class, entity.getId());
    assertThat(found).isNotNull();
}
```

### Lazy Loading 테스트

```java
@Test
@DisplayName("지연 로딩 동작 확인")
void lazyLoading() {
    // given
    Parent parent = Parent.create("parent");
    Child child = Child.create("child", parent);
    entityManager.persist(parent);
    entityManager.persist(child);
    entityManager.flush();
    entityManager.clear();

    // when
    Child found = repository.findById(child.getId()).orElseThrow();

    // then - 프록시 상태
    assertThat(found.getParent()).isInstanceOf(HibernateProxy.class);

    // 실제 접근 시 로딩
    String parentName = found.getParent().getName();
    assertThat(parentName).isEqualTo("parent");
}
```

---

## Test Naming Convention

| 유형 | 명명 규칙 | 예시 |
|------|----------|------|
| Repository 기본 | `{action}` | `saveAndFindById` |
| Query Method | `findBy{field}` | `findByEmail` |
| 페이징 | `findAllWithPaging` | `findAllWithPaging` |
| 정렬 | `findAllWithSorting` | `findAllWithSorting` |
| Auditing | `{auditingFeature}` | `createdDateAutoSet` |

---

## Best Practices

1. **entityManager.flush()/clear() 활용** - 영속성 컨텍스트와 DB 동기화
2. **독립적인 테스트 데이터** - 각 테스트마다 필요한 데이터만 생성
3. **@BeforeEach에서 DB 초기화** - 테스트 격리 보장
4. **실제 DB와 유사한 환경** - H2/MySQL Mode 또는 TestContainers 사용
5. **Auditing 설정 확인** - @Import로 JpaAuditing 활성화
6. **페이지/정렬 검증** - Page 객체의 다양한 속성 검증
7. **트랜잭션 경계 이해** - @DataJpaTest의 기본 롤백 동작 활용

---

## TestDataManager + TestEntityManager 조합

### 개요

@DataJpaTest에서 TestDataManager를 사용하여 데이터 준비를 간소화한다. TestEntityManager와 함께 사용하여 영속성 컨텍스트를 명확히 제어한다.

### 기본 설정

```java
@DataJpaTest
@Import({UserTestDataManager.class, OrderTestDataManager.class})
@DisplayName("OrderRepository 통합 테스트")
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderTestDataManager orderDataManager;

    @Autowired
    private UserTestDataManager userDataManager;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderDataManager.deleteAll();
        userDataManager.deleteAll();
    }
}
```

### TestDataManager로 데이터 준비

```java
@Test
@DisplayName("사용자별 주문 조회")
void findByUser() {
    // given - TestDataManager로 간단히 셋업
    User user = userDataManager.createByEmail("test@test.com");
    orderDataManager.createDefault(user);
    orderDataManager.createDefault(user);
    orderDataManager.clear();  // 영속성 컨텍스트 초기화

    // when
    List<Order> orders = orderRepository.findByUser(user);

    // then
    assertThat(orders).hasSize(2);
}

@Test
@DisplayName("상태별 주문 조회")
void findByStatus() {
    // given - TestDataManager로 상태 지정 생성
    User user = userDataManager.createDefault();
    orderDataManager.createWithStatus(user, OrderStatus.COMPLETED);
    orderDataManager.createWithStatus(user, OrderStatus.PENDING);
    orderDataManager.clear();

    // when
    List<Order> completed = orderRepository.findByStatus(OrderStatus.COMPLETED);

    // then
    assertThat(completed).hasSize(1);
    assertThat(completed.get(0).getStatus()).isEqualTo(OrderStatus.COMPLETED);
}
```

### TestEntityManager와 TestDataRepository 조합

```java
@Test
@DisplayName("FK 관계가 있는 데이터 생성")
void createWithFK() {
    // given - TestDataManager가 FK 자동 처리
    User user = userDataManager.findByEmailOrCreate("test@test.com");
    Order order = orderDataManager.createForUser(user);
    entityManager.clear();

    // when
    Order found = orderRepository.findById(order.getId()).orElseThrow();

    // then
    assertThat(found.getUser().getEmail().getValue()).isEqualTo("test@test.com");
}

@Test
@DisplayName("flush 후 영속성 컨텍스트 초기화로 DB 동기화")
void flushAndClear() {
    // given
    User user = userDataManager.createDefault();
    Order order = orderDataManager.createDefault(user);

    // when - flush로 DB 반영, clear로 1차 캐시 제거
    entityManager.flush();
    entityManager.clear();
    Order found = orderRepository.findById(order.getId()).orElseThrow();

    // then - DB에서 다시 조회됨
    assertThat(found).isNotNull();
    assertThat(found.getId()).isEqualTo(order.getId());
}
```

### TestDataManager를 사용한 복잡한 데이터 시나리오

```java
@Test
@DisplayName("시간 기반 조회 - N일 전 데이터")
void findCreatedBefore() {
    // given
    User user = userDataManager.createDefault();
    orderDataManager.createDaysAgo(user, 10);  // 10일 전
    orderDataManager.createDaysAgo(user, 5);   // 5일 전
    orderDataManager.createDefault(user);      // 오늘
    entityManager.clear();

    // when - 7일 전보다 오래된 주문 조회
    LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
    List<Order> oldOrders = orderRepository.findByCreatedAtBefore(weekAgo);

    // then
    assertThat(oldOrders).hasSize(1);
}

@Test
@DisplayName("페이징과 정렬 테스트")
void findAllWithPagingAndSorting() {
    // given - TestDataManager로 대량 데이터 생성
    User user = userDataManager.createDefault();
    for (int i = 0; i < 15; i++) {
        orderDataManager.createWithAmount(user, 10000 * (i + 1));
    }
    entityManager.clear();

    // when
    Page<Order> page = orderRepository.findByUser(
            user,
            PageRequest.of(0, 10, Sort.by("amount").descending())
    );

    // then
    assertThat(page.getContent()).hasSize(10);
    assertThat(page.getTotalElements()).isEqualTo(15);
    assertThat(page.getContent().get(0).getAmount()).isEqualTo(150000);  // 내림차순
}
```

### TestDataManager vs 순수 TestEntityManager

| 상황 | 추천 방식 |
|------|----------|
| 단순 엔티티 1-2개 | TestEntityManager 직접 사용 |
| FK 관계가 있는 데이터 | TestDataManager |
| 상태/시간 조건 데이터 | TestDataManager |
| 반복되는 데이터 패턴 | TestDataManager |
| 특수한 테스트 케이스 | TestEntityManager 직접 사용 |

### @Import로 TestDataManager 등록

```java
@DataJpaTest
@Import({
    UserTestDataManager.class,
    OrderTestDataManager.class,
    PaymentEventDataManager.class
})
class OrderRepositoryTest {
    // ...
}
```

### TestDataManager 내부 구조

```java
@Component
public class OrderTestDataManager extends BaseTestDataManager<Order, Long> {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    private UserTestDataManager userDataManager;

    public Order createDefault(User user) {
        Order order = Order.builder()
                .user(user)
                .amount(10000)
                .status(OrderStatus.PENDING)
                .build();
        return save(order);  // persist + flush
    }

    public Order createWithStatus(User user, OrderStatus status) {
        Order order = Order.builder()
                .user(user)
                .amount(10000)
                .status(status)
                .build();
        return save(order);
    }

    public Order createDaysAgo(User user, int daysAgo) {
        Order order = Order.builder()
                .user(user)
                .amount(10000)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now().minusDays(daysAgo))
                .build();
        return save(order);
    }
}
```

> **상세 가이드**: [test-data-manager-template.md](test-data-manager-template.md)

---

## 관련 문서

- [단위 테스트 템플릿](unit-test-template.md) - Service/Entity 테스트
- [E2E 테스트 템플릿](e2e-test-template.md) - Cucumber Step Definitions
- [SQL 데이터 가이드](sql-data-guide.md) - INSERT SQL 작성법
- [TestDataManager 템플릿](test-data-manager-template.md) - EntityManager 래핑 유틸리티
- [계층별 TDD 가이드](layered-tdd-guide.md) - Inside-Out TDD
