# ATDD Harness - Java/Spring

---

## 👤 About Me

**역할**: 백엔드 개발자 + DevOps (3~5년차)

**이번 캠프 과제**:
- ATDD 워크플로우 자동화 시스템 완성
- AI 기반 증강 학습 (ADR + Red Team Critique)

**핵심 가치**: 테스트와 검증을 통해 확실한 동작을 보장하는 것을 중요하게 생각합니다.

**소통 스타일**: 대화형을 선호합니다. 질문을 많이 하고 함께 고민하며 해결책을 찾아가는 방식을 좋아합니다.

**업무 스타일**:
- "돌아가는 코드"보다 "검증된 코드"를 지향
- 자동화와 인프라를 백엔드와 함께 고려하는 DevOps 마인드

---

## 프로젝트 개요
ATDD(Acceptance Test-Driven Development) 하네스. 요구사항 인터뷰부터 최종 검증까지 자동화된 워크플로우 제공.

## 사용자 컨텍스트
- **역할**: 백엔드 개발자 + DevOps
- **캠프 목표**: 운영 오류 감지 → 수정 코드 제안 → 자동 PR 생성 (자가 치유까지)
- **소통 스타일**: 질문환영형 - 모르면 계속 질문하고 함께 탐색

## 전제 조건 (이미 구성됨)
- AWS: ECS, ALB, RDS
- Monitoring: Prometheus, Loki, Promtail, Grafana

## 하네스 기술 스택
- Java 17+, Spring Boot 3.x, Spring Data JPA
- MySQL, Cucumber, RestAssured, JUnit5
- GitHub Actions (PR 자동화)

## 하네스 자동화 범위
```
운영 로그 오류 감지 → 실패 시나리오 추출 → Gherkin 생성
→ 수정 코드 작성 → 자동 PR 생성 (사람 승인)
```

## ATDD 워크플로우
```
/interview → /validate → /adr → /redteam → /design → /gherkin → /tdd → /refactor → /verify
                              ↑___________|
                               (반복 루프)
```

## 주요 디렉토리
- `.atdd/`: ATDD 메타데이터 (요구사항, 설계, 리포트)
  - `.atdd/design/adr/`: ADR 문서들
  - `.atdd/design/redteam/`: Red Team Critique 결과
- `.claude/skills/`: 각 Phase별 Skill 정의
  - `.claude/skills/adr/`: ADR 작성 Skill
  - `.claude/skills/redteam/`: Red Team Critique Skill
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
| `/validate` | 요구사항 검증 |
| `/adr` | ADR(Architecture Decision Record) 작성 |
| `/redteam` | Red Team Critique (6관점 비평) |
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
