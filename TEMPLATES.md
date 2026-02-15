# ATDD Harness - Templates

## 1. JPA Entity Template

```java
package com.example.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "table_name")
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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 정적 팩토리 메서드
    public static EntityName create(String fieldName) {
        EntityName entity = new EntityName();
        entity.fieldName = fieldName;
        entity.createdAt = LocalDateTime.now();
        entity.updatedAt = LocalDateTime.now();
        return entity;
    }

    // 비즈니스 메서드
    public void updateFieldName(String fieldName) {
        this.fieldName = fieldName;
        this.updatedAt = LocalDateTime.now();
    }

    // 소프트 삭제
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
```

---

## 2. Value Object Template

```java
package com.example.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class ValueObjectName {

    private String value;

    private ValueObjectName(String value) {
        validate(value);
        this.value = value;
    }

    public static ValueObjectName from(String value) {
        return new ValueObjectName(value);
    }

    private void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Value cannot be null or blank");
        }
        // 추가 검증 로직
    }
}
```

---

## 3. Repository Interface Template

```java
package com.example.domain.repository;

import com.example.domain.entity.EntityName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EntityNameRepository extends JpaRepository<EntityName, Long> {

    Optional<EntityName> findByFieldName(String fieldName);

    List<EntityName> findByFieldNameContaining(String keyword);

    @Query("SELECT e FROM EntityName e WHERE e.fieldName = :fieldName AND e.deletedAt IS NULL")
    Optional<EntityName> findActiveByFieldName(@Param("fieldName") String fieldName);

    boolean existsByFieldName(String fieldName);
}
```

---

## 4. Domain Service Template

```java
package com.example.domain.service;

import com.example.domain.entity.EntityName;
import com.example.domain.repository.EntityNameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EntityNameDomainService {

    private final EntityNameRepository repository;

    @Transactional(readOnly = true)
    public boolean existsByFieldName(String fieldName) {
        return repository.existsByFieldName(fieldName);
    }

    @Transactional
    public EntityName createIfNotExists(String fieldName) {
        return repository.findByFieldName(fieldName)
                .orElseGet(() -> {
                    EntityName entity = EntityName.create(fieldName);
                    return repository.save(entity);
                });
    }
}
```

---

## 5. Application Service Template

```java
package com.example.application.service;

import com.example.application.dto.EntityNameRequest;
import com.example.application.dto.EntityNameResponse;
import com.example.domain.entity.EntityName;
import com.example.domain.repository.EntityNameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EntityNameService {

    private final EntityNameRepository repository;

    @Transactional
    public EntityNameResponse create(EntityNameRequest request) {
        EntityName entity = EntityName.create(request.getFieldName());
        EntityName saved = repository.save(entity);
        return EntityNameResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public EntityNameResponse findById(Long id) {
        EntityName entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + id));
        return EntityNameResponse.from(entity);
    }

    @Transactional
    public EntityNameResponse update(Long id, EntityNameRequest request) {
        EntityName entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + id));
        entity.updateFieldName(request.getFieldName());
        return EntityNameResponse.from(entity);
    }

    @Transactional
    public void delete(Long id) {
        EntityName entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + id));
        entity.delete();
    }
}
```

---

## 6. REST Controller Template

```java
package com.example.interfaces.controller;

import com.example.application.dto.EntityNameRequest;
import com.example.application.dto.EntityNameResponse;
import com.example.application.service.EntityNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/entities")
@RequiredArgsConstructor
public class EntityNameController {

    private final EntityNameService service;

    @PostMapping
    public ResponseEntity<EntityNameResponse> create(@RequestBody EntityNameRequest request) {
        EntityNameResponse response = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/entities/" + response.getId()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityNameResponse> findById(@PathVariable Long id) {
        EntityNameResponse response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityNameResponse> update(
            @PathVariable Long id,
            @RequestBody EntityNameRequest request) {
        EntityNameResponse response = service.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
```

---

## 7. JUnit5 Unit Test Template

```java
package com.example.unit;

import com.example.domain.entity.EntityName;
import com.example.domain.repository.EntityNameRepository;
import com.example.application.service.EntityNameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class EntityNameServiceTest {

    @Mock
    private EntityNameRepository repository;

    @InjectMocks
    private EntityNameService service;

    @Nested
    @DisplayName("create 메서드는")
    class Create {

        @Test
        @DisplayName("유효한 요청으로 Entity를 생성한다")
        void create_withValidRequest() {
            // given
            given(repository.save(any(EntityName.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            var response = service.create(new EntityNameRequest("test"));

            // then
            assertThat(response.getFieldName()).isEqualTo("test");
            then(repository).should().save(any(EntityName.class));
        }
    }

    @Nested
    @DisplayName("findById 메서드는")
    class FindById {

        @Test
        @DisplayName("존재하는 ID로 조회 시 Entity를 반환한다")
        void findById_existingId() {
            // given
            EntityName entity = EntityName.create("test");
            given(repository.findById(1L)).willReturn(Optional.of(entity));

            // when
            var response = service.findById(1L);

            // then
            assertThat(response.getFieldName()).isEqualTo("test");
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 예외를 던진다")
        void findById_nonExistingId() {
            // given
            given(repository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.findById(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Entity not found");
        }
    }
}
```

---

## 8. Cucumber Step Definition Template

```java
package com.example.e2e;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.datatable.DataTable;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;

public class EntityNameStepDefinitions {

    @LocalServerPort
    private int port;

    private Response response;

    @Given("데이터베이스가 초기화되어 있다")
    public void databaseIsInitialized() {
        // Database cleanup and setup
    }

    @Given("다음 Entity가 존재한다")
    public void entitiesExist(DataTable dataTable) {
        // Create entities from data table
    }

    @When("Entity 생성 요청을 보낸다")
    public void sendCreateRequest(DataTable dataTable) {
        var request = dataTable.asMaps().get(0);

        response = RestAssured.given()
                .port(port)
                .contentType("application/json")
                .body(request)
                .when()
                .post("/api/v1/entities");
    }

    @When("Entity 조회 요청을 보낸다: {long}")
    public void sendFindByIdRequest(Long id) {
        response = RestAssured.given()
                .port(port)
                .when()
                .get("/api/v1/entities/" + id);
    }

    @Then("상태 코드 {int}를 받는다")
    public void verifyStatusCode(int statusCode) {
        response.then().statusCode(statusCode);
    }

    @Then("응답의 {string} 필드는 {string}이다")
    public void verifyResponseField(String field, String value) {
        response.then().body(field, equalTo(value));
    }
}
```

---

## 9. Gherkin Feature Template

```gherkin
Feature: EntityName 관리

  Background:
    Given 데이터베이스가 초기화되어 있다

  Scenario: Entity를 생성한다
    When Entity 생성 요청을 보낸다
      | fieldName |
      | test      |
    Then 상태 코드 201를 받는다
    And 응답의 "fieldName" 필드는 "test"이다

  Scenario: Entity를 조회한다
    Given 다음 Entity가 존재한다
      | id | fieldName |
      | 1  | existing  |
    When Entity 조회 요청을 보낸다: 1
    Then 상태 코드 200를 받는다
    And 응답의 "fieldName" 필드는 "existing"이다

  Scenario: 존재하지 않는 Entity 조회 시 404 반환
    When Entity 조회 요청을 보낸다: 999
    Then 상태 코드 404를 받는다

  Scenario Outline: 다양한 입력값으로 Entity를 생성한다
    When Entity 생성 요청을 보낸다
      | fieldName       |
      | <fieldName>     |
    Then 상태 코드 <statusCode>를 받는다

    Examples:
      | fieldName     | statusCode |
      | valid-name    | 201        |
      |               | 400        |
```

---

## 10. Integration Test Template

```java
package com.example.integration;

import com.example.domain.entity.EntityName;
import com.example.domain.repository.EntityNameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class EntityNameRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EntityNameRepository repository;

    @BeforeEach
    void setUp() {
        entityManager.clear();
    }

    @Test
    @DisplayName("Entity 저장 및 조회")
    void saveAndFind() {
        // given
        EntityName entity = EntityName.create("test");
        entityManager.persist(entity);

        // when
        Optional<EntityName> found = repository.findByFieldName("test");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getFieldName()).isEqualTo("test");
    }
}
```

---

## 11. DTO Template

```java
package com.example.application.dto;

import com.example.domain.entity.EntityName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityNameRequest {
    private String fieldName;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityNameResponse {
    private Long id;
    private String fieldName;

    public static EntityNameResponse from(EntityName entity) {
        return EntityNameResponse.builder()
                .id(entity.getId())
                .fieldName(entity.getFieldName())
                .build();
    }
}
```

---

## 12. Exception Handler Template

```java
package com.example.interfaces.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("IllegalArgumentException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "error", "Bad Request",
                        "message", e.getMessage()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("Unexpected exception", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "error", "Internal Server Error",
                        "message", "An unexpected error occurred"
                ));
    }
}
```
