# DDD Patterns Guide

## Strategic Patterns (전략적 패턴)

### 1. Bounded Context
도메인의 논리적 경계. 각 컨텍스트는 독립적인 모델을 가진다.

```
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│   Catalog   │  │   Order     │  │  Shipping   │
│  Context    │  │  Context    │  │  Context    │
├─────────────┤  ├─────────────┤  ├─────────────┤
│ Product     │  │ Order       │  │ Shipment    │
│ Category    │  │ OrderItem   │  │ Delivery    │
│ Price       │  │ Payment     │  │ Address     │
└─────────────┘  └─────────────┘  └─────────────┘
```

### 2. Context Map
컨텍스트 간 관계 정의

| 관계 유형 | 설명 |
|-----------|------|
| Shared Kernel | 공통 모델 공유 |
| Customer/Supplier | 하류팀이 요구사항 전달 |
| Conformist | 상류팀 모델 그대로 사용 |
| Anti-Corruption Layer | 변환 계층으로 격리 |
| Open Host Service | 표준 API 제공 |

### 3. Domain Events
도메인 간 통신을 위한 이벤트

```java
public record OrderCreatedEvent(
    OrderId orderId,
    CustomerId customerId,
    Money totalAmount,
    LocalDateTime occurredAt
) implements DomainEvent {}
```

---

## Tactical Patterns (전술적 패턴)

### 1. Entity
식별자를 가진 변경 가능한 객체

```java
@Entity
@Table(name = "orders")
public class Order {

    @EmbeddedId
    private OrderId id;  // 식별자

    @Embedded
    private CustomerId customerId;

    @ElementCollection
    private List<OrderItem> items;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // 동등성은 ID로 판단
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }
}
```

### 2. Value Object
불변, 식별자 없음, 속성으로 동등성 판단

```java
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Money {

    private BigDecimal amount;
    private String currency;

    private Money(BigDecimal amount, String currency) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount;
        this.currency = currency;
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money won(BigDecimal amount) {
        return new Money(amount, "KRW");
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

### 3. Aggregate
트랜잭션 일관성 경계. Aggregate Root를 통해서만 접근

```java
@Entity
public class Order {  // Aggregate Root

    @EmbeddedId
    private OrderId id;

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderItem> items = new ArrayList<>();

    // Aggregate 내부 객체는 Root를 통해서만 수정
    public void addItem(Product product, int quantity) {
        // 불변식 검증
        if (items.size() >= 100) {
            throw new OrderLimitExceededException();
        }
        items.add(OrderItem.of(product, quantity));
    }

    public void removeItem(OrderItemId itemId) {
        items.removeIf(item -> item.getId().equals(itemId));
    }
}
```

### 4. Repository
도메인 객체의 영속성 추상화

```java
// 도메인 계층에 위치 (인터페이스)
public interface OrderRepository {
    Order findById(OrderId id);
    void save(Order order);
    void delete(Order order);
    List<Order> findByCustomerId(CustomerId customerId);
}

// 인프라 계층에 위치 (구현)
@Repository
public class JpaOrderRepository implements OrderRepository {

    private final SpringDataOrderRepository jpaRepository;

    @Override
    public Order findById(OrderId id) {
        return jpaRepository.findById(id.getValue())
                .map(this::toDomain)
                .orElse(null);
    }

    @Override
    public void save(Order order) {
        jpaRepository.save(toEntity(order));
    }
}
```

### 5. Domain Service
도메인 로직이지만 Entity/VO에 속하지 않는 경우

```java
@Service
public class PricingService {

    public Money calculateTotal(Order order, DiscountPolicy discountPolicy) {
        Money subtotal = order.getItems().stream()
                .map(item -> item.getPrice().multiply(item.getQuantity()))
                .reduce(Money.zero(), Money::add);

        Money discount = discountPolicy.apply(subtotal);
        return subtotal.subtract(discount);
    }
}
```

### 6. Factory
복잡한 객체 생성 로직 캡슐화

```java
@Component
public class OrderFactory {

    public Order createOrder(CreateOrderCommand command) {
        OrderId orderId = OrderId.generate();
        CustomerId customerId = CustomerId.from(command.getCustomerId());

        Order order = new Order(orderId, customerId);

        for (CreateOrderCommand.Item item : command.getItems()) {
            Product product = productRepository.findById(ProductId.from(item.getProductId()));
            order.addItem(product, item.getQuantity());
        }

        return order;
    }
}
```

---

## Layer Architecture

```
┌────────────────────────────────────────────┐
│              Interfaces Layer              │
│  (Controller, DTO, Request/Response)       │
├────────────────────────────────────────────┤
│             Application Layer              │
│  (Application Service, Use Case)           │
├────────────────────────────────────────────┤
│              Domain Layer                  │
│  (Entity, VO, Aggregate, Domain Service)   │
├────────────────────────────────────────────┤
│           Infrastructure Layer             │
│  (Repository Impl, External API, Config)   │
└────────────────────────────────────────────┘
```

### 의존성 규칙
- Domain Layer는 다른 계층에 의존하지 않음
- Infrastructure는 Domain의 구현체
- Application은 Domain을 사용
- Interfaces는 Application을 호출

---

## Naming Conventions

| 개념 | 명명 규칙 | 예시 |
|------|----------|------|
| Entity | 명사 | Order, Customer, Product |
| Value Object | 명사 | Money, Address, Email |
| Repository | ~Repository | OrderRepository |
| Domain Service | ~Service | PricingService |
| Application Service | ~Service | OrderService |
| Factory | ~Factory | OrderFactory |
| Aggregate | Root Entity명 | Order Aggregate |
| Domain Event | ~Event | OrderCreatedEvent |

---

## Best Practices

### 1. Aggregate 설계 원칙
- 작게 유지하기
- Root를 통해서만 접근
- 경계 간 참조는 ID로
- 결과적 일관성 허용

### 2. 불변식 보호
```java
@Entity
public class Order {
    public void cancel() {
        // 상태 변경 전 검증
        if (status == OrderStatus.SHIPPED) {
            throw new OrderAlreadyShippedException();
        }
        this.status = OrderStatus.CANCELLED;
    }
}
```

### 3. 명시적 모델링
```java
// ❌ 암시적
order.setStatus("CANCELLED");

// ✅ 명시적
order.cancel();
```

### 4. 유비쿼터스 언어 사용
```java
// 비즈니스 용어와 코드 용어 일치
public enum OrderStatus {
    CREATED,      // 주문 생성
    PAID,         // 결제 완료
    SHIPPED,      // 배송 중
    DELIVERED,    // 배송 완료
    CANCELLED     // 주문 취소
}
```
