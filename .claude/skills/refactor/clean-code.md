# Clean Code Checklist

## 1. Meaningful Names (의미 있는 이름)

### 체크리스트
- [ ] 의도를 드러내는 이름인가?
- [ ] 발음 가능한 이름인가?
- [ ] 검색 가능한 이름인가?
- [ ] 클래스는 명사, 메서드는 동사인가?
- [ ] 헝가리안 표기법을 피했는가?
- [ ] 멤버 변수 접두어를 피했는가?

### Before
```java
int d; // elapsed time in days
List<int[]> list1;
public static final int WORK_DAYS_PER_WEEK = 5;
String n;
```

### After
```java
int elapsedTimeInDays;
List<int[]> flaggedCells;
String customerName;
```

---

## 2. Functions (함수)

### 체크리스트
- [ ] 함수는 한 가지 일만 하는가?
- [ ] 함수는 20줄 이하인가?
- [ ] 들여쓰기가 2단계 이하인가?
- [ ] 인자가 3개 이하인가?
- [ ] 사이드 이펙트가 없는가?
- [ ] 추상화 수준이 일관적인가?

### Before
```java
public void process(User user, Order order, Payment payment, boolean notify) {
    // 50 lines of code...
    // multiple levels of abstraction
    // side effects
}
```

### After
```java
public void processOrder(OrderCommand command) {
    validate(command);
    Order order = createOrder(command);
    Payment payment = processPayment(order);
    notifyCustomer(command, order, payment);
}
```

---

## 3. Comments (주석)

### 체크리스트
- [ ] 주석 없이 코드로 의도를 표현했는가?
- [ ] 불필요한 주석을 제거했는가?
- [ ] TODO 주석이 현재 유효한가?
- [ ] 복잡한 로직에 설명 주석이 있는가?

### Before
```java
// Check if user is valid
if (user.getStatus() == 1 && user.getDeletedAt() == null) {
    // Process the order
    ...
}
```

### After
```java
if (user.isActive()) {
    processOrder();
}
```

---

## 4. Formatting (형식)

### 체크리스트
- [ ] 일관된 들여쓰기를 사용했는가?
- [ ] 빈 줄로 개념을 분리했는가?
- [ ] 가로 길이가 120자 이하인가?
- [ ] 관련 코드가 세로로 가까운가?
- [ ] 변수 선언이 사용 위치 근처에 있는가?

### Before
```java
public class OrderService {
private OrderRepository repository;
public Order create(User user,List<Item> items) {
Order order=new Order();
order.setUser(user);
for(Item item:items){order.addItem(item);}
return repository.save(order);
}}
```

### After
```java
public class OrderService {

    private final OrderRepository repository;

    public Order create(User user, List<Item> items) {
        Order order = new Order(user);
        items.forEach(order::addItem);
        return repository.save(order);
    }
}
```

---

## 5. Objects and Data Structures

### 체크리스트
- [ ] getter/setter 남용을 피했는가?
- [ ] 데이터 추상화가 되어 있는가?
- [ ] 객체는 동작을 노출하고 데이터는 숨겼는가?
- [ ] Demeter의 법칙을 따르는가?

### Before
```java
public class User {
    private String name;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

// 사용
String userName = user.getName();
if (userName.length() > 10) { ... }
```

### After
```java
public class User {
    private UserName name;
    public boolean hasLongName() {
        return name.isLongerThan(10);
    }
}

// 사용
if (user.hasLongName()) { ... }
```

---

## 6. Error Handling

### 체크리스트
- [ ] 예외를 사용했는가? (에러 코드가 아닌)
- [ ] Checked Exception을 최소화했는가?
- [ ] 예외 메시지가 유용한가?
- [ ] null을 반환하지 않는가?
- [ ] null을 전달하지 않는가?

### Before
```java
public User findUser(Long id) {
    if (id == null) {
        return null;
    }
    User user = repository.findById(id);
    if (user == null) {
        return null;
    }
    return user;
}
```

### After
```java
public User findUser(Long id) {
    return repository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
}
```

---

## 7. SOLID Principles

### Single Responsibility Principle (SRP)
- [ ] 클래스는 하나의 책임만 가지는가?

```java
// ❌ 여러 책임
class User {
    void save() { ... }
    void sendEmail() { ... }
    void generateReport() { ... }
}

// ✅ 단일 책임
class User { ... }
class UserRepository { void save(User user) { ... } }
class EmailService { void send(User user) { ... } }
```

### Open/Closed Principle (OCP)
- [ ] 확장에는 열려있고 수정에는 닫혀있는가?

```java
// ✅ 새로운 결제 방식 추가 시 기존 코드 수정 불필요
interface PaymentProcessor {
    void process(Payment payment);
}

class CreditCardProcessor implements PaymentProcessor { ... }
class PayPalProcessor implements PaymentProcessor { ... }
```

### Liskov Substitution Principle (LSP)
- [ ] 하위 타입이 상위 타입을 대체할 수 있는가?

### Interface Segregation Principle (ISP)
- [ ] 클라이언트가 사용하지 않는 인터페이스에 의존하지 않는가?

### Dependency Inversion Principle (DIP)
- [ ] 추상화에 의존하고 구체화에 의존하지 않는가?

---

## 8. DRY (Don't Repeat Yourself)

### 체크리스트
- [ ] 복사-붙여넣기 한 코드가 없는가?
- [ ] 공통 로직을 추출했는가?
- [ ] 매직 넘버를 상수로 추출했는가?

### Before
```java
public void createUser(UserRequest request) {
    validateEmail(request.getEmail());
    validatePassword(request.getPassword());
    // ...
}

public void updateUser(Long id, UserRequest request) {
    validateEmail(request.getEmail());
    validatePassword(request.getPassword());
    // ...
}
```

### After
```java
private void validateUserRequest(UserRequest request) {
    validateEmail(request.getEmail());
    validatePassword(request.getPassword());
}
```

---

## 9. Code Smell Detection

### 체크리스트
- [ ] 긴 메서드가 없는가?
- [ ] 큰 클래스가 없는가?
- [ ] 중복 코드가 없는가?
- [ ] 과도한 매개변수가 없는가?
- [ ] 죽은 코드가 없는가?
- [ ] 깊은 중첩이 없는가?

---

## 10. Test Quality

### 체크리스트
- [ ] 테스트 커버리지가 80% 이상인가?
- [ ] 테스트가 빠르게 실행되는가?
- [ ] 테스트가 독립적인가?
- [ ] 테스트가 가독성 있는가?
- [ ] Given-When-Then 구조를 따르는가?

---

## Refactoring Summary Template

```markdown
# 리팩토링 요약

## 리팩토링 일시
[날짜]

## 대상 파일
- [파일 목록]

## 적용된 개선 사항

### Meaningful Names
- [변경 내용]

### Functions
- [변경 내용]

### SOLID
- [변경 내용]

## 테스트 결과
- Unit Tests: PASS
- Integration Tests: PASS
- E2E Tests: PASS

## 커버리지
- Before: X%
- After: Y%
```
