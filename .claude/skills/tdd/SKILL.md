---
name: tdd
description: Use when the user asks to "/tdd", "TDD 구현", "테스트 기반 개발", "Red-Green-Refactor", or needs to implement code using test-driven development.
disable-model-invocation: false
user-invocable: true
context: fork
agent: general-purpose
allowed-tools: Read, Grep, Glob, Write, Edit, Bash
references:
  - references/e2e-test-template.md
  - references/unit-test-template.md
  - references/repository-test-template.md
  - references/sql-data-guide.md
  - references/test-data-manager-template.md
  - references/layered-tdd-guide.md
  - ../gherkin/references/step-naming-convention.md
  - ../gherkin/references/advanced-given-patterns.md
---

# TDD 코드 구현

## 목표
TDD 사이클을 통해 코드를 구현한다.

## 입력
- `.atdd/context.json` - featurePath, module 참조
- `src/test/resources/features/**/*.feature` - featurePath 없을 때 기본 경로
- `.atdd/design/domain-model.md`
- `src/main/java/**/domain/entity/*.java`

## Context 기반 경로

### Feature 파일 경로 결정
1. `.atdd/context.json` 읽기
2. `featurePath` 필드 있으면 해당 경로 사용
3. 없으면 기본 경로 `src/test/resources/features/**/*.feature` 사용

### 멀티 모듈 지원
- `context.module` 있으면 `{module}/src/test/resources/features/` 경로 사용
- 단일 모듈이면 루트 기준 `src/test/resources/features/` 사용

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
// WHY: 테스트를 먼저 작성해야 무엇을 구현해야 할지 명확해진다
```java
@Test
@DisplayName("회원 생성")
void createUser() {
    // given - 테스트 전제 조건 설정
    UserRequest request = new UserRequest("test@test.com", "password", "name");

    // when - 테스트 대상 동작 수행
    UserResponse response = userService.create(request);

    // then - 결과 검증
    assertThat(response.getEmail()).isEqualTo("test@test.com");
}
```
// WHY: 테스트가 실패해야 구현이 필요함을 증명한다
→ 테스트 실행: 실패 ❌

### 2. GREEN (최소 구현)
// WHY: 테스트를 통과하는 최소 코드만 작성해야 오버엔지니어링을 방지한다
```java
@Service
public class UserService {
    public UserResponse create(UserRequest request) {
        // WHY: 하드코딩도 허용 - 테스트 통과가 최우선
        return new UserResponse(1L, request.getEmail(), request.getName());
    }
}
```
// WHY: GREEN 상태에서만 리팩토링이 안전하다
→ 테스트 실행: 성공 ✅

### 3. REFACTOR (코드 개선)
Phase 5에서 진행

## Red Flags - STOP and Start Over

다음 중 하나라도 해당하면 **코드를 삭제하고 테스트부터 다시 시작**:

- 테스트 없이 코드를 먼저 작성
- "이미 수동으로 테스트했어"
- "이건 너무 간단해서 테스트할 필요 없어"
- "이번 한 번만"이라고 생각
- RED 단계를 건너뜀

**All of these mean: Delete code. Write test first. No exceptions.**

## TDD 합리화 차단

| Excuse | Reality |
|--------|---------|
| "Too simple to test" | Simple code breaks too. |
| "I'll test after" | Tests after prove nothing. |
| "Just this once" | Every exception becomes the rule. |
| "I already tested manually" | Manual tests aren't repeatable. |
| "Deadline pressure" | TDD saves time in the long run. |

## ❌ BAD vs ✅ GOOD

### ❌ BAD: 테스트 없이 구현
```java
// BAD: 테스트 없이 바로 구현 - 무엇을 테스트해야 하는지 모름
@Service
public class UserService {
    public UserResponse create(UserRequest request) {
        // 구현 내용...
    }
}
```

### ✅ GOOD: 테스트 우선
```java
// GOOD: 테스트가 구현을 주도한다
// 1. 실패하는 테스트 작성
// 2. 최소 구현으로 테스트 통과
// 3. 리팩토링
```

## 워크플로우 분기

**새 기능 구현?** → 전체 TDD 사이클 (RED → GREEN → REFACTOR)
**버그 수정?** → 실패하는 테스트 먼저 작성 → 수정 → 통과 확인
**리팩토링만?** → `/refactor` 스킬 사용 (Phase 5)

## 프로세스

### 0. Context 로드
```
Read .atdd/context.json
```
- `featurePath` 확인: Feature 파일 경로
- `module` 확인: 멀티 모듈 시 사용할 모듈
- `topic` 확인: 작업명

**경로 결정 로직**:
```
IF context.featurePath 존재:
    feature_path = context.featurePath
ELSE IF context.module 존재:
    feature_path = "{module}/src/test/resources/features/**/*.feature"
ELSE:
    feature_path = "src/test/resources/features/**/*.feature"
```

### 1. Feature 파일 분석
```bash
Read {feature_path}
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

### 5. 테스트 통과 확인 및 루프 (GREEN)

```
    ┌──────────────────────────────────────┐
    │                                      │
    ▼                                      │
┌─────────────┐    실패    ┌─────────────┐
│ 테스트 실행  │ ─────────▶ │  코드 수정  │
│./gradlew check│           │  (GREEN)    │
└─────────────┘            └─────────────┘
        │
        │ 성공
        ▼
  ┌───────────┐
  │ 다음 단계  │
  └───────────┘
```

```bash
# 모든 테스트 실행
./gradlew check
```

**⚠️ 테스트 실패 시:**
1. 실패한 테스트 분석
2. 프로덕션 코드 또는 테스트 코드 수정
3. `./gradlew check` 재실행
4. **모든 테스트 통과까지 반복 (강제)**

**테스트가 통과할 때까지 스킬을 완료할 수 없음.**

```bash
# 개별 테스트 실행 (필요 시)
./gradlew test           # 단위 테스트
./gradlew integrationTest # 통합 테스트
./gradlew cucumber        # E2E 테스트
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
├── e2e/                     # E2E 테스트
│   └── step/               # Cucumber Step Definitions
└── fixture/                 # TestDataManager (TestData Fixture)
    └── *TestDataManager.java
```

## 테스트 실행 명령

| 명령어 | 설명 |
|--------|------|
| `./gradlew test` | 단위 테스트 |
| `./gradlew integrationTest` | Repository 테스트 |
| `./gradlew cucumber` | E2E 테스트 |
| `./gradlew check` | 모든 테스트 |
| `./gradlew jacocoTestReport` | 커버리지 리포트 |

## TestDataManager 패턴

### 개념
EntityManager를 래핑하여 테스트 데이터 셋업을 자동화하는 유틸리티. Given 절의 데이터 준비를 간소화하고 복잡한 FK 관계를 자동으로 처리한다.

### 사용 시나리오
1. **E2E 테스트 Given절**: Cucumber Step Definition에서 데이터 셋업
2. **Repository 테스트 데이터 준비**: @DataJpaTest에서 데이터 생성
3. **복잡한 FK 관계의 데이터 셋업**: FK 엔티티 자동 조회/생성

### 기본 구조
```java
@DataJpaTest
@Import({UserTestDataManager.class, OrderTestDataManager.class})
class OrderRepositoryTest {

    @Autowired
    private OrderTestDataManager orderDataManager;

    @Autowired
    private UserTestDataManager userDataManager;

    @BeforeEach
    void setUp() {
        orderDataManager.deleteAll();
        userDataManager.deleteAll();
    }

    @Test
    void findByUser() {
        // given - TestDataManager로 간단히 셋업
        User user = userDataManager.createByEmail("test@test.com");
        orderDataManager.createDefault(user);
        orderDataManager.clear();

        // when & then...
    }
}
```

### 주요 메서드 패턴

| 메서드 | 용도 |
|--------|------|
| `createFromDataTable(DataTable)` | Gherkin Given절에서 사용 |
| `createDefault()` | 기본값으로 빠른 생성 |
| `findByXxxOrCreate(value)` | 조회 후 없으면 생성 |
| `createWithStatus(status)` | 상태 기반 생성 |
| `createDaysAgo(days)` | 시간 기반 생성 |

### 참조
- [test-data-manager-template.md](references/test-data-manager-template.md) - 구현 가이드
- [advanced-given-patterns.md](../gherkin/references/advanced-given-patterns.md) - Gherkin 패턴

---

## 계층별 TDD (Layered TDD)

### Inside-Out 개발 순서

```
┌─────────────────────────────────────────────────────────────────┐
│                          API Layer                               │
│                    (Controller, E2E Test)                        │
│    4단계: Cucumber E2E 테스트 → Controller 구현                   │
├─────────────────────────────────────────────────────────────────┤
│                      Application Layer                           │
│                    (Service, Facade)                             │
│    3단계: Service 단위 테스트 → Service 구현                       │
├─────────────────────────────────────────────────────────────────┤
│                      Repository Layer                            │
│              (Repository, @DataJpaTest)                          │
│    2단계: Repository 통합 테스트 → Repository 구현                 │
├─────────────────────────────────────────────────────────────────┤
│                        Domain Layer                              │
│                  (Entity, VO, Domain Service)                    │
│    1단계: Domain 단위 테스트 → Entity/VO 구현                      │
└─────────────────────────────────────────────────────────────────┘
```

### 계층별 특징

| 계층 | 테스트 타입 | TestDataManager | 특징 |
|------|------------|-----------------|------|
| Domain | 순수 Java 단위 테스트 | ❌ 사용 안 함 | Mock 없음, 빠른 실행 |
| Repository | @DataJpaTest 통합 테스트 | ✅ 사용 | 실제 DB 테스트 |
| Application | Mockito 단위 테스트 | ❌ 사용 안 함 | Mock 기반 |
| API | Cucumber E2E 테스트 | ✅ 사용 | 전체 스택 테스트 |

### 계층별 테스트 실행

```bash
# 1단계: Domain 단위 테스트
./gradlew test --tests "*.domain.*"

# 2단계: Repository 통합 테스트
./gradlew integrationTest

# 3단계: Application 단위 테스트
./gradlew test --tests "*.application.*"

# 4단계: E2E 테스트
./gradlew cucumber

# 전체 검증
./gradlew check
```

### 참조
- [layered-tdd-guide.md](references/layered-tdd-guide.md) - 상세 가이드

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

## ⚠️ 종료 전 필수 체크리스트

**스킬 종료 전 반드시 수행:**
- [ ] 모든 테스트 통과 확인 (`./gradlew check`)
- [ ] 커버리지 80% 이상 달성 확인
- [ ] context.json의 `status`를 "completed"로 변경
- [ ] context.json의 `updated_at`을 현재 시간으로 변경

**❌ 위 체크리스트 미완료 시 스킬이 완료되지 않은 것으로 간주**

## 다음 단계
모든 테스트 통과 후 `/refactor` 실행

## 참조
- E2E 템플릿: [e2e-test-template.md](references/e2e-test-template.md)
- 단위 템플릿: [unit-test-template.md](references/unit-test-template.md)
- Repository 템플릿: [repository-test-template.md](references/repository-test-template.md)
- SQL 가이드: [sql-data-guide.md](references/sql-data-guide.md)
- TestDataManager 템플릿: [test-data-manager-template.md](references/test-data-manager-template.md)
- 계층별 TDD 가이드: [layered-tdd-guide.md](references/layered-tdd-guide.md)
- Step 네이밍 컨벤션: [step-naming-convention.md](../gherkin/references/step-naming-convention.md)
- 고급 Given 패턴: [advanced-given-patterns.md](../gherkin/references/advanced-given-patterns.md)
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)

## Definition of Done (DoD)

**⚠️ 스킬 완료로 인정받기 위해 다음 조건을 모두 충족해야 함:**

| # | 조건 | 검증 |
|---|------|------|
| 1 | 모든 테스트 통과 | 필수 |
| 2 | 커버리지 80% 이상 | 필수 |
| 3 | context.json `status` = "completed" | 필수 |
| 4 | context.json `updated_at` = 현재 시간 | 필수 |
| 5 | 모든 Gherkin 시나리오 실행 완료 | 필수 |

**context.json 업데이트 예시:**
```json
{
  "phase": "tdd",
  "status": "completed",
  "updated_at": "{ISO8601}"
}
```
