# Epic 목록

> Apple IAP Subscription - Refund & Upgrade 구현

---

## Epic 1: 구독 구매 및 상태 조회

- **범위**:
  - 포함: 구독 구매 요청, 구독 상태 조회, 구독 Entity 정의
  - 제외: 영수증 검증(Epic 2), Webhook 처리(Epic 3, 4)
- **Entity**: Subscription, SubscriptionStatus (Enum)
- **완료 기준 (DoD)**:
  - [ ] Subscription Entity 설계 완료 (DDD)
  - [ ] POST /api/subscriptions/purchase API 구현
  - [ ] GET /api/subscriptions/me API 구현
  - [ ] Gherkin 시나리오 작성 및 Cucumber 테스트 통과
  - [ ] 단위 테스트 커버리지 80% 이상
- **의존 Epic**: 없음
- **예상 소요**: 2시간

---

## Epic 2: Apple 영수증 검증

- **범위**:
  - 포함: Apple App Store Server API 연동, 영수증 검증 로직, 재시도 정책
  - 제외: Webhook 검증(Epic 3)
- **Entity**: AppleReceiptVerification (VO)
- **완료 기준 (DoD)**:
  - [ ] Apple 영수증 검증 서비스 구현
  - [ ] 재시도 정책 (3회, 지수 백오프) 적용
  - [ ] 네트워크 타임아웃/장애 예외 처리
  - [ ] Gherkin 시나리오 작성 및 테스트 통과
- **의존 Epic**: Epic 1 (구매 API에서 호출)
- **예상 소요**: 2시간

---

## Epic 3: Apple S2S Webhook 수신

- **범위**:
  - 포함: Webhook 수신 엔드포인트, JWS 서명 검증, 멱등성 처리
  - 제외: 이벤트별 비즈니스 로직(Epic 4)
- **Entity**: AppleWebhookEvent
- **완료 기준 (DoD)**:
  - [ ] POST /api/webhooks/apple 엔드포인트 구현
  - [ ] JWS 서명 검증 (nimbus-jose-jwt)
  - [ ] Apple 공개키 캐싱 (In-Memory)
  - [ ] 멱등성 키(transactionId) 중복 처리 방지
  - [ ] Gherkin 시나리오 작성 및 테스트 통과
- **의존 Epic**: Epic 1 (구독 상태 변경)
- **예상 소요**: 2시간

---

## Epic 4: Webhook 이벤트 처리 (갱신/만료/환불)

- **범위**:
  - 포함: SUBSCRIBED, DID_RENEW, EXPIRED, REFUND 이벤트 처리
  - 제외: 업그레이드(Epic 5), 무료체험(Epic 6)
- **Entity**: (Epic 1, 3의 Entity 활용)
- **완료 기준 (DoD)**:
  - [ ] SUBSCRIBED: 신규 구독 생성
  - [ ] DID_RENEW: 구독 기간 연장
  - [ ] EXPIRED/GRACE_PERIOD_EXPIRED: 구독 만료 처리
  - [ ] REFUND: 환불 즉시 권한 회수
  - [ ] DLQ(Dead Letter Queue) 실패 처리
  - [ ] Gherkin 시나리오 작성 및 테스트 통과
- **의존 Epic**: Epic 3 (Webhook 수신)
- **예상 소요**: 2시간

---

## Epic 5: 구독 업그레이드

- **범위**:
  - 포함: Basic→Pro, Basic→Ultra, Pro→Ultra 업그레이드
  - 제외: 다운그레이드
- **Entity**: SubscriptionUpgrade (VO), SubscriptionHistory
- **완료 기준 (DoD)**:
  - [ ] POST /api/subscriptions/upgrade API 구현
  - [ ] 업그레이드 검증 (현재 등급 < 대상 등급)
  - [ ] 즉시 적용 로직
  - [ ] 구독 이력 기록
  - [ ] Gherkin 시나리오 작성 및 테스트 통과
- **의존 Epic**: Epic 1, Epic 2 (검증 필요)
- **예상 소요**: 1.5시간

---

## Epic 6: 무료 체험

- **범위**:
  - 포함: 무료 체험 시작, 체험 종료 후 유료 전환
  - 제외: 체험 기간 설정 UI
- **Entity**: FreeTrial (Subscription에 포함 가능)
- **완료 기준 (DoD)**:
  - [ ] POST /api/subscriptions/free-trial API 구현
  - [ ] 무료 체험 상태 관리 (IN_TRIAL)
  - [ ] 체험 종료 시 만료 또는 유료 전환 처리
  - [ ] Gherkin 시나리오 작성 및 테스트 통과
- **의존 Epic**: Epic 1
- **예상 소요**: 1시간

---

## Epic 7: 구독 이력 관리 (Optional)

- **범위**:
  - 포함: 구독 변경 이력 조회
  - 제외: 실시간 알림
- **Entity**: SubscriptionHistory
- **완료 기준 (DoD)**:
  - [ ] GET /api/subscriptions/history API 구현
  - [ ] 이력 Entity 설계 및 저장
  - [ ] Gherkin 시나리오 작성 및 테스트 통과
- **의존 Epic**: Epic 1, Epic 5 (업그레이드 이력)
- **예상 소요**: 1시간

---

## 총 예상 소요

| 우선순위 | Epic | 소요 |
|----------|------|------|
| Must | Epic 1, 2, 3, 4, 5 | 9.5시간 |
| Should | Epic 6 | 1시간 |
| Could | Epic 7 | 1시간 |
| **Total** | | **11.5시간** |
