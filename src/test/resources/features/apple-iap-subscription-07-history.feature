@history @epic7
Feature: 구독 이력 관리
  사용자의 구독 변경 이력을 조회한다.

  Background:
    Given 데이터베이스가 초기화되어 있다

  # ============================================
  # Happy Path
  # ============================================

  @happy @history
  Scenario: 구독 이력 조회
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 다음 구독 이력이 존재한다
      | id | userId | action  | fromTier | toTier | changedAt           |
      | 1  | 1      | UPGRADE | BASIC    | PRO    | 2026-02-20T00:00:00 |
      | 2  | 1      | RENEW   | PRO      | PRO    | 2026-03-20T00:00:00 |
    When 사용자가 구독 이력 조회 요청을 보낸다
    Then 상태 코드 200을 받는다
    And 응답의 "histories" 배열 크기는 2이다

  # ============================================
  # Edge Cases - 리소스 없음
  # ============================================

  @edge @notfound @history
  Scenario: 구독 이력이 없는 사용자의 조회
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 구독 이력이 없다
    When 사용자가 구독 이력 조회 요청을 보낸다
    Then 상태 코드 200을 받는다
    And 응답의 "histories" 배열은 비어있다

  # ============================================
  # Edge Cases - 권한/인증
  # ============================================

  @edge @auth @history
  Scenario: 인증되지 않은 사용자의 이력 조회
    Given 사용자가 로그인되어 있지 않다
    When 사용자가 구독 이력 조회 요청을 보낸다
    Then 상태 코드 401을 받는다

  # ============================================
  # Edge Cases - 입력 검증
  # ============================================

  @edge @validation @history
  Scenario: 잘못된 페이지 번호로 이력 조회
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 다음 구독 이력이 존재한다
      | id | userId | action  | fromTier | toTier |
      | 1  | 1      | UPGRADE | BASIC    | PRO    |
    When 페이징으로 구독 이력 조회 요청을 보낸다
      | page | size |
      | -1   | 10   |
    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드에 "유효하지 않은 페이지 번호"가 포함된다

  @edge @validation @history
  Scenario: 너무 큰 페이지 크기로 이력 조회
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    When 페이징으로 구독 이력 조회 요청을 보낸다
      | page | size |
      | 0    | 1000 |
    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드에 "페이지 크기는 100을 초과할 수 없습니다"가 포함된다
