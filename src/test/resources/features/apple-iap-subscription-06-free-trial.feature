@freetrial @epic6
Feature: 무료 체험
  사용자가 무료 체험을 시작하고 유료 구독으로 전환한다.

  Background:
    Given 데이터베이스가 초기화되어 있다

  # ============================================
  # Happy Path
  # ============================================

  @happy @freetrial
  Scenario: 무료 체험 시작
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 사용자가 기존 구독이 없다
    When 무료 체험 시작 요청을 보낸다
      | productId |
      | trial_001 |
    Then 상태 코드 201을 받는다
    And 응답의 "status" 필드는 "IN_TRIAL"이다
    And 응답의 "trialEndsAt" 필드가 존재한다

  @happy @freetrial_convert
  Scenario: 무료 체험 종료 후 유료 구독 전환
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 다음 구독이 존재한다
      | id | userId | productTier | status   | trialEndsAt         |
      | 1  | 1      | BASIC       | IN_TRIAL | 2026-02-24T00:00:00 |
    When 무료 체험 종료 후 유료 구독 요청을 보낸다
      | productId | receiptData        |
      | basic_001 | base64_receipt_data |
    Then 상태 코드 200을 받는다
    And 응답의 "status" 필드는 "ACTIVE"이다
    And "trialEndsAt" 필드가 제거된다

  # ============================================
  # Edge Cases - 비즈니스 규칙
  # ============================================

  @edge @business @freetrial
  Scenario: 이미 활성 구독이 있는 사용자의 무료 체험 요청
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 다음 구독이 존재한다
      | id | userId | productTier | status |
      | 1  | 1      | BASIC       | ACTIVE |
    When 무료 체험 시작 요청을 보낸다
      | productId |
      | trial_001 |
    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드는 "이미 활성 구독이 있습니다"이다

  @edge @business @freetrial
  Scenario: 이미 무료 체험을 사용한 사용자의 재요청
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 사용자가 기존에 무료 체험을 사용했다
    When 무료 체험 시작 요청을 보낸다
      | productId |
      | trial_001 |
    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드는 "무료 체험은 1회만 가능합니다"이다

  # ============================================
  # Edge Cases - 권한/인증
  # ============================================

  @edge @auth @freetrial
  Scenario: 인증되지 않은 사용자의 무료 체험 요청
    Given 사용자가 로그인되어 있지 않다
    When 무료 체험 시작 요청을 보낸다
      | productId |
      | trial_001 |
    Then 상태 코드 401을 받는다

  # ============================================
  # Edge Cases - 입력 검증
  # ============================================

  @edge @validation @freetrial
  Scenario: 유효하지 않은 무료 체험 상품 ID
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    When 무료 체험 시작 요청을 보낸다
      | productId |
      | invalid   |
    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드에 "유효하지 않은 상품 ID"가 포함된다

  # ============================================
  # Edge Cases - 경계값
  # ============================================

  @edge @boundary @freetrial
  Scenario: 무료 체험 종료 직전 유료 전환
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 다음 구독이 존재한다
      | id | userId | productTier | status   | trialEndsAt         |
      | 1  | 1      | BASIC       | IN_TRIAL | 2026-02-24T00:00:01 |
    When 무료 체험 종료 후 유료 구독 요청을 보낸다
      | productId | receiptData        |
      | basic_001 | base64_receipt_data |
    Then 상태 코드 200을 받는다
    And 응답의 "status" 필드는 "ACTIVE"이다
