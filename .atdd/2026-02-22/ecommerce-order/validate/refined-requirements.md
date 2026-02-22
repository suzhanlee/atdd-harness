# 개선된 요구사항 (Refined Requirements)

## 프로젝트명
상품 구매 - 재고 차감 - 포인트 결제 플로우

## 비즈니스 목표
- 회원이 재고가 있는 상품을 구매할 수 있다
- 단일 상품 구매와 장바구니 구매 모두 지원한다
- 포인트로 결제하며, 포인트 부족 시 구매가 불가능하다

---

## 기능 요구사항 (MoSCoW)

### Must have

#### 1. 회원 인증
- 회원만 상품 구매 가능
- 인증되지 않은 요청 시 `UNAUTHORIZED` (401)

#### 2. 상품 조회
- `GET /products` - 상품 목록 조회
- `GET /products/{id}` - 상품 상세 조회 (재고 포함)

#### 3. 단일 상품 직접 구매
- `POST /orders/direct`
- Request:
  ```json
  {
    "productId": 1,
    "quantity": 2
  }
  ```
- Response:
  ```json
  {
    "orderId": 1,
    "totalPrice": 10000,
    "status": "COMPLETED"
  }
  ```

#### 4. 장바구니 구매
- `POST /orders/cart`
- Request: (body 없음, 인증된 회원의 장바구니 사용)
- Response:
  ```json
  {
    "orderId": 1,
    "totalPrice": 30000,
    "status": "COMPLETED",
    "items": [
      { "productId": 1, "productName": "상품A", "quantity": 2, "price": 10000 },
      { "productId": 2, "productName": "상품B", "quantity": 1, "price": 10000 }
    ]
  }
  ```

#### 5. 장바구니 관리
- `GET /carts` - 장바구니 조회
- `POST /carts/items` - 장바구니 상품 추가
  ```json
  { "productId": 1, "quantity": 2 }
  ```
- `PATCH /carts/items/{id}` - 수량 변경
  ```json
  { "quantity": 3 }
  ```
- `DELETE /carts/items/{id}` - 상품 삭제

#### 6. 재고 확인 및 차감
- 동시성 제어를 위한 **Pessimistic Lock** 적용
- `@Lock(LockModeType.PESSIMISTIC_WRITE)` 사용
- 재고 부족 시 주문 생성 실패

#### 7. 포인트 결제
- 회원 포인트 잔액 확인
- 포인트 부족 시 전체 취소 (주문 생성 안됨)
- 결제 성공 시 포인트 차감

---

## 예외 케이스 및 에러 응답

| 상황 | HTTP Status | Error Code | Message |
|------|-------------|------------|---------|
| 인증 실패 | 401 | `UNAUTHORIZED` | "로그인이 필요합니다" |
| 상품 없음 | 404 | `PRODUCT_NOT_FOUND` | "상품을 찾을 수 없습니다" |
| 재고 부족 | 400 | `OUT_OF_STOCK` | "재고가 부족합니다" (현재 재고: X개) |
| 포인트 부족 | 400 | `INSUFFICIENT_POINT` | "포인트가 부족합니다" (필요: X, 보유: Y) |
| 장바구니 비어있음 | 400 | `CART_EMPTY` | "장바구니가 비어있습니다" |
| 수량 오류 | 400 | `INVALID_QUANTITY` | "수량은 1 이상이어야 합니다" |

### 에러 응답 형식
```json
{
  "status": 400,
  "code": "OUT_OF_STOCK",
  "message": "재고가 부족합니다",
  "data": {
    "currentStock": 1,
    "requestedQuantity": 5
  }
}
```

---

## 비기능 요구사항

### 동시성
- **Pessimistic Lock** (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) 적용
- 재고 차감 시 다른 트랜잭션 대기
- 데드락 방지를 위한 Lock 획득 순서 통일

### 트랜잭션
- 주문 생성 + 재고 차감 + 포인트 차감은 **원자적 처리**
- `@Transactional` 적용

### 성능
- 구매 요청 응답 시간 **1초 이내**

---

## 기술적 제약
- Java 17+, Spring Boot 3.x, Spring Data JPA
- MySQL
- 동시성 제어: Pessimistic Lock

---

## 도메인 모델

### Member (회원)
- id: Long
- email: String
- point: Long

### Product (상품)
- id: Long
- name: String
- price: Long
- stock: Integer

### Cart (장바구니)
- id: Long
- memberId: Long

### CartItem (장바구니 아이템)
- id: Long
- cartId: Long
- productId: Long
- quantity: Integer

### Order (주문)
- id: Long
- memberId: Long
- totalPrice: Long
- status: OrderStatus (COMPLETED)

### OrderItem (주문 상품)
- id: Long
- orderId: Long
- productId: Long
- quantity: Integer
- price: Long

---

## Won't have (이번 버전)
- 재고 증가 기능 (입고)
- 어드민 기능
- 할인/쿠폰 기능
- 비회원 구매
- 주문 취소/환불
