# Skill Evaluation Checklist

skill-writing-meta-rules.md와 Anthropic 공식 가이드라인을 기반으로 한 스킬 품질 평가 체크리스트.

---

## 총점: 34점 만점

| 카테고리 | 항목 수 | 만점 |
|----------|---------|------|
| A. Frontmatter | 4 | 5 |
| B. 본문 구조 | 5 | 5 |
| C. 코드 예시 | 3 | 4 |
| D. 설득 원칙 | 4 | 5 |
| E. ATDD 패턴 | 5 | 6 |
| F. 검증 & 피드백 | 4 | 4 |

---

## A. Frontmatter (5점)

### A1. name 형식 (0-1점)

**평가 기준:**
- 문자, 숫자, 하이픈만 사용
- 64자 이내
- 동사 우선, 능동태 권장

| 점수 | 기준 |
|------|------|
| 1 | 완벽하게 준수 |
| 0 | 규칙 위반 (공백, 특수문자, 64자 초과) |

**GOOD:**
```yaml
name: skill-validator
name: creating-skills
name: condition-based-waiting
```

**BAD:**
```yaml
name: skill_validator  # underscore 사용
name: Skill Validator  # 공백, 대문자
name: this-is-a-very-long-skill-name-that-exceeds-sixty-four-characters-limit
```

---

### A2. description 시작 (0-1점)

**평가 기준:**
- "Use when..."으로 시작
- 3인칭 서술

| 점수 | 기준 |
|------|------|
| 1 | "Use when..." 시작 |
| 0 | 다른 형식 |

**GOOD:**
```yaml
description: Use when the user asks to validate SKILL.md quality...
```

**BAD:**
```yaml
description: This skill validates SKILL.md files...
description: Validates skill quality by...
```

---

### A3. description 내용 (0-2점)

**평가 기준:**
- 트리거 조건만 포함 (무엇을 하는지 ❌)
- 워크플로우 요약 금지
- 검색 가능한 키워드 포함

| 점수 | 기준 |
|------|------|
| 2 | 트리거 조건만, 키워드 포함 |
| 1 | 부분적으로 트리거 조건 |
| 0 | 워크플로우 요약 또는 모호함 |

**GOOD:**
```yaml
description: Use when the user asks to validate SKILL.md quality, "skill review", "스킬 검증", or needs to evaluate Claude Code skill files.
```

**BAD:**
```yaml
description: Use for TDD - write test first, watch it fail, write minimal code, refactor.
# 워크플로우 요약이므로 0점
```

---

### A4. user-invocable (0-1점)

**평가 기준:**
- 사용자 직접 호출 가능하면 `true` 명시

| 점수 | 기준 |
|------|------|
| 1 | user-invocable: true |
| 0 | 누락 또는 false |

---

## B. 본문 구조 (5점)

### B1. Overview 섹션 (0-1점)

**평가 기준:**
- 1-2문장으로 핵심 원칙 요약
- 스킬의 목적을 즉시 파악 가능

| 점수 | 기준 |
|------|------|
| 1 | 명확한 Overview 존재 |
| 0 | 누락 또는 모호함 |

**GOOD:**
```markdown
## 목표
Claude Code 스킬 파일의 품질을 6개 카테고리로 평가하고 수정 가이드를 제공한다.
```

---

### B2. Quick Reference (0-1점)

**평가 기준:**
- 테이블 형태 요약
- 주요 명령어/상태/기준 포함

| 점수 | 기준 |
|------|------|
| 1 | Quick Reference 테이블 존재 |
| 0 | 누락 |

---

### B3. Common Mistakes / Red Flags (0-1점)

**평가 기준:**
- 안티패턴 섹션 존재
- ❌ BAD / ✅ GOOD 대비

| 점수 | 기준 |
|------|------|
| 1 | 안티패턴 섹션 존재 |
| 0 | 누락 |

---

### B4. 파일 길이 (0-1점)

**평가 기준:**
- SKILL.md < 500줄
- 상세 내용은 references/로 분리

| 점수 | 기준 |
|------|------|
| 1 | 500줄 이내 |
| 0 | 500줄 초과 |

---

### B5. 참조 구조 (0-1점)

**평가 기준:**
- SKILL.md에서 1단계까지만 참조
- 중첩 참조 금지 (SKILL → advanced → details ❌)

| 점수 | 기준 |
|------|------|
| 1 | 1단계 참조만 |
| 0 | 중첩 참조 존재 |

---

## C. 코드 예시 (4점)

### C1. 예시 존재 여부 (0-1점)

**평가 기준:**
- 실행 가능한 코드 예시 존재

| 점수 | 기준 |
|------|------|
| 1 | 코드 예시 존재 |
| 0 | 누락 |

---

### C2. One Excellent Example (0-2점)

**평가 기준:**
- 완전하고 실행 가능한 하나의 예시
- WHY를 설명하는 주석 포함
- 실제 시나리오에서 추출

| 점수 | 기준 |
|------|------|
| 2 | 완벽한 예시 (WHY 주석 포함) |
| 1 | 예시 존재하나 품질 부족 |
| 0 | 누락 또는 불완전 |

**GOOD:**
```markdown
## Example

// WHY: PreparedStatement는 SQL Injection을 방지하기 위해 파라미터를 바인딩한다
String sql = "SELECT * FROM users WHERE id = ?";
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setString(1, userId);  // WHY: 사용자 입력을 직접 문자열 결합하지 않음
```

---

### C3. 안티패턴 대비 (0-1점)

**평가 기준:**
- ❌ BAD / ✅ GOOD 형식
- 문제점 명시

| 점수 | 기준 |
|------|------|
| 1 | 안티패턴 대비 존재 |
| 0 | 누락 |

---

## D. 설득 원칙 (5점)

### D1. Authority (0-2점)

**평가 기준:**
- "YOU MUST", "Never", "Always" 사용
- "No exceptions" 명시
- 의사결정 피로 감소

| 점수 | 기준 |
|------|------|
| 2 | 강력한 Authority 표현 |
| 1 | 부분적 사용 |
| 0 | 약한 표현만 ("Consider", "Try to") |

**GOOD:**
```markdown
Write code before test? Delete it. Start over. No exceptions.
```

**BAD:**
```markdown
Consider writing tests first when feasible.
```

---

### D2. 합리화 방지 (0-1점)

**평가 기준:**
- 허점 명시적 차단
- "Don't X", "No exceptions" 구체화

| 점수 | 기준 |
|------|------|
| 1 | 합리화 허점 차단 |
| 0 | 여지 남김 |

**GOOD:**
```markdown
**No exceptions:**
- Don't keep it as "reference"
- Don't "adapt" it while writing tests
- Delete means delete
```

---

### D3. Red Flags (0-1점)

**평가 기준:**
- "STOP and Start Over" 목록
- 명확한 위험 신호

| 점수 | 기준 |
|------|------|
| 1 | Red Flags 섹션 존재 |
| 0 | 누락 |

---

### D4. Rationalization Table (0-1점)

**평가 기준:**
- Excuse → Reality 매핑 테이블

| 점수 | 기준 |
|------|------|
| 1 | Rationalization Table 존재 |
| 0 | 누락 |

**GOOD:**
```markdown
| Excuse | Reality |
|--------|---------|
| "Too simple to test" | Simple code breaks. |
| "I'll test after" | Tests after prove nothing. |
```

---

## E. ATDD 패턴 (6점)

### E1. STOP Protocol (0-2점)

**평가 기준:**
- 종료 전 체크리스트 존재
- `- [ ]` 형태 항목
- context.json 업데이트 로직

| 점수 | 기준 |
|------|------|
| 2 | 완전한 STOP Protocol |
| 1 | 부분적 구현 |
| 0 | 누락 |

---

### E2. MUST 체크리스트 (0-1점)

**평가 기준:**
- 실행 전 필수 항목
- 실행 후 필수 항목

| 점수 | 기준 |
|------|------|
| 1 | MUST 체크리스트 존재 |
| 0 | 누락 |

---

### E3. Definition of Done (0-2점)

**평가 기준:**
- 완료 조건 명시적 정의
- 검증 가능한 체크리스트
- 조건별 검증 방법

| 점수 | 기준 |
|------|------|
| 2 | 완전한 DoD |
| 1 | 부분적 정의 |
| 0 | 누락 |

**GOOD:**
```markdown
## Definition of Done

| # | 조건 | 검증 |
|---|------|------|
| 1 | context.json status = "completed" | 필수 |
| 2 | 산출물 파일 생성 완료 | 필수 |
```

---

### E4. context.json 상태 관리 (0-1점)

**평가 기준:**
- 상태 업데이트 로직 명시
- JSON 예시 포함

| 점수 | 기준 |
|------|------|
| 1 | 상태 관리 로직 존재 |
| 0 | 누락 |

---

## F. 검증 & 피드백 (4점)

### F1. Checklist Pattern (0-1점)

**평가 기준:**
- `- [ ]` 형태 추적 항목
- Copy-and-use 가능

| 점수 | 기준 |
|------|------|
| 1 | Checklist Pattern 존재 |
| 0 | 누락 |

---

### F2. Feedback Loops (0-1점)

**평가 기준:**
- 각 단계 즉시 검증
- validator 실행 → 에러 수정 → 반복

| 점수 | 기준 |
|------|------|
| 1 | Feedback Loops 정의 |
| 0 | 누락 |

---

### F3. Conditional Workflow (0-1점)

**평가 기준:**
- 상황별 분기 안내
- "Creating new content? → Follow X"

| 점수 | 기준 |
|------|------|
| 1 | Conditional Workflow 존재 |
| 0 | 누락 |

---

### F4. Self-Check (0-1점)

**평가 기준:**
- 스킬 스스로 검증 방법
- "How to verify this skill worked"

| 점수 | 기준 |
|------|------|
| 1 | Self-Check 가이드 존재 |
| 0 | 누락 |

---

## Anti-Patterns 요약

| Anti-Pattern | Why Bad | 감점 |
|--------------|---------|------|
| Narrative storytelling | Too specific, not reusable | -2 |
| Multi-language examples | Mediocre quality | -2 |
| Code in flowcharts | Can't copy-paste | -1 |
| Generic labels (helper1) | No semantic meaning | -1 |
| Time-sensitive info | Becomes outdated | -1 |
| Windows-style paths | Breaks on Unix | -1 |
| Too many options | Confusing | -1 |
| Description에 워크플로우 요약 | Claude skips skill body | -2 |

---

## 등급 판정표

| 점수 | 등급 | 상태 | 설명 |
|------|------|------|------|
| 30-34 | A | PASS | 모든 항목 준수, 배포 가능 |
| 25-29 | B | PASS | 소수 항목 미달, 수정 권장 |
| 20-24 | C | WARN | 여러 항목 미달, 수정 필요 |
| 15-19 | D | FAIL | 주요 항목 미달, 전면 수정 |
| 0-14 | F | FAIL | 기본 구조 미비, 재작성 |
