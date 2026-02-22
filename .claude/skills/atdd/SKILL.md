---
name: atdd
description: ATDD 파이프라인을 시작합니다. /interview를 실행하고, 완료 후 Stop Hook이 자동으로 /validate를 트리거합니다.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Glob, Skill, AskUserQuestion
---

# ATDD 파이프라인

## 목표
/interview를 실행하여 요구사항 수집을 시작합니다.
interview 완료 후 Stop Hook이 자동으로 /validate를 트리거합니다.

---

## 실행 방식

### Topic 파라미터
```bash
/atdd payment-system
/atdd --topic user-auth
```

- `--topic` 또는 첫 번째 인자로 작업명 지정
- 작업명은 kebab-case 권장 (예: `payment-system`, `user-auth`)
- 지정하지 않으면 AskUserQuestion으로 요청

---

## Context Helper

시작 전, hook이 초기화한 state를 확인합니다:

```
.atdd/state.json → sessions.{session_id}.atdd
  - phase: 현재 단계
  - basePath: 작업 디렉토리
  - topic: 작업명
```

---

## 파이프라인

### 1단계: Interview (이 스킬이 실행)

```
Skill("interview", args="{topic}")
```

- **CRITICAL**: interview skill은 Phase A/B마다 AskUserQuestion으로 멈춘다
- 완료 조건: `{basePath}/interview/requirements-draft.md` 존재

**진행**:
1. topic 파라미터 확인 (없으면 AskUserQuestion)
2. `Skill("interview", args=topic)` 실행
3. interview가 Phase A/B에서 멈추면 이 스킬도 함께 멈춤
4. 사용자가 "완료"를 입력하면 interview가 다음 Phase 진행
5. requirements-draft.md 생성 시 interview 완료

---

### 2단계 이후: Stop Hook이 자동 진행

**이 스킬은 interview만 실행합니다.**

나머지 단계는 Stop Hook이 담당:

```
interview 완료 (requirements-draft.md 생성)
    ↓
Stop Hook 감지 → {"decision": "block", "reason": "Execute: Skill(\"validate\")"}
    ↓
validate 자동 실행
    ↓
validation-report.md + PASS → {"decision": "block", "reason": "Execute: Skill(\"gherkin\")"}
    ↓
gherkin 자동 실행
    ↓
*.feature 파일 생성 → {"decision": "allow"}
    ↓
세션 종료 👋
```

---

## MUST 체크리스트 (실행 전)

- [ ] topic 파라미터 확인 또는 AskUserQuestion
- [ ] state.json에서 basePath 확인

## MUST 체크리스트 (실행 후)

- [ ] interview skill 호출 완료
- [ ] interview Phase A/B에서 AskUserQuestion으로 사용자 확인 받음

## CRITICAL 체크리스트 (위반 시 실패)
- [ ] **interview Phase A에서 멈추고 사용자 입력 대기**
- [ ] **interview Phase B에서 멈추고 사용자 입력 대기**

---

## 출력

모든 출력물이 `{basePath}/`에 생성됨:

```
{basePath}/
├── interview/
│   ├── requirements-draft.md
│   └── interview-log.md
├── validate/
│   ├── validation-report.md  # Stop Hook이 실행한 validate가 생성
│   └── refined-requirements.md
└── scenarios/
    └── *.feature  # Stop Hook이 실행한 gherkin이 생성

src/test/resources/features/{topic}.feature  # Gherkin 시나리오 파일
```

---

## 워크플로우 요약

```mermaid
graph LR
    A[/atdd topic] --> B[Skill: interview]
    B --> C[Stop Hook]
    C --> D[Skill: validate]
    D --> E[Stop Hook]
    E --> F[Skill: gherkin]
    F --> G[완료]
```

---

## 참조

- Interview skill: [../interview/SKILL.md](../interview/SKILL.md)
- Stop Hook: [../../scripts/atdd-stop-hook.sh](../../scripts/atdd-stop-hook.sh)
