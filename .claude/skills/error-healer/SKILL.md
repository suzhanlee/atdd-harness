---
name: error-healer
description: S3 Loki에서 에러 로그를 분석하고 Agent Teams로 병렬 PR을 생성하는 자동 치유 시스템
disable-model-invocation: false
user-invocable: true
allowed-tools: [Read, Write, Edit, Bash, Grep, Glob, Task, TaskCreate, TaskList, TaskGet, TaskUpdate, Skill]
---

# Error Healer Skill

S3(Loki)에서 에러 로그를 수집, 분석, 수정하여 자동으로 PR을 생성하는 자동 치유 시스템입니다.

## 트리거
- `/error-healer`
- "에러 힐러 실행"
- "자동 에러 수정"
- "S3 로그 분석해서 PR 만들어"

## 매개변수

```
/error-healer [options]

Options:
  --bucket <path>       S3 버킷 경로 (필수, 예: s3://my-logs)
  --date <date>         분석할 날짜 (기본: 어제, 형식: YYYY-MM-DD)
  --prefix <prefix>     로그 경로 prefix (기본: "loki/")
  --max-errors <n>      최대 처리 에러 수 (기본: 10)
  --parallel <n>        병렬 분석 worker 수 (기본: 3)
  --dry-run             PR 생성 없이 분석만 수행
  --severity <level>    처리할 최소 심각도 (기본: P2, 범위: P0-P3)
```

## 전제 조건
- AWS CLI가 설정되어 있어야 함 (`aws configure`)
- S3 버킷에 Loki 로그가 저장되어 있어야 함
- Git working directory가 clean해야 함 (PR 생성 시)
- `GITHUB_TOKEN` 환경 변수 설정 (PR 생성용)

## 워크플로우

### Phase 1: 초기화

1. **매개변수 파싱**
```
bucket = args.bucket || env.S3_BUCKET || error("bucket required")
date = args.date || yesterday()
prefix = args.prefix || "loki/"
maxErrors = args.maxErrors || 10
parallelCount = args.parallel || 3
dryRun = args.dryRun || false
severityThreshold = args.severity || "P2"
```

2. **S3 접근 검증**
```bash
aws s3 ls ${bucket}/${prefix}${date}/ --recursive | head -1
```

3. **Agent Team 생성**
```javascript
// Team 리더로서 동작, teammates를 spawn
teamId = `error-healer-${Date.now()}`
```

### Phase 2: 로그 수집

1. **S3에서 로그 다운로드**
```bash
# 에러 로그 필터링하여 다운로드
aws s3 cp s3://${bucket}/${prefix}${date}/ ./logs/ --recursive \
  --exclude "*" --include "*error*" --include "*ERROR*"
```

2. **에러 추출 및 파싱**
```bash
# JSON 로그에서 에러 추출
cat logs/*.json | jq 'select(.level == "ERROR" or .level == "FATAL")' > errors.json
```

3. **에러 분류**
- **User Errors (P3)**: 4xx, 클라이언트 입력 오류
- **System Errors (P0-P2)**: 5xx, Exception, Timeout

### Phase 3: Agent Teams 분산 분석

1. **Task 생성**
```javascript
// Task #1: 로그 수집 완료 (이미 완료)
TaskCreate({
  subject: "Collect logs from S3",
  status: "completed"
})

// Task #2: 에러 분류 완료 (이미 완료)
TaskCreate({
  subject: "Classify errors by type and severity",
  status: "completed"
})

// Task #3~N: 개별 에러 분석 (병렬 처리)
for (error of errors.slice(0, maxErrors)) {
  TaskCreate({
    subject: `Analyze ${error.id} (${error.type})`,
    description: `5 Whys analysis for ${error.id}: ${error.message}`,
    metadata: { error: error }
  })
}

// Task #N+1: 요약 Issue 생성
TaskCreate({
  subject: "Create summary issue",
  blockedBy: [all analysis tasks]
})

// Task #N+2~M: P0~P2 에러 수정 및 PR 생성
for (p0p2Error of p0p2Errors) {
  TaskCreate({
    subject: `Fix ${p0p2Error.id} and create PR`,
    blockedBy: [corresponding analysis task]
  })
}
```

2. **분석 Teammate Spawn (병렬)**
```javascript
for (i = 0; i < Math.min(parallelCount, analysisTasks.length); i++) {
  Task({
    subagent_type: "general-purpose",
    model: "haiku",  // 비용 절감
    prompt: `
당신은 에러 분석 전문가입니다.
할당된 에러에 대해 5 Whys 기법으로 분석하고 심각도를 평가하세요.

## 에러 정보
${JSON.stringify(errors[i])}

## 코드베이스
프로젝트 루트: ${projectRoot}

## 수행 작업
1. 관련 코드 파일 찾기 (Grep, Read 사용)
2. 5 Whys 분석으로 근본 원인 파악
3. 심각도 평가 (P0-P3)
4. 분석 결과를 JSON으로 출력

## 출력 형식 (JSON)
{
  "errorId": "${errors[i].id}",
  "rootCause": "근본 원인 설명",
  "severity": "P0|P1|P2|P3",
  "recommendation": "수정 방안",
  "affectedFiles": ["file1.java", "file2.java"]
}
`,
    run_in_background: true
  })
}
```

3. **분석 결과 수집**
- Teammate 완료 대기
- Inbox에서 결과 수집
- 심각도별 정렬

### Phase 4: PR 생성 (P0~P2만)

dry-run이 아니고 P0~P2 에러가 있는 경우:

1. **수정 Teammate Spawn (분석 완료 후)**
```javascript
for (p0p2Error of p0p2Errors) {
  Task({
    subagent_type: "general-purpose",
    model: "sonnet",  // 정확한 수정을 위해 sonnet 사용
    prompt: `
당신은 수정 전문가입니다.
분석된 에러에 대해 TDD 방식으로 수정하고 PR을 생성하세요.

## 에러 분석 결과
${JSON.stringify(analysisResult)}

## 수행 작업
1. 새 브랜치 생성: fix/error-${error.id}-${date}
2. Gherkin 시나리오 작성 (Red)
3. 수정 코드 구현 (Green)
4. 테스트 실행 검증
5. 커밋 및 PR 생성

## 주의사항
- 반드시 테스트를 먼저 작성하세요
- 기존 테스트가 깨지지 않도록 하세요
- 커밋 메시지는 한국어로 작성하세요
`,
    run_in_background: true
  })
}
```

2. **품질 검증 (Hook)**
- PR task 완료 전 테스트 실행
- 테스트 실패 시 차단

### Phase 5: 요약 및 종료

1. **요약 Issue 생성**
```markdown
# Error Healer Report - ${date}

## 요약
- 분석 에러: ${totalErrors}건
- P0 (Critical): ${p0Count}건
- P1 (High): ${p1Count}건
- P2 (Medium): ${p2Count}건
- P3 (Low): ${p3Count}건

## 생성된 PR
| PR | 에러 | 심각도 | 상태 |
|----|------|--------|------|
| #42 | ERR-001 | P0 | Created |
| #43 | ERR-002 | P1 | Created |

## 권장 조치
- P0: 즉시 리뷰 및 머지
- P1: 당일 중 리뷰
- P2: 이번 주 내 리뷰
```

2. **Team 정리**
```javascript
// 모든 teammate 종료 요청
for (teammate of teammates) {
  // 자연스럽게 종료될 때까지 대기
}
```

## 심각도 Rubric

| 등급 | 기준 | 응답 시간 | 자동 PR |
|------|------|-----------|---------|
| **P0 (Critical)** | 서비스 중단, 데이터 손실, 보안 이슈 | 즉시 | ✅ |
| **P1 (High)** | 핵심 기능 장애, 반복 500 에러 | 4시간 이내 | ✅ |
| **P2 (Medium)** | 간헐적 오류, 기능 저하 | 24시간 이내 | ✅ |
| **P3 (Low)** | 사용자 입력 오류, 경고 | 선택적 | ❌ |

### P0 판정 기준
- HTTP 500 에러 빈도 > 10회/시간
- NullPointerException, StackOverflowError
- DB Connection Pool 고갈
- 보안 취약점 (SQL Injection 등)

### P1 판정 기준
- HTTP 500 에러 빈도 1-10회/시간
- SQLException, IOException
- 외부 서비스 타임아웃
- 데이터 무결성 위반

### P2 판정 기준
- HTTP 500 에러 빈도 < 1회/시간
- IllegalArgumentException
- 비즈니스 로직 오류
- 성능 저하 (응답 시간 > 5초)

### P3 판정 기준
- HTTP 4xx 에러
- 클라이언트 입력 검증 실패
- 인증/인가 실패 (정상 케이스)
- 경고 로그

## 출력 형식

### 콘솔 진행 상황
```
🚀 Error Healer 시작
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚙️  설정
   Bucket: s3://my-loki-bucket
   Date: 2026-02-22
   Max Errors: 10
   Parallel: 3 workers
   Dry Run: false

📂 Phase 1: 로그 수집
✅ S3 접근 확인
✅ 로그 다운로드: 15 files
✅ 에러 추출: 42건

📊 Phase 2: 에러 분류
   System Errors: 28건 (P0~P2)
   User Errors: 14건 (P3)

🔄 Phase 3: Agent Teams 분석 (3 workers)
   [1/10] Analyzing ERR-001... ✅ P0
   [2/10] Analyzing ERR-002... ✅ P1
   [3/10] Analyzing ERR-003... ✅ P2
   ...

📋 분석 완료
   🔴 P0 (Critical): 3건
   🟠 P1 (High): 8건
   🟡 P2 (Medium): 12건
   🟢 P3 (Low): 14건 (PR 생성 안 함)

🔧 Phase 4: PR 생성 (P0~P2)
   ERR-001: Creating PR... ✅ #42
   ERR-002: Creating PR... ✅ #43
   ...

📄 Phase 5: 요약
✅ Summary Issue 생성: #50

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✨ Error Healer 완료
   생성된 PR: 5개
   소요 시간: 12분 34초
   예상 비용: $0.15
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

## 환경 변수

```bash
# 필수
S3_BUCKET=your-loki-bucket           # --bucket 옵션으로 override 가능
AWS_REGION=ap-n northeast-2

# PR 생성용
GITHUB_TOKEN=ghp_xxx

# 선택
ERROR_HEALER_MAX_ERRORS=10           # --max-errors로 override
ERROR_HEALER_PARALLEL=3              # --parallel로 override
ERROR_HEALER_SEVERITY_THRESHOLD=P2   # --severity로 override
```

## 설정 파일 (.error-healer.json)

프로젝트 루트에 `.error-healer.json` 파일로 커스터마이징:

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
      { "exception": ["IllegalArgumentException", "ConstraintViolationException"] }
    ],
    "systemErrors": [
      { "httpStatus": [500, 502, 503, 504] },
      { "exception": ["NullPointerException", "SQLException", "IOException"] }
    ]
  },
  "hooks": {
    "prePr": ".claude/hooks/error-healer-quality.sh"
  }
}
```

## 에러 처리

| 상황 | 대응 |
|------|------|
| S3 접근 실패 | 명확한 에러 메시지 + AWS 설정 가이드 |
| 로그 파싱 실패 | 스킵하고 계속 진행 |
| 분석 타임아웃 | 해당 에러 스킵 |
| PR 생성 실패 | 수동 가이드 제공 |
| 테스트 실패 | PR 생성 차단, 로그 저장 |

## 주의사항

### Token 비용 관리
- 분석 teammate는 haiku 모델 사용
- 수정 teammate는 sonnet 모델 사용
- 큰 에러 로그는 청크 단위 처리
- 동시 worker 수 제한 (기본 3개)

### 보안
- S3/GitHub credential은 환경 변수로만 전달
- 로그에 민감 정보 마스킹
- PR에 credential 노출 방지

### 품질
- 모든 수정은 TDD 기반
- 기존 테스트 회귀 방지
- PR 생성 전 테스트 실행 필수

## 예시

### 기본 실행
```bash
/error-healer --bucket s3://my-loki-bucket
```

### Dry Run (분석만)
```bash
/error-healer --bucket s3://my-loki-bucket --dry-run
```

### 특정 날짜 분석
```bash
/error-healer --bucket s3://my-loki-bucket --date 2026-02-20
```

### 대량 처리
```bash
/error-healer --bucket s3://my-loki-bucket --max-errors 50 --parallel 5
```

### P0만 처리
```bash
/error-healer --bucket s3://my-loki-bucket --severity P0
```

## 관련 스킬
- `/monitor`: 에러 로그 모니터링만
- `/analyze-error`: 단일 에러 심층 분석
- `/fix`: 단일 에러 수정 및 PR 생성
