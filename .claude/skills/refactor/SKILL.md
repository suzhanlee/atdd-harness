---
name: refactor
description: Clean Code와 DDD 패턴으로 리팩토링한다. 코드 품질 개선 시 사용.
disable-model-invocation: true
user-invocable: true
context: fork
agent: general-purpose
allowed-tools: Read, Grep, Glob, Write, Edit, Bash
references:
  - references/clean-code.md
  - references/ddd-tactical.md
---

# Clean Code 리팩토링

## 목표
Clean Code 원칙과 DDD 패턴을 적용하여 코드 품질을 개선한다.

## 입력
- `src/main/java/**/*.java`
- `src/test/java/**/*.java`

## 상세 가이드
- Clean Code 체크리스트: [clean-code.md](references/clean-code.md)
- DDD 전술적 패턴: [ddd-tactical.md](references/ddd-tactical.md)

## 트리거
- `/refactor` 명령어 실행
- TDD 구현 완료 후 자동 제안

## Clean Code 원칙

### 1. Meaningful Names (의미 있는 이름)

```java
// ❌ 나쁜 예
int d; // elapsed time in days
List<int[]> list1;

// ✅ 좋은 예
int elapsedTimeInDays;
List<int[]> flaggedCells;
```

### 2. Small Functions (작은 함수)

```java
// ❌ 나쁜 예: 한 함수가 여러 일을 함
public void processUser(User user) {
    validateUser(user);
    saveUser(user);
    sendEmail(user);
    logActivity(user);
}

// ✅ 좋은 예: 한 함수는 한 가지만
public void processUser(User user) {
    validateAndSave(user);
    notifyUser(user);
}
```

### 3. No Side Effects (부작용 없음)

```java
// ❌ 나쁜 예: 검증하면서 수정함
public boolean validateUser(User user) {
    if (user.getName() == null) {
        user.setName("Unknown"); // side effect!
        return false;
    }
    return true;
}

// ✅ 좋은 예: 순수 함수
public boolean isValidUser(User user) {
    return user.getName() != null;
}
```

### 4. SOLID Principles

#### Single Responsibility Principle (SRP)
```java
// ❌ 나쁜 예
class User {
    void save() { ... }
    void sendEmail() { ... }
    void generateReport() { ... }
}

// ✅ 좋은 예
class User { ... }
class UserRepository { void save(User user) { ... } }
class EmailService { void send(User user) { ... } }
class ReportGenerator { void generate(User user) { ... } }
```

#### Open/Closed Principle (OCP)
```java
// ✅ 확장에는 열려있고, 수정에는 닫혀있음
interface PaymentProcessor {
    void process(Payment payment);
}

class CreditCardProcessor implements PaymentProcessor { ... }
class PayPalProcessor implements PaymentProcessor { ... }
```

### 5. DRY (Don't Repeat Yourself)

```java
// ❌ 나쁜 예: 중복 코드
public UserResponse createUser(UserRequest request) {
    validateEmail(request.getEmail());
    validatePassword(request.getPassword());
    // ...
}

public UserResponse updateUser(Long id, UserRequest request) {
    validateEmail(request.getEmail());
    validatePassword(request.getPassword());
    // ...
}

// ✅ 좋은 예: 재사용
private void validateUserRequest(UserRequest request) {
    validateEmail(request.getEmail());
    validatePassword(request.getPassword());
}
```

## DDD 패턴 검토

### 1. Entity vs Value Object

| 특성 | Entity | Value Object |
|------|--------|--------------|
| 식별자 | 있음 | 없음 |
| 동등성 | ID 기반 | 속성 기반 |
| 가변성 | 가변 | 불변 |

```java
// Entity
@Entity
public class User {
    @Id private Long id;  // 식별자 있음
    private String name;
    public void changeName(String name) { this.name = name; }  // 가변
}

// Value Object
@Embeddable
public class Email {
    private String value;
    private Email(String value) { this.value = value; }  // 생성자 private
    public static Email from(String value) { return new Email(value); }
    // setter 없음 = 불변
}
```

### 2. Aggregate 경계

```java
// ✅ 올바른 Aggregate 경계
@Entity
public class Order {  // Aggregate Root
    @Id private Long id;

    @OneToMany(cascade = CascadeType.ALL)
    private List<OrderItem> items;  // 같은 Aggregate

    public void addItem(Product product, int quantity) {
        // 불변식 검증
        if (items.size() >= 100) {
            throw new BusinessException("최대 100개까지 가능");
        }
        items.add(new OrderItem(product, quantity));
    }
}
```

### 3. Repository 패턴

```java
// ✅ 도메인 언어 사용
public interface UserRepository {
    Optional<User> findById(UserId id);
    Optional<User> findByEmail(Email email);
    void save(User user);
    void delete(User user);
}

// ❌ 구현 노출
public interface UserRepository {
    User selectById(Long id);  // SQL 느낌
    int insert(User user);     // SQL 느낌
}
```

### 4. Domain Events

```java
@Entity
public class User {
    @DomainEvents
    public Collection<Object> domainEvents() {
        return List.of(new UserCreatedEvent(this.id, this.email));
    }
}

public record UserCreatedEvent(Long userId, Email email) {}
```

## 리팩토링 프로세스

### 1. 코드 로드
```
Read src/main/java/**/*.java
Read src/test/java/**/*.java
```

### 2. Clean Code 검토
- Meaningful Names 체크
- Small Functions 체크
- No Side Effects 체크
- SOLID Principles 체크
- DRY 체크

### 3. DDD 패턴 검토
- Entity vs Value Object 구분
- Aggregate 경계 확인
- Repository 패턴 확인
- Domain Events 필요성

### 4. 리팩토링 실행
```java
// Before
public class UserService {
    public User create(String e, String p, String n) {
        User u = new User();
        u.setEmail(e);
        u.setPassword(p);
        u.setName(n);
        return repository.save(u);
    }
}

// After
public class UserService {
    public User create(CreateUserCommand command) {
        User user = User.create(
            Email.from(command.email()),
            Password.from(command.password()),
            UserName.from(command.name())
        );
        return userRepository.save(user);
    }
}
```

### 5. 테스트 재실행
```bash
./gradlew test
./gradlew integrationTest
./gradlew cucumber
```

## 출력 파일

### REFACTORING-log.md
```markdown
# 리팩토링 로그

## 일시
[날짜 시간]

## 리팩토링 항목

### 1. [클래스명]
- **Before**: [변경 전 설명]
- **After**: [변경 후 설명]
- **이유**: [변경 사유]

### 2. [클래스명]
...

## 개선 사항
- [개선 항목 1]
- [개선 항목 2]

## 테스트 결과
- Unit Tests: PASS
- Integration Tests: PASS
- E2E Tests: PASS
```

### clean-code-checklist.md
```markdown
# Clean Code 체크리스트

## Meaningful Names
- [x] 의도를 드러내는 이름 사용
- [x] 발음 가능한 이름
- [x] 검색 가능한 이름

## Functions
- [x] 함수는 한 가지 일만
- [x] 20줄 이하 유지

## SOLID
- [x] Single Responsibility Principle
- [x] Open/Closed Principle
...
```

## 검증 체크리스트

- [ ] 리팩토링 후 모든 테스트 통과
- [ ] Clean Code 원칙 준수
- [ ] DDD 패턴 적용
- [ ] 중복 코드 제거
- [ ] 복잡도 감소

## 다음 단계
리팩토링 완료 후 `/verify` 실행

## 참조
- Clean Code 체크리스트: [clean-code.md](references/clean-code.md)
- DDD 전술적 패턴: [ddd-tactical.md](references/ddd-tactical.md)
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
