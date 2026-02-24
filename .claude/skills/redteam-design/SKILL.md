---
name: redteam-design
description: This skill should be used when the user asks to "/redteam-design", "도메인 모델 비평", "RRAIRU", "설계 비판적 검토", or needs to critically review domain models using DDD perspectives.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit, AskUserQuestion, EnterPlanMode
references:
  - references/design-critique-perspectives.md
  - ../shared/context-helper.md
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

## STOP PROTOCOL

### ⚠️ 종료 전 필수 체크리스트

**스킬 종료 전 반드시 수행:**
- [ ] context.json의 **최상위 `status`**를 "completed"로 변경
- [ ] context.json의 `updated_at`을 현재 시간으로 변경
- [ ] 산출물 파일이 올바른 경로에 생성되었는지 확인

**❌ 위 체크리스트 미완료 시 Hook이 다음 skill을 트리거하지 않음**

### 완료 시 context.json 업데이트 예시

```json
{
  "phase": "redteam-design",
  "status": "completed",  // ⚠️ 반드시 "completed"로 변경
  "updated_at": "2026-02-24T21:30:00+09:00",
  "redteam_design": {
    "completed": true,
    ...
  }
}
```

---

## Context Helper
- [context-helper.md](../shared/context-helper.md)

---

## Context 로드

Red Team Design 시작 전, context.json을 읽어서 작업 경로를 결정합니다.

```markdown
Read .atdd/context.json
```

경로 결정:
- `basePath = context.basePath` (또는 `.atdd/{date}/{topic}`으로 계산)
- `redteamPath = {basePath}/redteam`

context.json이 없으면 에러:
```
⚠️ 현재 작업 컨텍스트가 없습니다.
먼저 `/interview`를 실행하여 새 작업을 시작해주세요.
```

---

## 입력
- `.atdd/context.json` (작업 컨텍스트)
- `{basePath}/design/erd.md` (ERD 문서)
- `{basePath}/design/domain-model.md` (도메인 모델)
- `{basePath}/design/traceability-matrix.md` (요구사항-도메인 추적 매트릭스)
- `{basePath}/validate/refined-requirements.md` (정제된 요구사항)
- `src/main/java/**/domain/entity/*.java` (Entity 클래스)

## 출력
- `{basePath}/redteam/design-critique-[날짜].md`
- `{basePath}/redteam/decisions.md`
- `{basePath}/redteam/backlog.md`

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

| 관점 | 초점 | 예시 질문 | 활용할 design 출력물 |
|------|------|-----------|---------------------|
| **R**esponsibility | 책임 분배 | "User가 비밀번호를 직접 검증하는 게 맞나?" | domain-model.md, Entity.java |
| **R**equirements Fit | 요구사항 적합성 | "요구사항에 없는 필드가 추가되었나?" | refined-requirements.md, **traceability-matrix.md** |
| **A**ggregate Boundary | Aggregate 경계 | "Order와 OrderItem이 별도 Aggregate여야 하나?" | erd.md, domain-model.md |
| **I**nvariants | 불변식 완전성 | "부분 취소 시 총액 재계산 로직이 있는가?" | Entity.java, domain-model.md |
| **R**elationships | 연관관계 설계 | "양방향 연관관계가 정말 필요한가?" | erd.md, Entity.java |
| **U**biquitous Language | 보편 언어 일치 | "코드의 `status`가 비즈니스 용어와 일치하는가?" | domain-model.md, Entity.java |

## 상세 가이드
- 6가지 관점 체크리스트: [design-critique-perspectives.md](references/design-critique-perspectives.md)

---

## Critique 프로세스

### 1. 설계 산출물 로드
```
Read {basePath}/design/erd.md
Read {basePath}/design/domain-model.md
Read {basePath}/design/traceability-matrix.md
Read {basePath}/validate/refined-requirements.md
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
{basePath}/redteam/design-critique-[날짜].md
```

### 4. Reflection Before Decision (증강 학습)
각 이슈를 ACCEPT/DEFER/REJECT하기 전에 **반영 방향을 스스로 생각**합니다:

**목적**: Conceptual Inquiry 패턴으로 65% 학습 보존

**진행 방식**:
1. Critique Report의 각 이슈 제시
2. 사용자에게 **"이 이슈를 어떻게 해결할까?"** 질문
3. 1~2문장으로 반영 방향 작성 (AI 도움 없이)
4. 그 후 ACCEPT/DEFER/REJECT 결정

**예시 질문**:
- "[INV-2] 상태 전이 규칙 미구현 → 어떻게 수정하시겠습니까?"
- "[REQ-1] Enum 불일치 → 어떤 Enum으로 변경하시겠습니까?"

**사용자 응답 예시**:
- "canTransitionTo() 메서드를 추가하고 상태 전이 테이블을 정의"
- "Status Enum을 ADR 기준인 INIT/PROCESSING/EXHAUSTED로 변경"

### 5. 사용자 결정 대기
각 이슈에 대해 사용자가 결정:
- **ACCEPT**: 비평 수용, 설계 수정
- **DEFER**: 나중에 처리, Backlog 추가
- **REJECT**: 거부, 사유 문서화

### 6. 종료 처리
모든 결정 완료 후:
1. context.json의 `status`를 "completed"로 변경
2. `updated_at` 업데이트
3. 세션 종료 → Hook이 `/compound` 자동 실행

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

## 반영 방향 작성 (Reflection Before Decision)

각 이슈를 결정하기 전에 **반영 방향을 1~2문장으로 작성**해주세요:

| 이슈 ID | 이슈 요약 | 반영 방향 (스스로 작성) | 결정 |
|---------|-----------|------------------------|------|
| [REQ-1] | Enum 불일치 | ________________________ | ☐ ACCEPT / DEFER / REJECT |
| [INV-2] | 상태 전이 규칙 | ________________________ | ☐ ACCEPT / DEFER / REJECT |
| ... | ... | ... | ... |

**작성 가이드**:
- AI 도움 없이 스스로 작성 (10~30초)
- 구체적인 수정 방향이나 메서드명 등
- "모르겠음"도 허용 → 이 경우 AI에게 개념 질문 가능

---

## 다음 단계
반영 방향 작성 후 각 항목에 대해 결정해주세요.
```

---

## 사용자 결정 프로세스

Critique Report를 받은 후, 각 이슈에 대해 **AskUserQuestion**으로 결정 수집:

### 결정 수집 방식

각 이슈에 대해 다음과 같이 AskUserQuestion 호출:

```
AskUserQuestion:
  questions:
    - question: "[REQ-1] Status Enum 불일치 - 어떻게 처리하시겠습니까?"
      header: "REQ-1 결정"
      multiSelect: false
      options:
        - label: "ACCEPT (수용)"
          description: "비평 수용, 설계 수정"
          markdown: |
            ## 반영 방향 (필수 작성)
            - 수정할 내용: _______________
            - 예상 소요 시간: _______________
        - label: "DEFER (보류)"
          description: "나중에 처리, Backlog 추가"
          markdown: |
            ## 보류 사유 (필수 작성)
            - 보류 이유: _______________
            - 재검토 시점: _______________
        - label: "REJECT (거부)"
          description: "비평 거부"
          markdown: |
            ## 거부 사유 (필수 작성)
            - 거부 이유: _______________
            - 대안 방안: _______________
```

### ACCEPT (수용)
```
비평을 수용하고 설계 수정
→ {basePath}/redteam/decisions.md에 ACCEPT + 반영 방향 기록
→ domain-model.md 또는 Entity 코드 수정
→ /redteam-design 재실행으로 검증
```

### DEFER (보류)
```
나중에 처리
→ {basePath}/redteam/decisions.md에 DEFER + 보류 사유 기록
→ {basePath}/redteam/backlog.md에 추가
→ 다음 단계 진행
```

### REJECT (거부)
```
비평을 거부
→ {basePath}/redteam/decisions.md에 REJECT + 거부 사유 + 대안 기록
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
- **반영 방향**: Password VO를 생성하고 검증 로직을 User에서 Password로 이동
- **예상 소요**: 1시간
- **결정 일시**: 2024-01-15

### [AGG-1] Order와 OrderItem Aggregate 경계
- **결정**: DEFER
- **보류 사유**: 현재 트랜잭션 경계로 충분, 성능 이슈 발생 시 재검토
- **재검토 시점**: Phase 2 개발 전
- **결정 일시**: 2024-01-15

### [UBIQ-1] status 필드명
- **결정**: REJECT
- **거부 사유**: paymentStatus는 결제팀에서 사용하는 비즈니스 용어와 일치함
- **대안 방안**: 용어집(ubiquitous-language.md)에 명시적으로 정의하여 오해 방지
- **결정 일시**: 2024-01-15
```

**Notes 매핑 규칙**:
- ACCEPT → `notes["수정할 내용"]` → 반영 방향
- ACCEPT → `notes["예상 소요 시간"]` → 예상 소요
- DEFER → `notes["보류 이유"]` → 보류 사유
- DEFER → `notes["재검토 시점"]` → 재검토 시점
- REJECT → `notes["거부 이유"]` → 거부 사유
- REJECT → `notes["대안 방안"]` → 대안 방안

---

## ⚠️ 종료 전 필수 체크리스트

**스킬 종료 전 반드시 수행:**
- [ ] context.json의 `status`를 "completed"로 변경
- [ ] context.json의 `updated_at`을 현재 시간으로 변경
- [ ] 산출물 파일이 올바른 경로에 생성되었는지 확인

**❌ 위 체크리스트 미완료 시 스킬이 완료되지 않은 것으로 간주**

---

## MUST 체크리스트 (실행 전)
- [ ] `{basePath}/design/domain-model.md` 존재
- [ ] `{basePath}/design/erd.md` 존재
- [ ] `{basePath}/validate/refined-requirements.md` 존재

## MUST 체크리스트 (실행 후)
- [ ] 6관점 분석 완료
- [ ] Critique Report 생성
- [ ] 반영 방향 테이블 제시 (Reflection Before Decision)
- [ ] 각 이슈에 대해 AskUserQuestion으로 결정 수집
- [ ] 사용자의 notes를 포함한 decisions.md 생성
- [ ] context.json 업데이트: `status`를 "completed"로 변경

### 완료 시 context.json 업데이트

```json
{
  "phase": "redteam-design",
  "status": "completed",
  "updated_at": "{ISO8601}"
}
```
Edit: .atdd/context.json

---

## 출력 파일

### {basePath}/redteam/design-critique-[날짜].md
Critique Report

### {basePath}/redteam/decisions.md
사용자 결정 로그

### {basePath}/redteam/backlog.md
보류된 이슈 목록

---

## 다음 단계
모든 결정 완료 후 `/gherkin` 실행

---

## Definition of Done (DoD)

**⚠️ 스킬 완료로 인정받기 위해 다음 조건을 모두 충족해야 함:**

| # | 조건 | 검증 |
|---|------|------|
| 1 | context.json **최상위** `status` = "completed" | 필수 |
| 2 | context.json `phase` = "redteam-design" | 필수 |
| 3 | 산출물 파일 생성 완료 (`design-critique-*.md`, `decisions.md`) | 필수 |
| 4 | 6관점 분석 완료 (RRAIRU: Responsibility, Requirements Fit, Aggregate Boundary, Invariants, Relationships, Ubiquitous Language) | 필수 |

**context.json 업데이트 예시:**
```json
{
  "phase": "redteam-design",
  "status": "completed",  // ⚠️ 최상위 레벨의 status
  "updated_at": "{ISO8601}"
}
```

---

## 참조
- 6관점 체크리스트: [design-critique-perspectives.md](references/design-critique-perspectives.md)
- DDD 패턴: [../design/references/ddd-patterns.md](../design/references/ddd-patterns.md)
- Context Helper: [context-helper.md](../shared/context-helper.md)
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
