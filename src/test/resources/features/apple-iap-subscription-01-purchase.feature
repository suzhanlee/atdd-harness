@purchase @epic1
Feature: 구독 구매 및 상태 조회
  사용자가 Apple IAP를 통해 구독을 구매하고 상태를 조회한다.

  Background:
    Given 데이터베이스가 초기화되어 있다

  # ============================================
  # Happy Path
  # ============================================

  @happy @purchase
  Scenario: 정상적인 구독 구매
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    When 구독 구매 요청을 보낸다
      | productId | receiptData        |
      | basic_001 | base64_receipt_data |
    Then 상태 코드 201을 받는다
    And 응답의 "status" 필드는 "ACTIVE"이다
    And 응답의 "productTier" 필드는 "BASIC"이다

  @happy @status
  Scenario: 구독 상태 조회
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 다음 구독이 존재한다
      | id | userId | productTier | status  | expiresAt           |
      | 1  | 1      | BASIC       | ACTIVE  | 2026-03-24T00:00:00 |
    When 사용자가 구독 상태 조회 요청을 보낸다
    Then 상태 코드 200을 받는다
    And 응답의 "status" 필드는 "ACTIVE"이다
    And 응답의 "productTier" 필드는 "BASIC"이다

  @happy @duplicate @purchase
  Scenario: 활성 구독이 있는 사용자의 중복 구매 방지
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 다음 구독이 존재한다
      | id | userId | productTier | status  | expiresAt           |
      | 1  | 1      | BASIC       | ACTIVE  | 2026-03-24T00:00:00 |
    When 사용자가 구독 구매 요청을 보낸다
      | productId | receiptData        |
      | pro_001   | base64_receipt_data |
    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드는 "이미 활성 구독이 있습니다"이다

  # ============================================
  # Edge Cases - 입력 검증
  # ============================================

  @edge @validation @purchase
  Scenario: 영수증 데이터가 누락된 구매 요청
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    When 구독 구매 요청을 보낸다
      | productId | receiptData |
      | basic_001 |             |
    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드에 "영수증 데이터가 필요합니다"가 포함된다

  @edge @validation @purchase
  Scenario: 유효하지 않은 productId로 구매 요청
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    When 구독 구매 요청을 보낸다
      | productId | receiptData        |
      | invalid   | base64_receipt_data |
    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드에 "유효하지 않은 상품 ID"가 포함된다

  # ============================================
  # Edge Cases - 비즈니스 규칙
  # ============================================

  @edge @business @purchase
  Scenario: 이미 활성 구독이 있는 사용자의 구매
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 다음 구독이 존재한다
      | id | userId | productTier | status  | expiresAt           |
      | 1  | 1      | BASIC       | ACTIVE  | 2026-03-24T00:00:00 |
    When 구독 구매 요청을 보낸다
      | productId | receiptData        |
      | pro_001   | base64_receipt_data |
    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드는 "이미 활성 구독이 있습니다"이다

  # ============================================
  # Edge Cases - 권한/인증
  # ============================================

  @edge @auth @purchase
  Scenario: 인증되지 않은 사용자의 구매 요청
    Given 사용자가 로그인되어 있지 않다
    When 구독 구매 요청을 보낸다
      | productId | receiptData        |
      | basic_001 | base64_receipt_data |
    Then 상태 코드 401을 받는다
    And 응답의 "error" 필드는 "인증이 필요합니다"이다

  # ============================================
  # Edge Cases - 외부 서비스
  # ============================================

  @edge @external @purchase
  Scenario: Apple 서버 타임아웃 시 재시도
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And Apple Mock 서버가 타임아웃을 반환한다
    When 구독 구매 요청을 보낸다
      | productId | receiptData        |
      | basic_001 | base64_receipt_data |
    Then 상태 코드 503을 받는다
    And 응답의 "error" 필드에 "일시적 오류"가 포함된다
    And 재시도가 3회 수행된다

  @edge @external @purchase
  Scenario: Apple 영수증 검증 실패
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And Apple Mock 서버가 검증 실패을 반환한다
    When 구독 구매 요청을 보낸다
      | productId | receiptData  |
      | basic_001 | invalid_receipt |
    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드는 "구매 확인 실패, 고객센터 문의"이다
