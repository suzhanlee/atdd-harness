# ADR Index

## 프로젝트: Apple IAP 구독 (Refund & Upgrade)

---

## Active ADRs

| Number | Title | Status | Date | Self-Critique |
|--------|-------|--------|------|---------------|
| 001 | Apple Webhook JWS 검증 및 공개키 캐싱 전략 | Proposed | 2026-02-24 | 22/25 (A) |
| 002 | 비동기 메시지 처리 아키텍처 (RabbitMQ) | Proposed | 2026-02-24 | 22/25 (A) |
| 003 | 구독 상태 관리 아키텍처 | Proposed | 2026-02-24 | 22/25 (A) |

---

## ADR Summary

### 001. Apple Webhook JWS 검증 및 공개키 캐싱 전략
- **결정**: nimbus-jose-jwt + Redis 캐싱 (24h TTL) + Fallback
- **핵심 트레이드오프**: Redis 선택 (신뢰성 4점, 확장성 4점, 팀 친숙도 4점)
- **재검토 조건**: Webhook 트래픽 500 TPS 초과

### 002. 비동기 메시지 처리 아키텍처 (RabbitMQ)
- **결정**: RabbitMQ + DLQ + 재처리 스케줄러
- **핵심 트레이드오프**: Kafka 대비 낮은 복잡도, 팀 친숙도 5점
- **재검토 조건**: 트래픽 500 TPS 초과 시 Kafka 전환 고려

### 003. 구독 상태 관리 아키텍처
- **결정**: 상태 패턴 + 이력 Entity + 낙관적 락
- **핵심 트레이드오프**: Event Sourcing 대비 낮은 복잡도, 명확한 이력 추적
- **재검토 조건**: 이력 테이블 1억 건 초과

---

## Deprecated ADRs

| Number | Title | Superseded By | Date |
|--------|-------|---------------|------|
| - | - | - | - |

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                     Apple App Store                              │
│                          │                                       │
│                    Webhook (JWS)                                 │
│                          ▼                                       │
├─────────────────────────────────────────────────────────────────┤
│                    API Gateway                                   │
│                          │                                       │
│                    JWS 검증                                      │
│                   (nimbus-jose-jwt)                              │
│                          │                                       │
│                   Redis (공개키 캐시)                            │
│                          ▼                                       │
├─────────────────────────────────────────────────────────────────┤
│                   RabbitMQ                                       │
│                          │                                       │
│                   Webhook Queue                                   │
│                          ▼                                       │
├─────────────────────────────────────────────────────────────────┤
│                   Consumer                                       │
│                          │                                       │
│              ┌───────────┴───────────┐                          │
│              ▼                       ▼                          │
│     Subscription Aggregate    SubscriptionHistory               │
│        (상태 패턴)              (이력 Entity)                    │
│              │                       │                          │
│              └───────────┬───────────┘                          │
│                          ▼                                       │
│                     MySQL                                        │
└─────────────────────────────────────────────────────────────────┘
```

---

## Key Decisions

| 영역 | 결정 | 이유 |
|------|------|------|
| **보안** | JWS 검증 + Redis 캐싱 | 성능과 신뢰성 균형 |
| **신뢰성** | RabbitMQ + DLQ | 팀 친숙도, 이벤트 유실 방지 |
| **데이터** | 상태 패턴 + 이력 Entity | DDD 패턴, 감사 추적 |

---

## ADR Creation Guide
1. 새 ADR 번호 할당 (순차적 증가)
2. **Phase A: Pre-Mortem 질문 답변**
3. **Phase B: Trade-off Matrix 작성 (최소 3개 대안)**
4. Phase C: 템플릿 기반 ADR 본문 작성
5. **Phase D: Self-Critique 수행 (평균 4점 이상 달성)**
6. `/redteam` 실행하여 비평 수행
7. 비평 반영 후 Status: Accepted로 변경

---

## Next Steps
1. `/redteam` 실행하여 ADR 비평 수행
2. `/design` 실행하여 도메인 모델 설계
3. `/redteam-design` 실행하여 도메인 모델 비평
