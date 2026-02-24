# Red Team Critique - 사용자 결정 로그

## 개요
이 문서는 Red Team Critique에서 식별된 이슈에 대한 사용자 결정을 기록합니다.

**결정 상태**: ✅ 완료 (30개 이슈 전부 결정)

---

## 결정 통계

| 검토 유형 | 총 이슈 | ACCEPT | DEFER | REJECT |
|-----------|---------|--------|-------|--------|
| ADR Critique | 22 | 9 | 12 | 0 |
| Design Critique (RRAIRU) | 8 | 7 | 1 | 0 |
| **Total** | **30** | **16** | **13** | **0** |

---

## ADR-001 Critique 결정

### [SEC-1] x5c 인증서 체인 검증 미명시 (HIGH)
- **결정**: ✅ ACCEPT
- **이유**: 보안 취약점 명확, 구현 필수
- **조치**: ADR에 x5c 체인 검증 로직 추가

### [SEC-2] Replay Attack 방지 메커니즘 미정의 (MEDIUM)
- **결정**: ✅ ACCEPT
- **이유**: 보안 강화 필요, 멱등성과 연계
- **조치**: signedDate 검증 + Redis 기반 중복 방지 로직 추가

### [PERF-1] Fallback 전환 시 응답 지연 (MEDIUM)
- **결정**: 📋 DEFER
- **이유**: 현재 설계로 충분, 성능 테스트 후 재검토
- **조치**: Backlog에 추가

### [SCALE-1] 24시간 TTL의 로테이션 대응 한계 (MEDIUM)
- **결정**: ✅ ACCEPT → ADR-001에 통합
- **이유**: 키 로테이션 대응 명확화 필요
- **조치**: TTL 단축 검토 및 캐시 무효화 로직 추가

### [MAIN-1] Fallback 전환 기준 모호 (LOW)
- **결정**: ✅ ACCEPT
- **이유**: 운영 명확성을 위해 기준 정의 필요
- **조치**: Redis 연결 타임아웃 임계값 정의

### [BIZ-1] Apple 키 로테이션 주기 미파악 (LOW)
- **결정**: ✅ ACCEPT
- **이유**: 실제 로테이션 주기 파악 필요
- **조치**: 모니터링을 통한 로테이션 빈도 수집

### [REL-1] Redis + Apple API 동시 장애 시나리오 (MEDIUM)
- **결정**: ✅ ACCEPT
- **이유**: 이중 장애 대응 필요
- **조치**: DLQ 저장 + 운영 가이드 문서화

---

## ADR-002 Critique 결정

### [SEC-1] 메시지 암호화 미언급 (LOW)
- **결정**: 📋 DEFER
- **이유**: VPC 내부 네트워크 격리로 충분
- **조치**: Backlog에 추가

### [PERF-1] 큐 적체 시 처리 지연 (MEDIUM)
- **결정**: 📋 DEFER
- **이유**: 구현 단계에서 구체화
- **조치**: Backlog에 추가

### [SCALE-1] Kafka 전환 비용 (MEDIUM)
- **결정**: 📋 DEFER
- **이유**: 500 TPS 도달 시 PoC 시작
- **조치**: Backlog에 추가

### [MAIN-1] DLQ 모니터링 기준 미정의 (MEDIUM)
- **결정**: 📋 DEFER
- **이유**: 구현 단계에서 구체화
- **조치**: Backlog에 추가

### [BIZ-1] 사용자별 순서 보장 검증 필요 (HIGH)
- **결정**: ✅ ACCEPT
- **이유**: 상태 일관성 보장을 위해 필수
- **조치**: ADR에 Consistent Hashing Exchange 방식 명시

### [BIZ-2] Apple 재시도와 큐 재시도 정책 충돌 (MEDIUM)
- **결정**: ✅ ACCEPT
- **이유**: 재시도 폭증 방지 필요
- **조치**: Webhook 수신 즉시 200 응답 정책 명시

### [REL-1] RabbitMQ 클러스터 장애 시 메시지 유실 (HIGH)
- **결정**: 📋 DEFER
- **이유**: Classic Queue로 시작, 추후 Quorum Queue 전환 검토
- **조치**: Backlog에 추가

### [REL-2] 재처리 스케줄러 신뢰성 (MEDIUM)
- **결정**: 📋 DEFER
- **이유**: 구현 단계에서 구체화
- **조치**: Backlog에 추가

---

## ADR-003 Critique 결정

### [SEC-1] 이력 테이블 접근 제어 (LOW)
- **결정**: 📋 DEFER
- **이유**: 성능 테스트 후 재검토
- **조치**: Backlog에 추가

### [PERF-1] 이력 테이블 대용량화 쿼리 성능 (MEDIUM)
- **결정**: 📋 DEFER
- **이유**: 성능 테스트 후 재검토
- **조치**: Backlog에 추가

### [SCALE-1] 낙관적 락 충돌 빈발 시 병목 (MEDIUM)
- **결정**: 📋 DEFER
- **이유**: 성능 테스트 후 재검토
- **조치**: Backlog에 추가

### [MAIN-1] 상태 전이 매트릭스 동기화 (MEDIUM)
- **결정**: ✅ ACCEPT
- **이유**: 문서와 코드 일치 보장 필요
- **조치**: 상태 전이 테스트 코드로 매트릭스 검증

### [BIZ-1] 멱등성 구현 구체화 필요 (HIGH)
- **결정**: ✅ ACCEPT
- **이유**: 중복 Webhook 처리 방지 필수
- **조치**: transactionId 기반 멱등성 키 저장 (Redis TTL 24시간)

### [BIZ-2] Apple 이벤트 타입과 상태 전이 매핑 (MEDIUM)
- **결정**: ✅ ACCEPT
- **이유**: 이벤트 처리 누락 방지
- **조치**: Apple 이벤트 타입별 상태 전이 매핑 테이블 작성

### [REL-1] 낙관적 락 재시도 실패 시나리오 (MEDIUM)
- **결정**: 📋 DEFER
- **이유**: 성능 테스트 후 재검토
- **조치**: Backlog에 추가

---

## ACCEPT 이슈 요약 (ADR 수정 필요)

| ADR | 이슈 | 수정 내용 |
|-----|------|-----------|
| 001 | x5c 체인 검증 | Apple Root CA 검증 로직 추가 |
| 001 | Replay Attack 방지 | signedDate 검증 + Redis 중복 방지 |
| 001 | TTL 로테이션 대응 | 캐시 무효화 로직 추가 |
| 001 | Fallback 기준 | 타임아웃 임계값 정의 |
| 001 | 키 로테이션 주기 | 모니터링 수집 방안 추가 |
| 001 | 이중 장애 | DLQ 저장 + 운영 가이드 |
| 002 | 순서 보장 | Consistent Hashing Exchange 명시 |
| 002 | 재시도 정책 | 즉시 200 응답 정책 |
| 003 | 멱등성 | transactionId 기반 멱등성 키 |
| 003 | 상태 전이 검증 | 테스트 코드로 매트릭스 검증 |
| 003 | 이벤트 매핑 | Apple 이벤트 타입 매핑 테이블 |

---

## Design Critique 결정 (RRAIRU)

**검토 일시**: 2026-02-24

### [INV-1] expiresAt >= currentPeriodEnd 불변식 누락 (HIGH)
- **결정**: ✅ ACCEPT
- **이유**: 데이터 일관성 보장을 위해 필수
- **조치**: Subscription 불변식에 추가, create()/renew()에서 검증

### [INV-2] upgrade() 시 productId 검증 불변식 누락 (HIGH)
- **결정**: ✅ ACCEPT
- **이유**: Entity 내부에서도 비즈니스 규칙 보장 필요
- **조치**: `Subscription.upgrade()` 내부에 검증 로직 추가

### [RESP-1] ProductId 등급 비교 책임 (MEDIUM)
- **결정**: ✅ ACCEPT
- **이유**: VO는 외부 의존성 없이 순수하게 유지
- **조치**: ProductId는 식별자만 유지, `UpgradeService.compareTier()`에서 등급 비교

### [REQ-1] 무료 체험 플래그 누락 (MEDIUM)
- **결정**: ✅ ACCEPT
- **이유**: S1 요구사항 충족을 위해 필수
- **조치**: Subscription에 `boolean isTrialPeriod` 필드 추가

### [AGG-1] Transaction이 별도 Aggregate인지 불명확 (MEDIUM)
- **결정**: ✅ ACCEPT
- **이유**: 트랜잭션 수가 무제한 증가 가능, 수명주기 다름
- **조치**: domain-model.md에 Transaction을 별도 Aggregate로 명시

### [INV-3] Money 음수 허용 여부 (MEDIUM)
- **결정**: ✅ ACCEPT
- **이유**: Money는 금액 표현, 음수는 혼란 야기
- **조치**: Money는 양수만 허용, 환불 금액은 `RefundAmount`로 별도 표현

### [REQ-2] 갱신 취소 상태 누락 (LOW)
- **결정**: 📋 DEFER
- **이유**: 현재 요구사항에 명시되지 않음
- **조치**: Backlog에 추가, 추후 요구사항 구체화 시 재검토

### [UBIQ-1] UPSELL vs UPGRADE 용어 혼용 (LOW)
- **결정**: ✅ ACCEPT
- **이유**: 용어 일관성을 위한 문서화 필요
- **조치**: 용어 가이드에 UPSELL = upgrade 명시

---

## ACCEPT 이슈 요약 (Design Critique)

| 이슈 | 수정 내용 |
|------|-----------|
| INV-1 | `expiresAt >= currentPeriodEnd` 불변식 추가 |
| INV-2 | `upgrade()` 내부 productId 검증 추가 |
| RESP-1 | ProductId에서 `isHigherTierThan()` 제거, Service로 이동 |
| REQ-1 | `isTrialPeriod` 필드 추가 |
| AGG-1 | Transaction을 별도 Aggregate로 명시 |
| INV-3 | Money 음수 불변식 유지, 환불은 별도 VO |
| UBIQ-1 | 용어 가이드 문서에 UPSELL = upgrade 추가 |
