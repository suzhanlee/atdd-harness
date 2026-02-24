# 001. Apple Webhook JWS 검증 및 공개키 캐싱 전략

## Metadata
| 항목 | 값 |
|------|-----|
| 작성자 | Suchan Lee |
| 작성일 | 2026-02-24 |
| 검토일 | 2026-08-24 (+6개월) |
| Status | Proposed |

---

## Pre-Mortem (Phase A 결과)

### Q1: 이 결정이 1년 후 실패한다면 가장 가능성 높은 이유는?
1. **잘못된 키 설정**: Apple 공개키 로테이션 시점에 캐시된 키가 만료되지 않아 서명 검증 실패
2. **트래픽 이슈로 미처리**: Redis 장애 시 Fallback 로직이 제대로 동작하지 않아 Webhook 처리 지연
3. **캐시 동기화 문제**: 다중 인스턴스 환경에서 키 불일치로 검증 실패

### Q2: 실패 당시 상황
| 항목 | 현재 | 실패 시나리오 |
|------|------|---------------|
| 트래픽 | 100 TPS | 1,000 TPS (10x 증가) |
| 팀 규모 | 2명 | 5명 |
| 데이터 크기 | 10GB | 500GB |
| 비즈니스 | 초기 서비스 | DAU 10만 달성 |

### Q3: 미리 알았다면 다른 선택
- 만약 **Apple이 예고 없이 키를 자주 로테이션**한다는 것을 알았다면 → 캐시 TTL을 1시간으로 단축하고 Pre-fetch 전략을 고려했을 것
- 만약 **Redis 장애 빈도가 높다**는 것을 알았다면 → In-Memory 캐시를 1차, Redis를 2차로 하는 계층형 캐싱을 고려했을 것

---

## Context

Apple App Store Server Notifications V2는 모든 Webhook 요청을 JWS(JSON Web Signature)로 서명한다. 서비스 보안을 위해 수신된 Webhook의 서명을 검증해야 한다.

### 비즈니스 요구사항
- 구독 상태 변경(갱신, 환불, 만료 등)을 실시간으로 처리해야 함
- 위조된 Webhook 요청을 차단해야 함
- Apple의 공개키 로테이션에 대응해야 함

### 기술적 제약사항
- Apple 공개키는 JWT 헤더의 x5c 인증서 체인에서 추출
- 공개키 조회를 위한 Apple API 호출은 네트워크 비용 발생
- 500ms 이내 Webhook 처리 완료 권장 (Apple 재시도 방지)

### 팀/조직 상황
- 팀의 Redis 운영 경험 보유
- nimbus-jose-jwt 라이브러리 사용 경험 있음
- Spring Boot 3.x 기반 서비스

---

## Decision

1. **JWS 검증 라이브러리**: `nimbus-jose-jwt` 사용
2. **공개키 캐싱 전략**: Redis 사용 (24시간 TTL)
3. **Fallback 전략**: Redis 장애 시 Apple API 직접 호출
4. **x5c 인증서 체인 검증**: Apple Root CA 기반 전체 체인 검증
5. **Replay Attack 방지**: `signedDate` 타임스탬프 검증 (5분 이내) + Redis 기반 중복 방지
6. **키 로테이션 대응**: 검증 실패 시 즉시 캐시 무효화 및 재조회

---

## Trade-off Matrix (Phase B 결과)

| 평가기준 | Redis (선택) | In-Memory Cache | Apple API 직접 호출 |
|----------|--------------|-----------------|---------------------|
| 신뢰성 | ⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| 확장성 | ⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| 성능 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ |
| 팀 친숙도 | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |
| 장애 격리 | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **총점** | **20** | **17** | **19** |

### 선택 설명 (트레이드오프)
Redis를 선택한 이유:
- **신뢰성과 확장성의 균형**: 다중 인스턴스 환경에서 키 일관성 보장
- **팀 친숙도**: 기존 Redis 운영 노하우 활용
- **Fallback 가능**: Redis 장애 시 Apple API 직접 호출로 전환 가능

In-Memory Cache는 성능이 가장 좋지만, 다중 인스턴스 환경에서 키 동기화 문제가 발생할 수 있어 배제.

Apple API 직접 호출은 신뢰성이 가장 높지만, 트래픽 증가 시 응답 시간 보장이 어려워 기본 전략으로는 부적합.

---

## Alternatives Considered

### Redis 캐싱 (선택)
- **장점**:
  - 다중 인스턴스 간 키 일관성 보장
  - 24시간 TTL로 로테이션 대응
  - 기존 인프라 활용
- **단점**:
  - Redis 의존성 추가
  - 캐시 미스 시 지연 발생 가능
- **실패 시나리오**: Redis 클러스터 전체 장애 시 캐시 복구까지 지연

### In-Memory Cache (Caffeine/Guava)
- **장점**:
  - 네트워크 오버헤드 없음 (최고 성능)
  - 외부 의존성 없음
- **단점**:
  - 인스턴스 간 키 불일치 가능
  - 재시작 시 캐시 초기화
- **선택하지 않은 이유**: 다중 인스턴스 환경에서 일관성 보장 어려움
- **실패 시나리오**: Rolling deployment 중 키 불일치로 검증 실패

### Apple API 직접 호출
- **장점**:
  - 항상 최신 키 보장
  - 캐시 관리 복잡도 없음
- **단점**:
  - 네트워크 지연 (100-300ms)
  - Apple API 장애 시 영향
  - Rate Limit 위험
- **선택하지 않은 이유**: Webhook 500ms 처리 목표 달성 어려움
- **실패 시나리오**: Apple API 장애 시 모든 Webhook 검증 실패

---

## Consequences

### 긍정적
- Webhook 검증 응답 시간 50ms 이내 달성 가능
- Apple 키 로테이션에 자동 대응
- 다중 인스턴스 환경에서 일관된 검증

### 부정적
- Redis 추가 운영 비용
- 캐시 미스 첫 요청은 지연 발생
- 키 로테이션 감지를 위한 모니터링 필요

### 위험
- **Redis 장애**: Fallback 로직으로 전환하나 초기 지연 발생
  - 완화 전략: Redis Sentinel/Cluster 구성, Circuit Breaker 패턴
  - **Fallback 전환 기준**: 연결 타임아웃 500ms 초과 시 전환, 3회 연속 실패 시 Circuit Open
- **키 로테이션 미감지**: 캐시된 키가 만료되기 전 로테이션 발생
  - 완화 전략: 검증 실패 시 즉시 캐시 무효화 및 재조회
  - **모니터링**: 키 로테이션 감지 시 즉시 알림, 실제 로테이션 주기 수집
- **이중 장애 (Redis + Apple API)**: 두 시스템 모두 장애 시 모든 Webhook 검증 실패
  - 완화 전략: 실패한 Webhook을 DLQ에 저장하여 후속 복구, 수동 개입을 위한 운영 가이드 문서화

---

## Reconsideration Trigger

### 재검토 조건 (정량적 임계값)

| 조건 | 임계값 | 현재 상태 | 재검토 필요 |
|------|--------|-----------|-------------|
| Webhook 트래픽 | 500 TPS | 100 TPS | ❌ |
| Redis 장애 빈도 | 월 2회 이상 | 0회 | ❌ |
| 검증 실패율 | 0.1% 초과 | 0% | ❌ |
| 평균 검증 시간 | 100ms 초과 | 50ms | ❌ |
| Apple 키 로테이션 주기 | 7일 미만 | 알 수 없음 | ⚠️ 모니터링 |

**재검토 시점**: 2026-08-24 또는 위 조건 달성 시

---

## Self-Critique Score (Phase D 결과)

| # | 항목 | 점수(1~5) | 비고 |
|---|------|-----------|------|
| 1 | Context 충분성 | 5 | 비즈니스/기술/팀 상황 모두 명시 |
| 2 | 대안 분석 깊이 | 4 | 3개 대안 구체적 비교 |
| 3 | Consequences 솔직성 | 5 | 부정적 결과와 완화 전략 포함 |
| 4 | Reconsideration 구체성 | 4 | 정량적 임계값 명확 |
| 5 | 설득력 | 4 | 트레이드오프 설명 논리적 |
| **총점** | | **22/25** | **평균 4.4** |

**평균 4점 미만 항목은 수정 완료**: [x] 예 [ ] 아니오

---

## Related
- Related ADRs: 002-async-message-processing-rabbitmq.md, 003-subscription-state-management.md
- Related Requirements: RQ-SEC-001 (JWS 검증), RQ-NF-001 (성능)

## References
- [Apple App Store Server Notifications JWS](https://developer.apple.com/documentation/appstoreservernotifications/jwstransactiondecodedpayload)
- [nimbus-jose-jwt Documentation](https://connect2id.com/products/nimbus-jose-jwt)
