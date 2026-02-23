# Epic 로드맵

> Apple IAP Subscription - Refund & Upgrade 구현

---

## 구현 순서

```
Epic 1: 구독 구매 및 상태 조회 (기반)
         ↓
    ┌────┴────┐
    ↓         ↓
Epic 2:    Epic 3:
영수증    Webhook
검증       수신
    ↓         ↓
    └────┬────┘
         ↓
    Epic 4: Webhook 이벤트 처리
         ↓
    ┌────┴────┐
    ↓         ↓
Epic 5:    Epic 6:
업그레이드  무료체험
    ↓
Epic 7: 이력관리 (Optional)
```

---

## 상세 일정

| 순서 | Epic | 의존성 | 예상 소요 | 우선순위 |
|------|------|--------|----------|----------|
| 1 | Epic 1: 구독 구매 및 상태 조회 | - | 2h | Must |
| 2 | Epic 2: Apple 영수증 검증 | Epic 1 | 2h | Must |
| 3 | Epic 3: Apple S2S Webhook 수신 | Epic 1 | 2h | Must |
| 4 | Epic 4: Webhook 이벤트 처리 | Epic 3 | 2h | Must |
| 5 | Epic 5: 구독 업그레이드 | Epic 1, 2 | 1.5h | Must |
| 6 | Epic 6: 무료 체험 | Epic 1 | 1h | Should |
| 7 | Epic 7: 구독 이력 관리 | Epic 5 | 1h | Could |

---

## 마일스톤

### M1: 기반 구축 (Epic 1 완료)
- Subscription Entity 설계
- 구매/상태조회 API 기본 구조
- **예상 시점**: 2시간차

### M2: 영수증 검증 완료 (Epic 2 완료)
- Apple 영수증 검증 연동
- 구매 프로세스 End-to-End 완료
- **예상 시점**: 4시간차

### M3: Webhook 기반 구축 (Epic 3, 4 완료)
- Apple S2S Notification 수신
- 갱신/만료/환불 이벤트 처리
- **예상 시점**: 8시간차

### M4: 핵심 기능 완료 (Epic 5 완료)
- 업그레이드 기능
- Must have 기능 모두 완료
- **예상 시점**: 9.5시간차

### M5: 전체 기능 완료 (Epic 6, 7 완료)
- 무료 체험, 이력 관리
- Should/Could 기능 완료
- **예상 시점**: 11.5시간차

---

## 병렬 작업 가능 영역

| 병렬 그룹 | Epic | 작업자 분담 가능 |
|-----------|------|------------------|
| Group A | Epic 2, Epic 3 | ✅ (Epic 1 완료 후) |
| Group B | Epic 5, Epic 6 | ✅ (Epic 4 완료 후) |

---

## 리스크 및 대응

| 리스크 | 영향도 | 대응 방안 |
|--------|--------|----------|
| Apple API 응답 지연 | Epic 2 지연 | Mock 서버로 테스트 선행 |
| JWS 검증 복잡도 | Epic 3 지연 | nimbus-jose-jwt 라이브러리 적극 활용 |
| 환불 동시성 이슈 | Epic 4 품질 | 낙관적 락 적용 |

---

## 다음 단계

Epic 분해 완료 후, **Epic 1부터 순차적으로** 진행:

```
Epic 1 → /validate → /gherkin → /adr → /redteam → /design → /redteam-design → /compound → /tdd → /refactor → /verify
```

각 Epic 완료 후 다음 Epic으로 진행합니다.
