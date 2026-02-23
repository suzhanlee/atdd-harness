# 인터뷰 로그

## 일시
2026-02-23

## Topic
apple-iap-subscription-refund-upgrade

---

## Phase별 기록

### Phase A: Blank Canvas

**사용자 작성 내용:**
- 프로젝트 명: 애플 인앱 결제 - 구독 플로우
- 비즈니스 목표: 애플 인앱 결제의 구독 관련 기능들을 만들어 BM을 구축
- 사용자 페르소나: 갱신/비갱신형 구독 상품을 구매/환불/업그레이드/자동 갱신 하고 싶은 사용자
- 핵심 기능:
  - 애플 인앱 결제
  - 구독 상품 업그레이드 시 자동 권한 부여
  - 구독 상품 환불
  - 갱신 상품 자동 갱신
- 기술적 제약: java/spring/jpa/ddd
- 제외: 소모성 상품, 프론트엔드

### Phase B: Clarification

**Q1: 업그레이드 적용 시점**
- 답변: **즉시 적용**
- 추가: 업그레이드 = 상위 버전으로 (더 많은 API 접근 권한)

**Q2: 환불 시 권한 처리**
- 답변: **즉시 회수**
- 추가: Apple에서 환불 시 webhook event를 받은 경우만 처리

**Q3: 자동 갱신 감지 방식**
- 답변: **Apple S2S Notification**

**Q4: 구독 상품 유형**
- 답변: 월간 구독, 비갱신형 구독, 무료 체험
- 추가: 상품 등급 = Basic / Pro / Ultra

**추가 명확화 내용:**
- 다운그레이드: 지원하지 않음
- 비갱신형: 만료(expired) 시 권한 회수
- 재구매 방지: 활성 구독 시 중복 구매 차단
- 권한: API 접근 권한

### Phase D: Self-Review

| 항목 | 점수 |
|------|------|
| 완전성 | 5 |
| 구체성 | 4 |
| 일관성 | 5 |
| 실현 가능성 | 5 |
| 명확성 | 4 |

**총점**: 23/25 (등급: A)

---

## 산출물
- `.atdd/apple-iap-subscription-refund-upgrade/interview/requirements-draft.md`
