# ATDD Harness - Java/Spring

## 프로젝트 개요
ATDD(Acceptance Test-Driven Development) 하네스. 요구사항 인터뷰부터 최종 검증까지 자동화된 워크플로우 제공.

## 기술 스택
- Java 17+, Spring Boot 3.x, Spring Data JPA
- MySQL, Cucumber, RestAssured, JUnit5

## ATDD 워크플로우
```
/interview → /epic-split? → /validate → /design → /gherkin → /tdd → /refactor → /verify
```

## 주요 디렉토리
- `.atdd/`: ATDD 메타데이터 (요구사항, 설계, 리포트)
- `.claude/skills/`: 각 Phase별 Skill 정의
- `src/main/java/**/domain/`: DDD Domain Layer
- `src/test/resources/features/`: Gherkin Feature Files

## 상세 문서
- Agent 정의: [AGENTS.md](AGENTS.md)
- 템플릿: [TEMPLATES.md](TEMPLATES.md)
- 워크플로우: [WORKFLOWS.md](WORKFLOWS.md)

## 주요 명령어
| 명령어 | 설명 |
|--------|------|
| `/interview` | 요구사항 인터뷰 |
| `/epic-split` | 큰 요구사항 Epic 분해 (선택) |
| `/validate` | 요구사항 검증 |
| `/design` | Entity/Domain 설계 |
| `/gherkin` | Gherkin 시나리오 추출 |
| `/tdd` | TDD 코드 구현 |
| `/refactor` | Clean Code 리팩토링 |
| `/verify` | 최종 검증 |

## 테스트 실행
```bash
./gradlew test           # Unit Tests
./gradlew integrationTest # Integration Tests
./gradlew cucumber       # E2E Tests (Cucumber)
./gradlew jacocoTestReport # Coverage Report
```

## 코딩 표준
- Clean Code 원칙 준수 (Martin Fowler)
- DDD 전략적/전술적 패턴 적용
- 커버리지 80% 이상 유지
