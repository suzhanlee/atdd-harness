# Happy Path 시나리오 - Apple IAP Subscription

> 사용자가 직접 Given-When-Then 형식으로 핵심 시나리오를 작성하는 파일입니다.

---

## Feature 1: 구독 구매 (Epic 1)
# 관련 요구사항: M1

Feature: 구독 구매

  Background:
    Given 데이터베이스가 초기화되어 있다

  # ============================================
  # Happy Path (정상 흐름)
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
    Given 다음 구독이 존재한다
      | id | userId | productTier | status  | expiresAt           |
      | 1  | 1      | BASIC       | ACTIVE  | 2026-03-24T00:00:00 |

    When 사용자가 구독 상태 조회 요청을 보낸다

    Then 상태 코드 200을 받는다
    And 응답의 "status" 필드는 "ACTIVE"이다
    And 응답의 "productTier" 필드는 "BASIC"이다

---

## Feature 2: Apple 영수증 검증 (Epic 2)
# 관련 요구사항: M1

Feature: 영수증 검증

  Background:
    Given 데이터베이스가 초기화되어 있다
    And Apple Mock 서버가 실행되어 있다

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

---

## Feature 3: Apple S2S Webhook 수신 (Epic 3)
# 관련 요구사항: M3, M5

Feature: Webhook 수신

  Background:
    Given 데이터베이스가 초기화되어 있다
    And Apple 공개키가 캐싱되어 있다

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
      | transactionId  | signedPayload     |
      | txn_12345      | valid_payload     |

    Then 상태 코드 200을 받는다
    And 중복 처리되지 않는다

---

## Feature 4: Webhook 이벤트 처리 (Epic 4)
# 관련 요구사항: M3, M4, M5

Feature: 구독 갱신 및 만료

  Background:
    Given 데이터베이스가 초기화되어 있다

  @happy @renewal
  Scenario: 구독 갱신 성공 (DID_RENEW)
    Given 다음 구독이 존재한다
      | id | userId | productTier | status  | expiresAt           |
      | 1  | 1      | BASIC       | ACTIVE  | 2026-03-24T00:00:00 |

    When DID_RENEW Webhook 이벤트를 수신한다
      | transactionId | productId | expiresAt           |
      | txn_12345     | basic_001 | 2026-04-24T00:00:00 |

    Then 상태 코드 200을 받는다
    And 구독의 "expiresAt"이 "2026-04-24T00:00:00"로 연장된다

  @happy @expire
  Scenario: 구독 만료 처리 (EXPIRED)
    Given 다음 구독이 존재한다
      | id | userId | productTier | status  | expiresAt           |
      | 1  | 1      | BASIC       | ACTIVE  | 2026-02-24T00:00:00 |

    When EXPIRED Webhook 이벤트를 수신한다
      | transactionId |
      | txn_12345     |

    Then 상태 코드 200을 받는다
    And 구독의 "status"가 "EXPIRED"로 변경된다

  @happy @refund
  Scenario: 환불 처리 (REFUND)
    Given 다음 구독이 존재한다
      | id | userId | productTier | status  | expiresAt           |
      | 1  | 1      | BASIC       | ACTIVE  | 2026-03-24T00:00:00 |

    When REFUND Webhook 이벤트를 수신한다
      | transactionId | refundDate        |
      | txn_12345     | 2026-02-24T00:00:00 |

    Then 상태 코드 200을 받는다
    And 구독의 "status"가 "REFUNDED"로 변경된다
    And 구독 권한이 즉시 회수된다

---

## Feature 5: 구독 업그레이드 (Epic 5)
# 관련 요구사항: M2

Feature: 구독 업그레이드

  Background:
    Given 데이터베이스가 초기화되어 있다

  @happy @upgrade
  Scenario: Basic에서 Pro로 업그레이드
    Given 다음 구독이 존재한다
      | id | userId | productTier | status  |
      | 1  | 1      | BASIC       | ACTIVE  |

    When 업그레이드 요청을 보낸다
      | targetTier |
      | PRO        |

    Then 상태 코드 200을 받는다
    And 응답의 "productTier" 필드는 "PRO"이다
    And 업그레이드가 즉시 적용된다

  @happy @upgrade
  Scenario: Basic에서 Ultra로 업그레이드
    Given 다음 구독이 존재한다
      | id | userId | productTier | status  |
      | 1  | 1      | BASIC       | ACTIVE  |

    When 업그레이드 요청을 보낸다
      | targetTier |
      | ULTRA      |

    Then 상태 코드 200을 받는다
    And 응답의 "productTier" 필드는 "ULTRA"이다

---

## Feature 6: 무료 체험 (Epic 6)
# 관련 요구사항: S6

Feature: 무료 체험

  Background:
    Given 데이터베이스가 초기화되어 있다

  @happy @freetrial
  Scenario: 무료 체험 시작
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 사용자가 기존 구독이 없다

    When 무료 체험 시작 요청을 보낸다
      | productId  |
      | trial_001  |

    Then 상태 코드 201을 받는다
    And 응답의 "status" 필드는 "IN_TRIAL"이다
    And 응답의 "trialEndsAt" 필드가 존재한다

---

## Feature 7: 구독 이력 관리 (Epic 7)
# 관련 요구사항: C8

Feature: 구독 이력 조회

  Background:
    Given 데이터베이스가 초기화되어 있다

  @happy @history
  Scenario: 구독 이력 조회
    Given 다음 구독 이력이 존재한다
      | id | userId | action   | fromTier | toTier | changedAt           |
      | 1  | 1      | UPGRADE  | BASIC    | PRO    | 2026-02-20T00:00:00 |
      | 2  | 1      | RENEW    | PRO      | PRO    | 2026-03-20T00:00:00 |

    When 사용자가 구독 이력 조회 요청을 보낸다

    Then 상태 코드 200을 받는다
    And 응답의 "histories" 배열 크기는 2이다

---

# ============================================
# 추가 Happy Path 시나리오
# ============================================

## Feature 1 추가: 중복 구매 방지 (Must Have)

  @happy @duplicate @purchase
  Scenario: 활성 구독이 있는 사용자의 중복 구매 방지
    Given 다음 구독이 존재한다
      | id | userId | productTier | status  | expiresAt           |
      | 1  | 1      | BASIC       | ACTIVE  | 2026-03-24T00:00:00 |

    When 사용자가 구독 구매 요청을 보낸다
      | productId | receiptData        |
      | pro_001   | base64_receipt_data |

    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드는 "이미 활성 구독이 있습니다"이다

---

## Feature 4 추가: 갱신 실패 및 유예 기간 만료

  @happy @renewal_failed
  Scenario: 갱신 실패 처리 (DID_FAIL_TO_RENEW)
    Given 다음 구독이 존재한다
      | id | userId | productTier | status  | expiresAt           |
      | 1  | 1      | BASIC       | ACTIVE  | 2026-02-24T00:00:00 |

    When DID_FAIL_TO_RENEW Webhook 이벤트를 수신한다
      | transactionId |
      | txn_12345     |

    Then 상태 코드 200을 받는다
    And 구독의 "status"가 "IN_GRACE_PERIOD"로 변경된다

  @happy @grace_period_expired
  Scenario: 유예 기간 만료 처리 (GRACE_PERIOD_EXPIRED)
    Given 다음 구독이 존재한다
      | id | userId | productTier | status        | expiresAt           |
      | 1  | 1      | BASIC       | IN_GRACE_PERIOD | 2026-02-24T00:00:00 |

    When GRACE_PERIOD_EXPIRED Webhook 이벤트를 수신한다
      | transactionId |
      | txn_12345     |

    Then 상태 코드 200을 받는다
    And 구독의 "status"가 "EXPIRED"로 변경된다
    And 구독 권한이 회수된다

---

## Feature 4 추가: 비갱신형 구독 만료 (Must Have)

  @happy @non_renewing_expire
  Scenario: 비갱신형 구독 만료 처리
    Given 다음 비갱신형 구독이 존재한다
      | id | userId | productTier | status  | expiresAt           | isAutoRenewable |
      | 1  | 1      | BASIC       | ACTIVE  | 2026-02-24T00:00:00 | false           |

    When 비갱신형 구독 만료 시간이 지나면

    Then 구독의 "status"가 "EXPIRED"로 변경된다
    And 구독 권한이 자동 회수된다

---

## Feature 6 추가: 무료 체험 종료 후 유료 전환

  @happy @freetrial_convert
  Scenario: 무료 체험 종료 후 유료 구독 전환
    Given 다음 구독이 존재한다
      | id | userId | productTier | status   | trialEndsAt         |
      | 1  | 1      | BASIC       | IN_TRIAL | 2026-02-24T00:00:00 |

    When 무료 체험 종료 후 유료 구독 요청을 보낸다
      | productId | receiptData        |
      | basic_001 | base64_receipt_data |

    Then 상태 코드 200을 받는다
    And 응답의 "status" 필드는 "ACTIVE"이다
    And "trialEndsAt" 필드가 제거된다

---

# ============================================
# Edge Cases (예외 흐름) - Phase B에서 작성
# ============================================
