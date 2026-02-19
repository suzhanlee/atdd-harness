# Unit Test Template (JUnit5 + Mockito)

## Service Unit Test Template

```java
package com.example.unit;

import com.example.application.dto.EntityNameRequest;
import com.example.application.dto.EntityNameResponse;
import com.example.application.service.EntityNameService;
import com.example.domain.entity.EntityName;
import com.example.domain.repository.EntityNameRepository;
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
                    .willAnswer(invocation -> invocation.getArgument(0));

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

## Entity Unit Test Template

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

    @Nested
    @DisplayName("상태 전이")
    class StateTransition {

        @Test
        @DisplayName("활성 상태에서 삭제 상태로 전이한다")
        void transitionFromActiveToDeleted() {
            // given
            EntityName entity = EntityName.create("test");
            assertThat(entity.isActive()).isTrue();

            // when
            entity.delete();

            // then
            assertThat(entity.isDeleted()).isTrue();
            assertThat(entity.isActive()).isFalse();
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

| 유형 | 명명 규칙 | 예시 |
|------|----------|------|
| Service | `{method}_{scenario}` | `create_withValidRequest` |
| Entity | `{method}_{scenario}` | `create_withValidName` |
| VO | `{method}_{scenario}` | `from_validEmail` |
| 예외 케이스 | `{method}_{scenario}_throwsException` | `create_withEmptyName_throwsException` |

---

## Best Practices

1. **Given-When-Then 구조 유지** - 테스트의 가독성을 높이는 3단계 구조
2. **Mock은 최소한으로** - 필요한 의존성만 Mocking하여 과도한 결합도 회피
3. **테스트 간 독립성 보장** - 각 테스트가 다른 테스트에 영향을 주지 않도록 격리
4. **의미 있는 DisplayName 사용** - 테스트의 의도를 명확히 표현
5. **ParameterizedTest로 중복 제거** - 유사한 테스트 케이스를 파라미터화
6. **AssertJ 사용으로 가독성 향상** - assertThat() 체이닝으로 명확한 검증
7. **BDDMockito 활용** - given/willReturn/then 패턴으로 가독성 향상
8. **@Nested로 그룹화** - 관련 테스트를 논리적으로 그룹화

---

## 관련 문서

- [통합 테스트 템플릿](integration-test-template.md) - Repository 테스트
- [E2E 테스트 템플릿](e2e-test-template.md) - Cucumber Step Definitions
