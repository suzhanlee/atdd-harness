# 요구사항 초안

## 프로젝트명
상품 구매 - 재고 차감 - 포인트 결제 플로우

## 비즈니스 목표
- 회원이 재고가 있는 상품을 구매할 수 있다
- 단일 상품 구매와 장바구니 구매 모두 지원한다
- 포인트로 결제하며, 포인트 부족 시 구매가 불가능하다

## 사용자 페르소나
- **주 사용자**: 이커머스 상품을 구매하려는 회원
- **페르소나 설명**: 간편한 구매 경험을 원하며, 재고 부족이나 포인트 부족 시 명확한 실패 안내를 기대함

## 기능 요구사항

### Must have
- [ ] 회원만 상품 구매 가능 (인증 필수)
- [ ] 단일 상품 직접 구매 API
- [ ] 장바구니 구매 API
- [ ] 재고 확인 및 차감 (동시성 제어를 위한 Lock 적용)
- [ ] 포인트 결제 (잔액 부족 시 전체 취소)
- [ ] 장바구니 상품 추가/수량 조절/삭제
- [ ] 장바구니 영구 저장 (DB)

### Should have
- [ ] 구매 이력 조회
- [ ] 상품 조회

### Could have
- [ ] 장바구니 담기 전 재고 미리보기

### Won't have (이번 버전)
- [ ] 재고 증가 기능 (입고)
- [ ] 어드민 기능
- [ ] 할인/쿠폰 기능
- [ ] 비회원 구매

## 비기능 요구사항
- **동시성**: 동일 상품 동시 구매 시 재고 정합성 보장 (Lock 활용)
- **트랜잭션**: 주문 생성, 재고 차감, 포인트 차감은 원자적 처리
- **성능**: 구매 요청 응답 시간 1초 이내

## 기술적 제약
- Java 17+, Spring Boot 3.x, Spring Data JPA
- MySQL
- 동시성 제어를 위한 적절한 Lock 전략 필요

## 도메인 모델 (예상)
- Member (회원): id, email, point
- Product (상품): id, name, price, stock
- Cart (장바구니): id, member_id
- CartItem (장바구니 아이템): id, cart_id, product_id, quantity
- Order (주문): id, member_id, total_price, status
- OrderItem (주문 상품): id, order_id, product_id, quantity, price

## API (예상)
- `POST /orders/direct` - 단일 상품 직접 구매
- `POST /orders/cart` - 장바구니 구매
- `GET /carts` - 장바구니 조회
- `POST /carts/items` - 장바구니 상품 추가
- `PATCH /carts/items/{id}` - 장바구니 수량 변경
- `DELETE /carts/items/{id}` - 장바구니 상품 삭제
