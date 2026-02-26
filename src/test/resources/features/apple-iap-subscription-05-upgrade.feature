@upgrade @epic5
Feature: 구독 업그레이드
  사용자가 구독 등급을 업그레이드한다.

  Background:
    Given 데이터베이스가 초기화되어 있다

  # ============================================
  # Happy Path
  # ============================================

  @happy @upgrade
  Scenario: Basic에서 Pro로 업그레이드
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 다음 구독이 존재한다
      | id | userId | productTier | status |
      | 1  | 1      | BASIC       | ACTIVE |
    When 업그레이드 요청을 보낸다
      | targetTier |
      | PRO        |
    Then 상태 코드 200을 받는다
    And 응답의 "productTier" 필드는 "PRO"이다
    And 업그레이드가 즉시 적용된다

  @happy @upgrade
  Scenario: Basic에서 Ultra로 업그레이드
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 다음 구독이 존재한다
      | id | userId | productTier | status |
      | 1  | 1      | BASIC       | ACTIVE |
    When 업그레이드 요청을 보낸다
      | targetTier |
      | ULTRA      |
    Then 상태 코드 200을 받는다
    And 응답의 "productTier" 필드는 "ULTRA"이다

  @happy @upgrade
  Scenario: Pro에서 Ultra로 업그레이드
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 다음 구독이 존재한다
      | id | userId | productTier | status |
      | 1  | 1      | PRO         | ACTIVE |
    When 업그레이드 요청을 보낸다
      | targetTier |
      | ULTRA      |
    Then 상태 코드 200을 받는다
    And 응답의 "productTier" 필드는 "ULTRA"이다

  # ============================================
  # Edge Cases - 비즈니스 규칙
  # ============================================

  @edge @business @upgrade
  Scenario: 동일 등급으로 업그레이드 요청
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 다음 구독이 존재한다
      | id | userId | productTier | status |
      | 1  | 1      | PRO         | ACTIVE |
    When 업그레이드 요청을 보낸다
      | targetTier |
      | PRO        |
    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드는 "동일 등급으로 업그레이드할 수 없습니다"이다

  @edge @business @upgrade
  Scenario: 다운그레이드 요청 (Pro → Basic)
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 다음 구독이 존재한다
      | id | userId | productTier | status |
      | 1  | 1      | PRO         | ACTIVE |
    When 업그레이드 요청을 보낸다
      | targetTier |
      | BASIC      |
    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드는 "다운그레이드는 지원하지 않습니다"이다

  @edge @business @upgrade
  Scenario: Ultra에서 Pro로 다운그레이드 시도
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 다음 구독이 존재한다
      | id | userId | productTier | status |
      | 1  | 1      | ULTRA       | ACTIVE |
    When 업그레이드 요청을 보낸다
      | targetTier |
      | PRO        |
    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드는 "다운그레이드는 지원하지 않습니다"이다

  # ============================================
  # Edge Cases - 리소스 없음
  # ============================================

  @edge @notfound @upgrade
  Scenario: 활성 구독 없이 업그레이드 요청
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 사용자의 활성 구독이 없다
    When 업그레이드 요청을 보낸다
      | targetTier |
      | PRO        |
    Then 상태 코드 404을 받는다
    And 응답의 "error" 필드는 "활성 구독을 찾을 수 없습니다"이다

  @edge @notfound @upgrade
  Scenario: 만료된 구독으로 업그레이드 요청
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 다음 구독이 존재한다
      | id | userId | productTier | status  |
      | 1  | 1      | BASIC       | EXPIRED |
    When 업그레이드 요청을 보낸다
      | targetTier |
      | PRO        |
    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드는 "만료된 구독은 업그레이드할 수 없습니다"이다

  # ============================================
  # Edge Cases - 권한/인증
  # ============================================

  @edge @auth @upgrade
  Scenario: 인증되지 않은 사용자의 업그레이드 요청
    Given 사용자가 로그인되어 있지 않다
    When 업그레이드 요청을 보낸다
      | targetTier |
      | PRO        |
    Then 상태 코드 401을 받는다

  # ============================================
  # Edge Cases - 입력 검증
  # ============================================

  @edge @validation @upgrade
  Scenario: 유효하지 않은 targetTier 값
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 다음 구독이 존재한다
      | id | userId | productTier | status |
      | 1  | 1      | BASIC       | ACTIVE |
    When 업그레이드 요청을 보낸다
      | targetTier |
      | INVALID    |
    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드에 "유효하지 않은 등급"이 포함된다
