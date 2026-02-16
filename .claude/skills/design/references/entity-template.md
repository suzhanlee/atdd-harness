# Entity Template

## Rich Domain Model 원칙

**Entity에 비즈니스 로직을 포함** (Anemic Domain Model 지양)

### ❌ Anemic Domain Model (피해야 할 패턴)

```java
@Entity
public class User {
    private String email;
    private String password;
    private String status;

    // getter/setter만 존재, 로직이 Service에 위치
}
```

### ✅ Rich Domain Model (권장 패턴)

```java
@Entity
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Email email;

    @Embedded
    private Password password;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    // 기본 생성자 (JPA용)
    protected User() {}

    // 정적 팩토리 메서드
    public static User register(Email email, Password password) {
        User user = new User();
        user.email = email;
        user.password = password;
        user.status = UserStatus.PENDING;
        return user;
    }

    // 비즈니스 메서드
    public void verifyEmail() {
        if (this.status != UserStatus.PENDING) {
            throw new IllegalStateException("이미 인증된 사용자입니다.");
        }
        this.status = UserStatus.ACTIVE;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
}
```

---

## Basic Entity Template

```java
package com.example.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "table_name", indexes = {
    @Index(name = "idx_table_name_field", columnList = "field_name")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class EntityName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 정적 팩토리 메서드
    public static EntityName create(String fieldName) {
        validate(fieldName);
        EntityName entity = new EntityName();
        entity.fieldName = fieldName;
        entity.status = Status.ACTIVE;
        entity.createdAt = LocalDateTime.now();
        entity.updatedAt = LocalDateTime.now();
        return entity;
    }

    // 검증 로직
    private static void validate(String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            throw new IllegalArgumentException("fieldName is required");
        }
        if (fieldName.length() > 100) {
            throw new IllegalArgumentException("fieldName must be at most 100 characters");
        }
    }

    // 비즈니스 메서드
    public void updateFieldName(String fieldName) {
        validate(fieldName);
        this.fieldName = fieldName;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.status = Status.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.status = Status.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    // 소프트 삭제
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    // 복구
    public void restore() {
        this.deletedAt = null;
    }

    // 상태 확인 메서드
    public boolean isActive() {
        return status == Status.ACTIVE && deletedAt == null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    // 내부 Enum
    public enum Status {
        ACTIVE,
        INACTIVE
    }
}
```

---

## Entity with Relationships

```java
package com.example.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount = 0;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 정적 팩토리 메서드
    public static Order create(Long customerId) {
        Order order = new Order();
        order.customerId = customerId;
        order.status = OrderStatus.CREATED;
        order.createdAt = LocalDateTime.now();
        order.updatedAt = LocalDateTime.now();
        return order;
    }

    // 비즈니스 메서드
    public void addItem(Long productId, String productName, int quantity, int price) {
        OrderItem item = OrderItem.create(this, productId, productName, quantity, price);
        this.items.add(item);
        recalculateTotal();
        this.updatedAt = LocalDateTime.now();
    }

    public void removeItem(Long itemId) {
        this.items.removeIf(item -> item.getId().equals(itemId));
        recalculateTotal();
        this.updatedAt = LocalDateTime.now();
    }

    private void recalculateTotal() {
        this.totalAmount = items.stream()
                .mapToInt(OrderItem::getSubtotal)
                .sum();
    }

    public void pay() {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("Only created orders can be paid");
        }
        this.status = OrderStatus.PAID;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel shipped or delivered orders");
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public enum OrderStatus {
        CREATED,
        PAID,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }
}
```

---

## Entity with Embedded Value Objects

```java
package com.example.domain.entity;

import com.example.domain.vo.Email;
import com.example.domain.vo.Password;
import com.example.domain.vo.UserName;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Email email;

    @Embedded
    private Password password;

    @Embedded
    private UserName name;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static User create(Email email, Password password, UserName name) {
        User user = new User();
        user.email = email;
        user.password = password;
        user.name = name;
        user.role = Role.USER;
        user.createdAt = LocalDateTime.now();
        user.updatedAt = LocalDateTime.now();
        return user;
    }

    public void changePassword(Password newPassword) {
        this.password = newPassword;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeName(UserName newName) {
        this.name = newName;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public enum Role {
        USER,
        ADMIN
    }
}
```

---

## BaseEntity (공통 필드)

```java
package com.example.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    protected LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    protected LocalDateTime deletedAt;

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
```

---

## DDL Template

```sql
-- 테이블 생성
CREATE TABLE entity_name (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    field_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    CONSTRAINT chk_field_name_length CHECK (CHAR_LENGTH(field_name) <= 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 인덱스 생성
CREATE INDEX idx_entity_name_field ON entity_name(field_name);
CREATE INDEX idx_entity_name_status ON entity_name(status);
CREATE INDEX idx_entity_name_created ON entity_name(created_at);

-- 소프트 삭제를 위한 뷰 (선택사항)
CREATE VIEW v_entity_name AS
SELECT * FROM entity_name WHERE deleted_at IS NULL;
```

---

## Checklist

### Entity 설계
- [ ] 기본키 (id) 존재
- [ ] 생성/수정 시간 필드 (created_at, updated_at)
- [ ] 소프트 삭제 필드 (deleted_at)
- [ ] 정적 팩토리 메서드 (create, register)
- [ ] 검증 로직 포함
- [ ] 비즈니스 메서드 (update*, delete, verify*)
- [ ] 상태 확인 메서드 (is*, has*)
- [ ] 적절한 인덱스 정의
- [ ] 제약조건 정의 (nullable, length, unique)
- [ ] 연관관계 매핑 (필요시)

### Rich Domain Model
- [ ] Anemic Domain Model이 아님 (getter/setter만 있는 경우)
- [ ] 비즈니스 로직이 Entity에 위치
- [ ] 불변식(Invariant) 검증 코드 포함
- [ ] 상태 전이 메서드 존재
- [ ] Value Object 활용

---

## Entity Unit Test Template (Inside-Out TDD)

Entity 단위 테스트는 비즈니스 로직을 검증합니다.

```java
package com.example.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User Entity 테스트")
class UserTest {

    // ========== 정적 팩토리 메서드 테스트 ==========

    @Test
    @DisplayName("정상적인 사용자 등록")
    void register_success() {
        // given
        Email email = Email.of("test@test.com");
        Password password = Password.of("password123!");

        // when
        User user = User.register(email, password);

        // then
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(user.isActive()).isFalse();
    }

    @Test
    @DisplayName("잘못된 이메일로 등록 실패")
    void register_invalidEmail_throwsException() {
        // given
        Email invalidEmail = Email.of("invalid-email");

        // when & then
        assertThatThrownBy(() -> User.register(invalidEmail, Password.of("password123!")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("올바른 이메일 형식이 아닙니다");
    }

    // ========== 불변식 테스트 ==========

    @Test
    @DisplayName("이메일 인증 성공")
    void verifyEmail_success() {
        // given
        User user = User.register(Email.of("test@test.com"), Password.of("password123!"));

        // when
        user.verifyEmail();

        // then
        assertThat(user.isActive()).isTrue();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("이미 인증된 사용자는 인증 실패")
    void verifyEmail_alreadyVerified_throwsException() {
        // given
        User user = User.register(Email.of("test@test.com"), Password.of("password123!"));
        user.verifyEmail();

        // when & then
        assertThatThrownBy(user::verifyEmail)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("이미 인증된 사용자입니다.");
    }

    // ========== 상태 전이 테스트 ==========

    @Test
    @DisplayName("사용자 비활성화")
    void deactivate_success() {
        // given
        User user = User.register(Email.of("test@test.com"), Password.of("password123!"));
        user.verifyEmail();

        // when
        user.deactivate();

        // then
        assertThat(user.isActive()).isFalse();
        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    @DisplayName("PENDING 상태에서는 비활성화 불가")
    void deactivate_pendingUser_throwsException() {
        // given
        User user = User.register(Email.of("test@test.com"), Password.of("password123!"));

        // when & then
        assertThatThrownBy(user::deactivate)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("활성화된 사용자만 비활성화할 수 있습니다.");
    }

    // ========== 비즈니스 메서드 테스트 ==========

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success() {
        // given
        User user = User.register(Email.of("test@test.com"), Password.of("oldPassword123!"));
        Password newPassword = Password.of("newPassword456!");

        // when
        user.changePassword(newPassword);

        // then
        assertThat(user.getPassword()).isEqualTo(newPassword);
    }

    // ========== 소프트 삭제 테스트 ==========

    @Test
    @DisplayName("사용자 삭제 (Soft Delete)")
    void delete_success() {
        // given
        User user = User.register(Email.of("test@test.com"), Password.of("password123!"));

        // when
        user.delete();

        // then
        assertThat(user.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("삭제된 사용자 복구")
    void restore_success() {
        // given
        User user = User.register(Email.of("test@test.com"), Password.of("password123!"));
        user.delete();

        // when
        user.restore();

        // then
        assertThat(user.isDeleted()).isFalse();
    }
}
```

---

## Value Object Template

Entity에서 사용하는 Value Object는 불변이어야 합니다.

```java
package com.example.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Email {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    @Column(name = "email", nullable = false, unique = true)
    private String value;

    private Email(String value) {
        validate(value);
        this.value = value;
    }

    public static Email of(String value) {
        return new Email(value);
    }

    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다: " + value);
        }
        if (value.length() > 255) {
            throw new IllegalArgumentException("이메일은 255자를 초과할 수 없습니다.");
        }
    }

    public String getDomain() {
        return value.substring(value.indexOf("@") + 1);
    }
}
```

---

## Service는 Entity에 로직을 위임

Service 계층에서는 비즈니스 로직을 직접 구현하지 않고 Entity에 위임합니다.

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ❌ 잘못된 예: Service에 로직이 위치
    public void verifyEmail_bad(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        if (user.getStatus() == UserStatus.ACTIVE) {  // 로직이 Service에
            throw new IllegalStateException("이미 인증된 사용자입니다.");
        }
        user.setStatus(UserStatus.ACTIVE);  // 상태 직접 변경
    }

    // ✅ 좋은 예: Entity에 로직 위임
    public void verifyEmail_good(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.verifyEmail();  // 로직은 Entity에
        userRepository.save(user);
    }

    public Long register(RegisterRequest request) {
        // 요청 검증 및 변환
        Email email = Email.of(request.getEmail());
        Password password = Password.of(request.getPassword());

        // Entity 생성 (정적 팩토리 메서드)
        User user = User.register(email, password);

        // 저장
        return userRepository.save(user).getId();
    }
}
```
