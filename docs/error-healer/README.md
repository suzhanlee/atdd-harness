# Error Healer Skill

S3(Loki)에서 에러 로그를 자동으로 분석하고 Agent Teams를 활용해 병렬로 PR을 생성하는 자동 치유 시스템입니다.

## 목차

- [개요](#개요)
- [설치](#설치)
- [빠른 시작](#빠른-시작)
- [매개변수](#매개변수)
- [워크플로우](#워크플로우)
- [심각도 분류](#심각도-분류)
- [설정](#설정)
- [Agent Teams](#agent-teams)
- [예시](#예시)
- [문제 해결](#문제-해결)

## 개요

### 기능

- **자동 로그 수집**: S3(Loki)에서 에러 로그 자동 수집
- **지능형 분류**: User Error vs System Error 자동 분류
- **심각도 평가**: P0(Critical) ~ P3(Low) 심각도 평가
- **병렬 분석**: Agent Teams로 여러 에러 동시 분석
- **TDD 수정**: 테스트 먼저 작성 후 수정 코드 구현
- **자동 PR**: P0~P2 에러에 대해 자동 PR 생성

### 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                    Error Healer Skill                        │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐              │
│  │   S3     │───▶│  Classify │───▶│  Spawn   │              │
│  │  Logs    │    │  Errors   │    │  Team    │              │
│  └──────────┘    └──────────┘    └──────────┘              │
│                                         │                    │
│                    ┌────────────────────┼────────────────┐  │
│                    │                    │                │  │
│                    ▼                    ▼                ▼  │
│              ┌──────────┐        ┌──────────┐      ┌───────┐│
│              │Analyzer 1│        │Analyzer 2│ ...  │Analyz ││
│              │ (haiku)  │        │ (haiku)  │      │(haiku)││
│              └──────────┘        └──────────┘      └───────┘│
│                    │                    │                │  │
│                    └────────────────────┼────────────────┘  │
│                                         ▼                    │
│              ┌─────────────────────────────────────────────┐│
│              │              P0~P2 Only                      ││
│              └─────────────────────────────────────────────┘│
│                    ┌────────────────────┼────────────────┐  │
│                    ▼                    ▼                ▼  │
│              ┌──────────┐        ┌──────────┐      ┌───────┐│
│              │ Fixer 1  │        │ Fixer 2  │ ...  │Fixer N││
│              │ (sonnet) │        │ (sonnet) │      │(sonnet││
│              └──────────┘        └──────────┘      └───────┘│
│                    │                    │                │  │
│                    └────────────────────┼────────────────┘  │
│                                         ▼                    │
│              ┌─────────────────────────────────────────────┐│
│              │              PRs Created                    ││
│              └─────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

## 설치

### 전제 조건

1. **AWS CLI 설정**
   ```bash
   aws configure
   # 또는 환경 변수
   export AWS_ACCESS_KEY_ID=xxx
   export AWS_SECRET_ACCESS_KEY=xxx
   export AWS_REGION=ap-northeast-2
   ```

2. **GitHub Token (PR 생성용)**
   ```bash
   export GITHUB_TOKEN=ghp_xxx
   ```

3. **S3 버킷 접근 권한**
   - `s3:ListBucket`
   - `s3:GetObject`

### 설치 확인

```bash
# atdd-harness 디렉토리에서
ls .claude/skills/error-healer/SKILL.md
```

## 빠른 시작

### 1. Dry Run (분석만)

```bash
/error-healer --bucket s3://my-loki-bucket --dry-run
```

분석 결과만 확인하고 PR은 생성하지 않습니다.

### 2. 기본 실행

```bash
/error-healer --bucket s3://my-loki-bucket
```

P0~P2 에러에 대해 자동으로 PR을 생성합니다.

### 3. 특정 날짜 분석

```bash
/error-healer --bucket s3://my-loki-bucket --date 2026-02-20
```

### 4. 대량 처리

```bash
/error-healer --bucket s3://my-loki-bucket --max-errors 50 --parallel 5
```

최대 50개 에러를 5개 워커로 병렬 처리합니다.

## 매개변수

| 매개변수 | 필수 | 기본값 | 설명 |
|----------|------|--------|------|
| `--bucket` | ✅ | - | S3 버킷 경로 (예: `s3://my-logs`) |
| `--date` | ❌ | 어제 | 분석할 날짜 (YYYY-MM-DD) |
| `--prefix` | ❌ | `loki/` | 로그 경로 prefix |
| `--max-errors` | ❌ | 10 | 최대 처리 에러 수 |
| `--parallel` | ❌ | 3 | 병렬 분석 worker 수 |
| `--dry-run` | ❌ | false | PR 생성 없이 분석만 |
| `--severity` | ❌ | P2 | 처리할 최소 심각도 |

## 워크플로우

### Phase 1: 로그 수집

```
S3 Bucket
    │
    ├── loki/2026-02-22/app-001.json
    ├── loki/2026-02-22/app-002.json
    └── loki/2026-02-22/error-001.json
    │
    ▼
에러 레벨 필터링
    │
    ▼
에러 추출 (JSON Array)
```

### Phase 2: 에러 분류

```
에러 로그
    │
    ├── System Errors (P0-P2)
    │   ├── 500 에러
    │   ├── NullPointerException
    │   ├── SQLException
    │   └── ...
    │
    └── User Errors (P3)
        ├── 400 Bad Request
        ├── 401 Unauthorized
        ├── 404 Not Found
        └── ...
```

### Phase 3: Agent Teams 분석

```javascript
// 1. 분석 Task 생성
for (error of errors) {
  TaskCreate({
    subject: `Analyze ${error.id}`
  })
}

// 2. Teammate 병렬 실행
for (i = 0; i < parallelCount; i++) {
  Task({
    subagent_type: "general-purpose",
    model: "haiku",
    prompt: analyzerPrompt,
    run_in_background: true
  })
}

// 3. 결과 수집
// Inbox에서 분석 결과 확인
```

### Phase 4: PR 생성

P0~P2 에러만 처리:

```javascript
for (p0p2Error of p0p2Errors) {
  Task({
    subagent_type: "general-purpose",
    model: "sonnet",
    prompt: fixerPrompt,
    run_in_background: true
  })
}
```

각 Fixer는:
1. 브랜치 생성: `fix/error-{id}-{date}`
2. Gherkin 작성 (Red)
3. 수정 코드 구현 (Green)
4. 테스트 검증
5. 커밋 및 PR 생성

### Phase 5: 요약

```markdown
# Error Healer Report - 2026-02-22

## Summary
- 분석: 42건
- P0: 3건
- P1: 8건
- P2: 12건
- P3: 19건

## PRs Created
- #42: fix: UserService NPE (P0)
- #43: fix: OrderService timeout (P1)
...
```

## 심각도 분류

| 등급 | 기준 | 응답 시간 | 자동 PR |
|------|------|-----------|---------|
| **P0** | 서비스 중단, 보안 이슈, 데이터 손실 | 즉시 | ✅ |
| **P1** | 핵심 기능 장애, 반복 500 에러 | 4시간 | ✅ |
| **P2** | 간헐적 오류, 기능 저하 | 24시간 | ✅ |
| **P3** | 사용자 입력 오류, 4xx | 선택적 | ❌ |

### P0 판정 예시

- HTTP 500 에러 빈도 > 10회/시간
- `NullPointerException` (핵심 로직)
- DB Connection Pool 고갈
- SQL Injection, XSS 등 보안 취약점

### P1 판정 예시

- HTTP 500 에러 빈도 1-10회/시간
- `SQLException`, `IOException`
- 외부 API 타임아웃
- 데이터 무결성 위반

### P2 판정 예시

- HTTP 500 에러 빈도 < 1회/시간
- `IllegalArgumentException`
- 비즈니스 로직 오류
- 응답 시간 > 5초

### P3 판정 예시

- HTTP 4xx 에러
- 입력 검증 실패
- 인증/인가 실패 (정상 케이스)

## 설정

### 환경 변수

```bash
# 필수
export S3_BUCKET=your-loki-bucket
export AWS_REGION=ap-northeast-2

# PR 생성용
export GITHUB_TOKEN=ghp_xxx

# 선택
export ERROR_HEALER_MAX_ERRORS=10
export ERROR_HEALER_PARALLEL=3
export ERROR_HEALER_SEVERITY_THRESHOLD=P2
```

### 설정 파일 (.error-healer.json)

프로젝트 루트에 `.error-healer.json` 생성:

```json
{
  "s3": {
    "bucket": "${S3_BUCKET}",
    "prefix": "loki/",
    "region": "ap-northeast-2"
  },
  "github": {
    "repository": "${GITHUB_REPOSITORY}",
    "defaultBranch": "main"
  },
  "analysis": {
    "maxErrors": 10,
    "parallelWorkers": 3,
    "severityThreshold": "P2"
  },
  "classification": {
    "userErrors": [
      { "httpStatus": [400, 401, 403, 404] },
      { "exception": ["IllegalArgumentException"] }
    ],
    "systemErrors": [
      { "httpStatus": [500, 502, 503, 504] },
      { "exception": ["NullPointerException", "SQLException"] }
    ]
  }
}
```

## Agent Teams

### Team 역할

| Role | Description | Model | Max |
|------|-------------|-------|-----|
| team-lead | 워크플로우 오케스트레이션 | - | 1 |
| analyzer | 에러 분석 (5 Whys) | haiku | 5 |
| fixer | 수정 및 PR 생성 | sonnet | 3 |

### Teammate 통신

```
team-lead
    │
    ├── Spawn: analyzer-0, analyzer-1, analyzer-2
    │       │
    │       └── Write to Inbox: 분석 결과 JSON
    │
    ├── Read Inbox: 분석 결과 수집
    │
    ├── Spawn: fixer-0, fixer-1
    │       │
    │       └── Write to Inbox: PR URL
    │
    └── Create Summary Issue
```

## 예시

### 예시 1: NullPointerException 수정

**입력 (S3 로그)**:
```json
{
  "timestamp": "2026-02-22T10:30:45Z",
  "level": "ERROR",
  "message": "NullPointerException",
  "stackTrace": "at com.example.service.UserService.findById(UserService.java:45)",
  "context": {
    "httpMethod": "GET",
    "httpPath": "/api/users/null",
    "httpStatus": 500
  }
}
```

**분석 결과**:
```json
{
  "errorId": "ERR-001",
  "severity": "P0",
  "rootCause": "Security 설정 누락으로 미인증 요청 허용",
  "recommendation": {
    "quickFix": "null 체크 추가",
    "rootCauseFix": "Security 설정 추가"
  }
}
```

**생성된 PR**:
- Branch: `fix/error-ERR-001-20260222`
- Files Changed:
  - `UserService.java`: null 체크 추가
  - `SecurityConfig.java`: 인증 설정 추가
  - `user-error-fix.feature`: 테스트 시나리오

### 예시 2: 대량 처리

```bash
/error-healer --bucket s3://prod-logs --max-errors 50 --parallel 5
```

**결과**:
```
🚀 Error Healer 완료
   분석 에러: 50건
   생성 PR: 12개
   소요 시간: 25분
   예상 비용: $0.45
```

## 문제 해결

### S3 접근 실패

```
Error: Access Denied when listing s3://my-bucket/loki/
```

**해결**:
1. AWS 자격 증명 확인: `aws sts get-caller-identity`
2. S3 권한 확인: `aws s3 ls s3://my-bucket/`
3. 버킷 이름 확인

### GitHub PR 생성 실패

```
Error: Could not create PR - authentication required
```

**해결**:
1. `GITHUB_TOKEN` 환경 변수 확인
2. Token 권한 확인 (repo scope)
3. 저장소 쓰기 권한 확인

### 테스트 실패로 PR 차단

```
Blocked: Tests failed. Fix before completing PR task.
```

**해결**:
1. 실패한 테스트 로그 확인
2. 수정 코드 재작성
3. 수동으로 테스트 실행: `./gradlew test`

### Token 비용 과다

**해결**:
1. `--max-errors` 줄이기
2. `--parallel` 줄이기
3. 분석에 haiku 모델 사용 (기본값)

## 관련 스킬

| 스킬 | 설명 |
|------|------|
| `/monitor` | 에러 로그 모니터링만 수행 |
| `/analyze-error` | 단일 에러 심층 분석 |
| `/fix` | 단일 에러 수정 및 PR 생성 |

## 파일 구조

```
atdd-harness/
├── .claude/
│   ├── skills/
│   │   └── error-healer/
│   │       ├── SKILL.md                      # 메인 스킬
│   │       └── references/
│   │           ├── error-healer-analyzer.md  # 분석 teammate 프롬프트
│   │           └── error-healer-fixer.md     # 수정 teammate 프롬프트
│   ├── scripts/
│   │   └── error-healer-quality.sh           # 품질 검증 hook
│   └── teams/
│       └── error-healer/
│           └── config.json                   # Team 설정
├── src/main/resources/error-healer/
│   ├── config-schema.json                    # 설정 스키마
│   └── templates/
│       ├── issue.md                          # Issue 템플릿
│       ├── pr.md                             # PR 템플릿
│       └── severity.md                       # 심각도 Rubric
└── docs/error-healer/
    └── README.md                             # 이 문서
```

## 라이선스

MIT License
