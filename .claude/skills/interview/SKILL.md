---
name: interview
description: 요구사항 인터뷰를 진행한다. 바람직한 어려움(Desirable Difficulties)을 적용한 4-Phase 워크플로우로 사용자의 지시 역량을 훈련한다. 사용자가 기능 요청, 새 프로젝트, 요구사항 정리를 요청할 때 사용.
disable-model-invocation: true
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit
references:
  - references/blank-template.md
  - references/clarification-questions.md
  - references/self-review-rubric.md
---

# 요구사항 인터뷰

## 목표
사용자가 직접 요구사항을 작성하여 **지시 역량**을 향상시킨다.
AI가 질문하고 사용자가 답하는 수동적 방식이 아닌, 사용자가 주도적으로 작성하는 훈련을 제공한다.

---

## STOP PROTOCOL

### 4-Phase 진행 규칙
각 Phase는 반드시 **별도 턴**으로 진행한다. 사용자가 다음 단계로 진행할 준비가 될 때까지 대기한다.

```
Phase A (Blank Canvas)    → 사용자 입력 대기 → "완료"/"다음" → Phase B
Phase B (Clarification)   → 사용자 입력 대기 → "완료"/"다음" → Phase C
Phase C (Draft Polish)    → Phase D 즉시 진행 (대기 없음)
Phase D (Self-Review)     → 인터뷰 완료
```

### Phase A 종료 필수 문구
```
---
👆 빈 템플릿을 작성해주세요.
작성 완료 후 "완료" 또는 "다음"이라고 입력해주세요.
```

### Phase B 종료 필수 문구
```
---
👆 질문에 답변해주세요.
답변 완료 후 "완료" 또는 "다음"이라고 입력해주세요.
```

---

## 4-Phase 워크플로우

### Phase A: Blank Canvas (눰 캔버스)

**목적**: 템플릿을 보기 전에 순수하게 요구사항을 생각하도록 유도

**진행 방식**:
1. 사용자에게 빈 템플릿 제시
2. 사용자가 직접 작성

**빈 템플릿**:
```markdown
# 요구사항 초안

## 프로젝트명
[직접 작성]

## 비즈니스 목표
1. [목표를 직접 작성하세요]
2. [어떤 문제를 해결하나요?]

## 사용자 페르소나
- **주 사용자**: [누가 사용하나요?]
- **페르소나 설명**: [어떤 사람인가요?]

## 핵심 기능 (최대 5개)
1. [가장 중요한 기능]
2. [두 번째로 중요한 기능]
3. ...

## 기술적 제약
- [반드시 지켜야 할 제약]

## 일정
- [목표 일정]

## 하지 않을 것 (Won't Have)
- [이번에 제외할 기능]
```

**상세 가이드**: [blank-template.md](references/blank-template.md)

**Phase A 종료 후**:
- STOP Protocol 적용 → 사용자 입력 대기
- "완료" 또는 "다음" 입력 시 Phase B 진행

---

### Phase B: Clarification (구체화)

**목적**: 모호한 답변을 측정 가능한 조건으로 구체화

**진행 방식**:
1. 사용자가 작성한 초안 분석
2. 모호한 부분에 대해 질문
3. 사용자가 구체화

**구체화 질문 예시**:

| 모호한 표현 | 구체화 질문 |
|-------------|-------------|
| "빠른 서비스" | "응답 시간이 어느 수준이어야 하나요?" |
| "많은 사용자" | "동시 접속자 수가 몇 명인가요?" |
| "좋은 UX" | "어떤 행동이 '좋은 UX'의 기준인가요?" |

**상세 가이드**: [clarification-questions.md](references/clarification-questions.md)

**Phase B 종료 후**:
- STOP Protocol 적용 → 사용자 입력 대기
- "완료" 또는 "다음" 입력 시 Phase C 진행

---

### Phase C: Draft Polish (초안 다듬기)

**목적**: 구체화된 내용을 바탕으로 요구사항 초안 완성

**진행 방식**:
1. Phase A, B 결과를 통합
2. MoSCoW 분류 적용
3. 초안 파일 작성

**MoSCoW 분류**:

| 분류 | 설명 |
|------|------|
| **Must have** | 필수 기능, 없으면 서비스 불가 |
| **Should have** | 중요 기능, 있으면 좋음 |
| **Could have** | 선택 기능, 나중에 추가 가능 |
| **Won't have** | 이번 버전 제외 |

**Phase C 완료 후**:
- STOP Protocol 없음
- 즉시 Phase D 진행

---

### Phase D: Self-Review (자가 평가)

**목적**: 작성한 요구사항을 스스로 평가하여 품질 검증

**진행 방식**:
1. 5개 항목을 1~5점으로 평가
2. 총점 및 등급 계산
3. 평균 4점 미만 항목은 수정 필요

**Self-Review 체크리스트**:

| # | 질문 | 점수(1~5) |
|---|------|-----------|
| 1 | 완전성: 모든 Must Have 기능이 포함되었는가? | |
| 2 | 구체성: 각 요구사항이 측정 가능한가? | |
| 3 | 일관성: 요구사항 간 충돌이 없는가? | |
| 4 | 실현 가능성: 기술적/일정적으로 구현 가능한가? | |
| 5 | 명확성: 6개월 후에도 누구나 이해할 수 있는가? | |

**평가 기준**:

| 총점 | 등급 | 조치 |
|------|------|------|
| 22-25 | A | 바로 `/validate` 진행 |
| 18-21 | B | 소수 항목 보완 권장 |
| 14-17 | C | 여러 항목 보완 필요 |
| 10-13 | D | 전면 수정 권장 |
| 5-9 | F | 처음부터 다시 작성 |

**상세 가이드**: [self-review-rubric.md](references/self-review-rubric.md)

---

## 트리거
- "새 기능을 만들고 싶어요"
- "요구사항 정리해줘"
- "프로젝트 시작할래요"
- `/interview` 명령어 실행

## MUST 체크리스트 (실행 전)
- [ ] 인터뷰 시작 확인

## MUST 체크리스트 (실행 후)
- [ ] Phase A: 빈 템플릿 작성 완료
- [ ] Phase B: 구체화 질문 답변 완료
- [ ] Phase C: requirements-draft.md 생성
- [ ] Phase D: Self-Review 수행 (등급 B 이상)
- [ ] interview-log.md 생성

## 출력 파일

### requirements-draft.md
```markdown
# 요구사항 초안

## 프로젝트명
[프로젝트 이름]

## 비즈니스 목표
- [목표 1]
- [목표 2]

## 사용자 페르소나
- [페르소나 1]: [설명]

## 기능 요구사항

### Must have
- [ ] [요구사항 1]
- [ ] [요구사항 2]

### Should have
- [ ] [요구사항 3]

### Could have
- [ ] [요구사항 4]

### Won't have (이번 버전)
- [ ] [요구사항 5]

## 비기능 요구사항
- 성능: [요구사항]
- 보안: [요구사항]

## 기술적 제약
- [제약 1]
```

### interview-log.md
```markdown
# 인터뷰 로그

## 일시
[날짜 시간]

## Phase별 기록

### Phase A: Blank Canvas
[사용자 작성 내용]

### Phase B: Clarification
**질문**: [구체화 질문]
**답변**: [사용자 답변]

### Phase D: Self-Review
**총점**: [X]/25 (등급: [A/B/C/D/F])
```

## 다음 단계
Self-Review 등급 B 이상 달성 시 `/validate` 실행

## 참조
- 빈 템플릿 가이드: [blank-template.md](references/blank-template.md)
- 구체화 질문: [clarification-questions.md](references/clarification-questions.md)
- Self-Review 기준: [self-review-rubric.md](references/self-review-rubric.md)
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
