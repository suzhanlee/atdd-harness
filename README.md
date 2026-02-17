# ATDD Harness

**Compound Engineering Platform for Claude Code**

ATDD Harness는 단순한 ATDD(Acceptance Test-Driven Development) 도구가 아닙니다. Claude Code와 결합된 **Compound Engineering 플랫폼**으로, 전문화된 스킬들의 조합을 통해 개발 생명주기 전체를 아우르는 복합적 솔루션을 제공합니다.

## 핵심 특징

### 1. 10개 전문 스킬 파이프라인
```
/interview → /validate → /adr ↔ /redteam → /design → /gherkin → /tdd → /refactor → /verify
                         └──────────────┘
                           듀얼 검증 루프
```

### 2. ADR + Red Team 듀얼 검증
- **ADR(Architecture Decision Record)**: 설계 의사결정 문서화
- **Red Team**: 6가지 관점(Security, Performance, Scalability, Maintainability, Business, Reliability)에서 비판적 검토

### 3. Desirable Difficulties 적용
Robert Bjork의 학습 원칙을 적용하여 깊은 학습 유도:
- **Pre-Mortem**: 템플릿 보기 전 실패 상상
- **Trade-off Matrix**: 최소 3개 대안 강제 분석
- **Self-Critique**: 작성 후 자가 비평 과정

### 4. Rich Domain Model 강제
Anemic Domain Model을 방지하고 Entity에 비즈니스 로직을 포함하는 DDD 전술적 패턴 적용.

### 5. Epic 기반 병렬 처리
큰 프로젝트를 Epic 단위로 분해하고 의존성을 관리하여 병렬 구현 지원.

### 6. 운영 환경 연동
Loki 기반 로그 분석을 통한 회귀 테스트 및 품질 모니터링.

---

## ATDD 워크플로우

```
┌─────────────┐     ┌─────────────┐     ┌─────────────────────────────────────────────────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  /interview │ ──▶ │  /validate  │ ──▶ │                       /design                           │ ──▶ │   /gherkin  │ ──▶ │    /tdd     │ ──▶ │  /refactor  │ ──▶ │   /verify   │
│   Phase 1   │     │   Phase 2   │     │  ┌──────────┐    ┌───────────┐                         │     │   Phase 3   │     │   Phase 4   │     │   Phase 5   │     │   Phase 6   │
└─────────────┘     └─────────────┘     │  │   /adr   │◀──▶│  /redteam │  (반복 루프)            │     └─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
       │                   │             │  │ Phase2.5a│    │ Phase2.5b │                         │           │                   │                   │                   │
       ▼                   ▼             │  └──────────┘    └───────────┘                         │           ▼                   ▼                   ▼                   ▼
 requirements/       validation/         └─────────────────────────────────────────────────────────┘       scenarios/        Inside-Out TDD      refactoring/        reports/
   ├─ draft.md         ├─ report.md                                                                                  ├─ *.feature      1. Entity Test       ├─ log.md           ├─ verification.md
   └─ log.md           └─ refined.md                                                                                 └─ summary.md     2. Repository Test   └─ checklist.md     └─ coverage/
                                                                                                                                                  3. Service Test
                                                                                                                                                  4. E2E Test
```

### Phase 1: Interview
사용자와의 대화를 통해 요구사항을 체계적으로 수집하고 MoSCoW 기법으로 우선순위 분류.

### Phase 2: Validate
요구사항의 Feasibility, Completeness, Consistency, Dependencies를 검증.

### Phase 2.5a: ADR
Pre-Mortem → Trade-off Matrix → ADR 본문 → Self-Critique 4단계로 설계 의사결정 문서화.

### Phase 2.5b: Red Team
6가지 관점에서 ADR을 비판적으로 검토하고 ACCEPT/DEFER/REJECT 결정.

### Phase 3: Design
Rich Domain Model 기반 Entity 설계 및 DDL 생성. 요구사항-도메인 매핑 검증 포함.

### Phase 4: Gherkin
요구사항을 Gherkin 시나리오(Happy Path + Exception Path)로 변환.

### Phase 5: TDD (Inside-Out)
Entity → Repository → Service → Controller 순서로 Inside-Out TDD 사이클 수행.

### Phase 6: Refactor
Clean Code 원칙과 DDD 패턴으로 리팩토링.

### Phase 7: Verify
전체 테스트 실행, 커버리지 분석, 코드 품질 체크 후 최종 검증 리포트 생성.

---

## 기술 스택

### 백엔드
- Java 17+
- Spring Boot 3.2.0
- Spring Data JPA

### 데이터베이스
- MySQL (운영)
- H2 (테스트)

### 테스트
- Cucumber (BDD/E2E)
- RestAssured (API 테스트)
- JUnit5 (단위 테스트)
- Mockito (Mocking)
- AssertJ (Assertion)
- TestContainers (통합 테스트)

### 품질
- JaCoCo (커버리지 80% 이상)
- Spotless (코드 포맷팅)

---

## 디렉토리 구조

```
atdd-harness/
├── src/main/java/
│   └── com/example/
│       ├── domain/           # DDD Domain Layer
│       │   ├── entity/       # JPA Entities (Rich Model)
│       │   ├── vo/           # Value Objects
│       │   ├── repository/   # Repository Interfaces
│       │   └── service/      # Domain Services
│       ├── application/      # Application Layer
│       │   ├── service/      # Application Services
│       │   └── dto/          # DTOs
│       └── interfaces/       # Interface Layer
│           ├── controller/   # REST Controllers
│           └── exception/    # Exception Handlers
├── src/test/
│   ├── java/
│   │   ├── unit/             # Unit Tests
│   │   ├── integration/      # Integration Tests
│   │   └── e2e/              # E2E Tests (Cucumber)
│   └── resources/
│       └── features/         # Gherkin Feature Files
├── .atdd/
│   ├── requirements/         # 요구사항 문서
│   ├── design/               # 설계 문서
│   │   ├── adr/              # ADR 문서들
│   │   └── redteam/          # Red Team 결과
│   ├── scenarios/            # 시나리오 요약
│   ├── refactoring/          # 리팩토링 로그
│   └── reports/              # 검증 리포트
└── .claude/
    └── skills/               # Phase별 Skill 정의
        ├── interview/
        ├── validate/
        ├── adr/
        ├── redteam/
        ├── design/
        ├── gherkin/
        ├── tdd/
        ├── refactor/
        ├── verify/
        └── epic-split/
```

---

## 시작하기

### 사전 요구사항
- JDK 17+
- Gradle 8.x
- MySQL 8.0+ (운영 환경)
- Claude Code CLI

### 설치 및 실행

```bash
# 저장소 클론
git clone <repository-url>
cd atdd-harness

# 의존성 설치
./gradlew build

# 테스트 실행
./gradlew test
```

### ATDD 워크플로우 시작

```bash
# 1. 요구사항 인터뷰
/interview

# 2. 요구사항 검증
/validate

# 3-1. 아키텍처 결정 기록
/adr

# 3-2. 설계 비평
/redteam

# 4. Entity/Domain 설계
/design

# 5. Gherkin 시나리오 추출
/gherkin

# 6. TDD 코드 구현
/tdd

# 7. Clean Code 리팩토링
/refactor

# 8. 최종 검증
/verify
```

---

## 스킬 명령어

| 명령어 | 설명 | 산출물 |
|--------|------|--------|
| `/interview` | 요구사항 인터뷰 | requirements-draft.md, interview-log.md |
| `/validate` | 요구사항 검증 | validation-report.md, refined-requirements.md |
| `/adr` | 아키텍처 결정 기록 | adr/[번호]-[제목].md, index.md |
| `/redteam` | 설계 비평 | critique-[번호].md, decisions.md, backlog.md |
| `/design` | Entity/Domain 설계 | erd.md, domain-model.md, *.java |
| `/gherkin` | Gherkin 시나리오 추출 | *.feature, scenarios-summary.md |
| `/tdd` | TDD 코드 구현 | 테스트 코드, 프로덕션 코드 |
| `/refactor` | Clean Code 리팩토링 | REFACTORING-log.md, clean-code-checklist.md |
| `/verify` | 최종 검증 | VERIFICATION-report.md, coverage-report/ |
| `/epic-split` | Epic 분해 (선택) | epics.md, epic-roadmap.md |

---

## 테스트

```bash
# 단위 테스트
./gradlew test

# 통합 테스트
./gradlew integrationTest

# E2E 테스트 (Cucumber)
./gradlew cucumber

# 커버리지 리포트
./gradlew jacocoTestReport

# 전체 테스트 + 커버리지
./gradlew test integrationTest cucumber jacocoTestReport
```

---

## 코딩 표준

### Clean Code 원칙 (Martin Fowler)
- 의도를 드러내는 이름 사용
- 함수는 한 가지 일만 (20줄 이하)
- 주석 대신 코드로 의도 표현
- SOLID 원칙 준수
- DRY (Don't Repeat Yourself)

### DDD 패턴
- **Rich Domain Model**: Entity에 비즈니스 로직 포함 (Anemic Model 지양)
- **Value Object**: 불변성 보장
- **Aggregate**: 일관성 경계 설정
- **Repository**: Collection-like 인터페이스

### 품질 기준
- 커버리지 80% 이상 유지
- Lint 에러 0개
- 모든 Gherkin 시나리오 통과

---

## 상세 문서

- **[AGENTS.md](AGENTS.md)**: 각 Phase별 Agent 정의
- **[TEMPLATES.md](TEMPLATES.md)**: 코드 및 문서 템플릿
- **[WORKFLOWS.md](WORKFLOWS.md)**: 워크플로우 상세 가이드

---

## 라이선스

MIT License
