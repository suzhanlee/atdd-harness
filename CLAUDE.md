# ATDD Harness - Java/Spring

## 프로젝트 개요
ATDD(Acceptance Test-Driven Development) 하네스. 요구사항 인터뷰부터 최종 검증까지 자동화된 워크플로우 제공.

## 기술 스택
- Java 17+, Spring Boot 3.x, Spring Data JPA
- MySQL, Cucumber, RestAssured, JUnit5

## ATDD 워크플로우
```
/atdd → /interview → /epic-split? → /validate → /adr ↔ /redteam → /design → /redteam-design → /compound → /gherkin → /tdd → /refactor → /verify
   ↓         ↓                                                                                                              ↓
Phase 0   Phase 1                                                                                                     (독립 실행)
                                                                                                                       /internalize
                                    ↑___________|
                                      반복 루프
```

## 주요 디렉토리
- `.atdd/`: ATDD 메타데이터 (요구사항, 설계, 리포트)
- `.claude/skills/`: 각 Phase별 Skill 정의
- `docs/learnings/episodes/`: 학습 Episode 파일
- `src/main/java/**/domain/`: DDD Domain Layer
- `src/test/resources/features/`: Gherkin Feature Files

## 상세 문서
- Agent 정의: [AGENTS.md](AGENTS.md)
- 템플릿: [TEMPLATES.md](TEMPLATES.md)
- 워크플로우: [WORKFLOWS.md](WORKFLOWS.md)

## ATDD Core 명령어
| 명령어 | Phase | 설명 |
|--------|-------|------|
| `/atdd` | 0 | ATDD 파이프라인 진입점 |
| `/interview` | 1 | 요구사항 인터뷰 |
| `/epic-split` | 1.5 | 큰 요구사항 Epic 분해 (선택) |
| `/validate` | 2 | 요구사항 검증 |
| `/adr` | 2.5a | Architecture Decision Record 작성 |
| `/redteam` | 2.5b | ADR 비판적 검토 (6관점) |
| `/design` | 2.5 | Entity/Domain 설계 |
| `/redteam-design` | 2.6 | 도메인 모델 비판적 검토 (RRAIRU) |
| `/compound` | 2.7 | 학습 Episode 생성 |
| `/internalize` | 2.8 | Episode 복습, Active Recall |
| `/gherkin` | 3 | Gherkin 시나리오 추출 |
| `/tdd` | 4 | TDD 코드 구현 (Inside-Out) |
| `/refactor` | 5 | Clean Code 리팩토링 |
| `/verify` | 6 | 최종 검증 |

## Runtime Self-Healing 명령어
| 명령어 | 설명 |
|--------|------|
| `/monitor` | S3 Loki 에러 로그 모니터링 |
| `/analyze-error` | 5 Whys 근본 원인 분석 |
| `/fix` | Self-Healing (Gherkin → Test → Fix → PR) |

## Utility 명령어
| 명령어 | 설명 |
|--------|------|
| `/commit` | 한국어 Atomic Commits |

## 스킬 간 관계 및 증강학습

### Red Team 계열 스킬 분담
- **`/redteam`** (Phase 2.5b): ADR(설계 의사결정) 비평
  - 관점: Security, Performance, Scalability, Maintainability, Business, Reliability
- **`/redteam-design`** (Phase 2.6): 도메인 모델(Entity, VO, Aggregate) 비평
  - 관점: Responsibility, Requirements Fit, Aggregate Boundary, Invariants, Relationships, Ubiquitous Language (RRAIRU)

### 바람직한 어려움 (Desirable Difficulties) 적용
Robert Bjork의 학습 원칙을 적용하여 설계 역량 강화:
- **Pre-Mortem**: 템플릿 보기 전 실패 상상 (ADR)
- **Trade-off Matrix**: 최소 3개 대안 강제 분석 (ADR)
- **Self-Explanation**: "왜 이렇게 설계했나요?" 질문 (redteam-design)
- **Contrastive Cases**: 안티패턴 vs 권장 패턴 비교 (redteam-design)
- **Feedback Loop**: 즉각적 Critique Report (redteam, redteam-design)

## Compound Learning System
ATDD 워크플로우에서 배운 것을 Episode로 저장하여 컴파운드 효과를 제공한다.

### 파일 구조
```
.atdd/design/{date}/{topic}/     # 설계 산출물
docs/learnings/episodes/{date}/{topic}/episode.md  # 학습 Episode
```

### 실행 흐름
```
/atdd --topic {작업명} → /interview → (Stop Hook) → /validate → /adr → /redteam → /design → /redteam-design → /compound
```

### Episode 구성
- Context: 문제 상황, 도메인 맥락
- Decisions: ADR에서 추출한 설계 결정
- Critique Feedback: Red Team 비평 결과
- Domain Model Result: 최종 설계 산출물
- Lessons Learned: 배운 점
- Tags: 검색용 태그

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
