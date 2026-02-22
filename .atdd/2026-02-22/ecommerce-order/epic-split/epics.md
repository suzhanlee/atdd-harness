# Epic 목록

## Epic 1: 상품 조회
- **범위**:
  - 포함: 상품 목록 조회, 상품 상세 조회 (재고 포함)
  - 제외: 재고 차감 (Epic 3에서 처리)
- **Entity**: Product
- **API**:
  - `GET /products` - 상품 목록
  - `GET /products/{id}` - 상품 상세
- **완료 기준 (DoD)**:
  - [ ] Product Entity 구현
  - [ ] 상품 목록 조회 API 구현
  - [ ] 상품 상세 조회 API 구현 (재고 포함)
  - [ ] Gherkin 시나리오 작성 및 테스트 통과
- **의존 Epic**: 없음
- **예상 소요**: 1시간

---

## Epic 2: 장바구니 관리
- **범위**:
  - 포함: 장바구니 조회, 상품 추가, 수량 변경, 상품 삭제
  - 제외: 장바구니 구매 (Epic 4)
- **Entity**: Cart, CartItem
- **API**:
  - `GET /carts` - 장바구니 조회
  - `POST /carts/items` - 상품 추가
  - `PATCH /carts/items/{id}` - 수량 변경
  - `DELETE /carts/items/{id}` - 상품 삭제
- **완료 기준 (DoD)**:
  - [ ] Cart, CartItem Entity 구현
  - [ ] 장바구니 CRUD API 구현
  - [ ] 인증된 회원만 접근 가능
  - [ ] Gherkin 시나리오 작성 및 테스트 통과
- **의존 Epic**: 없음 (Member 인증은 기본 전제)
- **예상 소요**: 1시간

---

## Epic 3: 단일 상품 직접 구매
- **범위**:
  - 포함: 단일 상품 직접 구매, 재고 확인/차감, 포인트 결제
  - 제외: 장바구니 구매 (Epic 4)
- **Entity**: Order, OrderItem (Member, Product 재사용)
- **API**:
  - `POST /orders/direct` - 단일 상품 직접 구매
- **비즈니스 로직**:
  - 재고 확인 (Pessimistic Lock)
  - 재고 차감
  - 포인트 잔액 확인
  - 포인트 차감
  - 주문 생성
- **완료 기준 (DoD)**:
  - [ ] Order, OrderItem Entity 구현
  - [ ] 직접 구매 API 구현
  - [ ] Pessimistic Lock 적용 (재고)
  - [ ] 원자적 트랜잭션 처리
  - [ ] 예외 케이스 처리 (재고 부족, 포인트 부족)
  - [ ] Gherkin 시나리오 작성 및 테스트 통과
- **의존 Epic**: 없음 (Member, Product는 기본 전제)
- **예상 소요**: 1.5시간

---

## Epic 4: 장바구니 구매
- **범위**:
  - 포함: 장바구니 구매, 장바구니 비우기
  - 제외: 장바구니 관리 (Epic 2)
- **Entity**: Order, OrderItem (Cart, CartItem 재사용)
- **API**:
  - `POST /orders/cart` - 장바구니 구매
- **비즈니스 로직**:
  - 장바구니 아이템 순회
  - 각 상품 재고 확인/차감 (Pessimistic Lock)
  - 포인트 잔액 확인
  - 포인트 차감
  - 주문 생성 (여러 OrderItem)
  - 장바구니 비우기
- **완료 기준 (DoD)**:
  - [ ] 장바구니 구매 API 구현
  - [ ] 다중 상품 재고 차감 처리
  - [ ] 데드락 방지 (Lock 획득 순서 통일)
  - [ ] 장바구니 비우기 처리
  - [ ] 예외 케이스 처리 (장바구니 비어있음)
  - [ ] Gherkin 시나리오 작성 및 테스트 통과
- **의존 Epic**: Epic 2 (장바구니), Epic 3 (주문 로직 재사용)
- **예상 소요**: 1시간

---

## 요약

| Epic | 제목 | Entity | 의존성 | 예상 소요 |
|------|------|--------|--------|----------|
| 1 | 상품 조회 | Product | - | 1h |
| 2 | 장바구니 관리 | Cart, CartItem | - | 1h |
| 3 | 단일 상품 직접 구매 | Order, OrderItem | - | 1.5h |
| 4 | 장바구니 구매 | Order, OrderItem | Epic 2, 3 | 1h |

**총 예상 소요**: 4.5시간
