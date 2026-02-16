# Test Template

## Unit Test Template (JUnit5 + Mockito)

```java
package com.example.unit;

import com.example.application.dto.EntityNameRequest;
import com.example.application.dto.EntityNameResponse;
import com.example.application.service.EntityNameService;
import com.example.domain.entity.EntityName;
import com.example.domain.repository.EntityNameRepository;
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
@DisplayName("EntityNameService 테스트")
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
            EntityNameRequest request = new EntityNameRequest("test");
            given(repository.save(any(EntityName.class)))
                    .willAnswer(invocation -> {
                        EntityName entity = invocation.getArgument(0);
                        return entity;
                    });

            // when
            EntityNameResponse response = service.create(request);

            // then
            assertThat(response.getFieldName()).isEqualTo("test");
            then(repository).should().save(any(EntityName.class));
        }

        @Test
        @DisplayName("중복된 이름으로 생성 시 예외를 던진다")
        void create_withDuplicateName_throwsException() {
            // given
            EntityNameRequest request = new EntityNameRequest("duplicate");
            given(repository.existsByFieldName("duplicate")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");

            then(repository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("findById 메서드는")
    class FindById {

        @Test
        @DisplayName("존재하는 ID로 조회 시 Entity를 반환한다")
        void findById_existingId_returnsEntity() {
            // given
            EntityName entity = EntityName.create("test");
            given(repository.findById(1L)).willReturn(Optional.of(entity));

            // when
            EntityNameResponse response = service.findById(1L);

            // then
            assertThat(response.getFieldName()).isEqualTo("test");
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 예외를 던진다")
        void findById_nonExistingId_throwsException() {
            // given
            given(repository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.findById(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");
        }
    }
}
```

---

## Integration Test Template (DataJpaTest)

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("EntityNameRepository 통합 테스트")
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
    @DisplayName("Entity 저장 및 ID로 조회")
    void saveAndFindById() {
        // given
        EntityName entity = EntityName.create("test");
        EntityName saved = entityManager.persist(entity);
        entityManager.flush();

        // when
        Optional<EntityName> found = repository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getFieldName()).isEqualTo("test");
    }

    @Test
    @DisplayName("필드명으로 조회")
    void findByFieldName() {
        // given
        EntityName entity = EntityName.create("test");
        entityManager.persist(entity);
        entityManager.flush();

        // when
        Optional<EntityName> found = repository.findByFieldName("test");

        // then
        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("페이징 조회")
    void findAllWithPaging() {
        // given
        for (int i = 0; i < 15; i++) {
            entityManager.persist(EntityName.create("test" + i));
        }
        entityManager.flush();

        // when
        Page<EntityName> page = repository.findAll(PageRequest.of(0, 10));

        // then
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(15);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }
}
```

---

## Cucumber Step Definition Template

```java
package com.example.e2e;

import com.example.application.dto.EntityNameRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;

public class EntityNameStepDefinitions {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private Response response;

    @Before
    public void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
    }

    @Given("데이터베이스가 초기화되어 있다")
    public void databaseIsInitialized() {
        // Database cleanup logic
        // Example: repository.deleteAll();
    }

    @Given("다음 Entity가 존재한다")
    public void entityExists(DataTable dataTable) {
        // Create entities from data table
        for (Map<String, String> row : dataTable.asMaps()) {
            EntityNameRequest request = new EntityNameRequest(row.get("fieldName"));
            // repository.save(EntityName.create(request.getFieldName()));
        }
    }

    @When("Entity 생성 요청을 보낸다")
    public void sendCreateRequest(DataTable dataTable) throws Exception {
        Map<String, String> data = dataTable.asMaps().get(0);

        response = RestAssured.given()
                .contentType("application/json")
                .body(objectMapper.writeValueAsString(data))
                .when()
                .post("/entities");
    }

    @When("Entity 조회 요청을 보낸다: {long}")
    public void sendFindByIdRequest(Long id) {
        response = RestAssured.given()
                .when()
                .get("/entities/" + id);
    }

    @When("Entity 수정 요청을 보낸다: {long}")
    public void sendUpdateRequest(Long id, DataTable dataTable) throws Exception {
        Map<String, String> data = dataTable.asMaps().get(0);

        response = RestAssured.given()
                .contentType("application/json")
                .body(objectMapper.writeValueAsString(data))
                .when()
                .put("/entities/" + id);
    }

    @When("Entity 삭제 요청을 보낸다: {long}")
    public void sendDeleteRequest(Long id) {
        response = RestAssured.given()
                .when()
                .delete("/entities/" + id);
    }

    @Then("상태 코드 {int}를 받는다")
    public void verifyStatusCode(int statusCode) {
        response.then().statusCode(statusCode);
    }

    @Then("응답의 {string} 필드는 {string}이다")
    public void verifyResponseField(String field, String value) {
        response.then().body(field, equalTo(value));
    }

    @Then("응답의 {string} 필드는 {int}이다")
    public void verifyResponseFieldInt(String field, int value) {
        response.then().body(field, equalTo(value));
    }

    @Then("응답에 {string} 필드가 존재한다")
    public void verifyFieldExists(String field) {
        response.then().body(field, notNullValue());
    }

    @Then("에러 메시지는 {string}이다")
    public void verifyErrorMessage(String message) {
        response.then().body("message", containsString(message));
    }
}
```

---

## Entity Test Template

```java
package com.example.unit.domain;

import com.example.domain.entity.EntityName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("EntityName 도메인 테스트")
class EntityNameTest {

    @Nested
    @DisplayName("create 정적 팩토리 메서드는")
    class Create {

        @Test
        @DisplayName("유효한 이름으로 Entity를 생성한다")
        void create_withValidName() {
            // when
            EntityName entity = EntityName.create("test");

            // then
            assertThat(entity.getFieldName()).isEqualTo("test");
            assertThat(entity.isActive()).isTrue();
            assertThat(entity.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("빈 이름으로 생성 시 예외를 던진다")
        void create_withEmptyName_throwsException() {
            // when & then
            assertThatThrownBy(() -> EntityName.create(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null 이름으로 생성 시 예외를 던진다")
        void create_withNullName_throwsException() {
            // when & then
            assertThatThrownBy(() -> EntityName.create(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("updateFieldName 메서드는")
    class UpdateFieldName {

        @Test
        @DisplayName("이름을 수정한다")
        void updateFieldName() {
            // given
            EntityName entity = EntityName.create("old");

            // when
            entity.updateFieldName("new");

            // then
            assertThat(entity.getFieldName()).isEqualTo("new");
        }
    }

    @Nested
    @DisplayName("delete 메서드는")
    class Delete {

        @Test
        @DisplayName("소프트 삭제 처리한다")
        void delete() {
            // given
            EntityName entity = EntityName.create("test");

            // when
            entity.delete();

            // then
            assertThat(entity.isDeleted()).isTrue();
        }
    }
}
```

---

## Value Object Test Template

```java
package com.example.unit.domain;

import com.example.domain.vo.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Email Value Object 테스트")
class EmailTest {

    @Nested
    @DisplayName("from 정적 팩토리 메서드는")
    class From {

        @Test
        @DisplayName("유효한 이메일로 생성한다")
        void from_validEmail() {
            // when
            Email email = Email.from("test@example.com");

            // then
            assertThat(email.getValue()).isEqualTo("test@example.com");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "invalid",
                "@example.com",
                "test@",
                "test @example.com"
        })
        @DisplayName("잘못된 형식의 이메일로 생성 시 예외를 던진다")
        void from_invalidEmail_throwsException(String invalidEmail) {
            // when & then
            assertThatThrownBy(() -> Email.from(invalidEmail))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("동등성 비교")
    class Equality {

        @Test
        @DisplayName("같은 값의 Email은 동등하다")
        void equality_sameValue() {
            // given
            Email email1 = Email.from("test@example.com");
            Email email2 = Email.from("test@example.com");

            // then
            assertThat(email1).isEqualTo(email2);
            assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
        }

        @Test
        @DisplayName("다른 값의 Email은 동등하지 않다")
        void equality_differentValue() {
            // given
            Email email1 = Email.from("test1@example.com");
            Email email2 = Email.from("test2@example.com");

            // then
            assertThat(email1).isNotEqualTo(email2);
        }
    }
}
```

---

## Test Naming Convention

| 테스트 유형 | 명명 규칙 | 예시 |
|------------|----------|------|
| 단위 테스트 | `{method}_{scenario}` | `create_withValidRequest` |
| 통합 테스트 | `{action}` | `saveAndFindById` |
| E2E 테스트 | Gherkin Step | `sendCreateRequest` |

---

## Best Practices

1. **Given-When-Then 구조 유지**
2. **Mock은 최소한으로**
3. **테스트 간 독립성 보장**
4. **의미 있는 DisplayName 사용**
5. **ParameterizedTest로 중복 제거**
6. **AssertJ 사용으로 가독성 향상**
