# Design Validation Guide

## 개요

Phase 2.5 Design 단계에서 수행하는 두 가지 검증에 대한 상세 가이드.

1. **요구사항-도메인 매핑 검증** (Step 8)
2. **SQL Sample Data 검증** (Step 10)

---

## 1. 요구사항-도메인 매핑 검증 (Step 8)

### 목적
요구사항의 각 항목이 Entity의 메서드 또는 Value Object로 올바르게 매핑되었는지 확인.

### 검증 절차

```
1. refined-requirements.md 로드
   └─▶ MoSCoW 분류된 요구사항 목록 추출

2. Must Have 요구사항 분석
   ├─▶ 각 요구사항을 Entity 메서드/VO에 매핑
   └─▶ 매핑 누락 항목 식별

3. Should Have 요구사항 분석
   ├─▶ 각 요구사항을 Entity 메서드/VO에 매핑
   └─▶ 매핑 누락 항목 식별

4. 검증 규칙 확인
   ├─▶ @NotNull → NOT NULL 제약조건
   ├─▶ @Size → 길이 제한
   ├─▶ @Email → 이메일 형식
   └─▶ 불변식 → 비즈니스 규칙 검증 코드

5. traceability-matrix.md 생성
   └─▶ 요구사항-도메인 매핑 매트릭스
```

### 매핑 예시

| 요구사항 | Entity | 메서드/VO | 검증 코드 |
|----------|--------|-----------|-----------|
| 회원가입 | User | `User.register()` | `Email.of()` 검증 |
| 이메일 인증 | User | `User.verifyEmail()` | 상태 전이 검증 |
| 로그인 | - | Service 레이어 | - |
| 비밀번호 변경 | User | `User.changePassword()` | 현재 비밀번호 검증 |
| 회원 탈퇴 | User | `User.delete()` | Soft Delete |

### 합격 기준

| 등급 | 기준 | 조치 |
|------|------|------|
| PASS | Must Have 100%, Should Have ≥ 80% | 다음 단계 진행 |
| WARN | Must Have 100%, Should Have < 80% | 경고 후 진행 |
| FAIL | Must Have < 100% | 설계 수정 필요 |

### traceability-matrix.md 템플릿

```markdown
# 요구사항-도메인 추적 매트릭스

## 검증 일시
{timestamp}

## Must Have 요구사항

| ID | 요구사항 | Entity | 메서드/VO | 검증 규칙 | 상태 |
|----|----------|--------|-----------|-----------|------|
| M1 | {요구사항} | {Entity} | {메서드} | {규칙} | ✅/⚠️/❌ |
| M2 | ... | ... | ... | ... | ... |

**Must Have 매핑률**: X/Y (Z%)

## Should Have 요구사항

| ID | 요구사항 | Entity | 메서드/VO | 검증 규칙 | 상태 |
|----|----------|--------|-----------|-----------|------|
| S1 | {요구사항} | {Entity} | {메서드} | {규칙} | ✅/⚠️/❌ |
| S2 | ... | ... | ... | ... | ... |

**Should Have 매핑률**: X/Y (Z%)

## Could Have 요구사항

| ID | 요구사항 | Entity | 메서드/VO | 검증 규칙 | 상태 |
|----|----------|--------|-----------|-----------|------|
| C1 | {요구사항} | {Entity} | {메서드} | {규칙} | ✅/⚠️/❌ |

**Could Have 매핑률**: X/Y (Z%)

## 검증 결과
- [ ] Must Have 100% 달성
- [ ] Should Have 80% 이상 달성
```

---

## 2. SQL Sample Data 검증 (Step 10)

### 목적
생성된 SQL Sample Data가 데이터베이스 제약조건과 비즈니스 규칙을 준수하는지 확인.

### 검증 절차

```
1. SQL Schema 로드
   └─▶ sql/schema/*.sql 분석

2. SQL Sample Data 로드
   └─▶ sql/data/*.sql 분석

3. 제약조건 검증
   ├─▶ NOT NULL: 모든 NOT NULL 컬럼에 값 존재
   ├─▶ UNIQUE: 중복값 없음
   ├─▶ CHECK: 조건 만족
   └─▶ FK: 참조 무결성

4. 비즈니스 규칙 검증
   ├─▶ 상태값 유효성
   ├─▶ 날짜 논리적 일관성
   └─▶ 도메인 규칙 준수

5. design-validation-report.md 생성
   └─▶ 검증 결과 리포트
```

### 검증 항목별 체크리스트

#### NOT NULL 제약조건

```sql
-- DDL
CREATE TABLE user (
    email VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL
);

-- Sample Data 검증
INSERT INTO user (email, name) VALUES ('test@test.com', '테스터'); -- ✅ PASS
INSERT INTO user (email, name) VALUES (NULL, '테스터');           -- ❌ FAIL
INSERT INTO user (email, name) VALUES ('test@test.com', NULL);   -- ❌ FAIL
```

#### UNIQUE 제약조건

```sql
-- DDL
CREATE TABLE user (
    email VARCHAR(255) UNIQUE
);

-- Sample Data 검증
INSERT INTO user (email) VALUES ('user1@test.com'); -- ✅ PASS
INSERT INTO user (email) VALUES ('user2@test.com'); -- ✅ PASS
INSERT INTO user (email) VALUES ('user1@test.com'); -- ❌ FAIL (중복)
```

#### CHECK 제약조건

```sql
-- DDL
CREATE TABLE user (
    status VARCHAR(20) CHECK (status IN ('PENDING', 'ACTIVE', 'INACTIVE'))
);

-- Sample Data 검증
INSERT INTO user (status) VALUES ('ACTIVE');   -- ✅ PASS
INSERT INTO user (status) VALUES ('PENDING');  -- ✅ PASS
INSERT INTO user (status) VALUES ('DELETED');  -- ❌ FAIL (허용되지 않은 값)
```

#### FK 무결성

```sql
-- DDL
CREATE TABLE orders (
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- Sample Data 검증
-- user 데이터: id 1, 2 존재
INSERT INTO orders (user_id) VALUES (1); -- ✅ PASS
INSERT INTO orders (user_id) VALUES (3); -- ❌ FAIL (존재하지 않는 FK)
```

#### 비즈니스 규칙

| 규칙 | 검증 방법 |
|------|-----------|
| 이메일 형식 | `@` 포함 여부 |
| 비밀번호 강도 | 최소 8자, 특수문자 포함 |
| 상태 전이 | PENDING → ACTIVE → INACTIVE |
| 날짜 일관성 | created_at ≤ updated_at |

### 합격 기준

| 항목 | 기준 |
|------|------|
| NOT NULL 준수 | 100% |
| UNIQUE 준수 | 100% |
| CHECK 준수 | 100% |
| FK 무결성 | 100% |
| 비즈니스 규칙 | 100% |

### design-validation-report.md 템플릿

```markdown
# 설계 검증 리포트

## 검증 일시
{timestamp}

## 검증 결과: ✅ PASS / ⚠️ WARN / ❌ FAIL

---

## 1. 요구사항-도메인 매핑 검증

### Must Have 요구사항

| ID | 요구사항 | Entity | 메서드/VO | 상태 |
|----|----------|--------|-----------|------|
| M1 | {요구사항} | {Entity} | {메서드} | ✅ |
| ... | ... | ... | ... | ... |

**매핑률**: X/Y (Z%)

### Should Have 요구사항

| ID | 요구사항 | Entity | 메서드/VO | 상태 |
|----|----------|--------|-----------|------|
| S1 | {요구사항} | {Entity} | {메서드} | ✅ |
| ... | ... | ... | ... | ... |

**매핑률**: X/Y (Z%)

---

## 2. SQL Sample Data 검증

### NOT NULL 제약조건

| 테이블 | 컬럼 | 검증 결과 | 비고 |
|--------|------|-----------|------|
| user | email | ✅ | 모든 레코드 값 존재 |
| user | name | ✅ | 모든 레코드 값 존재 |
| orders | user_id | ✅ | 모든 레코드 값 존재 |

### UNIQUE 제약조건

| 테이블 | 컬럼 | 검증 결과 | 비고 |
|--------|------|-----------|------|
| user | email | ✅ | 중복 없음 |

### CHECK 제약조건

| 테이블 | 제약조건 | 검증 결과 | 비고 |
|--------|----------|-----------|------|
| user | status IN (...) | ✅ | 모든 값 유효 |

### FK 무결성

| 테이블 | FK 컬럼 | 참조 테이블 | 검증 결과 | 비고 |
|--------|---------|-------------|-----------|------|
| orders | user_id | user | ✅ | 모든 FK 유효 |

### 비즈니스 규칙

| 규칙 | 검증 결과 | 비고 |
|------|-----------|------|
| 이메일 형식 | ✅ | 모든 이메일 @ 포함 |
| 상태값 유효성 | ✅ | PENDING, ACTIVE, INACTIVE만 사용 |
| 날짜 일관성 | ✅ | created_at ≤ updated_at |

---

## 검증 요약

| 검증 항목 | 결과 | 비고 |
|-----------|------|------|
| Must Have 매핑 | ✅ 100% | 3/3 |
| Should Have 매핑 | ✅ 100% | 2/2 |
| NOT NULL 준수 | ✅ | 모든 필드 준수 |
| UNIQUE 준수 | ✅ | 중복 없음 |
| FK 무결성 | ✅ | 모든 FK 유효 |
| 비즈니스 규칙 | ✅ | 모든 규칙 준수 |

## 최종 판정
✅ PASS - 모든 검증 항목 통과
```

---

## 검증 실패 시 대응

### FAIL 발생 시

1. **매핑 누락**: Entity에 메서드 추가 또는 Value Object 생성
2. **제약조건 위반**: Sample Data 수정
3. **비즈니스 규칙 위반**: 데이터 수정 또는 규칙 재정의

### 재검증 절차

```
1. 문제 항목 수정
2. 검증 스크립트 재실행
3. design-validation-report.md 갱신
4. 모든 항목 PASS 확인 후 다음 단계 진행
```

---

## 참조
- Entity 템플릿: [entity-template.md](entity-template.md)
- DDD 패턴: [ddd-patterns.md](ddd-patterns.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
