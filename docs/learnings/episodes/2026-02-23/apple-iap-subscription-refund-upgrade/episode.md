# Episode: Apple IAP Subscription, Refund & Upgrade

## Meta
- **날짜**: 2026-02-23 ~ 2026-02-24
- **관련 ADR**:
  - [ADR-001: Apple Webhook JWS 검증 및 공개키 캐싱 전략](../../../../.atdd/apple-iap-subscription-refund-upgrade/adr/001-apple-webhook-jws-verification.md)
  - [ADR-002: 비동기 메시지 처리 아키텍처 (RabbitMQ)](../../../../.atdd/apple-iap-subscription-refund-upgrade/adr/002-async-message-processing-rabbitmq.md)
  - [ADR-003: 구독 상태 관리 아키텍처](../../../../.atdd/apple-iap-subscription-refund-upgrade/adr/003-subscription-state-management.md)
- **요구사항**: [refined-requirements.md](../../../../.atdd/apple-iap-subscription-refund-upgrade/validate/refined-requirements.md)
- **Design Critique**: [design-critique-2026-02-24.md](../../../../.atdd/apple-iap-subscription-refund-upgrade/redteam/design-critique-2026-02-24.md)

---

## Competency Scores (역량 점수)

### ADR Self-Critique 점수

| ADR 번호 | Context | 대안분석 | Consequences | Reconsideration | 설득력 | 총점 | 등급 |
|----------|---------|----------|--------------|-----------------|--------|------|------|
| ADR-001 | 5/5 | 4/5 | 5/5 | 4/5 | 4/5 | 22/25 | B |
| ADR-002 | 5/5 | 4/5 | 5/5 | 4/5 | 4/5 | 22/25 | B |
| ADR-003 | 5/5 | 4/5 | 5/5 | 4/5 | 4/5 | 22/25 | B |
| **평균** | **5.0** | **4.0** | **5.0** | **4.0** | **4.0** | **22/25** | **B** |

### 역량 성장 추세

| 일시 | 평균 점수 | 비고 |
|------|-----------|------|
| 2026-02-23 | 22/25 | Apple IAP 구독 설계 |

---

## Context (맥락)

Apple App Store Server Notifications V2를 통한 구독 관리 시스템 구축이 필요한 상황.

### 비즈니스 요구사항
- 구독 생성, 갱신, 만료, 환불, 업그레이드 처리
- Apple Webhook JWS 서명 검증으로 위조 방지
- 서버 장애 시에도 이벤트 유실 방지
- 구독 상태 전이의 정확성 보장

### 기술적 제약사항
- Apple Webhook은 500ms 이내 응답 기대
- JWS(JSON Web Signature) 서명 검증 필수
- 팀의 RabbitMQ, Redis 운영 경험 보유
- DDD Aggregate 패턴 적용

### 팀 상황
- Kafka 운영 경험 없음 (RabbitMQ 선호)
- DDD 설계 경험 보유
- Spring Data JPA 숙련도 높음

---

## Decisions (결정)

### ADR-001: Apple Webhook JWS 검증 및 공개키 캐싱 전략

- **선택**: Redis 캐싱 (24시간 TTL) + Fallback (Apple API 직접 호출)
- **대안들**: In-Memory Cache, Apple API 직접 호출
- **이유**:
  - 다중 인스턴스 환경에서 키 일관성 보장
  - 팀 친숙도: 기존 Redis 운영 노하우 활용
  - Fallback 가능으로 신뢰성 확보

### ADR-002: 비동기 메시지 처리 아키텍처 (RabbitMQ)

- **선택**: RabbitMQ + DLQ + 재처리 스케줄러
- **대안들**: Apache Kafka, AWS SQS
- **이유**:
  - 팀 운영 경험 풍부
  - 현재 100 TPS 트래픽에 충분한 성능
  - 낮은 운영 비용

### ADR-003: 구독 상태 관리 아키텍처

- **선택**: 상태 패턴 + 이력 Entity + 낙관적 락 (JPA @Version)
- **대안들**: Event Sourcing, 상태만 관리
- **이유**:
  - 명확한 상태 전이 로직
  - 모든 변경 이력 추적 가능
  - DDD Aggregate Root 패턴 적용

---

## Critique Feedback (비평 피드백)

### Architecture (redteam) - 22개 이슈

| 이슈 | 관점 | 심각도 | 결정 | 비고 |
|------|------|--------|------|------|
| x5c 인증서 체인 검증 미명시 | Security | HIGH | ACCEPT | Apple Root CA 검증 로직 추가 |
| Replay Attack 방지 메커니즘 미정의 | Security | MEDIUM | ACCEPT | signedDate 검증 + Redis 중복 방지 |
| 사용자별 순서 보장 검증 필요 | Business | HIGH | ACCEPT | Consistent Hashing Exchange 명시 |
| Apple 재시도와 큐 재시도 정책 충돌 | Business | MEDIUM | ACCEPT | Webhook 수신 즉시 200 응답 |
| 멱등성 구현 구체화 필요 | Business | HIGH | ACCEPT | transactionId 기반 멱등성 키 |
| 이중 장애 (Redis + Apple API) | Reliability | MEDIUM | ACCEPT | DLQ 저장 + 운영 가이드 |
| 상태 전이 매트릭스 동기화 | Maintainability | MEDIUM | ACCEPT | 테스트 코드로 매트릭스 검증 |
| Apple 이벤트 타입 매핑 | Business | MEDIUM | ACCEPT | 매핑 테이블 작성 |
| 기타 (TTL, Fallback 기준 등) | Various | MEDIUM/LOW | DEFER | 성능 테스트 후 재검토 |

**통계**: 9 ACCEPT, 12 DEFER, 0 REJECT

### Domain Model (redteam-design) - 8개 이슈

| 이슈 | 관점 | 심각도 | 결정 | 비고 |
|------|------|--------|------|------|
| expiresAt >= currentPeriodEnd 불변식 누락 | Invariants | HIGH | ACCEPT | 불변식 추가, create()/renew()에서 검증 |
| upgrade() 내부 productId 검증 누락 | Invariants | HIGH | ACCEPT | Entity 내부 검증 로직 추가 |
| ProductId 등급 비교 책임 | Responsibility | MEDIUM | ACCEPT | VO는 식별자만, Service에서 비교 |
| 무료 체험 플래그 누락 | Requirements | MEDIUM | ACCEPT | isTrialPeriod 필드 추가 |
| Transaction이 별도 Aggregate인지 불명확 | Aggregate | MEDIUM | ACCEPT | 별도 Aggregate로 명시 |
| Money 음수 허용 여부 | Invariants | MEDIUM | ACCEPT | Money는 양수, 환불은 별도 VO |
| 갱신 취소 상태 누락 | Requirements | LOW | DEFER | Backlog 추가 |
| UPSELL vs UPGRADE 용어 혼용 | Language | LOW | ACCEPT | 용어 가이드 문서화 |

**통계**: 7 ACCEPT, 1 DEFER, 0 REJECT

---

## Domain Model Result (설계 결과)

### 핵심 Entity

| Entity | 역할 | Aggregate 여부 |
|--------|------|----------------|
| **Subscription** | 구독 Aggregate Root | ✅ Root |
| **Transaction** | 결제 트랜잭션 | 별도 Aggregate |
| **SubscriptionHistory** | 상태 변경 이력 | Subscription 하위 |
| **Refund** | 환불 정보 | Subscription 하위 |

### 핵심 VO

| VO | 속성 | 불변식 |
|----|------|--------|
| **SubscriptionStatus** | Enum (ACTIVE, EXPIRED, GRACE_PERIOD, BILLING_RETRY, REFUNDED, REVOKED) | 상태 전이 규칙 준수 |
| **Money** | amount, currency | amount ≥ 0, currency ≠ null |
| **Period** | startAt, endAt | startAt < endAt |
| **ProductId** | value (Apple Product ID) | value ≠ null |
| **WebhookEvent** | notificationType, transactionId, signedDate 등 | - |

### 주요 불변식

1. `originalTransactionId`는 null이 아니어야 함
2. `expiresAt >= currentPeriodEnd` (항상)
3. `upgrade()` 내부 productId 검증 필수
4. 상태 전이는 `SubscriptionStatusSpec` 규칙 준수

### 도메인 이벤트

- SubscriptionRenewedEvent
- SubscriptionExpiredEvent
- SubscriptionRefundedEvent
- SubscriptionUpgradedEvent
- GracePeriodEnteredEvent

---

## Lessons Learned (배운 점)

1. **재시도 정책 통합의 중요성**: Apple Webhook 재시도와 내부 큐 재시도가 충돌하면 재시도 폭증이 발생할 수 있다. Webhook 수신 즉시 200 응답 후 비동기 처리로 전환하는 정책이 핵심이다.

2. **불변식 이중 검증**: Entity 내부와 Service 양쪽에 불변식을 두면 Service를 우회한 직접 호출 시에도 비즈니스 규칙을 보장할 수 있다.

3. **Aggregate 경계 설계**: Transaction은 수명주기가 다르고 무제한 증가 가능하므로 Subscription과 별도 Aggregate로 분리하는 것이 적절하다.

---

## Tags

`#Apple-IAP` `#구독` `#환불` `#Webhook` `#JWS` `#RabbitMQ` `#DDD` `#낙관적락` `#Redis`

---

## References

- [Apple App Store Server Notifications](https://developer.apple.com/documentation/appstoreservernotifications)
- [DDD Aggregate Pattern](https://martinfowler.com/bliki/DDD_Aggregate.html)
- [State Pattern](https://refactoring.guru/design-patterns/state)
- [RabbitMQ Documentation](https://www.rabbitmq.com/docs)
