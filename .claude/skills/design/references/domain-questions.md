# 도메인 질문 가이드

## 목적
Phase A에서 사용자가 도메인에 대해 깊이 있게 생각하도록 유도한다.

---

## 기본 질문 (Entity/Aggregate)

```
Q1: 비즈니스에서 다루는 핵심 "사물"이나 "개념"은 무엇인가요?
    → Entity 후보 식별

Q2: 이 개체들을 어떻게 구분하나요?
    → 식별자(ID) 결정

Q3: 개체 간 어떤 관계가 있나요?
    → 1:1, 1:N, N:M 관계 파악

Q4: 각 개체가 수행하는 핵심 행동은 무엇인가요?
    → 비즈니스 메서드 식별

Q5: 어떤 규칙이 행동을 제약하나요?
    → 불변식(Invariant) 파악

Q6: 어떤 개체들이 함께 생성/수정/삭제되나요?
    → Aggregate 경계 식별
```

---

## 확장 질문 (전체 아키텍처)

```
Q7: 두 개 이상 Entity가 관여하는 로직이 있나요?
    → Domain Service 후보

Q8: 상태 변경 시 다른 시스템/사용자에게 알려야 하나요?
    → Domain Event 후보

Q9: 외부에서 데이터를 받아오나요? 어떤 형식인가요?
    → Parser/Extractor 후보

Q10: 복잡한 비즈니스 규칙이 있나요?
     → Policy/Specification 후보
```

---

## 질문 상세 가이드

### Q1-Q3: 핵심 개체 식별

```markdown
## Q1: 핵심 개체
- 비즈니스 용어에서 명사를 찾으세요
- 사용자가 "관리해야 한다"고 말하는 것들
- 예: 주문, 상품, 회원, 결제

## Q2: 식별자
- 자연키 vs 대리키
- UUID vs Auto-increment
- 복합키 필요성

## Q3: 관계
- 1:1 (프로필-회원)
- 1:N (회원-주문)
- N:M (상품-카테고리)
```

### Q4-Q6: 행동과 규칙

```markdown
## Q4: 핵심 행동
- 상태를 변경하는 메서드
- 예: 주문.취소(), 결제.승인()

## Q5: 불변식
- 항상 참이어야 하는 규칙
- 예: 주문 총액 = 항목별 금액 합계

## Q6: Aggregate 경계
- 함께 생성/수정/삭제되는 단위
- 트랜잭션 일관성 경계
```

### Q7-Q10: 아키텍처 확장

```markdown
## Q7: Domain Service
- 두 개 이상 Entity가 협력하는 로직
- 예: 이체(Account from, Account to)

## Q8: Domain Event
- 상태 변경 후 발생하는 사실
- 예: OrderCreated, PaymentCompleted

## Q9: Parser/Extractor
- CSV, JSON, XML 등 외부 데이터
- 예: CsvProductParser, JsonApiExtractor

## Q10: Policy/Specification
- 복잡한 비즈니스 규칙
- 예: DiscountPolicy, OrderCancelSpecification
```

---

## 도메인 질문 체크리스트

### Phase A 완료 확인

- [ ] 모든 Entity 후보 식별 완료
- [ ] Entity 간 관계 정의 완료
- [ ] 각 Entity의 핵심 행동 1개 이상 식별
- [ ] 주요 불변식 1개 이상 파악
- [ ] Aggregate 경계 제안 완료
- [ ] Domain Service 필요성 검토
- [ ] Domain Event 필요성 검토
- [ ] 외부 데이터 처리 방식 검토
- [ ] 복잡한 정책/규칙 검토

---

## STOP Protocol (Phase A 완료)

```
---
👆 도메인 질문에 답변해주세요.

[질문 목록 제시]

답변 완료 후 "완료" 또는 "다음"이라고 입력해주세요.
Phase B (Blank Architecture)로 진행합니다.
```
