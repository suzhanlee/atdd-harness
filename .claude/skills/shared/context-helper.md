# Context Helper

## 목적
ATDD 워크플로우에서 작업명(topic)과 날짜를 관리하는 공통 유틸리티 가이드.

---

## context.json 구조

```json
{
  "topic": "payment-system",
  "date": "2026-02-19",
  "status": "in_progress",
  "phase": "design",
  "created_at": "2026-02-19T10:00:00Z",
  "updated_at": "2026-02-19T14:30:00Z"
}
```

### 필드 설명

| 필드 | 설명 | 예시 |
|------|------|------|
| `topic` | 현재 작업명 (kebab-case) | `"payment-system"`, `"user-auth"` |
| `date` | 작업 시작 날짜 | `"2026-02-19"` |
| `status` | 작업 상태 | `"in_progress"`, `"completed"` |
| `phase` | 현재 ATDD Phase | `"interview"`, `"design"`, `"tdd"` |
| `created_at` | 생성 시각 | ISO 8601 형식 |
| `updated_at` | 마지막 수정 시각 | ISO 8601 형식 |

---

## 파일 경로 규칙

### Design 산출물
```
.atdd/design/{date}/{topic}/
├── adr/
│   ├── 001-*.md
│   ├── 002-*.md
│   └── index.md
├── redteam/
│   ├── critique-001.md
│   ├── design-critique-*.md
│   ├── decisions.md
│   └── backlog.md
├── erd.md
├── domain-model.md
└── traceability-matrix.md
```

### Episode 산출물
```
docs/learnings/episodes/{date}/{topic}/episode.md
```

---

## 스킬에서의 사용법

### 1. Context 읽기

모든 스킬은 시작 시 context.json을 읽어서 작업 경로를 결정합니다.

```markdown
## 프로세스

### 1. Context 로드
```
Read .atdd/context.json
```

경로가 없으면 새 작업으로 간주하고 topic을 요청합니다.

### 2. 작업 경로 결정
- date = context.date
- topic = context.topic
- base_path = `.atdd/design/{date}/{topic}`
```

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

---

## 경로 계산 예시

### ADR 스킬
```
입력: context.json → {date: "2026-02-19", topic: "payment-system"}
출력: .atdd/design/2026-02-19/payment-system/adr/001-database.md
```

### Redteam 스킬
```
입력: .atdd/design/2026-02-19/payment-system/adr/*.md
출력: .atdd/design/2026-02-19/payment-system/redteam/critique-001.md
```

### Compound 스킬
```
입력: .atdd/design/2026-02-19/payment-system/**/*
출력: docs/learnings/episodes/2026-02-19/payment-system/episode.md
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
