# Monitor Skill

운영 환경의 에러 로그를 S3(Loki 저장소)에서 조회하고 분석합니다.

## 트리거
- `/monitor`
- "운영 로그 확인해줘"
- "에러 로그 분석해줘"
- "최근 에러 있어?"

## 전제 조건
- AWS CLI가 설정되어 있어야 함 (`aws configure`)
- S3 버킷에 Loki 로그가 저장되어 있어야 함

## 프로세스

### 1. S3 로그 조회
```bash
# 최근 로그 파일 목록 조회
aws s3 ls s3://${LOKI_BUCKET}/loki/ --recursive | tail -100

# 또는 특정 기간 필터링 (최근 24시간)
aws s3 ls s3://${LOKI_BUCKET}/loki/$(date -u +%Y-%m-%d)/
```

### 2. 에러 패턴 분석
S3 Select를 사용하여 에러 로그 필터링:
```bash
aws s3api select-object-content \
  --bucket ${LOKI_BUCKET} \
  --key logs/app.json \
  --expression "SELECT * FROM s3object s WHERE s.level = 'ERROR' OR s.level = 'FATAL'" \
  --expression-type SQL \
  --input-serialization '{"JSON": {"Type": "LINES"}}' \
  --output-serialization '{"JSON": {}}' \
  output.json
```

### 3. 에러 분류
다음 패턴으로 분류:
- **5xx**: HTTP 서버 에러 (500, 502, 503, 504)
- **Exception**: Java Exception (NullPointerException, SQLException 등)
- **Timeout**: 요청 시간 초과
- **Connection**: DB/외부 서비스 연결 실패
- **Business**: 비즈니스 로직 에러

### 4. 우선순위 정렬
| 우선순위 | 기준 |
|----------|------|
| P0 (Critical) | 500 에러 다발, 서비스 중단 |
| P1 (High) | 반복되는 Exception |
| P2 (Medium) | 간헐적 Timeout |
| P3 (Low) | 기타 경고 |

### 5. 분석 리포트 생성
`.atdd/runtime/errors/error-report-{YYYYMMDD-HHmmss}.md` 파일 생성

## 출력 형식

### 콘솔 요약
```
📊 에러 로그 분석 리포트
기간: 2026-02-15 00:00 ~ 2026-02-16 00:00
총 에러: 42건

🔴 Critical (P0): 3건
   - NullPointerException in UserService: 2건
   - 500 Internal Server Error /api/orders: 1건

🟠 High (P1): 8건
   - SQLException (Connection timeout): 5건
   - IllegalArgumentException: 3건

🟡 Medium (P2): 15건
   - Request timeout /api/products: 15건

🟢 Low (P3): 16건
   - 기타 경고: 16건

상세 리포트: .atdd/runtime/errors/error-report-20260216-143022.md
```

### 리포트 파일 구조
```markdown
# 에러 리포트 - 2026-02-16

## 개요
- 분석 기간: 2026-02-15 00:00 ~ 2026-02-16 00:00
- 총 에러 수: 42건
- 분석 시간: 2026-02-16 14:30:22

## Critical (P0) - 3건

### ERR-001: NullPointerException in UserService
- **발생 시간**: 2026-02-16 10:23:45
- **빈도**: 2회
- **스택트레이스**:
  ```
  java.lang.NullPointerException
    at com.example.service.UserService.findById(UserService.java:45)
    at com.example.controller.UserController.getUser(UserController.java:23)
  ```
- **컨텍스트**: userId가 null로 전달됨
- **수정 제안**: `/analyze-error ERR-001` 실행

...

## 추천 조치
1. ERR-001: UserService NPE 수정 (높은 우선순위)
2. ERR-003: DB Connection Pool 설정 점검
3. ERR-010: /api/products 타임아웃 임계값 조정
```

## 환경 변수
```bash
# 필수
LOKI_BUCKET=your-loki-bucket-name

# 선택
MONITOR_HOURS=24  # 조회 기간 (시간)
```

## 다음 단계
- 특정 에러 심층 분석: `/analyze-error {error-id}`
- 자동 수정 진행: `/fix {error-id}`
