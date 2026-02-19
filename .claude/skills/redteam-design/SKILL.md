---
name: redteam-design
description: This skill should be used when the user asks to "/redteam-design", "도메인 모델 비평", "RRAIRU", "설계 비판적 검토", or needs to critically review domain models using DDD perspectives.
disable-model-invocation: true
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit, AskUserQuestion, EnterPlanMode
references:
  - references/design-critique-perspectives.md
---

# Red Team Design Critique

## 목표
Red Team 관점에서 도메인 모델(Entity, VO, Aggregate, Domain Service)을 비판적으로 검토하여 DDD 설계 품질을 향상시킨다.

## 바람직한 어려움 (Desirable Difficulties)
이 스킬은 사용자가 설계한 도메인 모델에 대한 비판적 피드백을 제공하여:
- **Self-Explanation**: Self-Reflection 질문을 통해 "왜 이렇게 설계했나요?" 스스로 고민
- **Contrastive Cases**: 안티패턴 vs 권장 패턴 비교를 통해 좋은 설계 학습
- **Feedback Loop**: 즉각적 Critique Report로 실수 인지 및 수정 훈련
- **Retrieval Practice**: 설계 결정 이유를 설명하며 설계 지식 인출

---

## 입력
- `.atdd/design/erd.md` (ERD 문서)
- `.atdd/design/domain-model.md` (도메인 모델)
- `.atdd/requirements/refined-requirements.md` (정제된 요구사항)
- `src/main/java/**/domain/entity/*.java` (Entity 클래스)

## 트리거
- `/redteam-design` 명령어 실행
- `/design` Phase D 완료 후 자동 제안

## Red Team이란?

Red Team은 설계에 대한 "비판적" 관점을 취하여 잠재적인 문제를 사전에 발견하는 기법이다.
설계자가 놓칠 수 있는 DDD 원칙 위반, 책임 배치 오류, 불변식 누락 등을 찾아낸다.

**기존 `/redteam`과의 차이**:
- `/redteam`: ADR(설계 의사결정) 비평 → Security, Performance, Scalability 등
- `/redteam-design`: 도메인 모델 비평 → Responsibility, Aggregate, Invariants 등

---

## 6가지 검토 관점 (RRAIRU)

| 관점 | 초점 | 예시 질문 |
|------|------|-----------|
| **R**esponsibility | 책임 분배 | "User가 비밀번호를 직접 검증하는 게 맞나?" |
| **R**equirements Fit | 요구사항 적합성 | "요구사항에 없는 필드가 추가되었나?" |
| **A**ggregate Boundary | Aggregate 경계 | "Order와 OrderItem이 별도 Aggregate여야 하나?" |
| **I**nvariants | 불변식 완전성 | "부분 취소 시 총액 재계산 로직이 있는가?" |
| **R**elationships | 연관관계 설계 | "양방향 연관관계가 정말 필요한가?" |
| **U**biquitous Language | 보편 언어 일치 | "코드의 `status`가 비즈니스 용어와 일치하는가?" |

## 상세 가이드
- 6가지 관점 체크리스트: [design-critique-perspectives.md](references/design-critique-perspectives.md)

---

## Critique 프로세스

### 1. 설계 산출물 로드
```
Read .atdd/design/erd.md
Read .atdd/design/domain-model.md
Read .atdd/requirements/refined-requirements.md
Glob src/main/java/**/domain/entity/*.java → Read each file
```

### 2. 6관점 분석 (RRAIRU)
각 관점에서 도메인 모델 검토:
- 잠재적 문제 식별
- 위험도 평가 (HIGH/MEDIUM/LOW)
- 개선 제안 작성
- Self-Reflection 질문 준비

### 3. Critique Report 생성

**진행 방식**:
1. **EnterPlanMode 호출** - 출력 파일 작성 계획 수립
2. 작성할 파일 목록 정리:
   - design-critique-[날짜].md
   - decisions.md
   - backlog.md
3. 각 파일의 구조 및 내용 계획
4. 사용자 승인 후 일괄 파일 생성

```
.atdd/design/redteam/design-critique-[날짜].md
```

### 4. 사용자 Self-Reflection
각 이슈에 대해 사용자에게 질문:
- "왜 이렇게 설계하셨나요?"
- "대안을 고려해보셨나요?"

### 5. 사용자 결정 대기
각 이슈에 대해 사용자가 결정:
- **ACCEPT**: 비평 수용, 설계 수정
- **DEFER**: 나중에 처리, Backlog 추가
- **REJECT**: 거부, 사유 문서화

---

## Critique Report 구조

```markdown
# Design Critique Report

## 개요
- **검토 일시**: YYYY-MM-DD HH:mm
- **검토 대상**: domain-model.md, erd.md, Entity 클래스
- **전체 위험도**: [HIGH | MEDIUM | LOW]

---

## Self-Reflection Questions
설계를 다시 생각해볼 질문들입니다:

1. [질문 1]
2. [질문 2]
...

---

## 이슈 목록

### [RESP-1] 책임 배치 이슈 제목
- **관점**: Responsibility
- **심각도**: HIGH
- **설명**: 문제 설명
- **현재 코드**:
  ```java
  // 안티패턴 예시
  ```
- **권장 패턴**:
  ```java
  // 개선된 코드
  ```
- **Self-Reflection**: "왜 이 메서드가 이 Entity에 위치해야 하나요?"
- **제안**: 개선 방안

### [REQ-1] 요구사항 적합성 이슈
- **관점**: Requirements Fit
- **심각도**: MEDIUM
- **설명**: 문제 설명
- **영향**: 어떤 영향이 있는가
- **Self-Reflection**: "이 필드가 어떤 요구사항을 만족하나요?"
- **제안**: 개선 방안

### [AGG-1] Aggregate 경계 이슈
- **관점**: Aggregate Boundary
- **심각도**: HIGH
- **설명**: 문제 설명
- **Self-Reflection**: "이 Entity들이 항상 함께 변경되나요?"
- **제안**: 개선 방안

### [INV-1] 불변식 이슈
- **관점**: Invariants
- **심각도**: HIGH
- **설명**: 문제 설명
- **누락된 불변식**: 어떤 규칙이 빠졌나
- **Self-Reflection**: "이 상태 변경 시 항상 유효한가요?"
- **제안**: 개선 방안

### [REL-1] 연관관계 이슈
- **관점**: Relationships
- **심각도**: MEDIUM
- **설명**: 문제 설명
- **Self-Reflection**: "이 연관관계가 정말 필요한가요?"
- **제안**: 개선 방안

### [UBIQ-1] 보편 언어 이슈
- **관점**: Ubiquitous Language
- **심각도**: LOW
- **설명**: 문제 설명
- **비즈니스 용어**: 실제 사용되는 용어
- **코드 용어**: 현재 코드의 용어
- **Self-Reflection**: "개발자가 아닌 사람이 이 코드를 이해할 수 있나요?"
- **제안**: 개선 방안

---

## 요약

| 관점 | 이슈 수 | HIGH | MEDIUM | LOW |
|------|---------|------|--------|-----|
| Responsibility | 2 | 1 | 1 | 0 |
| Requirements Fit | 1 | 0 | 1 | 0 |
| Aggregate Boundary | 1 | 1 | 0 | 0 |
| Invariants | 2 | 2 | 0 | 0 |
| Relationships | 1 | 0 | 1 | 0 |
| Ubiquitous Language | 1 | 0 | 0 | 1 |
| **Total** | **8** | **4** | **3** | **1** |

---

## 권장 사항
1. [가장 중요한 권장 사항 - HIGH 이슈]
2. [다음 우선순위]
3. ...

---

## 다음 단계
이슈 검토 후 각 항목에 대해 결정해주세요.
```

---

## 사용자 결정 프로세스

Critique Report를 받은 후, 각 이슈에 대해 결정:

### ACCEPT (수용)
```
비평을 수용하고 설계 수정
→ .atdd/design/redteam/decisions.md에 ACCEPT 기록
→ domain-model.md 또는 Entity 코드 수정
→ /redteam-design 재실행으로 검증
```

### DEFER (보류)
```
나중에 처리
→ .atdd/design/redteam/backlog.md에 추가
→ 다음 단계 진행
```

### REJECT (거부)
```
비평을 거부
→ .atdd/design/redteam/decisions.md에 거부 사유 기록
→ 다음 단계 진행
```

---

## 결정 기록 예시

**decisions.md**
```markdown
# 사용자 결정 로그

## Design Critique 2024-01-15

### [RESP-1] User Entity의 비밀번호 검증 책임
- **결정**: ACCEPT
- **이유**: Password VO로 책임 이동이 더 적절함
- **조치**: Password VO 생성, 검증 로직 이동

### [AGG-1] Order와 OrderItem Aggregate 경계
- **결정**: DEFER
- **이유**: 현재 트랜잭션 경계로 충분
- **조치**: 추후 성능 이슈 시 재검토

### [UBIQ-1] status 필드명
- **결정**: REJECT
- **이유**: paymentStatus는 비즈니스 용어와 일치함
- **사유**: 결제팀에서 사용하는 용어 그대로 사용
```

---

## MUST 체크리스트 (실행 전)
- [ ] domain-model.md 존재
- [ ] erd.md 존재
- [ ] refined-requirements.md 존재

## MUST 체크리스트 (실행 후)
- [ ] 6관점 분석 완료
- [ ] Critique Report 생성
- [ ] 사용자 Self-Reflection 질문 제시
- [ ] 각 이슈에 대한 사용자 결정 기록

---

## 출력 파일

### .atdd/design/redteam/design-critique-[날짜].md
Critique Report

### .atdd/design/redteam/decisions.md
사용자 결정 로그

### .atdd/design/redteam/backlog.md
보류된 이슈 목록

---

## 다음 단계
모든 결정 완료 후 `/gherkin` 실행

---

## 참조
- 6관점 체크리스트: [design-critique-perspectives.md](references/design-critique-perspectives.md)
- DDD 패턴: [../design/references/ddd-patterns.md](../design/references/ddd-patterns.md)
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
