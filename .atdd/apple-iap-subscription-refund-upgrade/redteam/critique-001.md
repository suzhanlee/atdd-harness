# Critique Report: ADR-001

## 개요
- **ADR**: 001. Apple Webhook JWS 검증 및 공개키 캐싱 전략
- **검토 일시**: 2026-02-24 18:35
- **전체 위험도**: MEDIUM

---

## 이슈 목록

### [SEC-1] x5c 인증서 체인 검증 미명시
- **관점**: Security
- **심각도**: HIGH
- **설명**: ADR에서는 JWS 서명 검증을 언급했으나, Apple이 제공하는 x5c 인증서 체인의 Root CA 검증 방법이 명시되지 않았다. 단순히 공개키만 캐싱하는 경우 중간자 공격에 취약할 수 있다.
- **영향**: 위조된 인증서로 서명된 Webhook이 수락될 위험
- **제안**:
  - Apple Root CA 인증서를 신뢰 저장소에 등록
  - x5c 체인 전체를 검증하는 로직 추가
  - 인증서 만료일 검증 포함

### [SEC-2] Replay Attack 방지 메커니즘 미정의
- **관점**: Security
- **심각도**: MEDIUM
- **설명**: 동일한 Webhook을 재전송하는 Replay Attack에 대한 방어 메커니즘이 정의되지 않았다.
- **영향**: 이미 처리된 환불/구독 변경 이벤트가 중복 처리될 위험
- **제안**:
  - JWS payload의 `signedDate` 타임스탬프 검증 (예: 5분 이내)
  - Redis에 처리된 Webhook ID 저장 (TTL 24시간)
  - 멱등성 키(transactionId) 기반 중복 처리 방지

### [PERF-1] Fallback 전환 시 응답 지연
- **관점**: Performance
- **심각도**: MEDIUM
- **설명**: Redis 장애 시 Apple API 직접 호출로 전환되는데, 이 경우 100-300ms 지연이 발생하여 500ms Webhook 처리 목표를 위협할 수 있다.
- **영향**: Apple Webhook 재시도 유발, 처리 지연 누적
- **제안**:
  - Circuit Breaker 패턴 적용 (반개방 상태에서 빠르게 복구)
  - In-Memory 1차 캐시 + Redis 2차 캐시 계층 구조 고려
  - Redis 장애 감지 시 미리 Apple API 호출로 Warm-up

### [SCALE-1] 24시간 TTL의 로테이션 대응 한계
- **관점**: Scalability
- **심각도**: MEDIUM
- **설명**: Apple이 예고 없이 공개키를 로테이션하는 경우, 24시간 TTL은 너무 길 수 있다. 재검토 조건에 "7일 미만"이 있으나, 실제 로테이션이 더 자주 발생할 수 있다.
- **영향**: 키 로테이션 직후 서명 검증 실패, Webhook 처리 중단
- **제안**:
  - TTL을 6시간으로 단축 검토
  - 검증 실패 시 즉시 캐시 무효화 + 재조회 로직 명시적 구현
  - Apple의 실제 로테이션 주기 조사 필요

### [MAIN-1] Fallback 전환 기준 모호
- **관점**: Maintainability
- **심각도**: LOW
- **설명**: Redis 장애 감지 및 Apple API 전환의 구체적인 기준이 정의되지 않았다. 어떤 상황을 "장애"로 간주하는가?
- **영향**: 일관되지 않은 Fallback 동작, 디버깅 어려움
- **제안**:
  - Redis 연결 타임아웃 임계값 정의 (예: 500ms)
  - 연속 실패 횟수 기반 전환 기준 (예: 3회 연속 실패)
  - Fallback 상태 모니터링 및 알림

### [BIZ-1] Apple 키 로테이션 주기 미파악
- **관점**: Business
- **심각도**: LOW
- **설명**: 재검토 조건에 "Apple 키 로테이션 주기: 7일 미만"이 있는데, 현재 "알 수 없음"으로 되어 있다. 이는 실제 로테이션 주기를 파악하지 못한 상태에서 설계한 것이다.
- **영향**: 잘못된 TTL 설정으로 인한 서명 검증 실패
- **제안**:
  - Apple 공식 문서 또는 커뮤니티에서 실제 로테이션 주기 조사
  - 모니터링을 통한 실제 로테이션 빈도 수집
  - 로테이션 감지 시 즉시 알림 구성

### [REL-1] Redis + Apple API 동시 장애 시나리오
- **관점**: Reliability
- **심각도**: MEDIUM
- **설명**: Redis 장애 → Apple API 호출로 전환했으나, Apple API도 장애인 경우에 대한 대응이 정의되지 않았다.
- **영향**: 모든 Webhook 검증 실패, 구독 상태 동기화 중단
- **제안**:
  - 이중 장애 시나리오에 대한 Graceful Degradation 정의
  - 실패한 Webhook을 DLQ에 저장하여 후속 복구
  - 수동 개입을 위한 운영 가이드 문서화

---

## 요약

| 관점 | 이슈 수 | HIGH | MEDIUM | LOW |
|------|---------|------|--------|-----|
| Security | 2 | 1 | 1 | 0 |
| Performance | 1 | 0 | 1 | 0 |
| Scalability | 1 | 0 | 1 | 0 |
| Maintainability | 1 | 0 | 0 | 1 |
| Business | 1 | 0 | 0 | 1 |
| Reliability | 1 | 0 | 1 | 0 |
| **Total** | **7** | **1** | **5** | **1** |

---

## 권장 사항

1. **[SEC-1] x5c 인증서 체인 검증 추가** (HIGH 우선순위)
   - Apple Root CA 검증 로직을 반드시 구현에 포함

2. **[SEC-2] Replay Attack 방지 메커니즘 추가** (MEDIUM 우선순위)
   - `signedDate` 검증 + Redis 기반 중복 방지

3. **[PERF-1] 계층형 캐싱 구조 고려** (MEDIUM 우선순위)
   - In-Memory(5분 TTL) + Redis(24시간 TTL) 구조로 지연 최소화

4. **[REL-1] 이중 장애 시나리오 대응 정의**
   - ADR의 Consequences 섹션에 Graceful Degradation 정책 추가

---

## 관련 ADR
- ADR-002 (RabbitMQ): DLQ를 통한 장애 복구 메커니즘 연계
