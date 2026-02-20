---
name: refactor
description: This skill should be used when the user asks to "/refactor", "리팩토링", "Clean Code 적용", "코드 개선", or needs to improve code quality using Clean Code, DDD patterns, and Martin Fowler's refactoring techniques.
disable-model-invocation: false
user-invocable: true
context: fork
agent: general-purpose
allowed-tools: Read, Grep, Glob, Write, Edit, Bash, AskUserQuestion, EnterPlanMode
references:
  - references/clean-code.md
  - references/ddd-tactical.md
  - references/refactoring-catalog.md
---

# Clean Code 리팩토링

## 목표
Martin Fowler의 리팩토링 기법, Clean Code 원칙, DDD 패턴을 적용하여 코드 품질을 개선한다.

## 입력
- `src/main/java/**/*.java`
- `src/test/java/**/*.java`

## 상세 가이드
- Clean Code 체크리스트: [clean-code.md](references/clean-code.md)
- DDD 전술적 패턴: [ddd-tactical.md](references/ddd-tactical.md)
- 리팩토링 카탈로그: [refactoring-catalog.md](references/refactoring-catalog.md)

## 트리거
- `/refactor` 명령어 실행
- TDD 구현 완료 후 자동 제안

---

## STOP PROTOCOL

### 2-Phase 진행 규칙
각 Phase는 반드시 **별도 턴**으로 진행한다.

```
Phase A (Code Analysis & Questions) → AskUserQuestion으로 리팩토링 방향 협의
Phase B (Plan & Execute)            → EnterPlanMode로 계획 수립 → 승인 후 실행
```

### Phase A 종료 조건
- 사용자가 리팩토링 범위와 우선순위를 확인
- 주요 리팩토링 항목에 동의

### Phase B 종료 필수 문구
```
---
리팩토링 계획을 검토해주세요.
승인하시면 "시작" 또는 "진행"이라고 입력해주세요.
수정이 필요하면 변경 사항을 말씀해주세요.
```

---

## 2-Phase 워크플로우

### Phase A: Code Analysis & Questions (코드 분석 및 질문)

**목적**: 코드 품질 문제를 식별하고 사용자와 리팩토링 방향을 협의

**진행 방식**:
1. 코드 분석 수행
2. Code Smell 식별
3. AskUserQuestion으로 리팩토링 방향 질문

**코드 분석 체크리스트**:

| 카테고리 | 항목 | 감지 방법 |
|----------|------|----------|
| 이름 | Mysterious Name | 의도가 불분명한 변수/함수명 |
| 구조 | Duplicated Code | 동일/유사 코드 블록 |
| 구조 | Long Function | 20줄 이상 함수 |
| 구조 | Long Parameter List | 4개 이상 매개변수 |
| 구조 | Large Class | 200줄 이상 또는 여러 책임 |
| 결합 | Feature Envy | 타 클래스 데이터 과다 사용 |
| 결합 | Switch Statements | 반복되는 switch/if-else |
| 품질 | Dead Code | 사용되지 않는 코드 |
| 품질 | Primitive Obsession | 기본 타입 과다 사용 |
| 품질 | Data Clumps | 항상 함께 사용되는 데이터 그룹 |

**AskUserQuestion 질문 예시**:

```
Q1: 리팩토링 범위 선택
- 질문: "어떤 범위에서 리팩토링을 진행할까요?"
- 옵션: [전체 코드, 특정 모듈만, 특정 파일만]

Q2: 우선순위 결정
- 질문: "다음 중 어떤 개선을 우선할까요?"
- 옵션: [가독성 향상, 중복 제거, 성능 최적화, 구조 개선]

Q3: 리팩토링 강도
- 질문: "얼마나 적극적으로 리팩토링할까요?"
- 옵션: [최소 변경 (필수만), 보통 (권장 사항), 적극적 (모든 개선)]
```

**Phase A 출력 형식**:

```markdown
## 코드 분석 결과

### 발견된 Code Smells
| 파일 | 라인 | 유형 | 설명 | 우선순위 |
|------|------|------|------|----------|
| UserService.java | 45-89 | Long Function | createOrder()가 44줄 | High |
| Order.java | 120 | Feature Envy | Payment 데이터 과다 사용 | Medium |
| ... | ... | ... | ... | ... |

### Clean Code 위반 사항
- [ ] Meaningful Names: 3건
- [ ] Small Functions: 2건
- [ ] DRY: 4건

### DDD 패턴 개선 필요
- [ ] Entity vs Value Object 구분
- [ ] Aggregate 경계 재설정

### 리팩토링 제안
1. **UserService.createOrder() 분해**
   - Extract Function으로 5개 메서드 분리
   - 이유: 단일 책임 원칙 준수

2. **Order-Payment 관계 재설계**
   - Feature Envy 해결: Payment 관련 로직 이동
   - 이유: 데이터와 행동의 응집도 향상
```

---

### Phase B: Plan & Execute (계획 수립 및 실행)

**목적**: 구체적인 리팩토링 계획을 수립하고 사용자 승인 하에 실행

**진행 방식**:
1. **EnterPlanMode 호출**
2. 리팩토링 계획 수립
3. 사용자 승인 대기
4. 승인 후 순차 실행
5. 각 단계마다 테스트 검증

**Plan Mode에서 수행할 작업**:

```markdown
## 리팩토링 계획

### 작업 항목
1. [UserService.java] createOrder() 함수 분해
   - Before: 44줄, 5가지 책임
   - After: 5개 메서드로 분리 (각 10줄 이내)
   - 기법: Extract Function

2. [Order.java] Payment 로직 이동
   - Before: Order.calculatePaymentFee()
   - After: Payment.calculateFee()
   - 기법: Move Function

3. [UserRequest.java] Parameter Object 도입
   - Before: 5개 매개변수
   - After: UserCommand record 사용
   - 기법: Introduce Parameter Object

### 파일 변경 목록
- src/main/java/.../UserService.java (수정)
- src/main/java/.../Order.java (수정)
- src/main/java/.../Payment.java (수정)
- src/main/java/.../UserCommand.java (신규)

### 실행 순서
1. UserService 리팩토링 → 테스트 실행
2. Order/Payment 리팩토링 → 테스트 실행
3. Parameter Object 도입 → 테스트 실행
4. 전체 테스트 실행
```

**Martin Fowler의 리팩토링 기법 적용**:

| 기법 | 적용 시점 | 예시 |
|------|----------|------|
| Extract Function | 긴 함수 발견 시 | createOrder() → validate(), calculate(), save() |
| Move Function | Feature Envy 발견 시 | Order의 Payment 로직 → Payment 클래스 |
| Introduce Parameter Object | Data Clumps 발견 시 | (street, city, zip) → Address |
| Replace Conditional with Polymorphism | Switch 문 반복 시 | switch(type) → interface |
| Rename Variable | 의도 불분명 시 | d → elapsedTimeInDays |

---

## Clean Code 원칙

### 1. Meaningful Names (의미 있는 이름)

```java
// Bad
int d; // elapsed time in days
List<int[]> list1;

// Good
int elapsedTimeInDays;
List<int[]> flaggedCells;
```

### 2. Small Functions (작은 함수)

```java
// Bad: 한 함수가 여러 일을 함
public void processUser(User user) {
    validateUser(user);
    saveUser(user);
    sendEmail(user);
    logActivity(user);
}

// Good: 한 함수는 한 가지만
public void processUser(User user) {
    validateAndSave(user);
    notifyUser(user);
}
```

### 3. No Side Effects (부작용 없음)

```java
// Bad: 검증하면서 수정함
public boolean validateUser(User user) {
    if (user.getName() == null) {
        user.setName("Unknown"); // side effect!
        return false;
    }
    return true;
}

// Good: 순수 함수
public boolean isValidUser(User user) {
    return user.getName() != null;
}
```

### 4. SOLID Principles

#### Single Responsibility Principle (SRP)
```java
// Bad
class User {
    void save() { ... }
    void sendEmail() { ... }
    void generateReport() { ... }
}

// Good
class User { ... }
class UserRepository { void save(User user) { ... } }
class EmailService { void send(User user) { ... } }
class ReportGenerator { void generate(User user) { ... } }
```

#### Open/Closed Principle (OCP)
```java
// Good: 확장에는 열려있고, 수정에는 닫혀있음
interface PaymentProcessor {
    void process(Payment payment);
}

class CreditCardProcessor implements PaymentProcessor { ... }
class PayPalProcessor implements PaymentProcessor { ... }
```

### 5. DRY (Don't Repeat Yourself)

```java
// Bad: 중복 코드
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

// Good: 재사용
private void validateUserRequest(UserRequest request) {
    validateEmail(request.getEmail());
    validatePassword(request.getPassword());
}
```

---

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
// Good: 올바른 Aggregate 경계
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
// Good: 도메인 언어 사용
public interface UserRepository {
    Optional<User> findById(UserId id);
    Optional<User> findByEmail(Email email);
    void save(User user);
    void delete(User user);
}

// Bad: 구현 노출
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

---

## 리팩토링 프로세스

### 1. 코드 로드
```
Read src/main/java/**/*.java
Read src/test/java/**/*.java
```

### 2. Code Smell 탐지
- Martin Fowler의 Code Smells 체크
- Clean Code 위반 사항 체크
- DDD 패턴 위반 체크

### 3. 리팩토링 계획 수립 (Plan Mode)
- 발견된 문제점 정리
- 개선 방안 제시
- 우선순위 결정
- 사용자 승인

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

---

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
- **기법**: [적용한 리팩토링 기법]

### 2. [클래스명]
...

## 개선 사항
- [개선 항목 1]
- [개선 항목 2]

## 적용된 리팩토링 기법
- Extract Function: X건
- Move Function: X건
- Introduce Parameter Object: X건

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

---

## 검증 체크리스트

- [ ] 리팩토링 후 모든 테스트 통과
- [ ] Clean Code 원칙 준수
- [ ] DDD 패턴 적용
- [ ] 중복 코드 제거
- [ ] 복잡도 감소
- [ ] Martin Fowler 리팩토링 기법 적절히 적용

---

## 다음 단계
리팩토링 완료 후 `/verify` 실행

---

## 참조
- Clean Code 체크리스트: [clean-code.md](references/clean-code.md)
- DDD 전술적 패턴: [ddd-tactical.md](references/ddd-tactical.md)
- 리팩토링 카탈로그: [refactoring-catalog.md](references/refactoring-catalog.md)
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
