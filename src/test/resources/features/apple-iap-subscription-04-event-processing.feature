@event @epic4
Feature: Webhook 이벤트 처리
  구독 갱신, 만료, 환불 등 Apple Webhook 이벤트를 처리한다.

  Background:
    Given 데이터베이스가 초기화되어 있다

  # ============================================
  # Happy Path - 갱신
  # ============================================

  @happy @renewal
  Scenario: 구독 갱신 성공 (DID_RENEW)
    Given 다음 구독이 존재한다
      | id | userId | productTier | status | expiresAt           |
      | 1  | 1      | BASIC       | ACTIVE | 2026-03-24T00:00:00 |
    When DID_RENEW Webhook 이벤트를 수신한다
      | transactionId | productId | expiresAt           |
      | txn_12345     | basic_001 | 2026-04-24T00:00:00 |
    Then 상태 코드 200을 받는다
    And 구독의 "expiresAt"이 "2026-04-24T00:00:00"로 연장된다

  # ============================================
  # Happy Path - 만료
  # ============================================

  @happy @expire
  Scenario: 구독 만료 처리 (EXPIRED)
    Given 다음 구독이 존재한다
      | id | userId | productTier | status | expiresAt           |
      | 1  | 1      | BASIC       | ACTIVE | 2026-02-24T00:00:00 |
    When EXPIRED Webhook 이벤트를 수신한다
      | transactionId |
      | txn_12345     |
    Then 상태 코드 200을 받는다
    And 구독의 "status"가 "EXPIRED"로 변경된다

  # ============================================
  # Happy Path - 환불
  # ============================================

  @happy @refund
  Scenario: 환불 처리 (REFUND)
    Given 다음 구독이 존재한다
      | id | userId | productTier | status | expiresAt           |
      | 1  | 1      | BASIC       | ACTIVE | 2026-03-24T00:00:00 |
    When REFUND Webhook 이벤트를 수신한다
      | transactionId | refundDate          |
      | txn_12345     | 2026-02-24T00:00:00 |
    Then 상태 코드 200을 받는다
    And 구독의 "status"가 "REFUNDED"로 변경된다
    And 구독 권한이 즉시 회수된다

  # ============================================
  # Happy Path - 갱신 실패
  # ============================================

  @happy @renewal_failed
  Scenario: 갱신 실패 처리 (DID_FAIL_TO_RENEW)
    Given 다음 구독이 존재한다
      | id | userId | productTier | status | expiresAt           |
      | 1  | 1      | BASIC       | ACTIVE | 2026-02-24T00:00:00 |
    When DID_FAIL_TO_RENEW Webhook 이벤트를 수신한다
      | transactionId |
      | txn_12345     |
    Then 상태 코드 200을 받는다
    And 구독의 "status"가 "IN_GRACE_PERIOD"로 변경된다

  @happy @grace_period_expired
  Scenario: 유예 기간 만료 처리 (GRACE_PERIOD_EXPIRED)
    Given 다음 구독이 존재한다
      | id | userId | productTier | status          | expiresAt           |
      | 1  | 1      | BASIC       | IN_GRACE_PERIOD | 2026-02-24T00:00:00 |
    When GRACE_PERIOD_EXPIRED Webhook 이벤트를 수신한다
      | transactionId |
      | txn_12345     |
    Then 상태 코드 200을 받는다
    And 구독의 "status"가 "EXPIRED"로 변경된다
    And 구독 권한이 회수된다

  # ============================================
  # Happy Path - 비갱신형
  # ============================================

  @happy @non_renewing_expire
  Scenario: 비갱신형 구독 만료 처리
    Given 다음 비갱신형 구독이 존재한다
      | id | userId | productTier | status | expiresAt           | isAutoRenewable |
      | 1  | 1      | BASIC       | ACTIVE | 2026-02-24T00:00:00 | false           |
    When 비갱신형 구독 만료 시간이 지나면
    Then 구독의 "status"가 "EXPIRED"로 변경된다
    And 구독 권한이 자동 회수된다

  # ============================================
  # Edge Cases - 리소스 없음
  # ============================================

  @edge @notfound @event
  Scenario: 존재하지 않는 구독에 대한 갱신 이벤트
    Given 구독이 존재하지 않는다
    When DID_RENEW Webhook 이벤트를 수신한다
      | transactionId | productId | expiresAt           |
      | txn_nonexist  | basic_001 | 2026-04-24T00:00:00 |
    Then 상태 코드 200을 받는다
    And 신규 구독이 자동 생성된다

  @edge @notfound @event
  Scenario: 이미 만료된 구독에 대한 환불 이벤트
    Given 다음 구독이 존재한다
      | id | userId | productTier | status  | expiresAt           |
      | 1  | 1      | BASIC       | EXPIRED | 2026-01-24T00:00:00 |
    When REFUND Webhook 이벤트를 수신한다
      | transactionId |
      | txn_expired   |
    Then 상태 코드 200을 받는다
    And 로그만 기록되고 추가 처리는 하지 않는다

  # ============================================
  # Edge Cases - 비즈니스 규칙
  # ============================================

  @edge @business @event
  Scenario: 업그레이드 진행 중 환불 이벤트 수신
    Given 다음 구독이 존재한다
      | id | userId | productTier | status |
      | 1  | 1      | BASIC       | ACTIVE |
    And 업그레이드 트랜잭션이 진행 중이다
    When REFUND Webhook 이벤트를 수신한다
      | transactionId |
      | txn_12345     |
    Then 환불 이벤트가 우선 처리된다
    And 업그레이드 트랜잭션이 롤백된다
    And 구독의 "status"가 "REFUNDED"로 변경된다

  # ============================================
  # Edge Cases - 경계값
  # ============================================

  @edge @boundary @event
  Scenario: 만료 임박 시간에 갱신 이벤트 수신
    Given 다음 구독이 존재한다
      | id | userId | productTier | status | expiresAt           |
      | 1  | 1      | BASIC       | ACTIVE | 2026-02-24T00:00:01 |
    When DID_RENEW Webhook 이벤트를 수신한다
      | transactionId | productId | expiresAt           |
      | txn_12345     | basic_001 | 2026-03-24T00:00:00 |
    Then 상태 코드 200을 받는다
    And 구독의 "status"가 "ACTIVE"로 유지된다
    And "expiresAt"이 연장된다
