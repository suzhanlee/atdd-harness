---
name: validate
description: This skill should be used when the user asks to "/validate", "요구사항 검증", "검증해줘", or needs to validate requirements before design phase.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit
references:
  - references/predict-matrix.md
  - references/gap-analysis-guide.md
  - references/validation-criteria.md
  - references/validation-templates.md
  - references/epic-mode-guide.md
---

# 요구사항 검증

## 목표
사용자가 검증 결과를 예측하고 실제 결과와 비교하여 **비판적 사고**를 훈련한다.
단순 검증 자동화가 아닌, 사용자의 예측 능력을 향상시키는 훈련을 제공한다.

---

## STOP PROTOCOL

### ⚠️ 종료 전 필수 체크리스트

**스킬 종료 전 반드시 수행:**
- [ ] context.json의 `status`를 "completed"로 변경
- [ ] context.json의 `updated_at`을 현재 시간으로 변경
- [ ] 산출물 파일이 올바른 경로에 생성되었는지 확인

**❌ 위 체크리스트 미완료 시 스킬이 완료되지 않은 것으로 간주**

---

### 4-Phase 진행 규칠
각 Phase는 반드시 **별도 턴**으로 진행한다. 사용자가 다음 단계로 진행할 준비가 될 때까지 대기한다.

```
Phase A (Predict)         → 사용자 입력 대기 → "완료"/"다음" → Phase B
Phase B (Verify)          → 자동 진행 (AI가 실제 검증)
Phase C (Compare)         → 사용자 입력 대기 → "완료"/"다음" → Phase D
Phase D (Refinement)      → 검증 완료
```

### Phase A 종료 필수 문구
```
---
👆 Predict Matrix를 완성해주세요.
완료 후 "완료" 또는 "다음"이라고 입력해주세요.
그러면 AI가 실제 검증을 수행합니다.
```

### Phase C 종료 필수 문구
```
---
👆 Gap 분석을 완료해주세요.
분석 완료 후 "완료" 또는 "다음"이라고 입력해주세요.
Phase D (Refinement)로 진행합니다.
```

---

## 입력 우선순위

1. `{basePath}/interview/epics.md` (존재 시 Epic별 검증 모드)
2. `{basePath}/interview/requirements-draft.md` (기본 모드)
3. `{basePath}/interview/interview-log.md` (참조용)

---

## 4-Phase 워크플로우

### Phase A: Predict (예측)

**목적**: 사용자가 검증 결과를 미리 예측하여 비판적 사고 유도

**진행 방식**:
1. 검증 대상 요구사항 로드
2. 사용자에게 Predict Matrix 제시
3. 사용자가 각 항목별 결과 예측

**Predict Matrix 템플릿**:

| 항목 | 예측 결과 | 예측 근거 |
|------|-----------|-----------|
| 기술 스택 호환성 | ✅ PASS / ⚠️ WARN / ❌ FAIL | [근거] |
| 외부 의존성 | ✅ PASS / ⚠️ WARN / ❌ FAIL | [근거] |
| 일정/리소스 | ✅ PASS / ⚠️ WARN / ❌ FAIL | [근거] |
| 기능 명세 완전성 | ✅ PASS / ⚠️ WARN / ❌ FAIL | [근거] |
| 예외 케이스 포함 | ✅ PASS / ⚠️ WARN / ❌ FAIL | [근거] |
| 용어 통일 | ✅ PASS / ⚠️ WARN / ❌ FAIL | [근거] |

**상세 가이드**: [predict-matrix.md](references/predict-matrix.md)

**Phase A 종료 후**:
- STOP Protocol 적용 → 사용자 입력 대기
- "완료" 또는 "다음" 입력 시 Phase B 진행

---

### Phase B: Verify (실제 검증)

**목적**: AI가 실제 검증을 수행하고 결과 산출

**진행 방식**:
1. Phase A에서 예측한 항목들에 대해 실제 검증 수행
2. 검증 결과 문서화

**검증 항목**:

| 항목 | 설명 |
|------|------|
| Feasibility | 기술 스택 호환성, 외부 의존성, 일정/리소스 |
| Completeness | 기능 명세, 예외 케이스, 비기능 요구사항 |
| Consistency | 충돌, 모호성, 용어 통일 |
| Dependencies | 외부 API, DB, 타 시스템 연동 |

**상세 기준**: [validation-criteria.md](references/validation-criteria.md)

**Phase B 완료 후**:
- STOP Protocol 없음
- 즉시 Phase C 진행 (Gap Matrix 제시)

---

### Phase C: Compare (Gap 분석)

**목적**: 예측과 실제 결과를 비교하여 학습 효과 극대화

**진행 방식**:
1. 예측 vs 실제 결과 Gap Matrix 제시
2. 사용자가 Gap 분석 수행
3. 학습 포인트 정리

**Gap Matrix 템플릿**:

| 항목 | 내 예측 | 실제 결과 | Gap | 학습 포인트 |
|------|---------|-----------|-----|-------------|
| 기술 스택 호환성 | ✅ | ⚠️ | 🔻 | [무엇을 놓쳤는가?] |
| 외부 의존성 | ✅ | ✅ | = | - |
| 예외 케이스 | ❌ | ❌ | = | 예측 정확! |

**Gap 범례**:
- 🔻 = 예측보다 나쁨 (예측 실패)
- = = 예측 일치
- 🔺 = 예측보다 좋음 (과도한 비관)

**상세 가이드**: [gap-analysis-guide.md](references/gap-analysis-guide.md)

**Phase C 종료 후**:
- STOP Protocol 적용 → 사용자 입력 대기
- "완료" 또는 "다음" 입력 시 Phase D 진행

---

### Phase D: Refinement (개선)

**목적**: 검증 결과를 바탕으로 요구사항 개선

**진행 방식**:
1. FAIL/WARN 항목에 대한 개선 사항 정리
2. refined-requirements.md 작성
3. 검증 리포트 생성

**분기 처리**:

### PASS
```
검증 통과 ✅
모든 검증 항목을 충족했습니다.
```

### FAIL
```
검증 실패 ❌
다음 항목에 대한 조치가 필요합니다:
1. [조치 항목 1]
2. [조치 항목 2]

조치 후 /validate를 다시 실행하세요.
```

---

## 실행 모드

| 명령어 | 동작 |
|--------|------|
| `/validate` | 모든 미검증 Epic을 의존성 순서대로 검증 |
| `/validate 3` | Epic 3만 검증 (의존 Epic 완료 여부 체크) |
| `/validate --fast` | 의존성 무시하고 모든 Epic 병렬 검증 |

> Epic 모드 상세 가이드: [epic-mode-guide.md](references/epic-mode-guide.md)

---

## MUST 체크리스트 (실행 전)

- [ ] 입력 파일 존재: `{basePath}/interview/requirements-draft.md` 또는 `{basePath}/interview/epics.md`
- [ ] Phase A: Predict Matrix 제시

## MUST 체크리스트 (실행 후)

- [ ] Phase A: 사용자 예측 완료
- [ ] Phase B: 실제 검증 수행
- [ ] Phase C: Gap 분석 완료
- [ ] Phase D: `{basePath}/validate/validation-report.md` 생성
- [ ] Phase D: `{basePath}/validate/refined-requirements.md` 생성 (필요시)
- [ ] 결과 판단: PASS → 검증 완료 | FAIL → 수정 후 재실행
- [ ] context.json 업데이트: `status`를 "completed"로 변경

### 완료 시 context.json 업데이트

```json
{
  "phase": "validate",
  "status": "completed",
  "updated_at": "{ISO8601}"
}
```
Edit: .atdd/context.json

## 입력
- `{basePath}/interview/requirements-draft.md` (또는 `{basePath}/interview/epics.md`)
- `{basePath}/interview/interview-log.md` (참조용)

## 출력
- `{basePath}/validate/validation-report.md`
- `{basePath}/validate/refined-requirements.md` (필요시)

---

## 출력 파일

### validation-report.md

```markdown
# 검증 리포트

## 검증 일시
[날짜 시간]

## 검증 대상
- [파일 경로]

## Gap Matrix

| 항목 | 예측 | 실제 | Gap |
|------|------|------|-----|
| 기술 스택 호환성 | ✅ | ✅ | = |
| ... | ... | ... | ... |

## 검증 결과

### Feasibility
| 항목 | 결과 | 비고 |
|------|------|------|
| 기술 스택 호환성 | ✅ PASS | |
| 외부 의존성 | ⚠️ WARN | Redis 클러스터링 미검증 |

### Completeness
| 항목 | 결과 | 비고 |
|------|------|------|
| 기능 명세 | ✅ PASS | |
| 예외 케이스 | ❌ FAIL | 네트워크 타임아웃 누락 |

### Consistency
| 항목 | 결과 | 비고 |
|------|------|------|
| 용어 통일 | ✅ PASS | |

### Dependencies
| 항목 | 결과 | 비고 |
|------|------|------|
| 외부 API | ✅ PASS | |

## 종합 결과: ⚠️ WARN

## 개선 필요 사항
1. [개선 사항 1]
2. [개선 사항 2]
```

> 리포트 템플릿: [validation-templates.md](references/validation-templates.md)

---

## Definition of Done (DoD)

**⚠️ 스킬 완료로 인정받기 위해 다음 조건을 모두 충족해야 함:**

| # | 조건 | 검증 |
|---|------|------|
| 1 | context.json `status` = "completed" | 필수 |
| 2 | context.json `updated_at` = 현재 시간 | 필수 |
| 3 | 산출물 파일 생성 완료 (`validation-report.md`) | 필수 |
| 4 | 검증 결과 PASS (FAIL 시 수정 후 재실행) | 필수 |

**context.json 업데이트 예시:**
```json
{
  "phase": "validate",
  "status": "completed",
  "updated_at": "{ISO8601}"
}
```

---

## 참조

- Predict Matrix 가이드: [predict-matrix.md](references/predict-matrix.md)
- Gap 분석 가이드: [gap-analysis-guide.md](references/gap-analysis-guide.md)
- 검증 기준: [validation-criteria.md](references/validation-criteria.md)
- 리포트 템플릿: [validation-templates.md](references/validation-templates.md)
- Epic 모드 가이드: [epic-mode-guide.md](references/epic-mode-guide.md)
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
