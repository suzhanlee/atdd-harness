@verification @epic2
Feature: Apple 영수증 검증
  Apple App Store Server API를 통해 영수증을 검증한다.

  Background:
    Given 데이터베이스가 초기화되어 있다
    And Apple Mock 서버가 실행되어 있다

  # ============================================
  # Happy Path
  # ============================================

  @happy @verification
  Scenario: 영수증 검증 성공
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    When 영수증 검증 요청을 보낸다
      | receiptData        |
      | valid_receipt_data |
    Then 상태 코드 200을 받는다
    And 응답의 "valid" 필드는 true이다

  # ============================================
  # Edge Cases - 입력 검증
  # ============================================

  @edge @validation @verification
  Scenario: 빈 영수증 데이터 검증 요청
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    When 영수증 검증 요청을 보낸다
      | receiptData |
      |             |
    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드에 "영수증 데이터가 필요합니다"가 포함된다

  @edge @validation @verification
  Scenario: 잘못된 Base64 형식의 영수증
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    When 영수증 검증 요청을 보낸다
      | receiptData    |
      | not_base64!@#$ |
    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드에 "잘못된 영수증 형식"이 포함된다

  # ============================================
  # Edge Cases - 외부 서비스
  # ============================================

  @edge @external @verification
  Scenario: Apple 서버 연결 실패
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And Apple Mock 서버가 연결 거부을 반환한다
    When 영수증 검증 요청을 보낸다
      | receiptData        |
      | valid_receipt_data |
    Then 상태 코드 503을 받는다
    And 응답의 "error" 필드에 "일시적 오류, 잠시 후 재시도"가 포함된다

  @edge @external @verification
  Scenario: Apple 서버 5xx 응답
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And Apple Mock 서버가 500 오류을 반환한다
    When 영수증 검증 요청을 보낸다
      | receiptData        |
      | valid_receipt_data |
    Then 상태 코드 503을 받는다
    And 3회 재시도 후 실패한다
