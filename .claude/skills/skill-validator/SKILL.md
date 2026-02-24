---
name: skill-validator
description: Use when the user asks to validate SKILL.md quality, "skill review", "스킬 검증", "스킬 평가", or needs to evaluate or improve Claude Code skill files against best practices.
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit, AskUserQuestion, EnterPlanMode
references:
  - references/skill-evaluation-checklist.md
  - ../shared/skill-writing-meta-rules.md
---

# Skill Validator

## 목표
Claude Code 스킬(SKILL.md) 파일의 품질을 6개 카테고리 34점 만점으로 평가하고, 미달 항목에 대한 구체적 수정 가이드를 제공한다.

## 범위
- ✅ 스킬 품질 평가 (6개 카테고리)
- ✅ 점수 기반 등급 산출 (A~F)
- ✅ 수정 가이드 제공
- ✅ EnterPlanMode로 일괄 수정 지원
- ❌ 스킬 실행/테스트 (별도 검증 필요)

---

## STOP PROTOCOL

### 종료 전 필수 체크리스트
- [ ] Evaluation Report 생성 완료
- [ ] 총점 및 등급 산출 완료
- [ ] 미달 항목별 수정 가이드 제공
- [ ] (등급 C 이하 시) EnterPlanMode 제안

---

## Quick Reference

| 명령어 | 설명 |
|--------|------|
| `/skill-validator` | 모든 스킬 평가 |
| `/skill-validator {path}` | 특정 스킬 평가 |
| `/skill-validator --fix {path}` | 평가 후 수정 진입 |

| 등급 | 점수 | 조치 |
|------|------|------|
| A | 30-34 | 승인, 배포 가능 |
| B | 25-29 | 소수 수정 후 배포 |
| C | 20-24 | 수정 필요 (EnterPlanMode 권장) |
| D | 15-19 | 전면 수정 |
| F | 0-14 | 재작성 |

---

## 평가 카테고리 (34점 만점)

| 카테고리 | 항목 수 | 만점 | 평가 내용 |
|----------|---------|------|-----------|
| A. Frontmatter | 4 | 5 | name 형식, description 트리거 조건 |
| B. 본문 구조 | 5 | 5 | Overview, Quick Ref, Common Mistakes |
| C. 코드 예시 | 3 | 4 | One Excellent Example, WHY 주석 |
| D. 설득 원칙 | 4 | 5 | Authority, 합리화 방지, Red Flags |
| E. ATDD 패턴 | 5 | 6 | STOP Protocol, MUST 체크리스트, DoD |
| F. 검증 & 피드백 | 4 | 4 | Checklist Pattern, Feedback Loops |

---

## 워크플로우

### Phase A: 스킬 로드

1. **args에서 스킬 경로 확인**
   ```
   지정된 경로가 있으면 해당 스킬만 평가
   ```

2. **경로 미지정 시 전체 검색**
   ```
   Glob: .claude/skills/**/SKILL.md
   ```

3. **스킬 파일 + 참조 파일 읽기**
   ```
   - SKILL.md 본문
   - references/*.md (존재 시)
   ```

---

### Phase B: 체크리스트 평가

각 항목별로 0/1/2점 평가:
- **2점**: 완벽하게 준수
- **1점**: 부분적으로 준수
- **0점**: 미준수 또는 누락

상세 평가 기준: [skill-evaluation-checklist.md](references/skill-evaluation-checklist.md)

#### A. Frontmatter (5점)

| # | 항목 | 점수 | 평가 기준 |
|---|------|------|-----------|
| A1 | name 형식 | 0-1 | 문자/숫자/하이픈만, 64자 이내 |
| A2 | description 시작 | 0-1 | "Use when..."으로 시작 |
| A3 | description 내용 | 0-2 | 트리거 조건만 (워크플로우 요약 ❌) |
| A4 | user-invocable | 0-1 | true 명시 |

#### B. 본문 구조 (5점)

| # | 항목 | 점수 | 평가 기준 |
|---|------|------|-----------|
| B1 | Overview | 0-1 | 1-2문장 핵심 원칙 |
| B2 | Quick Reference | 0-1 | 테이블 형태 요약 |
| B3 | Common Mistakes | 0-1 | 안티패턴/Red Flags 섹션 |
| B4 | 길이 | 0-1 | SKILL.md < 500줄 |
| B5 | 참조 구조 | 0-1 | 1단계까지만 참조 |

#### C. 코드 예시 (4점)

| # | 항목 | 점수 | 평가 기준 |
|---|------|------|-----------|
| C1 | 존재 여부 | 0-1 | 실행 가능한 코드 예시 존재 |
| C2 | 품질 | 0-2 | One Excellent Example (WHY 주석 포함) |
| C3 | 안티패턴 | 0-1 | ❌ BAD / ✅ GOOD 대비 |

#### D. 설득 원칙 (5점)

| # | 항목 | 점수 | 평가 기준 |
|---|------|------|-----------|
| D1 | Authority | 0-2 | "YOU MUST", "No exceptions" |
| D2 | 합리화 방지 | 0-1 | 허점 명시적 차단 |
| D3 | Red Flags | 0-1 | "STOP and Start Over" 목록 |
| D4 | Rationalization Table | 0-1 | Excuse → Reality 매핑 |

#### E. ATDD 패턴 (6점)

| # | 항목 | 점수 | 평가 기준 |
|---|------|------|-----------|
| E1 | STOP Protocol | 0-2 | 종료 전 체크리스트 존재 |
| E2 | MUST 체크리스트 | 0-1 | 실행 전/후 필수 항목 |
| E3 | Definition of Done | 0-2 | 완료 조건 명시적 정의 |
| E4 | context.json | 0-1 | 상태 업데이트 로직 |

#### F. 검증 & 피드백 (4점)

| # | 항목 | 점수 | 평가 기준 |
|---|------|------|-----------|
| F1 | Checklist Pattern | 0-1 | `- [ ]` 형태 추적 항목 |
| F2 | Feedback Loops | 0-1 | 각 단계 즉시 검증 |
| F3 | Conditional Workflow | 0-1 | 상황별 분기 안내 |
| F4 | Self-Check | 0-1 | 스킬 스스로 검증 방법 |

---

### Phase C: 등급 판정 & 리포트 생성

#### 등급 체계

| 점수 | 등급 | 상태 | 조치 |
|------|------|------|------|
| 30-34 | **A** | PASS | 승인, 배포 가능 |
| 25-29 | **B** | PASS | 소수 수정 후 배포 |
| 20-24 | **C** | WARN | 수정 필요 (EnterPlanMode 권장) |
| 15-19 | **D** | FAIL | 전면 수정 |
| 0-14 | **F** | FAIL | 재작성 필요 |

#### Evaluation Report 템플릿

```markdown
# Skill Evaluation Report

## 개요
- **스킬**: {스킬명}
- **경로**: {파일 경로}
- **평가 일시**: YYYY-MM-DD HH:mm

## 점수 요약

| 카테고리 | 만점 | 획득 | 비고 |
|----------|------|------|------|
| A. Frontmatter | 5 | X | |
| B. 본문 구조 | 5 | X | |
| C. 코드 예시 | 4 | X | |
| D. 설득 원칙 | 5 | X | |
| E. ATDD 패턴 | 6 | X | |
| F. 검증 & 피드백 | 4 | X | |
| **총점** | **34** | **X** | **등급: X** |

## 상세 평가

### A. Frontmatter (X/5)
| # | 항목 | 점수 | 이유 |
|---|------|------|------|
| A1 | name 형식 | 0-1 | ... |
| A2 | description 시작 | 0-1 | ... |
| A3 | description 내용 | 0-2 | ... |
| A4 | user-invocable | 0-1 | ... |

### B. 본문 구조 (X/5)
...

## 강점
1. [가장 잘 된 부분]
2. ...

## 개선 필요
1. **[HIGH]** [가장 시급한 수정]
2. **[MEDIUM]** [다음 우선순위]
3. **[LOW]** [선택적 개선]

## 권장 조치
[등급별 권장 사항]
```

---

### Phase D: 수정 제안 (등급 C 이하 시)

#### 우선순위 분류
- **HIGH**: 핵심 기능에 영향 (Frontmatter, Authority)
- **MEDIUM**: 품질 저하 방지 (구조, 예시)
- **LOW**: 개선 권장 (일관성, 가독성)

#### EnterPlanMode 진입

등급 C 이하인 경우:
1. AskUserQuestion으로 수정 진행 여부 확인
2. "Yes" 선택 시 EnterPlanMode 호출
3. Plan Mode에서 수정 계획 수립
4. 사용자 승인 후 일괄 수정
5. 재평가 제안

---

## Common Mistakes

### ❌ Frontmatter Anti-Patterns
```yaml
# BAD: 워크플로우 요약
description: Use for TDD - write test first, watch it fail, write code

# GOOD: 트리거 조건만
description: Use when implementing any feature or bugfix, before writing implementation code
```

### ❌ 합리화 허점
```markdown
# BAD: 여지 남김
Write code before test? Delete it.

# GOOD: 완전 차단
Write code before test? Delete it. Start over.
**No exceptions:**
- Don't keep it as "reference"
- Don't "adapt" it while writing tests
```

### ❌ Narrative Storytelling
```markdown
# BAD: 이야기 형식
Yesterday I was debugging and found...

# GOOD: 패턴/가이드 형식
When encountering X error, follow these steps...
```

---

## Definition of Done

| # | 조건 | 검증 |
|---|------|------|
| 1 | Evaluation Report 생성 완료 | 필수 |
| 2 | 6개 카테고리 모두 평가 완료 | 필수 |
| 3 | 총점 및 등급 산출 완료 | 필수 |
| 4 | 미달 항목별 수정 가이드 제공 | 필수 |
| 5 | (등급 C 이하 시) EnterPlanMode 제안 | 조건부 |

---

## 참조
- 평가 체크리스트: [skill-evaluation-checklist.md](references/skill-evaluation-checklist.md)
- 메타 규칙: [skill-writing-meta-rules.md](../shared/skill-writing-meta-rules.md)
