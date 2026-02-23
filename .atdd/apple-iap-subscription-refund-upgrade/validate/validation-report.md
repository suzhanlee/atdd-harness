# 검증 리포트

## 검증 일시
2026-02-24

## 검증 대상
- `.atdd/apple-iap-subscription-refund-upgrade/interview/requirements-draft.md`

---

## Gap Matrix

| 항목 | 예측 | 실제 | Gap |
|------|------|------|-----|
| 기술 스택 호환성 | ✅ | ✅ | = |
| 외부 의존성 (Apple API) | ✅ | ⚠️ | 🔻 |
| 일정/리소스 | ✅ | ✅ | = |
| 기능 명세 완전성 | ✅ | ✅ | = |
| 예외 케이스 포함 | ❌ | ❌ | = |
| 비기능 요구사항 | ⚠️ | ⚠️ | = |
| 용어 통일 | ✅ | ✅ | = |
| 요구사항 간 충돌 | - | ✅ | = |
| 외부 API 명세 | ❌ | ⚠️ | 🔺 |
| 데이터베이스 설계 | ⚠️ | ⚠️ | = |

**Gap 범례**: 🔻=예측 실패, =일치, 🔺=과도한 비관

---

## 검증 결과

### Feasibility

| 항목 | 결과 | 비고 |
|------|------|------|
| 기술 스택 호환성 | ✅ PASS | Java 17 + Spring Boot 3.x + JPA + MySQL로 Apple IAP 연동 가능 |
| 외부 의존성 (Apple API) | ⚠️ WARN | S2S Notification V2는 JWS 검증 필요. Apple 공개키 로테이션 처리, JWS 검증 라이브러리 추가 필요 |
| 일정/리소스 | ✅ PASS | AI Agent 활용 시 충분 |

### Completeness

| 항목 | 결과 | 비고 |
|------|------|------|
| 기능 명세 완전성 | ✅ PASS | Must/Should/Could/Won't 분류 명확 |
| 예외 케이스 포함 | ❌ FAIL | 네트워크 타임아웃, 영수증 검증 실패, Webhook 서명 검증 실패, 중복 Webhook, Apple API 장애, 업그레이드 중 환불 등 미정의 |
| 비기능 요구사항 | ⚠️ WARN | 보안/신뢰성/확장성은 언급되었으나 구체적 수치(SLA, 재시도 횟수 등) 미정의 |

### Consistency

| 항목 | 결과 | 비고 |
|------|------|------|
| 용어 통일 | ✅ PASS | 용어 정의 섹션 존재, 본문과 일치 |
| 요구사항 간 충돌 | ✅ PASS | 명확한 충돌 없음 |

### Dependencies

| 항목 | 결과 | 비고 |
|------|------|------|
| 외부 API 명세 | ⚠️ WARN | 참고 문서 링크 존재하나 S2S Notification V2 이벤트 타입, JWS 검증 방식 등 세부 명세 부족 |
| 데이터베이스 설계 | ⚠️ WARN | ADR/Design Phase에서 진행 예정 |

---

## 종합 결과: ⚠️ WARN

---

## 개선 필요 사항

### ❌ FAIL (필수 개선)

1. **예외 케이스 보강**
   - 네트워크 타임아웃 시 재시도 정책
   - 영수증 검증 실패 시 사용자 피드백
   - Webhook 서명 검증 실패 처리
   - 중복 Webhook 수신 시 멱등성 키 전략
   - Apple API 장애 시 Fallback 정책
   - 업그레이드 도중 환불 발생 시 처리

### ⚠️ WARN (권장 개선)

1. **외부 의존성 기술 스택 보강**
   - JWS 검증 라이브러리 (예: nimbus-jose-jwt)
   - Apple 공개키 캐싱 및 로테이션 처리

2. **비기능 요구사항 구체화**
   - API 응답 시간 목표 (예: 95% 요청 200ms 이내)
   - 재시도 정책 (횟수, 간격, 백오프)
   - 동시 요청 처리량 목표

3. **외부 API 명세 보강**
   - S2S Notification V2 이벤트 타입 목록
   - JWS 검증 절차 상세

---

## 다음 단계

1. `refined-requirements.md`의 개선 사항을 반영
2. `/gherkin` 진행으로 Gherkin 시나리오 작성
3. `/adr` → `/redteam` → `/design` → `/redteam-design` → `/compound` 진행
