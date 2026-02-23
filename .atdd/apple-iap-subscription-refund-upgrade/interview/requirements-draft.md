# 요구사항 초안

## 프로젝트명
Apple In-App Purchase - Subscription Flow

## 비즈니스 목표
- 애플 인앱 결제의 구독 관련 기능을 구현하여 BM(Business Model) 구축
- API 접근 권한을 관리하는 구독 시스템 구현

## 사용자 페르소나
- **주 사용자**: 앱 사용자
- **페르소나 설명**: 구독 상품(Basic/Pro/Ultra)을 구매하여 API 접근 권한을 얻고자 하는 사용자. 월간 구독, 비갱신형 구독, 무료 체험을 이용할 수 있다.

## 구독 상품 구조

| 상품명 | 권한 레벨 | 설명 |
|--------|-----------|------|
| Basic | 낮음 | 기본 API 접근 |
| Pro | 중간 | 확장 API 접근 |
| Ultra | 높음 | 전체 API 접근 |

## 기능 요구사항

### Must have (필수)

#### 1. 구독 구매
- [ ] 사용자는 애플 인앱 결제로 구독 상품을 구매할 수 있다
- [ ] 구매 완료 시 애플 서버 검증(In-App Receipt Validation)을 수행한다
- [ ] 구매 검증 완료 후 구독 권한을 부여한다
- [ ] 활성 구독이 있는 사용자의 중복 구매를 방지한다

#### 2. 구독 업그레이드
- [ ] 사용자는 현재 구독보다 상위 등급으로 업그레이드할 수 있다
  - Basic → Pro, Basic → Ultra, Pro → Ultra
- [ ] 업그레이드는 즉시 적용된다
- [ ] 다운그레이드는 지원하지 않는다

#### 3. 환불 처리 (Apple Webhook)
- [ ] Apple S2S Notification V2를 통해 환불 이벤트를 수신한다
- [ ] 환불 이벤트 수신 시 해당 구독 권한을 즉시 회수한다

#### 4. 비갱신형 구독 만료
- [ ] 비갱신형 구독 만료 시 권한을 자동 회수한다

#### 5. 자동 갱신 (Apple Webhook)
- [ ] Apple S2S Notification V2를 통해 갱신 이벤트를 수신한다
- [ ] 갱신 성공 시 구독 기간을 연장한다
- [ ] 갱신 실패 시 적절히 처리한다

### Should have (중요)

#### 6. 무료 체험
- [ ] 사용자는 무료 체험을 시작할 수 있다
- [ ] 무료 체험 종료 후 유료 구독으로 전환할 수 있다

#### 7. 구독 상태 조회
- [ ] 사용자는 자신의 현재 구독 상태를 조회할 수 있다
- [ ] 구독 상태: ACTIVE, EXPIRED, CANCELLED, REFUNDED, IN_GRACE_PERIOD

### Could have (선택)

#### 8. 구독 이력 관리
- [ ] 사용자의 구독 변경 이력을 조회할 수 있다

### Won't have (이번 버전 제외)

- [ ] 소모성 상품(Consumable) 관련 로직
- [ ] 프론트엔드 UI
- [ ] 다운그레이드 기능

## 비기능 요구사항

### 보안
- 애플 영수증 검증을 서버 사이드에서 수행 (클라이언트 검증만으로는 부족)
- Webhook 요청의 무결성 검증

### 신뢰성
- Apple S2S Notification 수신 실패 시 재시도 메커니즘
- 멱등성 보장 (동일 알림 중복 처리 방지)

### 확장성
- 새로운 구독 상품 추가가 용이한 구조

## 기술적 제약
- Java 17+, Spring Boot 3.x
- Spring Data JPA, MySQL
- DDD (Domain-Driven Design) 패턴 적용
- Apple App Store Server API / S2S Notification V2 연동

## 용어 정의

| 용어 | 정의 |
|------|------|
| Subscription | 구독 상품 (월간/비갱신형) |
| Auto-Renewable | 자동 갱신 구독 (월간) |
| Non-Renewing | 비갱신형 구독 (만료 시 종료) |
| Upgrade | 상위 등급 구독으로 변경 |
| Entitlement | API 접근 권한 |
| S2S Notification | Apple Server-to-Server Notification V2 |

## 참고 문서
- [Apple App Store Server Notifications V2](https://developer.apple.com/documentation/appstoreservernotifications)
- [Apple In-App Purchase](https://developer.apple.com/documentation/storekit/in-app_purchase)
