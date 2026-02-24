# 003. 구독 상태 관리 아키텍처

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
1. **상태 전이 불일치**: 동시성 이슈로 구독 상태가 예상과 다르게 변경됨
2. **이력 추적 실패**: 상태 변경 이력이 누락되어 감사/CS 대응 불가
3. **트랜잭션 경합**: 높은 트래픽에서 데드락 또는 타임아웃 발생

### Q2: 실패 당시 상황
| 항목 | 현재 | 실패 시나리오 |
|------|------|---------------|
| 트래픽 | 100 TPS | 500 TPS (동시 갱신 폭증) |
| 팀 규모 | 2명 | 5명 |
| 데이터 크기 | 10GB | 100GB |
| 비즈니스 | 초기 서비스 | 연말 정산 시즌 |

### Q3: 미리 알았다면 다른 선택
- 만약 **동시성 이슈가 빈번**할 것을 알았다면 → 낙관적 락 대신 분산 락을 고려했을 것
- 만약 **이력 조회 성능이 중요**할 것을 알았다면 → 이력 테이블을 별도로 분리했을 것

---

## Context

Apple IAP 구독은 다양한 상태(ACTIVE, EXPIRED, GRACE_PERIOD, BILLING_RETRY 등)를 가지며, Apple Webhook을 통해 상태 변경 이벤트가 수신된다. 상태 관리의 정확성과 이력 추적이 중요하다.

### 비즈니스 요구사항
- 구독 상태 전이의 정확성 보장
- 모든 상태 변경 이력 추적 (감사, CS 대응)
- 동시성 제어 (같은 구독에 대한 동시 Webhook)

### 기술적 제약사항
- Apple Webhook은 중복 발송 가능 (멱등성 필요)
- 트랜잭션 내에서 상태 변경 및 이력 기록
- DDD Aggregate Root 패턴 적용

### 팀/조직 상황
- DDD 설계 경험 보유
- Spring Data JPA 숙련도 높음
- Event Sourcing은 경험 없음

---

## Decision

1. **상태 패턴**: `SubscriptionStatus` Enum으로 상태 정의
2. **이력 관리**: `SubscriptionHistory` Entity로 모든 변경 이력 기록
3. **트랜잭션**: 동기 처리 (Aggregate Root 내에서 상태 변경 + 이력 기록)
4. **동시성 제어**: 낙관적 락 (JPA @Version)
5. **멱등성 구현**: `transactionId` 기반 멱등성 키 사용 (Redis TTL 24시간)
6. **상태 전이 검증**: 테스트 코드로 상태 전이 매트릭스 검증, CI에서 커버리지 측정

---

## Trade-off Matrix (Phase B 결과)

| 평가기준 | 상태 패턴 + 이력 Entity (선택) | Event Sourcing | 상태만 관리 |
|----------|------------------------------|----------------|-------------|
| 정확성 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| 성능 | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| 확장성 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| 팀 친숙도 | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| 운영 복잡도 | ⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| **총점** | **23** | **17** | **19** |

### 선택 설명 (트레이드오프)
상태 패턴 + 이력 Entity를 선택한 이유:
- **정확성과 팀 친숙도의 균형**: DDD Aggregate 패턴으로 익숙한 방식
- **이력 추적**: 별도 Entity로 명시적 이력 관리
- **동기 트랜잭션**: 단일 트랜잭션으로 일관성 보장

Event Sourcing은 완벽한 이력 추적이 가능하지만, 팀 경험 부족과 운영 복잡도로 배제. 상태만 관리하는 방식은 이력 추적이 불가능하여 비즈니스 요구사항 미충족.

---

## Alternatives Considered

### 상태 패턴 + 이력 Entity (선택)
- **장점**:
  - 명확한 상태 전이 로직
  - 모든 변경 이력 추적 가능
  - DDD Aggregate Root 패턴 적용
  - JPA @Version으로 낙관적 락
- **단점**:
  - 이력 테이블 크기 증가
  - 상태 전이 로직 복잡도
- **실패 시나리오**: 높은 동시성에서 낙관적 락 충돌 빈발

### Event Sourcing
- **장점**:
  - 완전한 이력 추적
  - 시점 복구 가능
  - 감사 로그 자동 생성
- **단점**:
  - 높은 학습 곡선
  - 이벤트 스키마 버전 관리
  - CQRS 필수
- **선택하지 않은 이유**: 팀 경험 부족, 현재 요구사항에 과도함
- **실패 시나리오**: 운영 미숙으로 데이터 불일치

### 상태만 관리 (이력 없음)
- **장점**:
  - 단순한 구현
  - 높은 성능
- **단점**:
  - 이력 추적 불가
  - 감사 요구사항 미충족
  - CS 대응 어려움
- **선택하지 않은 이유**: 비즈니스 요구사항 미충족
- **실패 시나리오**: 환불 분쟁 시 증빙 불가

---

## Consequences

### 긍정적
- 구독 상태 전이의 명확한 제어
- 모든 변경 이력으로 감사/CS 대응 가능
- DDD Aggregate로 일관성 경계 설정
- 낙관적 락으로 동시성 제어

### 부정적
- 이력 테이블 지속적 증가
- 상태 전이 로직 유지보수 필요
- 높은 동시성에서 재시도 발생 가능

### 위험
- **낙관적 락 충돌**: 동시 Webhook으로 인한 버전 충돌
  - 완화 전략: 재시도 로직, 사용자 ID 기반 순차 처리
- **이력 테이블 크기**: 장기간 운영 시 대용량 테이블
  - 완화 전략: 아카이빙 정책 (1년 후 cold storage)
- **상태 전이 복잡도**: 새로운 상태 추가 시 전이 규칙 수정
  - 완화 전략: 상태 전이 매트릭스 문서화, 테스트 커버리지

---

## Reconsideration Trigger

### 재검토 조건 (정량적 임계값)

| 조건 | 임계값 | 현재 상태 | 재검토 필요 |
|------|--------|-----------|-------------|
| 동시 Webhook 빈도 | 10회/분 초과 | <1회/분 | ❌ |
| 낙관적 락 충돌율 | 5% 초과 | 0% | ❌ |
| 이력 테이블 크기 | 1억 건 초과 | 0건 | ❌ |
| 상태 전이 지연 | 1초 초과 | <100ms | ❌ |
| 새로운 상태 추가 | 3개 이상 | 0개 | ❌ |

**재검토 시점**: 2026-08-24 또는 위 조건 달성 시
**Event Sourcing 전환 고려**: 이력 기반 비즈니스 로직 복잡도 증가 시

---

## Apple 이벤트 타입 매핑 테이블

| Apple 이벤트 타입 | 상태 전이 | 설명 |
|-------------------|-----------|------|
| SUBSCRIBED | → ACTIVE | 신규 구독 |
| DID_RENEW | → ACTIVE | 갱신 성공 |
| DID_FAIL_TO_RENEW | → BILLING_RETRY | 갱신 실패 |
| DID_CHANGE_RENEWAL_STATUS | (상태 유지) | 갱신 의사 변경 |
| EXPIRED | → EXPIRED | 구독 만료 |
| GRACE_PERIOD_EXPIRED | → EXPIRED | 유예 기간 종료 |
| OFFER_REDEEMED | → ACTIVE | 프로모션 코드 사용 |
| PRICE_CHANGE | (상태 유지) | 가격 변경 통지 |
| REFUND | → REFUNDED | 환불 처리 |
| REFUND_DECLINED | (상태 유지) | 환불 거절 |
| RENEWAL_EXTENDED | (만료일 연장) | 갱신 기간 연장 |
| RENEWAL_EXTENSION | (만료일 연장) | 갱신 연장 요청 |
| REVOKE | → REVOKED | 가족 공유 취소 |

**정의되지 않은 이벤트**: 로그 기록 후 기본 처리 (상태 유지)

---

## Self-Critique Score (Phase D 결과)

| # | 항목 | 점수(1~5) | 비고 |
|---|------|-----------|------|
| 1 | Context 충분성 | 5 | DDD/트랜잭션 요구사항 명확 |
| 2 | 대안 분석 깊이 | 4 | 3개 대안 구체적 비교 |
| 3 | Consequences 솔직성 | 5 | 부정적 결과와 완화 전략 포함 |
| 4 | Reconsideration 구체성 | 4 | 정량적 임계값 명확 |
| 5 | 설득력 | 4 | DDD 패턴 기반 논리적 선택 |
| **총점** | | **22/25** | **평균 4.4** |

**평균 4점 미만 항목은 수정 완료**: [x] 예 [ ] 아니오

---

## Related
- Related ADRs: 001-apple-webhook-jws-verification.md, 002-async-message-processing-rabbitmq.md
- Related Requirements: RQ-DOM-001 (구독 상태), RQ-DOM-002 (이력 관리)

## References
- [DDD Aggregate Pattern](https://martinfowler.com/bliki/DDD_Aggregate.html)
- [State Pattern](https://refactoring.guru/design-patterns/state)
- [Apple Subscription Status](https://developer.apple.com/documentation/appstoreservernotifications/status)
