# Martin Fowler's Refactoring Catalog

## 리팩토링 원칙

### 1. 리팩토링 정의
> "리팩토링은 겉보기 동작(externally observable behavior)을 유지하면서 코드의 내부 구조를 변경하는 것이다."

### 2. 리팩토링을 하는 이유
- 소프트웨어 설계 개선
- 소프트웨어 이해도 향상
- 버그 찾기 도움
- 프로그램 빠르게 작성 가능

### 3. 코드를 보며 질문하기
- "이 코드를 처음 보는 사람도 이해할 수 있을까?"
- "중복이 있는가?"
- "변경이 발생하면 여러 곳을 수정해야 하는가?"

---

## Code Smells (코드 냄새)

### 1. Mysterious Name (이해하기 어려운 이름)
**증상**: 변수, 함수, 클래스 이름이 의도를 드러내지 않음

**해결**: Rename Variable, Rename Function, Rename Field

```java
// Bad
int d;
public int calc(int a, int b) { ... }

// Good
int elapsedTimeInDays;
public int add(int augend, int addend) { ... }
```

---

### 2. Duplicated Code (중복 코드)
**증상**: 같은 코드 구조가 여러 곳에 반복됨

**해결**: Extract Function, Pull Up Method, Extract Variable

```java
// Bad: 두 메서드에 동일한 검증 로직
public void createOrder(Order order) {
    validateCustomer(order.getCustomer());
    validateItems(order.getItems());
    // ...
}

public void updateOrder(Order order) {
    validateCustomer(order.getCustomer());
    validateItems(order.getItems());
    // ...
}

// Good: 공통 로직 추출
public void createOrder(Order order) {
    validateOrder(order);
    // ...
}

private void validateOrder(Order order) {
    validateCustomer(order.getCustomer());
    validateItems(order.getItems());
}
```

---

### 3. Long Function (긴 함수)
**증상**: 함수가 너무 길어서 이해하기 어려움

**해결**: Extract Function, Replace Temp with Query, Introduce Parameter Object

```java
// Bad: 50줄짜리 함수
public void processOrder(Order order) {
    // 검증 (10줄)
    // 계산 (15줄)
    // 저장 (10줄)
    // 알림 (15줄)
}

// Good: 함수 분리
public void processOrder(Order order) {
    validateOrder(order);
    Money total = calculateTotal(order);
    saveOrder(order);
    notifyCustomer(order);
}
```

**규칙**: "함수의 길이가 6줄을 넘지 않게 하라" (저자의 개인적 기준)

---

### 4. Long Parameter List (긴 매개변수 목록)
**증상**: 매개변수가 3-4개를 넘어감

**해결**: Introduce Parameter Object, Preserve Whole Object, Remove Flag Argument

```java
// Bad
public void createOrder(
    Long customerId,
    String customerEmail,
    String customerName,
    List<Long> productIds,
    String shippingAddress,
    String paymentMethod
) { ... }

// Good
public void createOrder(OrderRequest request) { ... }

record OrderRequest(
    CustomerInfo customer,
    List<Long> productIds,
    ShippingInfo shipping,
    PaymentInfo payment
) {}
```

---

### 5. Large Class (큰 클래스)
**증상**: 클래스가 너무 많은 일을 함

**해결**: Extract Class, Extract Subclass, Extract Interface

```java
// Bad: 너무 많은 책임
class Order {
    // 주문 관련
    // 결제 관련
    // 배송 관련
    // 알림 관련
}

// Good: 책임 분리
class Order { /* 주문 관련만 */ }
class PaymentProcessor { /* 결제 관련 */ }
class ShippingService { /* 배송 관련 */ }
class NotificationService { /* 알림 관련 */ }
```

---

### 6. Feature Envy (기능 욕심)
**증상**: 다른 클래스의 데이터를 더 많이 사용함

**해결**: Move Function, Extract Function

```java
// Bad: Order가 Payment의 데이터를 과도하게 사용
class Order {
    public Money calculatePaymentFee(Payment payment) {
        return payment.getAmount().multiply(payment.getFeeRate());
    }
}

// Good: 로직이 데이터가 있는 곳으로 이동
class Payment {
    public Money calculateFee() {
        return amount.multiply(feeRate);
    }
}
```

---

### 7. Switch Statements (Switch 문)
**증상**: 같은 switch문이 여러 곳에 반복됨

**해결**: Replace Type Code with Subclasses, Replace Conditional with Polymorphism

```java
// Bad: 반복되는 switch
public Money calculatePrice(Product product) {
    switch (product.getType()) {
        case ELECTRONICS: return price.multiply(1.1);
        case CLOTHING: return price.multiply(1.05);
        case FOOD: return price;
        default: throw new IllegalArgumentException();
    }
}

// Good: 다형성 사용
interface Product {
    Money calculatePrice(Money basePrice);
}

class Electronics implements Product {
    public Money calculatePrice(Money basePrice) {
        return basePrice.multiply(1.1);
    }
}
```

---

### 8. Dead Code (죽은 코드)
**증상**: 실행되지 않는 코드

**해결**: Remove Dead Code

```java
// Bad
public void oldMethod() {
    // 더 이상 사용되지 않음
}

// Good
// 삭제
```

---

### 9. Primitive Obsession (기본 타입 집착)
**증상**: 기본 타입을 과도하게 사용

**해결**: Replace Primitive with Object, Introduce Parameter Object

```java
// Bad
public void createOrder(Long customerId, String email, String address) { ... }

// Good
public void createOrder(CustomerId customerId, Email email, Address address) { ... }
```

---

### 10. Data Clumps (데이터 뭉치)
**증상**: 여러 곳에서 같은 데이터 그룹이 함께 사용됨

**해결**: Extract Class, Introduce Parameter Object

```java
// Bad: 항상 함께 사용됨
void createOrder(String street, String city, String zipCode) { ... }
void updateAddress(Long id, String street, String city, String zipCode) { ... }

// Good: 객체로 추출
record Address(String street, String city, String zipCode) {}

void createOrder(Address address) { ... }
void updateAddress(Long id, Address address) { ... }
```

---

## 리팩토링 카탈로그 (주요 기법)

### Composing Methods (메서드 구성)

| 기법 | 설명 |
|------|------|
| Extract Function | 코드 조각을 별도 함수로 추출 |
| Inline Function | 함수 본문을 호출부에 인라인 |
| Extract Variable | 표현식을 변수로 추출 |
| Inline Variable | 변수를 사용처로 인라인 |
| Replace Temp with Query | 임시 변수를 메서드로 대체 |
| Split Loop | 하나의 루프를 여러 개로 분리 |

### Organizing Data (데이터 구성)

| 기법 | 설명 |
|------|------|
| Encapsulate Variable | 변수를 getter/setter로 캡슐화 |
| Rename Variable | 변수 이름 변경 |
| Introduce Parameter Object | 매개변수 그룹을 객체로 |
| Combine Functions into Class | 관련 함수들을 클래스로 |

### Simplifying Conditionals (조건문 단순화)

| 기법 | 설명 |
|------|------|
| Decompose Conditional | 복잡한 조건문을 분해 |
| Consolidate Conditional Expression | 여러 조건을 하나로 통합 |
| Replace Nested Conditional with Guard Clauses | 중첩 조건을 Guard로 대체 |
| Replace Conditional with Polymorphism | 조건문을 다형성으로 대체 |

### Refactoring APIs (API 리팩토링)

| 기법 | 설명 |
|------|------|
| Separate Query from Modifier | 조회와 수정 분리 |
| Parameterize Function | 매개변수로 함수 일반화 |
| Remove Flag Argument | 플래그 매개변수 제거 |
| Introduce Special Case | 특수 케이스 객체 도입 |

---

## 리팩토링 수행 절차

### 1. 작은 단계로 진행
```
1. 원본 코드 백업 (또는 버전 관리)
2. 작은 변경 적용
3. 테스트 실행
4. 커밋 (테스트 통과 시)
5. 반복
```

### 2. 테스트 우선
- 리팩토링 전 반드시 테스트가 있어야 함
- 리팩토링 후 테스트가 통과해야 함

### 3. 한 번에 하나씩
- 여러 리팩토링을 동시에 진행하지 않음
- 하나 완료 후 커밋, 다음 진행

---

## 리팩토링 질문 가이드

코드를 분석할 때 다음 질문들을 던져보세요:

### 이름 관련
- 이 변수/함수/클래스 이름이 의도를 표현하는가?
- 더 명확한 이름이 있을까?

### 함수 관련
- 이 함수가 한 가지 일만 하는가?
- 함수 이름이 하는 일을 정확히 설명하는가?
- 함수 길이가 적절한가?

### 중복 관련
- 같은 코드가 다른 곳에도 있는가?
- 비슷한 로직을 통합할 수 있는가?

### 구조 관련
- 이 클래스가 하나의 책임만 가지는가?
- 이 데이터와 로직이 같은 클래스에 있어야 하는가?

### 조건문 관련
- 중첩된 조건문을 단순화할 수 있는가?
- switch/if-else를 다형성으로 대체할 수 있는가?

---

## 참조
- Martin Fowler, "Refactoring: Improving the Design of Existing Code" (2nd Edition)
- https://refactoring.com/
