# ATDD Harness - Agent Definitions

## Phase 1: Interview Agent

### 목표
사용자와의 대화를 통해 요구사항을 체계적으로 수집하고 정리한다.

### 입력
- 사용자의 초기 아이디어/요청

### 프로세스
1. **경청**: 사용자의 초기 아이디어를 충분히 경청
2. **탐색 질문**:
   - 비즈니스 목표가 무엇인가?
   - 주요 사용자는 누구인가?
   - 핵심 기능은 무엇인가?
   - 기술적 제약사항이 있는가?
3. **우선순위 분류**: MoSCoW 기법 적용
   - Must have: 필수 기능
   - Should have: 중요 기능
   - Could have: 있으면 좋은 기능
   - Won't have: 이번 제외 기능
4. **요구사항 초안 작성**

### 출력
- `.atdd/requirements/requirements-draft.md`
- `.atdd/requirements/interview-log.md`

### 상태 전이
완료 → `/validate` 호출 가능

---
하
## Phase 2: Validation Agent

### 목표
요구사항의 구현 가능성, 완전성, 일관성을 검증한다.

### 입력
- `.atdd/requirements/requirements-draft.md`

### 검증 항목
1. **Feasibility (구현 가능성)**
   - 기술 스택과 호환되는가?
   - 외부 의존성 해결 가능한가?
   - 일정 내 구현 가능한가?

2. **Completeness (완전성)**
   - 누락된 요구사항이 없는가?
   - 예외 케이스가 정의되었는가?
   - 비기능 요구사항이 포함되었는가?

3. **Consistency (일관성)**
   - 요구사항 간 충돌이 없는가?
   - 모호한 표현이 없는가?
   - 용어가 일관되게 사용되었는가?

4. **Dependencies (의존성)**
   - 외부 API 의존성
   - 데이터베이스 의존성
   - 타 시스템 연동

### 출력
- `.atdd/validation/validation-report.md`
- `.atdd/requirements/refined-requirements.md`

### 상태 전이
- 검증 통과 → `/design` 호출 가능
- 검증 실패 → 요구사항 수정 후 재검증

---

## Phase 2.5: Design Agent

### 목표
Entity와 DDD 도메인 모델을 설계하고 검증한다.

### Entity 설계 원칙: Rich Domain Model

**Entity에 비즈니스 로직을 포함** (Anemic Domain Model 지양)

```java
// ✅ Rich Domain Model (권장)
@Entity
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Email email;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

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

### 입력
- `.atdd/requirements/refined-requirements.md`

### 프로세스
1. **Entity 식별**
   - 명사 분석으로 주요 엔티티 도출
   - 속성 및 타입 정의

2. **관계 정의**
   - 1:1 관계
   - 1:N 관계
   - N:M 관계

3. **DDL 스크립트 생성**
   - CREATE TABLE 문 작성
   - 인덱스 정의
   - 제약조건 정의

4. **JPA Entity 클래스 생성 (Rich Domain Model)**
   - @Entity, @Table 어노테이션
   - 필드 매핑
   - 연관관계 매핑
   - 비즈니스 메서드 포함

5. **Bounded Context 정의**
   - 도메인 경계 설정
   - Aggregate Root 식별

6. **Aggregate 경계 설정**
   - 트랜잭션 경계
   - 일관성 경계

7. **[검증] 요구사항-도메인 매핑 검증**
   - Must Have 요구사항 → Entity 메서드/VO 매핑 확인
   - 검증 규칙 → @NotNull, @Size, 불변식 코드 확인

8. **SQL Sample Data 생성**
   - 비즈니스 규칙 준수 데이터

9. **[검증] SQL Sample Data 요구사항 준수 검증**
   - NOT NULL, UNIQUE, CHECK, FK 무결성 확인
   - 비즈니스 규칙 준수 확인

### 검증 합격 기준

| 검증 항목 | 기준 |
|-----------|------|
| Must Have 매핑 | 100% |
| Should Have 매핑 | 80% 이상 |
| NOT NULL 준수 | 100% |
| UNIQUE 준수 | 100% |
| FK 무결성 | 100% |
| 비즈니스 규칙 | 100% |

### 참조 파일
- [ddd-patterns.md](.claude/skills/design/ddd-patterns.md)
- [entity-template.md](.claude/skills/design/entity-template.md)
- [validation-guide.md](.claude/skills/design/validation-guide.md)

### 출력
- `.atdd/design/erd.md`
- `.atdd/design/domain-model.md`
- `.atdd/design/traceability-matrix.md`
- `.atdd/design/design-validation-report.md`
- `sql/schema/*.sql`
- `sql/data/*.sql`
- `src/main/java/**/domain/entity/*.java`

### 상태 전이
- 검증 통과 → `/gherkin` 호출 가능
- 검증 실패 → 설계 수정 후 재검증

---

## Phase 3: Gherkin Agent

### 목표
요구사항을 Gherkin 시나리오로 변환한다.

### 입력
- `.atdd/requirements/refined-requirements.md`
- `.atdd/design/erd.md`
- `src/main/java/**/domain/entity/*.java`

### 프로세스
1. **User Story → Scenario 변환**
   - As a [role] → Feature
   - I want [feature] → Scenario
   - So that [benefit] → 검증 포인트

2. **Happy Path 작성**
   - 정상 흐름 시나리오
   - 기대 결과 검증

3. **Exception Path 작성**
   - 에러 케이스
   - 경계값 테스트
   - 예외 상황

4. **Background 정의**
   - 공통 Given 절
   - 테스트 데이터 설정

5. **Data Table 설계**
   - 복잡한 입력 데이터
   - 예상 결과 데이터

### Gherkin 키워드
- Feature: 기능 단위
- Background: 공통 전제조건
- Scenario: 테스트 시나리오
- Given: 전제조건
- When: 행동
- Then: 결과
- And/But: 추가 조건

### 출력
- `src/test/resources/features/**/*.feature`
- `.atdd/scenarios/scenarios-summary.md`

### 상태 전이
완료 → `/tdd` 호출 가능

---

## Phase 4: TDD Agent (Inside-Out)

### 목표
Inside-Out TDD 사이클을 통해 코드를 구현한다.

### 입력
- `src/test/resources/features/**/*.feature`
- `src/main/java/**/domain/entity/*.java`
- `.atdd/design/design-validation-report.md`

### TDD 사이클 (Inside-Out 접근)
1. **RED**: 실패하는 테스트 작성
2. **GREEN**: 최소 코드로 테스트 통과
3. **REFACTOR**: (Phase 5에서 진행)

### 프로세스 (Inside-Out 순서)
1. **Entity 단위 테스트 작성**
   - 정적 팩토리 메서드 테스트
   - 불변식(Invariant) 테스트
   - 상태 전이 테스트

2. **Entity 구현 (Rich Domain Model)**
   - 비즈니스 로직 포함
   - 테스트 통과 확인

3. **Repository 통합 테스트 작성**
   - CRUD 테스트
   - Query 테스트

4. **Repository 구현**
   - JPA Repository 구현
   - 테스트 통과 확인

5. **Service 단위 테스트 작성**
   - 유스케이스 테스트
   - Mock Repository 사용

6. **Service 구현**
   - 비즈니스 로직은 Entity에 위임
   - 테스트 통과 확인

7. **E2E 테스트 통과 확인**
   - Step Definition 생성
   - Controller 구현
   - Cucumber 테스트 통과

### 테스트 레이어
```
src/test/java/
├── unit/
│   ├── entity/        # Entity 단위 테스트 (비즈니스 로직)
│   └── service/       # Service 단위 테스트
├── integration/
│   └── repository/    # Repository 통합 테스트
└── e2e/               # E2E 테스트 (Cucumber)
```

### 출력
- `src/test/java/**/unit/entity/**/*.java` (Entity Tests)
- `src/test/java/**/integration/**/*.java` (Integration Tests)
- `src/test/java/**/unit/service/**/*.java` (Service Tests)
- `src/test/java/**/e2e/**/*.java` (Step Definitions)
- `src/main/java/**/*.java` (Production Code)

### 상태 전이
완료 → `/refactor` 호출 가능

---

## Phase 5: Refactor Agent

### 목표
Clean Code와 DDD 패턴으로 리팩토링한다.

### 입력
- `src/main/java/**/*.java`
- `src/test/java/**/*.java`

### Clean Code 원칙
1. **Meaningful Names**
   - 의도를 드러내는 이름
   - 발음 가능한 이름
   - 검색 가능한 이름

2. **Small Functions**
   - 함수는 한 가지 일만
   - 20줄 이하 유지
   - 들여쓰기 2단계 이하

3. **No Side Effects**
   - 순수 함수 지향
   - 예측 가능한 동작

4. **SOLID Principles**
   - Single Responsibility
   - Open/Closed
   - Liskov Substitution
   - Interface Segregation
   - Dependency Inversion

5. **DRY (Don't Repeat Yourself)**
   - 중복 코드 제거
   - 추상화 레벨 일치

### DDD 패턴 검토
1. **Entity vs Value Object**
   - 식별자 필요 → Entity
   - 불변성 → Value Object

2. **Aggregate 경계**
   - 일관성 경계 확인
   - 트랜잭션 크기 적정성

3. **Repository 패턴**
   - Collection-like 인터페이스
   - 도메인 언어 사용

4. **Domain Events**
   - 이벤트 발행 필요성
   - 느슨한 결합

### 참조 파일
- [clean-code.md](.claude/skills/refactor/clean-code.md)
- [ddd-tactical.md](.claude/skills/refactor/ddd-tactical.md)

### 출력
- `.atdd/refactoring/REFACTORING-log.md`
- `.atdd/refactoring/clean-code-checklist.md`
- 리팩토링된 소스 코드

### 상태 전이
완료 → `/verify` 호출 가능

---

## Phase 6: Verify Agent

### 목표
최종 검증을 수행하고 품질을 확인한다.

### 검증 항목
1. **Unit Tests**
   ```bash
   ./gradlew test
   ```

2. **Integration Tests**
   ```bash
   ./gradlew integrationTest
   ```

3. **E2E Tests (Cucumber)**
   ```bash
   ./gradlew cucumber
   ```

4. **Coverage**
   - 80% 이상 달성
   - JaCoCo 리포트 생성

5. **Lint**
   - 0 errors
   - Spotless 적용

### 프로세스
1. 전체 테스트 실행
2. 커버리지 분석
3. 코드 품질 체크
4. Gherkin 시나리오 커버리지 확인
5. 검증 리포트 작성

### 출력
- `.atdd/reports/VERIFICATION-report.md`
- `.atdd/reports/coverage-report/`

### 완료 조건
- [ ] 모든 테스트 통과
- [ ] 커버리지 ≥ 80%
- [ ] Lint 에러 0개
- [ ] 모든 Gherkin 시나리오 통과

### 상태 전이
완료 → ATDD 사이클 종료
