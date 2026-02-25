# Scenario Template

## Basic Feature Template

```gherkin
Feature: [기능명]
  [기능에 대한 간단한 설명]

  Background:
    Given 데이터베이스가 초기화되어 있다
    And [공통 전제조건]

  Scenario: [시나리오명]
    Given [전제조건]
    When [행동]
    Then [결과]
    And [추가 결과]
```

---

## CRUD Feature Template

```gherkin
Feature: [Entity] 관리

  Background:
    Given 데이터베이스가 초기화되어 있다

  # Create
  Scenario: [Entity]를 생성한다
    When [Entity] 생성 요청을 보낸다
      | field1 | field2 | field3 |
      | value1 | value2 | value3 |
    Then 상태 코드 201를 받는다
    And 응답의 "field1" 필드는 "value1"이다

  Scenario: 필수 필드 누락으로 [Entity] 생성 실패
    When [Entity] 생성 요청을 보낸다
      | field1 | field2 |
      | value1 |        |
    Then 상태 코드 400를 받는다

  # Read
  Scenario: [Entity]를 조회한다
    Given 다음 [Entity]가 존재한다
      | id | field1 | field2 |
      | 1  | value1 | value2 |
    When [Entity] 조회 요청을 보낸다: 1
    Then 상태 코드 200를 받는다
    And 응답의 "id" 필드는 1이다

  Scenario: 존재하지 않는 [Entity] 조회 실패
    When [Entity] 조회 요청을 보낸다: 999
    Then 상태 코드 404를 받는다

  # Update
  Scenario: [Entity]를 수정한다
    Given 다음 [Entity]가 존재한다
      | id | field1 | field2 |
      | 1  | value1 | value2 |
    When [Entity] 수정 요청을 보낸다: 1
      | field1 | field2 |
      | new1   | new2   |
    Then 상태 코드 200를 받는다
    And 응답의 "field1" 필드는 "new1"이다

  # Delete
  Scenario: [Entity]를 삭제한다
    Given 다음 [Entity]가 존재한다
      | id | field1 |
      | 1  | value1 |
    When [Entity] 삭제 요청을 보낸다: 1
    Then 상태 코드 204를 받는다

  Scenario: 삭제된 [Entity]는 조회할 수 없다
    Given 다음 [Entity]가 삭제되어 있다
      | id |
      | 1  |
    When [Entity] 조회 요청을 보낸다: 1
    Then 상태 코드 404를 받는다
```

---

## Scenario Outline Template

```gherkin
Feature: [기능명] 데이터 검증

  Background:
    Given 데이터베이스가 초기화되어 있다

  Scenario Outline: 다양한 입력값으로 [Entity]를 생성한다
    When [Entity] 생성 요청을 보낸다
      | field1   | field2     |
      | <field1> | <field2>   |
    Then 상태 코드 <statusCode>를 받는다

    Examples: 유효한 입력
      | field1   | field2   | statusCode |
      | valid1   | valid2   | 201        |
      | another1 | another2 | 201        |

    Examples: 유효하지 않은 입력
      | field1   | field2   | statusCode |
      |          | valid2   | 400        |
      | valid1   |          | 400        |
      | invalid@ | valid2   | 400        |
```

---

## Authentication Feature Template

```gherkin
Feature: 인증 관리

  Background:
    Given 데이터베이스가 초기화되어 있다
    And 다음 사용자가 존재한다
      | id | email         | password     | name |
      | 1  | user@test.com | password123! | 사용자 |

  Scenario: 로그인 성공
    When 로그인 요청을 보낸다
      | email         | password     |
      | user@test.com | password123! |
    Then 상태 코드 200를 받는다
    And 응답에 "accessToken" 필드가 존재한다

  Scenario: 잘못된 비밀번호로 로그인 실패
    When 로그인 요청을 보낸다
      | email         | password    |
      | user@test.com | wrongpass   |
    Then 상태 코드 401를 받는다

  Scenario: 존재하지 않는 이메일로 로그인 실패
    When 로그인 요청을 보낸다
      | email          | password     |
      | nobody@test.com| password123! |
    Then 상태 코드 401를 받는다

  Scenario: 인증이 필요한 API 호출
    When 인증 없이 보호된 API를 호출한다
    Then 상태 코드 401를 받는다

  Scenario: 유효한 토큰으로 보호된 API 호출
    Given 유효한 액세스 토큰이 있다
    When 토큰과 함께 보호된 API를 호출한다
    Then 상태 코드 200를 받는다
```

---

## Business Rule Feature Template

```gherkin
Feature: 주문 관리

  Background:
    Given 데이터베이스가 초기화되어 있다
    And 다음 상품이 존재한다
      | id | name  | price | stock |
      | 1  | 상품A | 10000 | 10    |
      | 2  | 상품B | 20000 | 5     |

  Scenario: 정상적인 주문
    Given 사용자가 로그인되어 있다
    When 주문 생성 요청을 보낸다
      | productId | quantity |
      | 1         | 2        |
    Then 상태 코드 201를 받는다
    And 주문 상태는 "CREATED"이다
    And 상품A 재고가 8개로 감소한다

  Scenario: 재고 부족으로 주문 실패
    Given 사용자가 로그인되어 있다
    When 주문 생성 요청을 보낸다
      | productId | quantity |
      | 1         | 20       |
    Then 상태 코드 400를 받는다
    And 에러 메시지는 "재고가 부족합니다"이다

  Scenario: 주문 취소
    Given 다음 주문이 존재한다
      | id | status  | productId | quantity |
      | 1  | CREATED | 1         | 2        |
    When 주문 취소 요청을 보낸다: 1
    Then 상태 코드 200를 받는다
    And 주문 상태는 "CANCELLED"이다
    And 상품A 재고가 다시 10개가 된다

  Scenario: 배송 완료된 주문은 취소할 수 없다
    Given 다음 주문이 존재한다
      | id | status    |
      | 1  | DELIVERED |
    When 주문 취소 요청을 보낸다: 1
    Then 상태 코드 400를 받는다
    And 에러 메시지는 "이미 배송 완료된 주문은 취소할 수 없습니다"이다
```

---

## Search/Filter Feature Template

```gherkin
Feature: 상품 검색

  Background:
    Given 데이터베이스가 초기화되어 있다
    And 다음 상품이 존재한다
      | id | name     | category | price | status  |
      | 1  | 노트북A  | 전자기기 | 1000000 | ACTIVE |
      | 2  | 노트북B  | 전자기기 | 1500000 | ACTIVE |
      | 3  | 셔츠A    | 의류     | 50000  | ACTIVE |
      | 4  | 노트북C  | 전자기기 | 2000000 | INACTIVE |

  Scenario: 키워드로 검색
    When 상품 검색 요청을 보낸다
      | keyword |
      | 노트북  |
    Then 상태 코드 200를 받는다
    And 검색 결과는 2개이다
    And 첫 번째 결과의 이름에 "노트북"이 포함된다

  Scenario: 카테고리로 필터링
    When 상품 검색 요청을 보낸다
      | category |
      | 전자기기 |
    Then 상태 코드 200를 받는다
    And 검색 결과는 2개이다
    And 모든 결과의 카테고리는 "전자기기"이다

  Scenario: 가격 범위로 필터링
    When 상품 검색 요청을 보낸다
      | minPrice | maxPrice |
      | 100000   | 1000000  |
    Then 상태 코드 200를 받는다
    And 검색 결과의 가격은 100000 이상 1000000 이하이다

  Scenario: 정렬
    When 상품 검색 요청을 보낸다
      | sort     | order |
      | price    | desc  |
    Then 상태 코드 200를 받는다
    And 결과는 가격 내림차순으로 정렬된다

  Scenario: 페이징
    When 상품 검색 요청을 보낸다
      | page | size |
      | 0    | 2    |
    Then 상태 코드 200를 받는다
    And 현재 페이지의 결과는 2개이다
    And 전체 페이지 수는 2이다
```

---

## Best Practices

1. **하나의 시나리오 = 하나의 행동**
2. **비기술자도 이해할 수 있는 언어 사용**
3. **구체적인 데이터 사용 (Data Table)**
4. **Background 활용으로 중복 제거**
5. **Scenario Outline로 데이터 기반 테스트**
6. **Happy Path + Exception Path 모두 작성**
