---
name: tdd
description: This skill should be used when the user asks to "/tdd", "TDD 구현", "테스트 기반 개발", "Red-Green-Refactor", or needs to implement code using TDD cycle.
disable-model-invocation: true
user-invocable: true
context: fork
agent: general-purpose
allowed-tools: Read, Grep, Glob, Write, Edit, Bash
references:
  - references/e2e-test-template.md
  - references/unit-test-template.md
  - references/repository-test-template.md
  - references/sql-data-guide.md
  - ../gherkin/references/step-naming-convention.md
---

# TDD 코드 구현

## 목표
TDD 사이클을 통해 코드를 구현한다.

## 입력
- `src/test/resources/features/**/*.feature`
- `.atdd/design/domain-model.md`
- `src/main/java/**/domain/entity/*.java`

## 템플릿
- E2E 테스트: [e2e-test-template.md](references/e2e-test-template.md)
- 단위 테스트: [unit-test-template.md](references/unit-test-template.md)
- Repository 테스트: [repository-test-template.md](references/repository-test-template.md)
- SQL 데이터: [sql-data-guide.md](references/sql-data-guide.md)

## 트리거
- `/tdd` 명령어 실행
- Gherkin 시나리오 작성 완료 후 자동 제안

## TDD 사이클

```
    ┌─────────────────────────────────────┐
    │                                     │
    ▼                                     │
┌─────────┐      ┌─────────┐      ┌─────────┐
│   RED   │─────▶│  GREEN  │─────▶│REFACTOR │
│ 실패하는 │      │ 최소 구현 │      │(Phase 5)│
│  테스트  │      │         │      │         │
└─────────┘      └─────────┘      └─────────┘
```

### 1. RED (실패하는 테스트 작성)
```java
@Test
@DisplayName("회원 생성")
void createUser() {
    // given
    UserRequest request = new UserRequest("test@test.com", "password", "name");

    // when
    UserResponse response = userService.create(request);

    // then
    assertThat(response.getEmail()).isEqualTo("test@test.com");
}
```
→ 테스트 실행: 실패 ❌

### 2. GREEN (최소 구현)
```java
@Service
public class UserService {
    public UserResponse create(UserRequest request) {
        return new UserResponse(1L, request.getEmail(), request.getName());
    }
}
```
→ 테스트 실행: 성공 ✅

### 3. REFACTOR (코드 개선)
Phase 5에서 진행

## 프로세스

### 1. Feature 파일 분석
```bash
Read src/test/resources/features/*.feature
```

### 2. Step Definition 생성 (RED)

**Step 패턴 준수**:
- [step-naming-convention.md](../gherkin/references/step-naming-convention.md)에 정의된 패턴 사용
- Gherkin 시나리오의 Step이 Convention을 따르는지 확인
- Convention을 벗어난 Step이 있으면 경고 후 수정 제안

Cucumber Step Definitions 작성

```java
public class UserStepDefinitions {

    @When("회원가입 요청을 보낸다")
    public void sendCreateRequest(DataTable dataTable) {
        Map<String, String> request = dataTable.asMaps().get(0);
        response = RestAssured.given()
            .contentType("application/json")
            .body(request)
            .post("/api/v1/users");
    }

    @Then("상태 코드 {int}를 받는다")
    public void verifyStatusCode(int statusCode) {
        response.then().statusCode(statusCode);
    }
}
```

### 3. 단위 테스트 작성 (RED)
JUnit5 테스트 작성

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService service;

    @Test
    @DisplayName("회원 생성")
    void createUser() {
        // given
        UserRequest request = new UserRequest("test@test.com", "password", "name");
        given(repository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // when
        UserResponse response = service.create(request);

        // then
        assertThat(response.getEmail()).isEqualTo("test@test.com");
    }
}
```

### 4. 프로덕션 코드 구현 (GREEN)

#### Repository 구현
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

#### Service 구현
```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    @Transactional
    public UserResponse create(UserRequest request) {
        User user = User.create(request.getEmail(), request.getPassword(), request.getName());
        User saved = repository.save(user);
        return UserResponse.from(saved);
    }
}
```

#### Controller 구현
```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody UserRequest request) {
        UserResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + response.getId()))
            .body(response);
    }
}
```

### 5. 테스트 통과 확인 (GREEN)

```bash
# 단위 테스트
./gradlew test

# 통합 테스트
./gradlew integrationTest

# E2E 테스트
./gradlew cucumber

# 모든 테스트
./gradlew check
```

### 6. 커버리지 확인

```bash
./gradlew jacocoTestReport
```
→ 80% 이상 달성 목표

## 테스트 레이어 구조

```
src/test/java/
├── unit/                    # 단위 테스트
│   ├── domain/             # Entity, VO 테스트
│   └── application/        # Service 테스트
├── integration/             # Repository 테스트 (@DataJpaTest)
│   └── repository/         # JPA 연동 검증
└── e2e/                     # E2E 테스트
    └── step/               # Cucumber Step Definitions
```

## 테스트 실행 명령

| 명령어 | 설명 |
|--------|------|
| `./gradlew test` | 단위 테스트 |
| `./gradlew integrationTest` | Repository 테스트 |
| `./gradlew cucumber` | E2E 테스트 |
| `./gradlew check` | 모든 테스트 |
| `./gradlew jacocoTestReport` | 커버리지 리포트 |

## 출력 파일

### src/test/java/**/e2e/**/*.java
Step Definition 클래스들

### src/test/java/**/unit/**/*.java
단위 테스트 클래스들

### src/test/java/**/integration/**/*.java
Repository 테스트 클래스들

### src/main/java/**/*.java
프로덕션 코드

## 검증 체크리스트

- [ ] 모든 테스트 통과
- [ ] 커버리지 80% 이상
- [ ] 모든 Gherkin 시나리오 실행
- [ ] 코드 컴파일 에러 없음
- [ ] Lint 에러 없음

## 다음 단계
모든 테스트 통과 후 `/refactor` 실행

## 참조
- E2E 템플릿: [e2e-test-template.md](references/e2e-test-template.md)
- 단위 템플릿: [unit-test-template.md](references/unit-test-template.md)
- Repository 템플릿: [repository-test-template.md](references/repository-test-template.md)
- SQL 가이드: [sql-data-guide.md](references/sql-data-guide.md)
- Step 네이밍 컨벤션: [step-naming-convention.md](../gherkin/references/step-naming-convention.md)
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
