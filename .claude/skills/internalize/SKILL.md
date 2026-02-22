---
name: internalize
description: This skill should be used when the user asks to "/internalize", "에피소드 복습", "설계 복습", "내재화", "Active Recall", or wants to review learning episodes through Active Recall for design capability improvement.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit, AskUserQuestion
references:
  - ../shared/context-helper.md
---

# /internalize - 설계 역량 내재화 스킬

## 목적
저장된 Episode를 복습하여 설계 역량을 내재화합니다.
**바람직한 어려움 (Desirable Difficulties)** 을 통해 실제 역량 향상을 도모합니다.

## 학습 이론
Robert Bjork의 **Desirable Difficulties** 적용:
- **Active Recall**: 문제를 먼저 보고 스스로 생각하기
- **Retrieval Practice**: 기억에서 정보를 인출하는 연습
- **Spacing Effect**: 시간 간격을 두고 복습

---

## 실행 방법

```bash
/internalize                    # 전체 Episode 목록에서 선택
/internalize --recent           # 최근 30일 Episode 복습
/internalize --topic {키워드}   # 특정 주제 Episode 복습
/internalize {episode경로}      # 특정 Episode 직접 지정
```

### 매개변수
| 매개변수 | 설명 | 예시 |
|----------|------|------|
| 없음 | 전체 Episode 목록 표시 후 선택 | `/internalize` |
| `--recent` | 최근 30일 내 Episode만 필터링 | `/internalize --recent` |
| `--topic {키워드}` | 태그/주제로 필터링 | `/internalize --topic 결제` |
| `{경로}` | 특정 Episode 직접 지정 | `/internalize 2026-02-20/payment` |

---

## 워크플로우

### Phase 1: Episode 선택

#### 1. Episode 검색
```markdown
Glob docs/learnings/episodes/**/episode.md
```

#### 2. 매개변수 필터링

**매개변수 없음** (전체 목록):
```markdown
- 검색된 모든 Episode 목록 표시
- 날짜순 정렬 (최신순)
- AskUserQuestion으로 사용자가 선택
```

**--recent** (최근 30일):
```markdown
- 오늘 날짜에서 30일 이전까지만 필터링
- 필터링된 목록 표시
- AskUserQuestion으로 사용자가 선택
```

**--topic {키워드}**:
```markdown
- Episode 파일 내용에서 태그 또는 Context 섹션 검색
- 키워드가 포함된 Episode만 필터링
- AskUserQuestion으로 사용자가 선택
```

**{경로} 지정**:
```markdown
- 직접 Episode 경로 읽기
- 존재하지 않으면 에러 메시지
```

#### 3. Episode 없음 처리
```markdown
⚠️ 복습할 Episode가 없습니다.

먼저 `/compound`를 실행하여 학습 Episode를 생성해주세요.
```

---

### Phase 2: 문제 제시 (바람직한 어려움)

선택된 Episode에서 **Context 섹션만** 추출하여 문제 형태로 제시:

```markdown
## 🎯 설계 문제

### 상황
[Episode의 Context 섹션 내용]

### 질문
이 상황에서 다음을 설계하세요:

1. **핵심 Entity와 Aggregate 경계는?**
   - 어떤 도메인 개념이 Entity가 되어야 할까요?
   - Aggregate Root는 무엇이고, 경계는 어디까지일까요?

2. **가장 중요한 Trade-off는 무엇이고, 어떤 선택을 해야 할까요?**
   - 고려해야 할 대안들은 무엇인가요?
   - 어떤 기준으로 결정해야 할까요?

---
💡 **스스로 생각한 후 아래로 스크롤하세요**

---

## 준비되셨나요?

[Enter를 누르면 정답이 공개됩니다]
```

**중요**: 이 단계에서 사용자에게 생각할 시간을 줍니다.
- `AskUserQuestion`으로 "정답을 보시겠습니까?" 확인

---

### Phase 3: 정답 리마인드

사용자가 준비되면 Episode의 **결과**를 공개:

```markdown
## 📖 설계 결과

### 설계 결정 (ADR)
[Episode의 Decisions 섹션]

### 비평 피드백 (Red Team)
[Episode의 Critique Feedback 섹션]

### 설계 결과물
[Episode의 Domain Model Result 섹션]

---

## 💡 교훈 (Lesson Learned)
[Episode의 Lessons Learned 섹션]

---

## 🔄 복기 (Self-Check)

스스로에게 질문해보세요:

1. **내가 생각한 설계와 실제 결정의 차이는?**
   - 어떤 부분이 비슷했나요?
   - 어떤 부분이 달랐나요?

2. **놓친 Trade-off가 있었나?**
   - 고려하지 못했던 대안이 있었나요?
   - 어떤 관점을 간과했나요?

3. **다음에 비슷한 문제를 만나면?**
   - 무엇을 기억해야 할까요?
```

---

## Episode 필수 구조

`/internalize` 스킬이 작동하려면 Episode에 다음 섹션이 필요합니다:

| 섹션 | 용도 | Phase |
|------|------|-------|
| `## Context (맥락)` | 문제 제시용 | Phase 2 |
| `## Decisions (결정)` | 정답 공개용 | Phase 3 |
| `## Critique Feedback (비평 피드백)` | 정답 공개용 | Phase 3 |
| `## Domain Model Result (설계 결과)` | 정답 공개용 | Phase 3 |
| `## Lessons Learned (배운 점)` | 정답 공개용 | Phase 3 |
| `## Tags` | 검색용 | Phase 1 |

---

## MUST 체크리스트 (실행 전)
- [ ] Episode 파일 존재 확인
- [ ] Episode에 필수 섹션 존재 확인

## MUST 체크리스트 (실행 후)
- [ ] 문제(Context) 제시 완료
- [ ] 사용자 확인 후 정답 공개
- [ ] Self-Check 질문 제공
- [ ] context.json 업데이트: `status`를 "completed"로 변경

### 완료 시 context.json 업데이트

```json
{
  "phase": "internalize",
  "status": "completed",
  "updated_at": "{ISO8601}"
}
```
Edit: .atdd/context.json

---

## 예시 실행

### /internalize (전체 목록)
```markdown
📋 **복습 가능한 Episode 목록**

| # | 날짜 | 주제 | 태그 |
|---|------|------|------|
| 1 | 2026-02-20 | payment-system | #결제 #동시성 |
| 2 | 2026-02-18 | user-auth | #인증 #JWT |
| 3 | 2026-02-15 | order-system | #주문 #DDD |

복습할 Episode를 선택하세요.
[AskUserQuestion: Episode 선택]
```

### /internalize --topic 결제
```markdown
🔍 **'결제' 관련 Episode**

| # | 날짜 | 주제 | 태그 |
|---|------|------|------|
| 1 | 2026-02-20 | payment-system | #결제 #동시성 |

선택된 Episode로 복습을 시작합니다...
```

---

## 참조
- Context Helper: [context-helper.md](../shared/context-helper.md)
- Compound 스킬: [../compound/SKILL.md](../compound/SKILL.md)
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
