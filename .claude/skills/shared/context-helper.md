# Context Helper

## 목적
ATDD 워크플로우에서 작업명(topic)과 날짜를 관리하는 공통 유틸리티 가이드.

---

## context.json 구조

```json
{
  "topic": "payment-system",
  "date": "2026-02-20",
  "status": "in_progress",
  "phase": "gherkin",
  "featurePath": "src/test/resources/features/payment-system.feature",
  "module": null,
  "basePath": ".atdd/2026-02-20/payment-system",
  "created_at": "2026-02-20T10:00:00Z",
  "updated_at": "2026-02-20T14:30:00Z"
}
```

### 필드 설명

| 필드 | 설명 | 예시 |
|------|------|------|
| `topic` | 현재 작업명 (kebab-case) | `"payment-system"`, `"user-auth"` |
| `date` | 작업 시작 날짜 | `"2026-02-20"` |
| `status` | 작업 상태 | `"in_progress"`, `"completed"` |
| `phase` | 현재 ATDD Phase | `"interview"`, `"gherkin"`, `"tdd"` |
| `featurePath` | Feature 파일 절대 경로 | `"src/test/resources/features/payment-system.feature"` |
| `module` | 멀티 모듈 시 모듈명 | `"api"`, `"core"`, `null` |
| `basePath` | 모든 산출물의 기본 경로 | `".atdd/2026-02-20/payment-system"` |
| `created_at` | 생성 시각 | ISO 8601 형식 |
| `updated_at` | 마지막 수정 시각 | ISO 8601 형식 |

---

## 파일 경로 규칙

### basePath 기반 Phase 경로

**basePath는 context.json에서 가져옵니다.** (형식: `.atdd/{date}/{topic}`)

```
# 각 Phase별 하위 경로
interview/      = {basePath}/interview/          # Phase 1
validate/       = {basePath}/validate/           # Phase 2
adr/            = {basePath}/adr/                # Phase 2.5a
redteam/        = {basePath}/redteam/            # Phase 2.5b
design/         = {basePath}/design/             # Phase 2.5
scenarios/      = {basePath}/scenarios/          # Phase 3
```

### 통일된 폴더 구조

```
.atdd/
├── context.json                    # 세션 컨텍스트 (루트 유지)
│
└── {date}/
    └── {topic}/
        ├── interview/              # Phase 1
        │   ├── requirements-draft.md
        │   ├── epics.md
        │   └── interview-log.md
        │
        ├── validate/               # Phase 2
        │   ├── validation-report.md
        │   └── refined-requirements.md
        │
        ├── adr/                    # Phase 2.5a
        │   ├── index.md
        │   └── 001-*.md
        │
        ├── redteam/                # Phase 2.5b
        │   ├── critique-*.md
        │   ├── design-critique-*.md
        │   ├── decisions.md
        │   └── backlog.md
        │
        ├── design/                 # Phase 2.5
        │   ├── erd.md
        │   ├── domain-model.md
        │   └── traceability-matrix.md
        │
        └── scenarios/              # Phase 3
            ├── draft-happy-path.md
            ├── draft-edge-cases.md
            ├── scenarios-summary.md
            └── coverage-matrix.md
```

### Episode 산출물 (프로젝트 레벨, 영구 보관)
```
docs/learnings/episodes/{date}/{topic}/episode.md
```
> Episode는 프로젝트 레벨에 영구 보관하여 다른 세션에서도 검색/참조 가능

### Feature 파일
```
# 단일 모듈
src/test/resources/features/{topic}.feature

# 멀티 모듈 (module 지정 시)
{module}/src/test/resources/features/{topic}.feature
```

---

## Feature Path 관리

### Gherkin 스킬에서의 경로 설정
1. Context 로드 후 topic 확인
2. 모듈 탐지 (settings.gradle 파싱)
3. Feature 파일 생성: `src/test/resources/features/{topic}.feature`
4. Context 업데이트: `featurePath` 필드 기록

### TDD 스킬에서의 경로 참조
1. Context 로드 후 `featurePath` 확인
2. `featurePath` 있으면 해당 경로 사용
3. 없으면 기본 경로 `src/test/resources/features/**/*.feature` 사용

---

## 스킬에서의 사용법

### 1. Context 읽기

**모든 스킬은 시작 시 context.json을 읽어서 basePath를 가져옵니다.**

```markdown
### 작업 경로 결정 절차

1. Read .atdd/context.json
2. basePath = context.basePath
3. context.json이 없으면 에러 (interview 스킬만 예외 - 새로 생성)
```

> **중요**: basePath는 항상 context.json에서 가져옵니다. 직접 계산하지 않습니다.

### 2. Context 쓰기 (interview 스킬에서만)

새 작업 시작 시 context.json을 생성합니다.

```markdown
### 1. 작업명 확인
- args에서 topic 파라미터 확인
- 없으면 AskUserQuestion으로 요청

### 2. Context 파일 생성
```json
{
  "topic": "{topic}",
  "date": "{오늘날짜}",
  "status": "in_progress",
  "phase": "interview",
  "basePath": ".atdd/{오늘날짜}/{topic}",
  "created_at": "{ISO8601}",
  "updated_at": "{ISO8601}"
}
```
Write to .atdd/context.json
```

### 3. Context 업데이트

Phase 변경 시 updated_at과 phase를 갱신합니다.

```markdown
### Phase 변경 시
```json
{
  ...기존필드,
  "phase": "design",
  "updated_at": "{현재시각}"
}
```
Edit .atdd/context.json
```

### 4. Context 완료 업데이트

각 스킬 완료 시 status를 "completed"로 변경합니다.

```json
{
  ...기존필드,
  "phase": "{현재_스킬명}",
  "status": "completed",
  "updated_at": "{현재시각}"
}
```
Edit .atdd/context.json

**중요**:
- `phase`는 스킬명만 (예: "validate", NOT "validate_completed")
- `status`는 반드시 "completed"

---

## 경로 계산 예시

모든 스킬은 먼저 `Read .atdd/context.json`으로 basePath를 가져옵니다.

### interview 스킬
```
1. Context 생성: basePath = ".atdd/2026-02-20/payment-system"
2. 출력: {basePath}/interview/requirements-draft.md
```

### validate 스킬
```
1. Read context.json → basePath 가져오기
2. 입력: {basePath}/interview/requirements-draft.md
3. 출력: {basePath}/validate/validation-report.md
```

### ADR 스킬
```
1. Read context.json → basePath 가져오기
2. 입력: {basePath}/validate/refined-requirements.md
3. 출력: {basePath}/adr/001-database.md
```

### Redteam 스킬
```
1. Read context.json → basePath 가져오기
2. 입력: {basePath}/adr/*.md
3. 출력: {basePath}/redteam/critique-001.md
```

### Design 스킬
```
1. Read context.json → basePath 가져오기
2. 입력: {basePath}/validate/refined-requirements.md
3. 출력: {basePath}/design/erd.md, {basePath}/design/domain-model.md
```

### Gherkin 스킬
```
1. Read context.json → basePath 가져오기
2. 입력: {basePath}/validate/refined-requirements.md, {basePath}/design/erd.md
3. 출력: {basePath}/scenarios/draft-happy-path.md
```

### Compound 스킬
```
1. Read context.json → basePath 가져오기
2. 입력: {basePath}/**/* (interview/, validate/, adr/, redteam/, design/, scenarios/)
3. 출력: docs/learnings/episodes/{date}/{topic}/episode.md
```

---

## 없는 경우 처리

context.json이 없으면:

1. **interview 스킬**: 새로 생성
2. **다른 스킬**: 에러 메시지 출력 후 `/interview` 실행 안내

```markdown
⚠️ 현재 작업 컨텍스트가 없습니다.
먼저 `/interview --topic {작업명}`을 실행해주세요.
```
