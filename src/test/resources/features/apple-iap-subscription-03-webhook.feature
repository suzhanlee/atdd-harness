@webhook @epic3
Feature: Apple S2S Webhook 수신
  Apple로부터 S2S Notification V2 Webhook을 수신하고 검증한다.

  Background:
    Given 데이터베이스가 초기화되어 있다
    And Apple 공개키가 캐싱되어 있다

  # ============================================
  # Happy Path
  # ============================================

  @happy @webhook
  Scenario: Webhook 수신 성공
    When Apple Webhook 요청을 보낸다
      | signedPayload                    |
      | valid_jws_signed_payload_base64 |
    Then 상태 코드 200을 받는다

  @happy @idempotency
  Scenario: 멱등성 키 중복 처리
    Given 동일한 transactionId의 Webhook이 이미 처리되었다
      | transactionId |
      | txn_12345     |
    When 동일한 transactionId로 Apple Webhook 요청을 보낸다
      | transactionId | signedPayload |
      | txn_12345     | valid_payload |
    Then 상태 코드 200을 받는다
    And 중복 처리되지 않는다

  # ============================================
  # Edge Cases - 보안 검증
  # ============================================

  @edge @security @webhook
  Scenario: JWS 서명 검증 실패
    Given Apple 공개키가 캐싱되어 있다
    When 서명이 위조된 Apple Webhook 요청을 보낸다
      | signedPayload      |
      | forged_jws_payload |
    Then 상태 코드 401을 받는다
    And 보안 로그가 기록된다

  @edge @security @webhook
  Scenario: 만료된 JWS 토큰
    Given Apple 공개키가 캐싱되어 있다
    When 만료된 JWS 토큰으로 Apple Webhook 요청을 보낸다
      | signedPayload       |
      | expired_jws_payload |
    Then 상태 코드 401을 받는다

  # ============================================
  # Edge Cases - 멱등성
  # ============================================

  @edge @idempotency @webhook
  Scenario: 동일 transactionId 중복 처리 방지
    Given 동일한 transactionId의 Webhook이 이미 처리되었다
      | transactionId |
      | txn_12345     |
    When 동일한 transactionId로 Apple Webhook 요청을 보낸다
      | transactionId | signedPayload |
      | txn_12345     | valid_payload |
    Then 상태 코드 200을 받는다
    And 구독 상태가 변경되지 않는다
    And 중복 처리 로그가 기록된다

  # ============================================
  # Edge Cases - 입력 검증
  # ============================================

  @edge @validation @webhook
  Scenario: signedPayload 누락
    When Apple Webhook 요청을 보낸다
      | signedPayload |
      |               |
    Then 상태 코드 400을 받는다

  @edge @validation @webhook
  Scenario: 지원하지 않는 알림 타입
    Given Apple 공개키가 캐싱되어 있다
    When 지원하지 않는 타입의 Apple Webhook 요청을 보낸다
      | signedPayload            |
      | unsupported_type_payload |
    Then 상태 코드 200을 받는다
    And 이벤트가 무시된다

  # ============================================
  # Edge Cases - 처리 실패
  # ============================================

  @edge @failure @webhook
  Scenario: Webhook 처리 중 예외 발생 시 DLQ 저장
    Given Apple 공개키가 캐싱되어 있다
    And 데이터베이스 연결에 실패한다
    When Apple Webhook 요청을 보낸다
      | signedPayload |
      | valid_payload |
    Then 상태 코드 500을 받는다
    And 실패한 이벤트가 DLQ에 저장된다
