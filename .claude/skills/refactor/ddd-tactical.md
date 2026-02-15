# DDD Tactical Patterns

## 1. Entity vs Value Object

### 구분 기준

| 특성 | Entity | Value Object |
|------|--------|--------------|
| 식별자 | 있음 (ID) | 없음 |
| 동등성 | ID 기반 | 속성 기반 |
| 가변성 | 가변 | 불변 |
| 수명 | 생성~삭제 | 수명 개념 없음 |

### Entity 예시

```java
@Entity
@Table(name = "users")
public class User {

    @EmbeddedId
    private UserId id;  // 식별자

    @Embedded
    private Email email;

    @Embedded
    private UserName name;

    // 가변 - 상태 변경 가능
    public void changeName(UserName newName) {
        this.name = newName;
    }

    // 동등성은 ID로 판단
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

### Value Object 예시

```java
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Money {

    private BigDecimal amount;
    private String currency;

    // private 생성자 - 불변 보장
    private Money(BigDecimal amount, String currency) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount;
        this.currency = currency;
    }

    // 정적 팩토리 메서드
    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money won(long amount) {
        return new Money(BigDecimal.valueOf(amount), "KRW");
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO, "KRW");
    }

    // 새로운 VO 반환 - 불변
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)), this.currency);
    }

    // setter 없음 - 불변
}
```

---

## 2. Aggregate

### Aggregate 설계 원칙

1. **작게 유지하기** - Root + 최소한의 Entity/VO
2. **Root를 통해서만 접근** - 내부 객체 직접 접근 금지
3. **ID로 다른 Aggregate 참조** - 객체 참조 대신 ID 사용
4. **결과적 일관성** - Aggregate 간 즉시 일관성 보장 X

### Aggregate 예시

```java
@Entity
@Table(name = "orders")
public class Order {  // Aggregate Root

    @EmbeddedId
    private OrderId id;

    @Embedded
    private CustomerId customerId;

    @ElementCollection
    @CollectionTable(
        name = "order_items",
        joinColumns = @JoinColumn(name = "order_id")
    )
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // Root를 통해서만 내부 객체 수정
    public void addItem(Product product, int quantity) {
        // 불변식 검증
        if (items.size() >= 100) {
            throw new OrderLimitExceededException("Maximum 100 items per order");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        OrderItem item = OrderItem.create(product, quantity);
        items.add(item);
    }

    public void removeItem(OrderItemId itemId) {
        items.removeIf(item -> item.getId().equals(itemId));
    }

    // 상태 변경도 Root를 통해서만
    public void pay() {
        if (status != OrderStatus.CREATED) {
            throw new InvalidOrderStatusException("Only created orders can be paid");
        }
        this.status = OrderStatus.PAID;
    }

    public void cancel() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            throw new InvalidOrderStatusException("Cannot cancel shipped or delivered orders");
        }
        this.status = OrderStatus.CANCELLED;
    }

    // 불변식
    public Money calculateTotal() {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(Money.zero(), Money::add);
    }
}
```

---

## 3. Repository

### Repository 인터페이스 (도메인 계층)

```java
// 도메인 계층에 위치
public interface OrderRepository {
    Order findById(OrderId id);
    List<Order> findByCustomerId(CustomerId customerId);
    void save(Order order);
    void delete(Order order);
}
```

### Repository 구현 (인프라 계층)

```java
// 인프라 계층에 위치
@Repository
@RequiredArgsConstructor
public class JpaOrderRepository implements OrderRepository {

    private final SpringDataOrderRepository jpaRepository;

    @Override
    public Order findById(OrderId id) {
        return jpaRepository.findById(id.getValue())
                .map(this::toDomain)
                .orElse(null);
    }

    @Override
    public List<Order> findByCustomerId(CustomerId customerId) {
        return jpaRepository.findByCustomerId(customerId.getValue())
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(Order order) {
        OrderEntity entity = toEntity(order);
        jpaRepository.save(entity);
    }

    @Override
    public void delete(Order order) {
        jpaRepository.deleteById(order.getId().getValue());
    }

    // 매핑 로직
    private Order toDomain(OrderEntity entity) { ... }
    private OrderEntity toEntity(Order domain) { ... }
}
```

---

## 4. Domain Service

### 언제 사용하는가?

- Entity나 VO에 속하지 않는 도메인 로직
- 여러 Aggregate가 관여하는 로직
- 외부 시스템 연동이 필요한 로직

### Domain Service 예시

```java
@Service
@RequiredArgsConstructor
public class PricingService {

    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;

    public Money calculateOrderTotal(Order order, CouponCode couponCode) {
        // 여러 Aggregate가 관여
        Money subtotal = order.calculateSubtotal();

        if (couponCode != null) {
            Coupon coupon = couponRepository.findByCode(couponCode);
            subtotal = coupon.apply(subtotal);
        }

        return subtotal;
    }

    public boolean isEligibleForDiscount(Customer customer, Order order) {
        return customer.getMembershipLevel().isPremium()
                || order.calculateTotal().isGreaterThan(Money.won(100_000));
    }
}
```

---

## 5. Domain Events

### Event 정의

```java
public interface DomainEvent {
    LocalDateTime occurredAt();
}

public record OrderCreatedEvent(
    OrderId orderId,
    CustomerId customerId,
    Money totalAmount,
    LocalDateTime occurredAt
) implements DomainEvent {

    public static OrderCreatedEvent of(Order order) {
        return new OrderCreatedEvent(
            order.getId(),
            order.getCustomerId(),
            order.calculateTotal(),
            LocalDateTime.now()
        );
    }
}

public record OrderPaidEvent(
    OrderId orderId,
    Money paidAmount,
    LocalDateTime occurredAt
) implements DomainEvent {}
```

### Event 발행

```java
@Entity
public class Order {

    @DomainEvents
    public Collection<DomainEvent> domainEvents() {
        List<DomainEvent> events = new ArrayList<>();

        if (this.status == OrderStatus.PAID && this.previousStatus == OrderStatus.CREATED) {
            events.add(new OrderPaidEvent(this.id, this.calculateTotal(), LocalDateTime.now()));
        }

        return events;
    }

    @AfterDomainEventPublication
    public void clearDomainEvents() {
        // 이벤트 발행 후 정리
    }
}
```

### Event 처리

```java
@Component
@RequiredArgsConstructor
public class OrderEventHandler {

    private final NotificationService notificationService;
    private final InventoryService inventoryService;

    @EventListener
    public void handle(OrderCreatedEvent event) {
        notificationService.sendOrderConfirmation(event.customerId(), event.orderId());
    }

    @EventListener
    public void handle(OrderPaidEvent event) {
        inventoryService.reserveStock(event.orderId());
    }
}
```

---

## 6. Factory

### 복잡한 생성 로직 캡슐화

```java
@Component
@RequiredArgsConstructor
public class OrderFactory {

    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;

    public Order createOrder(CreateOrderCommand command) {
        // ID 생성
        OrderId orderId = OrderId.generate();
        CustomerId customerId = CustomerId.from(command.getCustomerId());

        // Order 생성
        Order order = new Order(orderId, customerId);

        // Item 추가
        for (CreateOrderCommand.Item item : command.getItems()) {
            Product product = productRepository.findById(ProductId.from(item.getProductId()))
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));
            order.addItem(product, item.getQuantity());
        }

        // Coupon 적용
        if (command.getCouponCode() != null) {
            Coupon coupon = couponRepository.findByCode(CouponCode.from(command.getCouponCode()));
            order.applyCoupon(coupon);
        }

        return order;
    }
}
```

---

## 7. Module 구조

### Package 구조

```
com.example.
├── domain                    # 도메인 계층
│   ├── model
│   │   ├── order/           # Order Bounded Context
│   │   │   ├── Order.java
│   │   │   ├── OrderItem.java
│   │   │   ├── OrderId.java
│   │   │   ├── OrderStatus.java
│   │   │   ├── OrderRepository.java
│   │   │   ├── OrderFactory.java
│   │   │   └── event/
│   │   │       ├── OrderCreatedEvent.java
│   │   │       └── OrderPaidEvent.java
│   │   └── user/            # User Bounded Context
│   │       ├── User.java
│   │       └── ...
│   └── shared               # 공통 Value Objects
│       ├── Money.java
│       └── Email.java
├── application              # 애플리케이션 계층
│   ├── OrderService.java
│   ├── OrderCommand.java
│   └── OrderQuery.java
├── infrastructure           # 인프라 계층
│   ├── persistence
│   │   ├── JpaOrderRepository.java
│   │   └── OrderEntity.java
│   └── external
│       └── PaymentGateway.java
└── interfaces               # 인터페이스 계층
    ├── OrderController.java
    ├── OrderRequest.java
    └── OrderResponse.java
```

---

## Checklist

- [ ] Entity와 Value Object 구분
- [ ] Aggregate 경계 정의
- [ ] Root를 통해서만 접근
- [ ] Repository 인터페이스 도메인 계층에 위치
- [ ] Domain Service로 복잡한 로직 처리
- [ ] Domain Events로 느슨한 결합
- [ ] Factory로 복잡한 생성 로직 캡슐화
