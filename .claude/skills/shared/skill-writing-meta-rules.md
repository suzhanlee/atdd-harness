# 스킬 작성 메타 규칙

superpowers:writing-skills, Anthropic 공식 Skill 작성 가이드라인, 그리고 ATDD harness의 기존 스킬들(interview, tdd 등)을 분석하여 스킬을 잘 만드는 메타 규칙을 추출함.

---

## 1. 핵심 철학 (Core Philosophy)

### 1.1 Concise is Key
- 컨텍스트 윈도우는 공유 자원
- 기본 가정: "Claude는 이미 매우 똑똑하다"
- 불필요한 설명 제거, Claude가 모르는 정보만 추가

### 1.2 스킬이란?
- **Skills are:** 재사용 가능한 기법, 패턴, 도구, 참조 가이드
- **Skills are NOT:** 한 번 문제를 해결한 이야기 (Narrative)

---

## 2. 스킬 구조 (Structure)

### 2.1 Frontmatter 규칙
```yaml
---
name: skill-name-with-hyphens  # 문자, 숫자, 하이픈만 (64자 이내)
description: Use when [구체적 트리거 조건]  # 1024자 이내, 3인칭
---
```

### 2.2 Description 작성 핵심
- **"Use when..."으로 시작**
- **무엇을 하는지가 아니라 언제 사용하는지**
- 구체적 트리거, 증상, 상황 포함
- **절대 프로세스/워크플로우 요약 금지** (Claude가 description만 읽고 스킬을 스킵할 수 있음)

```yaml
# ❌ BAD: 워크플로우 요약
description: Use for TDD - write test first, watch it fail, write minimal code, refactor

# ✅ GOOD: 트리거 조건만
description: Use when implementing any feature or bugfix, before writing implementation code
```

### 2.3 본문 구조
```markdown
# Skill Name

## Overview (1-2문장 핵심 원칙)

## When to Use (선택적으로 flowchart)

## Core Pattern / Process

## Quick Reference (테이블)

## Implementation (코드 예시 또는 파일 링크)

## Common Mistakes

## 참조
```

---

## 3. 설득 원칙 (Persuasion Principles)

### 3.1 Authority (규칙 강제 스킬에 필수)
- "YOU MUST", "Never", "Always" 사용
- "No exceptions" 명시
- 의사결정 피로 감소, 합리화 차단

```markdown
✅ Write code before test? Delete it. Start over. No exceptions.
❌ Consider writing tests first when feasible.
```

### 3.2 Commitment
- 발표 요구: "Announce skill usage"
- 명시적 선택 강제
- TodoWrite로 체크리스트 추적

### 3.3 Scarcity
- 시간 제약: "Before proceeding"
- 순차적 의존성: "Immediately after X"

### 3.4 Social Proof
- 보편적 패턴: "Every time", "Always"
- 실패 모드: "X without Y = failure"

### 3.5 Unity (협업 스킬에)
- "our codebase", "we're colleagues"

### 3.6 피해야 할 것
- **Liking**: 비굴한 동의 유발
- **Reciprocity**: 조작적으로 느껴짐

---

## 4. 스킬 타입별 접근

### 4.1 Discipline-Enforcing (TDD, verification)
- Authority + Commitment + Social Proof
- 합리화 테이블, Red Flags 목록 포함

### 4.2 Technique (how-to)
- 엣지 케이스, 변형 시나리오 포함

### 4.3 Pattern (mental model)
- 인식 시나리오
- 반례 (언제 적용하지 않는지)

### 4.4 Reference (API docs)
- 검색 가능한 키워드
- 갭 테스트

---

## 5. Claude Search Optimization (CSO)

### 5.1 Rich Description
- 검색 가능한 키워드 포함
- 에러 메시지, 증상, 동의어, 도구명

### 5.2 Descriptive Naming
- 동사 우선, 능동태
- ✅ `creating-skills` > ❌ `skill-creation`
- ✅ `condition-based-waiting` > ❌ `async-test-helpers`

### 5.3 Token Efficiency
- 자주 로드되는 스킬: <200 words
- 일반 스킬: <500 words
- 상세 내용은 별도 파일로

---

## 6. 파일 조직

### 6.1 Progressive Disclosure
```
skill-name/
├── SKILL.md              # 메인 (500줄 이내)
├── references/           # 상세 참조
│   ├── guide.md
│   └── examples.md
└── scripts/              # 실행 스크립트
```

### 6.2 참조 깊이
- **SKILL.md에서 1단계까지만** 참조
- 중첩 참조 금지 (SKILL → advanced → details ❌)

### 6.3 Heavy Reference 분리 기준
- 100+ 라인: 별도 파일
- 재사용 가능한 도구: scripts/
- 원칙/컨셉: 인라인

---

## 7. 코드 예시

### 7.1 One Excellent Example
- 5개 언어 구현 ❌
- 완전하고 실행 가능한 하나의 예시
- WHY를 설명하는 주석
- 실제 시나리오에서 추출

### 7.2 Anti-Patterns
```markdown
## Red Flags - STOP and Start Over
- [나쁜 패턴 1]
- [나쁜 패턴 2]

**All of these mean: [올바른 행동]**
```

---

## 8. 워크플로우 & 피드백

### 8.1 Checklist Pattern
```markdown
Copy this checklist and track your progress:
- [ ] Step 1: ...
- [ ] Step 2: ...
```

### 8.2 Feedback Loops
- validator 실행 → 에러 수정 → 반복
- 각 단계에서 즉시 검증

### 8.3 Conditional Workflow
```markdown
**Creating new content?** → Follow "Creation workflow"
**Editing existing content?** → Follow "Editing workflow"
```

---

## 9. 합리화 방지

### 9.1 Close Every Loophole Explicitly
```markdown
# ❌ BAD
Write code before test? Delete it.

# ✅ GOOD
Write code before test? Delete it. Start over.

**No exceptions:**
- Don't keep it as "reference"
- Don't "adapt" it while writing tests
- Delete means delete
```

### 9.2 Rationalization Table
```markdown
| Excuse | Reality |
|--------|---------|
| "Too simple to test" | Simple code breaks. |
| "I'll test after" | Tests after prove nothing. |
```

### 9.3 Red Flags List
```markdown
## Red Flags - STOP and Start Over
- Code before test
- "I already manually tested it"
- "This is different because..."

**All of these mean: [올바른 행동]**
```

---

## 10. ATDD Harness 스킬 특화 패턴

### 10.1 STOP Protocol
- Phase 종료 시 사용자 확인 필수
- AskUserQuestion으로 명시적 확인

### 10.2 Context 파일 사용
```json
// .atdd/context.json
{
  "topic": "{topic}",
  "status": "in_progress",
  "phase": "interview",
  "basePath": ".atdd/{date}/{topic}"
}
```

### 10.3 MUST 체크리스트
- 실행 전/후 필수 항목 명시
- 위반 시 실패로 간주

### 10.4 Definition of Done (DoD)
- 스킬 완료 조건 명시적 정의
- 검증 가능한 체크리스트

---

## 11. Anti-Patterns 요약

| Anti-Pattern | Why Bad |
|--------------|---------|
| Narrative storytelling | Too specific, not reusable |
| Multi-language examples | Mediocre quality, maintenance burden |
| Code in flowcharts | Can't copy-paste |
| Generic labels (helper1, step2) | No semantic meaning |
| Time-sensitive info | Becomes outdated |
| Windows-style paths | Breaks on Unix |
| Too many options | Confusing |
| Description에 워크플로우 요약 | Claude skips skill body |

---

## 12. 스킬 작성 체크리스트

### Frontmatter
- [ ] name: 문자/숫자/하이픈만, 64자 이내
- [ ] description: "Use when..." 시작, 트리거 조건만, 3인칭
- [ ] description: 500자 이내 권장

### 본문
- [ ] Overview: 1-2문장 핵심 원칙
- [ ] Quick Reference 테이블
- [ ] Common Mistakes 섹션
- [ ] One excellent code example
- [ ] No narrative storytelling
- [ ] Consistent terminology

### 구조
- [ ] SKILL.md < 500줄
- [ ] 참조는 1단계까지만
- [ ] Heavy reference는 별도 파일

### 검증
- [ ] TodoWrite로 체크리스트 추적
- [ ] Definition of Done 정의
- [ ] STOP Protocol 준수 (필요 시)

---

## 결론

좋은 스킬은 **간결하고, 설득적**이어야 합니다:

1. **간결성**: Claude는 이미 똑똑하다. 모르는 것만 추가하라.
2. **설득**: Authority, Commitment, Scarcity, Social Proof를 활용하라.
3. **구조**: Progressive disclosure로 필요할 때만 로드하라.
4. **명확성**: Description은 "언제"만, 본문에서 "어떻게"를 설명하라.
