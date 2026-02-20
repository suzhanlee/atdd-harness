---
name: atdd
description: ATDD 파이프라인을 시작합니다. /interview → /epic-split (조건부) → /validate를 자동으로 연결합니다.
disable-model-invocation: true
user-invocable: true
allowed-tools: Read, Glob, Skill, AskUserQuestion
---

# ATDD 파이프라인

## 목표
/interview → /epic-split (조건부) → /validate를 자동으로 연결하여
요구사항 수집부터 검증까지 원클릭으로 진행합니다.

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

### 1단계: Interview

```
Skill("interview", args="{topic}")
```

- STOP Protocol로 사용자와 상호작용
- 완료 조건: `{basePath}/interview/requirements-draft.md` 존재

**진행**:
1. topic 파라미터 확인 (없으면 AskUserQuestion)
2. `Skill("interview", args=topic)` 실행
3. interview skill이 STOP Protocol로 사용자와 대화
4. requirements-draft.md 생성 시 완료

---

### 2단계: Epic 분해 (조건부)

```
# requirements-draft.md의 기능 개수 확인
기능 = count("- [") in requirements-draft.md

if 기능 > 3:
    Skill("epic-split")
else:
    스킵 → 바로 3단계
```

**기능 개수 확인 방법**:
- `{basePath}/interview/requirements-draft.md` 읽기
- `- [` 패턴 개수 세기 (체크박스 항목)

**분기**:
| 기능 개수 | 동작 |
|-----------|------|
| ≤ 3개 | 스킵, 바로 validate |
| > 3개 | epic-split 실행 |

---

### 3단계: Validate

```
Skill("validate")
```

- STOP Protocol로 사용자와 상호작용
- 완료 조건: `{basePath}/validate/validation-report.md` + 결과가 PASS

**진행**:
1. `Skill("validate")` 실행
2. validate skill이 STOP Protocol로 사용자와 대화
3. validation-report.md 생성
4. PASS 확인

---

## MUST 체크리스트 (실행 전)

- [ ] topic 파라미터 확인 또는 AskUserQuestion
- [ ] state.json에서 basePath 확인

## MUST 체크리스트 (실행 후)

- [ ] interview skill 완료
- [ ] (필요시) epic-split skill 완료
- [ ] validate skill 완료
- [ ] 완료 메시지 출력

---

## 출력

모든 skill의 출력물이 `{basePath}/`에 생성됨:

```
{basePath}/
├── interview/
│   ├── requirements-draft.md
│   └── interview-log.md
├── (epic-split 실행 시)
│   └── epics.md
│   └── epic-roadmap.md
└── validate/
    └── validation-report.md
```

---

## 완료 메시지

```
🎉 ATDD 파이프라인 완료!

📁 결과물: {basePath}

다음 단계:
- /design으로 설계 시작
- 또는 epics.md가 있으면 첫 Epic부터 진행
```

---

## 워크플로우 요약

```mermaid
graph LR
    A[/atdd topic] --> B[Skill: interview]
    B --> C{기능 > 3개?}
    C -->|Yes| D[Skill: epic-split]
    C -->|No| E[Skill: validate]
    D --> E
    E --> F[완료]
```

---

## 참조

- Interview skill: [../interview/SKILL.md](../interview/SKILL.md)
- Epic-split skill: [../epic-split/SKILL.md](../epic-split/SKILL.md)
- Validate skill: [../validate/SKILL.md](../validate/SKILL.md)
