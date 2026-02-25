# TDD Implementation Plan - Presentation Layer Controllers

## Context
- **Feature**: Apple IAP Subscription with Refund and Upgrade
- **Phase**: TDD - Presentation Layer (Task #3)
- **Date**: 2026-02-25
- **Status**: In Progress

## Scope

### Controllers to Implement
1. **SubscriptionController** - 구독 관리 API
   - POST /api/v1/subscriptions/validate - 영수증 검증
   - GET /api/v1/subscriptions/{id} - 구독 조회
   - GET /api/v1/subscriptions/user/{userId} - 사용자 구독 목록
   - GET /api/v1/subscriptions/{id}/upgrade/quote - 업그레이드 견적
   - POST /api/v1/subscriptions/{id}/upgrade - 구독 업그레이드
   - GET /api/v1/subscriptions/{id}/refund/estimate - 환불 예상 금액

2. **WebhookController** - Apple Webhook 수신
   - POST /api/v1/webhooks/apple - Apple Webhook 수신
   - POST /api/v1/webhooks/apple/health - Health Check

## TDD Implementation Order

### Phase 1: WebhookController (우선 구현)
Webhook은 E2E 테스트의 기반이 되므로 먼저 구현

#### Test Cases (RED)
1. **handleAppleWebhook()**
   - 정상적인 Webhook 수신 및 처리
   - 서명 검증 실패 시나리오
   - 잘못된 페이로드 형식
   - 멱등성 검사 (중복 요청)

2. **healthCheck()**
   - Health Check 정상 응답

#### Implementation (GREEN)
- WebhookRequest → ProcessWebhookUseCase.process() → WebhookResponse
- 예외 처리 및 적절한 HTTP 상태 코드 반환

### Phase 2: SubscriptionController

#### Test Cases (RED)
1. **validateReceipt()**
   - 정상적인 영수증 검증
   - 유효하지 않은 영수증
   - 필수 필드 누락

2. **getSubscription()**
   - 정상적인 구독 조회
   - 존재하지 않는 구독 ID

3. **getUserSubscriptions()**
   - 사용자 구독 목록 조회
   - 활성 구독만 필터링

4. **getUpgradeQuote()**
   - 정상적인 업그레이드 견적 조회
   - 업그레이드 불가능한 경우
   - 존재하지 않는 구독

5. **upgradeSubscription()**
   - 정상적인 업그레이드 처리
   - 잘못된 제품 ID
   - 업그레이드 불가능한 상태

6. **getRefundEstimate()**
   - 정상적인 환불 예상 금액 조회
   - 환불 불가능한 경우

#### Implementation (GREEN)
- 각 엔드포인트별 UseCase 호출
- Request/Response DTO 매핑
- 예외 처리 및 HTTP 상태 코드 매핑

## Test Files to Create

```
src/test/java/com/example/unit/interfaces/controller/
├── WebhookControllerTest.java
└── SubscriptionControllerTest.java
```

## Implementation Files to Modify

```
src/main/java/com/example/subscription/interfaces/controller/
├── WebhookController.java (구현 완료)
└── SubscriptionController.java (구현 완료)
```

## Test Strategy

### Unit Tests (MockMvc + Mockito)
- UseCase Mocking
- HTTP 요청/응답 검증
- 예외 처리 검증
- Request Validation 검증

### Test Coverage Target
- Controller Methods: 100%
- Edge Cases: 모든 예외 시나리오 포함

## Dependencies (Already Implemented)
- ✅ Domain Layer (Entity, VO, Repository)
- ✅ Application Layer (UseCase)
- ✅ Infrastructure Layer (JwsVerifier, AppleClient)

## Expected Test Count
- WebhookController: ~5 test cases
- SubscriptionController: ~15 test cases
- **Total**: ~20 test cases

## Success Criteria
1. 모든 단위 테스트 통과
2. 커버리지 80% 이상
3. MockMvc를 통한 HTTP 계층 테스트 완료
4. 예외 시나리오 100% 커버

## Next Steps (After Controller TDD)
- Task #4: Cucumber E2E 테스트 완성
- Task #5: 최종 검증 및 커버리지 확인
