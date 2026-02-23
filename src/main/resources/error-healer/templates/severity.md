# Error Severity Rubric

Error Healer에서 사용하는 에러 심각도 분류 기준입니다.

## P0 (Critical) - 즉시 조치

### 정의
서비스 중단, 데이터 손실, 보안 위협을 유발하는 치명적인 에러

### 판정 기준

#### HTTP 상태
- 500 Internal Server Error (빈도 > 10회/시간)
- 502 Bad Gateway (지속적)
- 503 Service Unavailable

#### 예외 타입
- `NullPointerException` (핵심 비즈니스 로직)
- `StackOverflowError`
- `OutOfMemoryError`
- `SQLException` (Connection Pool 고갈)
- 보안 관련 예외 (SQL Injection, XSS 등)

#### 영향도
- 사용자 서비스 이용 불가
- 데이터 무결성 손상
- 보안 취약점 노출
- 결제/주문 등 핵심 기능 마비

#### 응답 시간
- **즉시** (15분 이내)

#### 자동 PR
- ✅ 생성

---

## P1 (High) - 4시간 이내 조치

### 정의
핵심 기능에 영향을 주지만 서비스 전체 중단은 없는 에러

### 판정 기준

#### HTTP 상태
- 500 Internal Server Error (빈도 1-10회/시간)
- 504 Gateway Timeout

#### 예외 타입
- `SQLException` (개별 쿼리 실패)
- `IOException` (파일/네트워크)
- `DataAccessException`
- `TransactionException`
- 외부 서비스 연동 실패

#### 영향도
- 특정 기능 사용 불가
- 일부 사용자 영향
- 데이터 불일치 가능성
- 성능 심각 저하 (응답 시간 > 10초)

#### 응답 시간
- **4시간 이내**

#### 자동 PR
- ✅ 생성

---

## P2 (Medium) - 24시간 이내 조치

### 정의
간헐적 발생하거나 일시적인 기능 저하를 유발하는 에러

### 판정 기준

#### HTTP 상태
- 500 Internal Server Error (빈도 < 1회/시간)
- 429 Too Many Requests (비정상)

#### 예외 타입
- `IllegalArgumentException`
- `IllegalStateException`
- `NoSuchElementException`
- `EntityNotFoundException`
- 비즈니스 로직 오류

#### 영향도
- 특정 시나리오에서 기능 저하
- 소수 사용자 영향
- 워크어라운드 존재
- 성능 저하 (응답 시간 5-10초)

#### 응답 시간
- **24시간 이내**

#### 자동 PR
- ✅ 생성

---

## P3 (Low) - 선택적 조치

### 정의
사용자 입력 오류, 예상 가능한 예외, 경고성 로그

### 판정 기준

#### HTTP 상태
- 400 Bad Request
- 401 Unauthorized (정상 인증 실패)
- 403 Forbidden (정상 권한 없음)
- 404 Not Found
- 422 Unprocessable Entity

#### 예외 타입
- `ConstraintViolationException`
- `MethodArgumentNotValidException`
- `MissingServletRequestParameterException`
- `HttpMessageNotReadableException`
- 커스텀 비즈니스 예외 (검증용)

#### 영향도
- 사용자 입력 오류
- 정상적인 인증/인가 실패
- 예상된 비즈니스 규칙 위반
- UI/UX 개선 필요

#### 응답 시간
- **선택적** (스프린트 내 처리)

#### 자동 PR
- ❌ 생성하지 않음

---

## 분류 의사결정 플로우차트

```
에러 발생
    │
    ├─ 서비스 중단? ──────────────────── Yes ──→ P0
    │
    No
    │
    ├─ 보안 이슈? ────────────────────── Yes ──→ P0
    │
    No
    │
    ├─ 데이터 손실 가능? ─────────────── Yes ──→ P0
    │
    No
    │
    ├─ 500 에러 빈도 > 10회/시간? ────── Yes ──→ P0
    │
    No
    │
    ├─ 500 에러 빈도 1-10회/시간? ─────── Yes ──→ P1
    │
    No
    │
    ├─ 핵심 기능 장애? ───────────────── Yes ──→ P1
    │
    No
    │
    ├─ 500 에러 발생? ─────────────────── Yes ──→ P2
    │
    No
    │
    ├─ 비즈니스 로직 오류? ────────────── Yes ──→ P2
    │
    No
    │
    └─ 4xx 에러 또는 검증 예외? ──────── Yes ──→ P3
```

## 빈도 계산 기준

| 기간 | P0 임계값 | P1 임계값 | P2 임계값 |
|------|-----------|-----------|-----------|
| 1시간 | > 10회 | 1-10회 | < 1회 |
| 1일 | > 240회 | 24-240회 | < 24회 |

## 주의사항

1. **보안 이슈는 항상 P0**
   - 인증 우회, 권한 상승, 데이터 노출

2. **결제/주문 에러는 P0-P1**
   - 금전적 손실 가능성

3. **P3도 모니터링 필요**
   - 반복되는 P3는 UI/UX 개선 필요 신호

4. **상황에 따른 유연성**
   - 이벤트 기간에는 임계값 하향 조정
