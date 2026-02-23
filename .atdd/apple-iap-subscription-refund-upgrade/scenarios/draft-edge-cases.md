# Edge Cases 시나리오 - Apple IAP Subscription

> 예외 흐름 및 경계값 테스트 시나리오

---

## Feature 1: 구독 구매 - Edge Cases

### 입력 검증 실패

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

### 비즈니스 규칙 위반

  @edge @business @purchase
  Scenario: 이미 활성 구독이 있는 사용자의 구매
    Given 다음 구독이 존재한다
      | id | userId | productTier | status  | expiresAt           |
      | 1  | 1      | BASIC       | ACTIVE  | 2026-03-24T00:00:00 |

    When 구독 구매 요청을 보낸다
      | productId | receiptData        |
      | pro_001   | base64_receipt_data |

    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드는 "이미 활성 구독이 있습니다"이다

### 권한/인증 실패

  @edge @auth @purchase
  Scenario: 인증되지 않은 사용자의 구매 요청
    Given 사용자가 로그인되어 있지 않다

    When 구독 구매 요청을 보낸다
      | productId | receiptData        |
      | basic_001 | base64_receipt_data |

    Then 상태 코드 401을 받는다
    And 응답의 "error" 필드는 "인증이 필요합니다"이다

### 외부 서비스 장애

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
    And Apple Mock 서버가 검증 실패를 반환한다

    When 구독 구매 요청을 보낸다
      | productId | receiptData        |
      | basic_001 | invalid_receipt    |

    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드는 "구매 확인 실패, 고객센터 문의"이다

---

## Feature 2: 영수증 검증 - Edge Cases

### 입력 검증 실패

  @edge @validation @verification
  Scenario: 빈 영수증 데이터 검증 요청
    When 영수증 검증 요청을 보낸다
      | receiptData |
      |             |

    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드에 "영수증 데이터가 필요합니다"가 포함된다

  @edge @validation @verification
  Scenario: 잘못된 Base64 형식의 영수증
    When 영수증 검증 요청을 보낸다
      | receiptData        |
      | not_base64!@#$     |

    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드에 "잘못된 영수증 형식"이 포함된다

### 외부 서비스 장애

  @edge @external @verification
  Scenario: Apple 서버 연결 실패
    Given Apple Mock 서버가 연결 거부를 반환한다

    When 영수증 검증 요청을 보낸다
      | receiptData        |
      | valid_receipt_data |

    Then 상태 코드 503을 받는다
    And 응답의 "error" 필드에 "일시적 오류, 잠시 후 재시도"가 포함된다

  @edge @external @verification
  Scenario: Apple 서버 5xx 응답
    Given Apple Mock 서버가 500 오류를 반환한다

    When 영수증 검증 요청을 보낸다
      | receiptData        |
      | valid_receipt_data |

    Then 상태 코드 503을 받는다
    And 3회 재시도 후 실패한다

---

## Feature 3: Webhook 수신 - Edge Cases

### 보안 검증 실패

  @edge @security @webhook
  Scenario: JWS 서명 검증 실패
    Given Apple 공개키가 캐싱되어 있다

    When 서명이 위조된 Apple Webhook 요청을 보낸다
      | signedPayload        |
      | forged_jws_payload   |

    Then 상태 코드 401을 받는다
    And 보안 로그가 기록된다

  @edge @security @webhook
  Scenario: 만료된 JWS 토큰
    Given Apple 공개키가 캐싱되어 있다

    When 만료된 JWS 토큰으로 Apple Webhook 요청을 보낸다
      | signedPayload        |
      | expired_jws_payload  |

    Then 상태 코드 401을 받는다

### 멱등성

  @edge @idempotency @webhook
  Scenario: 동일 transactionId 중복 처리 방지
    Given 동일한 transactionId의 Webhook이 이미 처리되었다
      | transactionId |
      | txn_12345     |

    When 동일한 transactionId로 Apple Webhook 요청을 보낸다
      | transactionId | signedPayload     |
      | txn_12345     | valid_payload     |

    Then 상태 코드 200을 받는다
    And 구독 상태가 변경되지 않는다
    And 중복 처리 로그가 기록된다

### 입력 검증

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
      | signedPayload              |
      | unsupported_type_payload   |

    Then 상태 코드 200을 받는다
    And 이벤트가 무시된다

### 처리 실패

  @edge @failure @webhook
  Scenario: Webhook 처리 중 예외 발생 시 DLQ 저장
    Given Apple 공개키가 캐싱되어 있다
    And 데이터베이스 연결에 실패한다

    When Apple Webhook 요청을 보낸다
      | signedPayload     |
      | valid_payload     |

    Then 상태 코드 500을 받는다
    And 실패한 이벤트가 DLQ에 저장된다

---

## Feature 4: 이벤트 처리 - Edge Cases

### 리소스 없음

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

### 비즈니스 규칙

  @edge @business @event
  Scenario: 업그레이드 진행 중 환불 이벤트 수신
    Given 다음 구독이 존재한다
      | id | userId | productTier | status  |
      | 1  | 1      | BASIC       | ACTIVE  |
    And 업그레이드 트랜잭션이 진행 중이다

    When REFUND Webhook 이벤트를 수신한다
      | transactionId |
      | txn_12345     |

    Then 환불 이벤트가 우선 처리된다
    And 업그레이드 트랜잭션이 롤백된다
    And 구독의 "status"가 "REFUNDED"로 변경된다

### 경계값

  @edge @boundary @event
  Scenario: 만료 임박 시간에 갱신 이벤트 수신
    Given 다음 구독이 존재한다
      | id | userId | productTier | status  | expiresAt           |
      | 1  | 1      | BASIC       | ACTIVE  | 2026-02-24T00:00:01 |

    When DID_RENEW Webhook 이벤트를 수신한다
      | transactionId | productId | expiresAt           |
      | txn_12345     | basic_001 | 2026-03-24T00:00:00 |

    Then 상태 코드 200을 받는다
    And 구독의 "status"가 "ACTIVE"로 유지된다
    And "expiresAt"이 연장된다

---

## Feature 5: 구독 업그레이드 - Edge Cases

### 비즈니스 규칙 위반

  @edge @business @upgrade
  Scenario: 동일 등급으로 업그레이드 요청
    Given 다음 구독이 존재한다
      | id | userId | productTier | status  |
      | 1  | 1      | PRO         | ACTIVE  |

    When 업그레이드 요청을 보낸다
      | targetTier |
      | PRO        |

    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드는 "동일 등급으로 업그레이드할 수 없습니다"이다

  @edge @business @upgrade
  Scenario: 다운그레이드 요청 (Pro → Basic)
    Given 다음 구독이 존재한다
      | id | userId | productTier | status  |
      | 1  | 1      | PRO         | ACTIVE  |

    When 업그레이드 요청을 보낸다
      | targetTier |
      | BASIC      |

    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드는 "다운그레이드는 지원하지 않습니다"이다

  @edge @business @upgrade
  Scenario: Ultra에서 Pro로 다운그레이드 시도
    Given 다음 구독이 존재한다
      | id | userId | productTier | status  |
      | 1  | 1      | ULTRA       | ACTIVE  |

    When 업그레이드 요청을 보낸다
      | targetTier |
      | PRO        |

    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드는 "다운그레이드는 지원하지 않습니다"이다

### 리소스 없음

  @edge @notfound @upgrade
  Scenario: 활성 구독 없이 업그레이드 요청
    Given 사용자의 활성 구독이 없다

    When 업그레이드 요청을 보낸다
      | targetTier |
      | PRO        |

    Then 상태 코드 404을 받는다
    And 응답의 "error" 필드는 "활성 구독을 찾을 수 없습니다"이다

  @edge @notfound @upgrade
  Scenario: 만료된 구독으로 업그레이드 요청
    Given 다음 구독이 존재한다
      | id | userId | productTier | status  |
      | 1  | 1      | BASIC       | EXPIRED |

    When 업그레이드 요청을 보낸다
      | targetTier |
      | PRO        |

    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드는 "만료된 구독은 업그레이드할 수 없습니다"이다

### 권한/인증

  @edge @auth @upgrade
  Scenario: 인증되지 않은 사용자의 업그레이드 요청
    Given 사용자가 로그인되어 있지 않다

    When 업그레이드 요청을 보낸다
      | targetTier |
      | PRO        |

    Then 상태 코드 401을 받는다

### 입력 검증

  @edge @validation @upgrade
  Scenario: 유효하지 않은 targetTier 값
    Given 다음 구독이 존재한다
      | id | userId | productTier | status  |
      | 1  | 1      | BASIC       | ACTIVE  |

    When 업그레이드 요청을 보낸다
      | targetTier |
      | INVALID    |

    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드에 "유효하지 않은 등급"이 포함된다

---

## Feature 6: 무료 체험 - Edge Cases

### 비즈니스 규칙 위반

  @edge @business @freetrial
  Scenario: 이미 활성 구독이 있는 사용자의 무료 체험 요청
    Given 다음 구독이 존재한다
      | id | userId | productTier | status  |
      | 1  | 1      | BASIC       | ACTIVE  |

    When 무료 체험 시작 요청을 보낸다
      | productId  |
      | trial_001  |

    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드는 "이미 활성 구독이 있습니다"이다

  @edge @business @freetrial
  Scenario: 이미 무료 체험을 사용한 사용자의 재요청
    Given 사용자가 기존에 무료 체험을 사용했다

    When 무료 체험 시작 요청을 보낸다
      | productId  |
      | trial_001  |

    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드는 "무료 체험은 1회만 가능합니다"이다

### 권한/인증

  @edge @auth @freetrial
  Scenario: 인증되지 않은 사용자의 무료 체험 요청
    Given 사용자가 로그인되어 있지 않다

    When 무료 체험 시작 요청을 보낸다
      | productId  |
      | trial_001  |

    Then 상태 코드 401을 받는다

### 입력 검증

  @edge @validation @freetrial
  Scenario: 유효하지 않은 무료 체험 상품 ID
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |

    When 무료 체험 시작 요청을 보낸다
      | productId  |
      | invalid    |

    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드에 "유효하지 않은 상품 ID"가 포함된다

### 경계값

  @edge @boundary @freetrial
  Scenario: 무료 체험 종료 직전 유료 전환
    Given 다음 구독이 존재한다
      | id | userId | productTier | status   | trialEndsAt         |
      | 1  | 1      | BASIC       | IN_TRIAL | 2026-02-24T00:00:01 |

    When 무료 체험 종료 후 유료 구독 요청을 보낸다
      | productId | receiptData        |
      | basic_001 | base64_receipt_data |

    Then 상태 코드 200을 받는다
    And 응답의 "status" 필드는 "ACTIVE"이다

---

## Feature 7: 구독 이력 관리 - Edge Cases

### 리소스 없음

  @edge @notfound @history
  Scenario: 구독 이력이 없는 사용자의 조회
    Given 사용자가 로그인되어 있다
      | userId | email            |
      | 1      | user@example.com |
    And 구독 이력이 없다

    When 사용자가 구독 이력 조회 요청을 보낸다

    Then 상태 코드 200을 받는다
    And 응답의 "histories" 배열은 비어있다

### 권한/인증

  @edge @auth @history
  Scenario: 인증되지 않은 사용자의 이력 조회
    Given 사용자가 로그인되어 있지 않다

    When 사용자가 구독 이력 조회 요청을 보낸다

    Then 상태 코드 401을 받는다

### 입력 검증

  @edge @validation @history
  Scenario: 잘못된 페이지 번호로 이력 조회
    Given 다음 구독 이력이 존재한다
      | id | userId | action   | fromTier | toTier |
      | 1  | 1      | UPGRADE  | BASIC    | PRO    |

    When 사용자가 구독 이력 조회 요청을 보낸다
      | page | size |
      | -1   | 10   |

    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드에 "유효하지 않은 페이지 번호"가 포함된다

  @edge @validation @history
  Scenario: 너무 큰 페이지 크기로 이력 조회
    Given 사용자가 로그인되어 있다

    When 사용자가 구독 이력 조회 요청을 보낸다
      | page | size |
      | 0    | 1000 |

    Then 상태 코드 400을 받는다
    And 응답의 "error" 필드에 "페이지 크기는 100을 초과할 수 없습니다"가 포함된다

---

## Edge Cases 요약

| Feature | 시나리오 수 |
|---------|------------|
| 구독 구매 | 6 |
| 영수증 검증 | 5 |
| Webhook 수신 | 7 |
| 이벤트 처리 | 5 |
| 구독 업그레이드 | 7 |
| 무료 체험 | 5 |
| 구독 이력 관리 | 4 |
| **Total** | **39** |
