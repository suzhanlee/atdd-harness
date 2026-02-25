# TestDataManager Quick Reference

## 개요
EntityManager를 래핑하여 테스트 데이터 셋업을 자동화하는 유틸리티. E2E/Repository 테스트에서 Given 절을 간소화한다.

## 사용 시나리오
- **E2E 테스트 Given절**: Cucumber Step Definition에서 데이터 셋업
- **Repository 테스트**: @DataJpaTest에서 데이터 생성
- **복잡한 FK 관계**: FK 엔티티 자동 조회/생성

## 주요 메서드 패턴

| 메서드 | 용도 |
|--------|------|
| `createFromDataTable(DataTable)` | Gherkin Given절에서 사용 |
| `createDefault()` | 기본값으로 빠른 생성 |
| `findByXxxOrCreate(value)` | 조회 후 없으면 생성 |
| `createWithStatus(status)` | 상태 기반 생성 |
| `createDaysAgo(days)` | 시간 기반 생성 |

## 사용 예시

### @DataJpaTest에서 사용
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
        // given
        User user = userDataManager.createByEmail("test@test.com");
        orderDataManager.createDefault(user);
        orderDataManager.clear();

        // when & then...
    }
}
```

### FK 관계 처리
```java
// 1단계: 독립 엔티티 먼저 생성
User user = userDataManager.createByEmail("user@test.com");

// 2단계: User를 참조하는 Order 생성
Order order = orderDataManager.createDefault(user);

// 자동 조회/생성 패턴
Order order = orderDataManager.createForUser("user@test.com"); // User 자동 생성
```

## 상세 구현 가이드
- [test-data-manager-template.md](test-data-manager-template.md) - 전체 구현 코드
- [advanced-given-patterns.md](../gherkin/references/advanced-given-patterns.md) - Gherkin 패턴
