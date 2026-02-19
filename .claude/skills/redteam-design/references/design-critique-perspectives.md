# Design Critique 6가지 검토 관점 체크리스트 (RRAIRU)

---

## 1. Responsibility (책임 분배)

### 핵심 질문
"이 로직이 정말 이 객체에 속해야 하는가?"

### 체크리스트

#### God Object / God Entity
- [ ] 하나의 Entity가 너무 많은 필드를 가지고 있지 않은가? (10개 이상 주의)
- [ ] 하나의 Entity가 너무 많은 메서드를 가지고 있지 않은가? (15개 이상 주의)
- [ ] 하나의 Entity가 여러 도메인 개념을 포함하고 있지 않은가?

#### Service vs Entity 배치
- [ ] 단일 Entity의 상태 변경이 Entity 메서드로 구현되었는가?
- [ ] 두 개 이상 Entity가 관여하는 로직이 Domain Service로 분리되었는가?
- [ ] 외부 시스템 연동이 Infrastructure 계층으로 분리되었는가?

#### DTO / VO / Entity 구분
- [ ] Entity가 Request/Response DTO로 직접 사용되지 않는가?
- [ ] 불변 값이 VO로 분리되었는가?
- [ ] Entity 내부의 복잡한 값이 VO로 캡슐화되었는가?

### 안티패턴 vs 권장 패턴

```java
// ❌ 안티패턴: God Entity
@Entity
public class User {
    private String email;
    private String password;
    private String name;
    private String phone;
    private String address;
    private String zipCode;
    private String status;
    private String role;
    private LocalDateTime lastLoginAt;
    private int loginFailCount;
    private boolean locked;

    // 너무 많은 책임...
    public boolean validatePassword(String raw) { ... }
    public boolean canLogin() { ... }
    public void lock() { ... }
    public void unlock() { ... }
    public boolean isAdmin() { ... }
    public void changePassword(String newPwd) { ... }
    public void updateProfile(...) { ... }
    public void updateAddress(...) { ... }
}

// ✅ 권장 패턴: 책임 분배
@Entity
public class User {
    @Embedded private Email email;
    @Embedded private Password password;
    @Embedded private Profile profile;
    @Embedded private LoginStatus loginStatus;

    public void login(Password rawPassword, PasswordEncoder encoder) {
        loginStatus.attemptLogin(password, rawPassword, encoder);
    }
}

@Embeddable
public class LoginStatus {
    private int failCount;
    private boolean locked;
    private LocalDateTime lastLoginAt;

    public void attemptLogin(Password stored, Password input, PasswordEncoder encoder) {
        if (locked) throw new AccountLockedException();
        if (!stored.matches(input, encoder)) {
            increaseFailCount();
            return;
        }
        resetFailCount();
        updateLastLoginAt();
    }
}
```

### Self-Reflection 질문
- "왜 이 메서드가 이 Entity에 위치해야 하나요?"
- "이 Entity가 변경되어야 하는 이유가 하나뿐인가요?"
- "이 로직을 다른 객체로 이동하면 어떤 장점이 있나요?"

---

## 2. Requirements Fit (요구사항 적합성)

### 핵심 질문
"이 설계가 요구사항을 정확히 반영하는가?"

### 체크리스트

#### Must Have 매핑 (traceability-matrix.md 활용)
- [ ] traceability-matrix.md에서 모든 Must Have 요구사항이 Entity/메서드로 매핑되었는가?
- [ ] traceability-matrix.md의 상태가 모두 ✅인가?
- [ ] 요구사항 ID와 코드 요소의 추적 가능성이 있는가?

#### Over-Engineering
- [ ] 요구사항에 없는 기능이 추가되었는가?
- [ ] 현재 필요하지 않은 확장성을 고려한 복잡한 설계가 있는가?
- [ ] YAGNI(You Aren't Gonna Need It) 원칙을 준수하는가?

#### 누락 검사
- [ ] 요구사항에 명시된 제약조건이 코드에 반영되었는가?
- [ ] 에지 케이스가 요구사항에 명시되어 있는가?

### 안티패턴 vs 권장 패턴

```java
// ❌ 안티패턴: 요구사항에 없는 Over-Engineering
@Entity
public class Product {
    private String name;
    private BigDecimal price;

    // 요구사항에 없는 기능
    private List<ProductVariant> variants;  // MVP에 없음
    private MultiCurrencyPrice multiCurrencyPrice;  // 해외 결제 요구사항 없음
    private Inventory inventory;  // 재고 관리 요구사항 없음
    private SEOInfo seoInfo;  // SEO 요구사항 없음
}

// ✅ 권장 패턴: 요구사항에 맞는 설계
@Entity
public class Product {
    private ProductId id;
    private ProductName name;
    private Money price;

    // Must Have: M3 - 상품 등록
    public static Product create(ProductName name, Money price) {
        return new Product(ProductId.generate(), name, price);
    }

    // Must Have: M4 - 상품 가격 변경
    public void changePrice(Money newPrice) {
        this.price = newPrice;
    }
}
```

### Self-Reflection 질문
- "이 필드가 어떤 요구사항을 만족하나요?"
- "요구사항 문서에서 근거를 찾을 수 있나요?"
- "이 기능이 MVP에 정말 필요한가요?"

---

## 3. Aggregate Boundary (Aggregate 경계)

### 핵심 질문
"이 Entity들이 항상 함께 생성/수정/삭제되는가?"

### 체크리스트

#### 트랜잭션 경계
- [ ] Aggregate 내부 객체들이 하나의 트랜잭션에서만 수정되는가?
- [ ] Aggregate 간 참조가 ID로 이루어지는가?
- [ ] 결과적 일관성(eventual consistency)이 허용되는가?

#### Root 접근
- [ ] Aggregate 내부 객체가 Root를 통해서만 접근되는가?
- [ ] 외부에서 내부 Entity를 직접 조회/수정하지 않는가?
- [ ] Repository가 Aggregate Root만 조회하는가?

#### 크기 적절성
- [ ] Aggregate가 너무 크지 않은가? (성능 이슈)
- [ ] 불변식 보호를 위해 함께 있어야 하는 객체만 포함되는가?

### 안티패턴 vs 권장 패턴

```java
// ❌ 안티패턴: 너무 큰 Aggregate
@Entity
public class Order {
    private OrderId id;
    private List<OrderItem> items;
    private Payment payment;      // 별도 Aggregate여야 함
    private Shipping shipping;    // 별도 Aggregate여야 함
    private Customer customer;    // 객체 참조, ID 참조여야 함
    private List<Review> reviews; // 별도 Aggregate여야 함
}

// ✅ 권장 패턴: 적절한 Aggregate 경계
@Entity
public class Order {  // Aggregate Root
    @EmbeddedId private OrderId id;
    @Embedded private CustomerId customerId;  // ID 참조
    @ElementCollection private List<OrderItem> items;  // 내부
    @Enumerated(EnumType.STRING) private OrderStatus status;

    // 불변식 보호를 위한 경계
    public void addItem(Product product, int quantity) {
        if (items.size() >= 100) {
            throw new OrderLimitExceededException();
        }
        items.add(OrderItem.of(product, quantity));
    }
}

@Entity
public class Payment {  // 별도 Aggregate
    @EmbeddedId private PaymentId id;
    @Embedded private OrderId orderId;  // ID 참조
    @Embedded private Money amount;
    @Enumerated(EnumType.STRING) private PaymentStatus status;
}
```

### Self-Reflection 질문
- "이 Entity들이 항상 함께 변경되나요?"
- "별도 트랜잭션으로 분리할 수 있나요?"
- "Aggregate 간 ID 참조를 사용하면 어떤 장점이 있나요?"

---

## 4. Invariants (불변식)

### 핵심 질문
"이 객체가 항상 유효한 상태를 유지하는가?"

### 체크리스트

#### 항상 유효한 상태
- [ ] 생성 시점에 모든 필수 필드가 유효한가?
- [ ] Setter 대신 의미 있는 메서드로 상태 변경하는가?
- [ ] 상태 변경 후에도 불변식이 유지되는가?

#### 상태 전이
- [ ] 상태 전이 규칙이 코드로 표현되었는가?
- [ ] 불가능한 상태 전이가 방지되는가?
- [ ] 상태 전이 이벤트가 발생하는가?

#### 경계 조건
- [ ] null 체크가 적절한가?
- [ ] 숫자 범위 검증이 있는가?
- [ ] 컬렉션 크기 제한이 있는가?

### 안티패턴 vs 권장 패턴

```java
// ❌ 안티패턴: 불변식 미보호
@Entity
public class Order {
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItem> items;

    // Setter로 인해 무효 상태 가능
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}

// 사용처
order.setStatus(OrderStatus.CANCELLED);
order.setTotalAmount(BigDecimal.ZERO);  // 취소 후 금액이 0이어야 하는 규칙 누락

// ✅ 권장 패턴: 불변식 보호
@Entity
public class Order {
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @Embedded
    private Money totalAmount;
    @ElementCollection
    private List<OrderItem> items = new ArrayList<>();

    // 정적 팩토리 메서드로 유효한 상태 보장
    public static Order create(CustomerId customerId) {
        Order order = new Order();
        order.status = OrderStatus.CREATED;
        order.totalAmount = Money.zero();
        return order;
    }

    // 상태 전이 규칙 캡슐화
    public void cancel() {
        if (status == OrderStatus.SHIPPED) {
            throw new OrderAlreadyShippedException();
        }
        this.status = OrderStatus.CANCELLED;
        this.totalAmount = Money.zero();
    }

    // 불변식: 주문 생성 시 총액 재계산
    public void addItem(Product product, int quantity) {
        items.add(OrderItem.of(product, quantity));
        recalculateTotal();
    }

    private void recalculateTotal() {
        this.totalAmount = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.zero(), Money::add);
    }
}
```

### Self-Reflection 질문
- "이 상태 변경 시 항상 유효한가요?"
- "어떤 조건에서 무효 상태가 될 수 있나요?"
- "Setter 없이 어떻게 상태를 변경할 수 있나요?"

---

## 5. Relationships (연관관계)

### 핵심 질문
"이 연관관계가 정말 필요한가? 단방향으로 충분하지 않은가?"

### 체크리스트

#### 순환 참조
- [ ] Entity 간 순환 참조가 없는가?
- [ ] 양방향 연관관계로 인한 무한 루프 위험이 없는가?

#### 양방향 연관관계
- [ ] 양방향 연관관계가 정말 필요한가?
- [ ] 연관관계 편의 메서드가 구현되었는가?
- [ ] Lombok @ToString, @EqualsAndHashCode에서 제외되었는가?

#### CASCADE
- [ ] CASCADE.REMOVE가 적절한가? (의도치 않은 삭제 방지)
- [ ] 고아 객체(Orphan) 처리가 적절한가?

#### Lazy Loading
- [ ] N+1 문제를 방지하기 위해 Fetch Join이 고려되었는가?
- [ ] 지연 로딩 설정이 적절한가?

### 안티패턴 vs 권장 패턴

```java
// ❌ 안티패턴: 불필요한 양방향 연관관계
@Entity
public class Team {
    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();
}

@Entity
public class Member {
    @ManyToOne
    private Team team;

    // Team에서 Member 목록을 조회할 일이 없다면 단방향으로 충분
}

// ❌ 안티패턴: CASCADE.REMOVE 오용
@Entity
public class Order {
    @OneToMany(mappedBy = "order", cascade = CascadeType.REMOVE)
    private List<OrderItem> items;  // OK - OrderItem은 Order 생명주기에 종속

    @ManyToOne(cascade = CascadeType.REMOVE)
    private Customer customer;  // 위험! Order 삭제 시 Customer도 삭제
}

// ✅ 권장 패턴: 단방향 + ID 참조
@Entity
public class Order {
    @Embedded
    private CustomerId customerId;  // ID 참조로 단순화

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;  // Aggregate 내부는 CASCADE 허용
}

// ✅ 권장 패턴: 필요한 경우에만 양방향
@Entity
public class Member {
    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;
}

@Entity
public class Team {
    // 팀원 목록 조회 기능이 실제로 필요한 경우에만 추가
    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    private List<Member> members;

    // 연관관계 편의 메서드
    public void addMember(Member member) {
        members.add(member);
        member.setTeam(this);
    }
}
```

### Self-Reflection 질문
- "이 연관관계가 정말 필요한가요?"
- "단방향으로 변경하면 어떤 문제가 있나요?"
- "양방향으로 인한 복잡성보다 이득이 큰가요?"

---

## 6. Ubiquitous Language (보편 언어)

### 핵심 질문
"코드가 비즈니스 전문가도 이해할 수 있는 언어로 작성되었는가?"

### 체크리스트

#### 명명 일치
- [ ] Entity/VO 이름이 비즈니스 용어와 일치하는가?
- [ ] 메서드 이름이 비즈니스 행위와 일치하는가?
- [ ] 상태/필드 이름이 도메인 전문가가 사용하는 용어인가?

#### Enum 값
- [ ] Enum 값이 비즈니스 용어와 일치하는가?
- [ ] 기술적 용어(STATUS_1, TYPE_A 등)가 아닌가?

#### 주석/문서
- [ ] 복잡한 비즈니스 규칙에 주석이 있는가?
- [ ] 비즈니스 용어와 코드 용어의 매핑이 문서화되었는가?

### 안티패턴 vs 권장 패턴

```java
// ❌ 안티패턴: 기술 중심 명명
@Entity
public class OrderEntity {
    private Integer status;  // 0: 생성, 1: 결제, 2: 배송, 3: 완료
    private Integer type;    // 무슨 의미?
    private String field1;   // 무슨 필드?
    private String field2;

    public void updateStatus(Integer newStatus) {  // 의미 없는 메서드
        this.status = newStatus;
    }
}

// ✅ 권장 패턴: 유비쿼터스 언어
@Entity
public class Order {
    @Enumerated(EnumType.STRING)
    private OrderStatus status;  // CREATED, PAID, SHIPPED, DELIVERED

    @Enumerated(EnumType.STRING)
    private OrderType type;  // NORMAL, GIFT, SUBSCRIPTION

    @Embedded
    private ShippingAddress shippingAddress;  // 명확한 의미

    // 비즈니스 행위를 표현하는 메서드
    public void markAsPaid() {
        if (status != OrderStatus.CREATED) {
            throw new InvalidOrderStateException();
        }
        this.status = OrderStatus.PAID;
    }
}

// 비즈니스 용어와 일치하는 Enum
public enum OrderStatus {
    CREATED("주문 생성"),
    PAID("결제 완료"),
    SHIPPED("배송 중"),
    DELIVERED("배송 완료"),
    CANCELLED("주문 취소");

    private final String description;

    // 비즈니스 규칙을 표현
    public boolean canCancel() {
        return this == CREATED || this == PAID;
    }
}
```

### Self-Reflection 질문
- "개발자가 아닌 사람이 이 코드를 이해할 수 있나요?"
- "비즈니스 전문가가 이 메서드 이름을 읽고 무슨 일을 하는지 알 수 있나요?"
- "요구사항 문서의 용어와 코드의 용어가 일치하나요?"

---

## 심각도 분류 기준

| 심각도 | 기준 | 예시 |
|--------|------|------|
| **HIGH** | DDD 원칙 위반, 데이터 무결성 위험, 불변식 누락 | God Object, 잘못된 Aggregate 경계, 상태 전이 규칙 누락 |
| **MEDIUM** | 설계 품질 저하, 유지보수성 이슈 | 불필요한 양방향 연관관계, Over-Engineering |
| **LOW** | 명명 규칙, 문서화 이슈 | 유비쿼터스 언어 불일치, 주석 부족 |

---

## 검토 체크리스트 사용법

1. **설계 산출물 읽기**: domain-model.md, erd.md, traceability-matrix.md, Entity 코드
2. **각 관점 스캔**: 6가지 관점에서 체크리스트 항목 확인
   - Requirements Fit → traceability-matrix.md에서 Must Have 매핑 확인
3. **이슈 식별**: 해당되는 항목에 대해 이슈 작성
4. **심각도 평가**: HIGH/MEDIUM/LOW 분류
5. **Self-Reflection 질문 준비**: 사용자가 스스로 고민할 질문
6. **제안 작성**: 개선 방안 + 코드 예시
7. **Critique Report 생성**: `.atdd/design/redteam/design-critique-[날짜].md`

---

## 관점 간 연관성

```
Responsibility ←→ Aggregate Boundary
    (책임 분배가 Aggregate 경계에 영향)

Requirements Fit ←→ Invariants
    (요구사항이 불변식을 결정)

Relationships ←→ Aggregate Boundary
    (연관관계 설계가 Aggregate 경계에 영향)

Ubiquitous Language ←→ All
    (모든 관점에서 명명 일치 필요)
```
