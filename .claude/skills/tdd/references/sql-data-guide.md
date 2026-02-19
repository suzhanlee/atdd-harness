# SQL Test Data Guide

E2E 테스트에서 도메인 제약조건(외래키, NOT NULL 등)을 준수하는 INSERT SQL 작성 가이드입니다.

---

## 1. 엔티티 간 관계 파악 방법

### JPA 어노테이션으로 관계 식별

| JPA 어노테이션 | 관계 | DB 표현 | 예시 |
|----------------|------|---------|------|
| `@OneToOne` | 1:1 | FK (UNIQUE) | User - Profile |
| `@OneToMany` | 1:N | 자식 FK | User - Orders |
| `@ManyToOne` | N:1 | FK | Order - User |
| `@ManyToMany` | N:M | 조인 테이블 | Student - Course |

### 관계 파악 체크리스트

1. **Entity에서 @*ToOne, @*ToMany 식별**
   ```java
   @Entity
   public class Order {
       @ManyToOne
       @JoinColumn(name = "user_id")
       private User user;  // FK: user_id → users.id

       @OneToMany(mappedBy = "order")
       private List<OrderItem> items;  // 자식 테이블에서 참조
   }
   ```

2. **mappedBy/JoinColumn으로 주인 확인**
   - `@JoinColumn`이 있는 쪽이 FK 관리 주인
   - `mappedBy`는 반대편 매핑 참조

3. **nullable 여부 확인**
   ```java
   @JoinColumn(name = "user_id", nullable = false)  // 필수 관계
   private User user;
   ```

---

## 2. INSERT 순서 규칙 (핵심)

### 원칙: 부모 → 자식 순서

```
1. 독립 엔티티 먼저 (FK 없음)
2. FK를 가진 엔티티는 참조 대상 INSERT 후
3. N:M 조인 테이블은 마지막
```

### 예시: 주문 시스템

```sql
-- 1순위: 독립 엔티티 (FK 없음)
INSERT INTO users (id, email, name) VALUES (1, 'user@test.com', '테스터');
INSERT INTO products (id, name, price) VALUES (100, '상품A', 10000);

-- 2순위: FK를 가진 엔티티
INSERT INTO orders (id, user_id, status) VALUES (1, 1, 'CREATED');

-- 3순위: N:M 조인 테이블 (양쪽 FK 참조)
INSERT INTO order_products (order_id, product_id, quantity) VALUES (1, 100, 2);
```

### FK 체인이 있는 경우

```sql
-- 체인: Category → Product → OrderItem → Order → User

-- 1단계: 최상위 부모
INSERT INTO categories (id, name) VALUES (1, '전자제품');

-- 2단계: 자식
INSERT INTO products (id, category_id, name) VALUES (100, 1, '노트북');

-- 3단계: 손자
INSERT INTO order_items (id, product_id, quantity) VALUES (1, 100, 1);
```

---

## 3. @Sql 어노테이션 활용

### 단일 SQL 파일

```java
@Sql("/testdata/users.sql")
class UserE2ETest {
    // users.sql이 테스트 실행 전 자동 실행
}
```

### 다중 SQL 파일 (순서 중요!)

```java
@Sql({
    "/testdata/users.sql",      // 1순위: 독립 엔티티
    "/testdata/products.sql",   // 2순위: 독립 엔티티
    "/testdata/orders.sql",     // 3순위: users 참조
    "/testdata/order-items.sql" // 4순위: orders, products 참조
})
class OrderE2ETest {
    // FK 제약조건을 고려한 순서로 실행
}
```

### 실행 시점 제어

```java
// 테스트 전 셋업
@Sql(scripts = "/testdata/setup.sql",
     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)

// 테스트 후 정리
@Sql(scripts = "/testdata/cleanup.sql",
     executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
```

### SQL 파일 위치

```
src/test/resources/
├── testdata/
│   ├── users.sql
│   ├── products.sql
│   ├── orders.sql
│   └── cleanup.sql
```

---

## 4. 제약조건 체크리스트

### NOT NULL 제약조건

| JPA 표현 | SQL 작성 시 주의 |
|----------|-----------------|
| `nullable = false` | 반드시 값 지정 |
| `@Column(nullable = false)` | NULL 불가 |
| 원시 타입 (int, long) | 기본값 필요 |

```java
// Entity
@Column(nullable = false)
private String email;

// SQL - 반드시 값 지정
INSERT INTO users (id, email) VALUES (1, 'test@test.com');  // OK
INSERT INTO users (id, email) VALUES (1, NULL);             // 에러!
```

### UNIQUE 제약조건

| JPA 표현 | SQL 작성 시 주의 |
|----------|-----------------|
| `unique = true` | 중복 금지 |
| `@Column(unique = true)` | 같은 값 재사용 불가 |

```sql
-- 첫 번째 테스트: OK
INSERT INTO users (id, email) VALUES (1, 'test@test.com');

-- 같은 테스트 클래스 내 다른 테스트: 에러!
-- DELETE 후 INSERT 또는 다른 이메일 사용 필요
INSERT INTO users (id, email) VALUES (2, 'test@test.com');  -- Duplicate entry!
```

### LENGTH 제약조건

| JPA 표현 | SQL 작성 시 주의 |
|----------|-----------------|
| `length = N` | 길이 제한 |
| `@Column(length = 100)` | 최대 100자 |

```sql
-- OK
INSERT INTO users (id, name) VALUES (1, '홍길동');

-- 에러: Data truncation
INSERT INTO users (id, name) VALUES (2, REPEAT('a', 101));
```

### ENUM 제약조건

| JPA 표현 | SQL 작성 시 주의 |
|----------|-----------------|
| `@Enumerated(EnumType.STRING)` | 유효한 ENUM 값 |
| `@Enumerated(EnumType.ORDINAL)` | 유효한 순서 |

```java
// Entity
public enum OrderStatus {
    CREATED, PAID, SHIPPED, DELIVERED, CANCELLED
}

@Enumerated(EnumType.STRING)
private OrderStatus status;
```

```sql
-- OK: STRING 타입
INSERT INTO orders (id, status) VALUES (1, 'CREATED');

-- 에러: 유효하지 않은 값
INSERT INTO orders (id, status) VALUES (2, 'INVALID_STATUS');
```

---

## 5. @Id 생성 전략별 대응

### IDENTITY (MySQL AUTO_INCREMENT)

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```

```sql
-- id 생략 (AUTO_INCREMENT 사용)
INSERT INTO users (email, name) VALUES ('test@test.com', '테스터');

-- 명시적 id도 가능 (권장하지 않음)
INSERT INTO users (id, email, name) VALUES (1, 'test@test.com', '테스터');
```

### TABLE / SEQUENCE

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
}
```

```sql
-- 명시적 ID 지정 필요
INSERT INTO users (id, email, name) VALUES (1, 'test@test.com', '테스터');
INSERT INTO users (id, email, name) VALUES (2, 'test2@test.com', '테스터2');

-- 참조하는 자식 테이블에서 사용할 ID 미리 확보
INSERT INTO orders (id, user_id) VALUES (1, 1);
```

### 권장 사항

| 전략 | 테스트 SQL | 비고 |
|------|----------|------|
| IDENTITY | id 생략 또는 명시 | 일관성 위해 명시 권장 |
| SEQUENCE | id 명시 | 필수 |
| TABLE | id 명시 | 필수 |
| UUID | id 명시 | 필수 |

---

## 6. @Embedded 컬럼 전개

### Entity 정의

```java
@Entity
public class User {
    @Id
    private Long id;

    private String email;

    @Embedded
    private Address address;
}

@Embeddable
public class Address {
    private String street;
    private String city;
    private String zipCode;
}
```

### SQL 작성 - 컬럼명으로 전개

```sql
-- @Embedded 필드는 컬럼명으로 전개됨
INSERT INTO users (id, email, street, city, zip_code)
VALUES (1, 'test@test.com', '강남대로', '서울', '12345');
```

### @AttributeOverride 사용 시

```java
@Entity
public class User {
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "home_street")),
        @AttributeOverride(name = "city", column = @Column(name = "home_city"))
    })
    private Address homeAddress;
}
```

```sql
-- 오버라이드된 컬럼명 사용
INSERT INTO users (id, email, home_street, home_city, zip_code)
VALUES (1, 'test@test.com', '강남대로', '서울', '12345');
```

---

## 7. 소프트 삭제 데이터 표현

### Entity 정의

```java
@Entity
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class User {
    @Id
    private Long id;

    private LocalDateTime deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
```

### SQL 작성

```sql
-- 활성 사용자 (deleted_at IS NULL)
INSERT INTO users (id, email, name, deleted_at)
VALUES (1, 'active@test.com', '홍길동', NULL);

-- 삭제된 사용자 (deleted_at NOT NULL)
INSERT INTO users (id, email, name, deleted_at)
VALUES (2, 'deleted@test.com', '삭제된사용자', '2024-01-01 00:00:00');
```

### 테스트 시나리오 예시

```gherkin
Scenario: 삭제된 사용자는 조회되지 않는다
  Given 삭제된 사용자가 존재한다
  When 사용자 목록을 조회한다
  Then 목록에 삭제된 사용자가 포함되지 않는다
```

```sql
-- deleted_at IS NULL 조건으로 자동 필터링
SELECT * FROM users WHERE deleted_at IS NULL;
```

---

## 8. DELETE 전략

### 8.1 FK 역순 삭제 (권장)

```sql
-- 자식 → 부모 순서 (INSERT의 역순)
DELETE FROM order_items WHERE order_id = 1;
DELETE FROM orders WHERE id = 1;
DELETE FROM users WHERE id = 1;
```

### 8.2 TRUNCATE (FK 비활성화 필요)

```sql
-- MySQL
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- H2
SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE users;
SET REFERENTIAL_INTEGRITY TRUE;
```

### 8.3 @Before 훅에서 정리

```java
@Before
public void cleanUp() {
    jdbcTemplate.execute("DELETE FROM order_items");
    jdbcTemplate.execute("DELETE FROM orders");
    jdbcTemplate.execute("DELETE FROM users");
    // FK 역순으로 삭제
}
```

### 8.4 @Sql로 정리

```java
@Sql(scripts = "/testdata/cleanup.sql",
     executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
```

```sql
-- cleanup.sql
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM users;
```

---

## 9. TestContainers 연동

### MySQL TestContainers 설정

```java
@TestConfiguration
static class TestContainersConfig {
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");
}

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepositoryIntegrationTest {
    // 각 테스트마다 독립적인 DB 환경
}
```

### application-test.yml

```yaml
spring:
  datasource:
    url: jdbc:tc:mysql:8.0:///testdb
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver
  jpa:
    hibernate:
      ddl-auto: create-drop
```

---

## 10. 문제 해결

### 자주 발생하는 에러

| 에러 | 원인 | 해결 |
|------|------|------|
| `Cannot add or update a child row` | 부모 레코드 없음 | 부모 INSERT 먼저 |
| `Duplicate entry` | UNIQUE 위반 | DELETE 후 INSERT |
| `Data truncation` | 길이 초과 | length 확인 |
| `Column cannot be null` | NOT NULL 위반 | 필수 컬럼 값 지정 |
| `Cannot delete or update a parent row` | 자식 레코드 존재 | 자식 DELETE 먼저 |

### 디버깅 팁

```java
// SQL 로깅 활성화
@SpyBean
private JdbcTemplate jdbcTemplate;

// 또는 application-test.yml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### 제약조건 확인 쿼리

```sql
-- MySQL: 테이블 제약조건 확인
SELECT
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'your_database';

-- H2: 제약조건 확인
SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS;
```

---

## 체크리스트

### INSERT 전 체크리스트

- [ ] FK 참조 대상이 먼저 INSERT 되었는가?
- [ ] NOT NULL 컬럼에 모두 값이 지정되었는가?
- [ ] UNIQUE 컬럼에 중복 값이 없는가?
- [ ] ENUM 값이 유효한가?
- [ ] @Embedded 컬럼이 전개되었는가?

### DELETE 전 체크리스트

- [ ] 자식 테이블을 먼저 삭제했는가?
- [ ] FK 역순으로 삭제하는가?
- [ ] TRUNCATE 시 FK 체크를 비활성화했는가?

---

## 관련 문서

- [E2E 테스트 템플릿](e2e-test-template.md) - Cucumber + @Sql 활용
- [통합 테스트 템플릿](integration-test-template.md) - Repository 테스트
