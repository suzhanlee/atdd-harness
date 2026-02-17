# Epic 1 검증 리포트

## Epic 정보
- **제목**: 사용자 인증
- **검증 일시**: 2026-02-17

## 검증 결과: ✅ PASS

---

## 세부 검증

### 1. Feasibility (구현 가능성)

| 항목 | 결과 | 비고 |
|------|------|------|
| 기술 스택 호환성 | ✅ | Java 17, Spring Boot 3.x, Spring Security + JWT 라이브러리 호환 |
| Entity 설계 적절성 | ✅ | User Entity 1개로 단순. DDD Aggregate Root로 적절 |
| 일정 | ✅ | 예상 1시간 → 회원가입/로그인/JWT 필터 구현 충분히 가능 |

**상세 분석**:
- Spring Security 6.x + JWT (jjwt 또는 java-jwt) 조합은 검증된 패턴
- Spring Boot 3.x는 Java 17+ 필수 → 프로젝트 기술 스택과 일치
- Entity가 1개(User)로 복잡도 낮음 → 빠른 구현 가능

### 2. Completeness (완전성)

| 항목 | 결과 | 비고 |
|------|------|------|
| 범위 명확성 | ✅ | 포함: 회원가입, 로그인, JWT 인증 / 제외: 소셜 로그인, 비밀번호 찾기 |
| DoD 완전성 | ✅ | 4개 항목 모두 테스트 가능한 완료 기준 |
| 예외 케이스 | ⚠️ | 일부 예외 케이스 보완 권장 |

**예외 케이스 보완 권장 사항**:
- [ ] 중복 이메일/username 처리
- [ ] 잘못된 로그인 정보 응답
- [ ] JWT 만료/유효하지 않은 토큰 처리
- [ ] 권한 없는 요청 처리 (401/403)

### 3. Consistency (일관성)

| 항목 | 결과 | 비고 |
|------|------|------|
| 용어 통일 | ✅ | User, username, email, password 용어 일관적 |
| 의존성 충돌 | ✅ | 의존 Epic 없음 → 충돌 없음 |

**용어 검증**:
- requirements-draft.md와 epics.md 간 User Entity 정의 일치
- JWT 관련 용어(access token, refresh token)는 구현 시 명확화 필요

### 4. Dependencies (의존성)

| 의존성 | 결과 | 비고 |
|--------|------|------|
| 의존 Epic 완료 여부 | ✅ | 의존 Epic 없음 |
| 외부 의존성 | ✅ | JWT 라이브러리, Spring Security (모두 Maven 중앙 저장소 사용 가능) |

**필요 의존성**:
```gradle
// Spring Security
implementation 'org.springframework.boot:spring-boot-starter-security'

// JWT (선택지)
implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
```

---

## Entity 설계 제안

```java
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // encoded

    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters, Setters
}

public enum Role {
    USER, ADMIN
}
```

---

## 조치 사항

| # | 항목 | 우선순위 | 상태 |
|---|------|----------|------|
| 1 | 예외 케이스 정의 보완 (중복 이메일, 인증 실패 등) | 권장 | 대기 |
| 2 | JWT 토큰 전략 결정 (Access only vs Access+Refresh) | 권장 | 대기 |

---

## 결론

Epic 1은 **모든 필수 검증 항목을 통과**했습니다.

- 기술 스택 호환성 ✅
- 범위 명확 ✅
- DoD 완전 ✅
- 의존성 문제 없음 ✅

일부 예외 케이스 정의는 구현 단계에서 보완해도 무방합니다.

---

## 다음 단계
- [x] Epic 1 검증 완료
- [ ] Epic 2 검증: `/validate` 실행
- [ ] 또는 바로 `/design` 진행 가능 (Epic 1만 먼저 구현 시)
