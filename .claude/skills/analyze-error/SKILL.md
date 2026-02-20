---
name: analyze-error
description: 특정 에러를 심층 분석하고 5 Whys 기법으로 근본 원인을 파악하여 수정 방안을 제안한다.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit, Bash
---

# Analyze Error Skill

특정 에러를 심층 분석하고 수정 방안을 제안합니다.

## 트리거
- `/analyze-error {error-id}`
- "이 에러 분석해줘"
- "ERR-001 원인 찾아줘"

## 전제 조건
- `/monitor` 실행으로 에러 리포트가 생성되어 있어야 함
- 또는 구체적인 에러 로그가 제공되어야 함

## 프로세스

### 1. 에러 컨텍스트 수집
에러 리포트에서 해당 에러의 상세 정보를 조회:
- 스택트레이스
- 발생 시점의 요청 컨텍스트
- 관련 로그 (전후 5분)

### 2. 코드 분석
스택트레이스에서 식별된 소스 파일 분석:
- 문제 발생 지점의 코드 확인
- 관련 의존성 파악
- 테스트 코드 존재 여부 확인

### 3. 근본 원인 분석 (Root Cause Analysis)
5 Whys 기법으로 근본 원인 추적:
```
에러: NullPointerException at UserService.java:45

Q1: 왜 NPE가 발생했나?
A1: userId 파라미터가 null이었다.

Q2: 왜 userId가 null이었나?
A2: 클라이언트가 userId를 전송하지 않았다.

Q3: 왜 전송하지 않았나?
A3: 프론트엔드에서 로그인 상태를 확인하지 않았다.

Q4: 왜 로그인 상태를 확인하지 않았나?
A4: API에 인증 가드가 없다.

Q5: 왜 인증 가드가 없나?
A5: 새로 추가된 API에 Security 설정이 누락되었다.

근본 원인: Security 설정 누락으로 인한 미인증 요청 허용
```

### 4. 수정 방안 도출

#### 즉시 수정 (Quick Fix)
- Null 체크 추가
- 기본값 반환

#### 근본 수정 (Root Cause Fix)
- Security 설정 추가
- 인증 가드 적용

#### 예방 조치 (Prevention)
- 테스트 코드 추가
- API 문서화

### 5. 분석 리포트 생성
`.atdd/runtime/errors/analysis-{error-id}.md` 파일 생성

## 출력 형식

### 콘솔 출력
```
🔍 에러 분석: ERR-001

📋 에러 정보
- 타입: NullPointerException
- 위치: UserService.java:45
- 발생 횟수: 2회

🔎 근본 원인 분석 (5 Whys)
┌─────────────────────────────────────────────────────┐
│ Q1: NPE 발생 이유?                                   │
│ A1: userId가 null                                    │
│                                                      │
│ Q2: userId가 null인 이유?                            │
│ A2: 클라이언트에서 미전송                            │
│                                                      │
│ Q3: 미전송 이유?                                     │
│ A3: 로그인 상태 미확인                               │
│                                                      │
│ Q4: 미확인 이유?                                     │
│ A4: 인증 가드 없음                                   │
│                                                      │
│ Q5: 인증 가드 없는 이유?                             │
│ A5: Security 설정 누락                               │
└─────────────────────────────────────────────────────┘

📌 근본 원인: Security 설정 누락

💡 수정 방안
┌─────────────────────────────────────────────────────┐
│ 즉시 수정 (Quick Fix)                                │
│ - findById()에 null 체크 추가                        │
│                                                      │
│ 근본 수정 (Root Cause Fix)                           │
│ - SecurityConfig에 /api/users/** 인증 추가           │
│                                                      │
│ 예방 조치 (Prevention)                               │
│ - 미인증 요청 테스트 케이스 추가                     │
└─────────────────────────────────────────────────────┘

📁 상세 분석: .atdd/runtime/errors/analysis-ERR-001.md

🚀 다음 단계: /fix ERR-001
```

### 분석 파일 구조
```markdown
# 에러 분석 - ERR-001

## 개요
- 에러 ID: ERR-001
- 타입: NullPointerException
- 위치: com.example.service.UserService.findById(UserService.java:45)
- 발생 횟수: 2회
- 분석 시간: 2026-02-16 14:35:10

## 스택트레이스
```
java.lang.NullPointerException
  at com.example.service.UserService.findById(UserService.java:45)
  at com.example.controller.UserController.getUser(UserController.java:23)
  at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
  ...
```

## 관련 코드

### UserService.java:45
```java
// 문제 발생 지점
public User findById(Long userId) {
    return userRepository.findById(userId).orElse(null); // userId가 null이면 NPE
}
```

## 근본 원인 분석 (5 Whys)

| 단계 | 질문 | 답변 |
|------|------|------|
| 1 | NPE 발생 이유? | userId가 null |
| 2 | userId가 null인 이유? | 클라이언트 미전송 |
| 3 | 미전송 이유? | 로그인 상태 미확인 |
| 4 | 미확인 이유? | 인증 가드 없음 |
| 5 | 인증 가드 없는 이유? | Security 설정 누락 |

**근본 원인**: Security 설정 누락으로 미인증 요청 허용

## 수정 방안

### 1. 즉시 수정 (Quick Fix)
```java
public Optional<User> findById(Long userId) {
    if (userId == null) {
        return Optional.empty();
    }
    return userRepository.findById(userId);
}
```

### 2. 근본 수정 (Root Cause Fix)
```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/users/**").authenticated()
        );
        return http.build();
    }
}
```

### 3. 예방 조치 (Prevention)
- Gherkin 시나리오 추가
- 통합 테스트 추가

## 영향도 분석
- 영향받는 API: GET /api/users/{id}
- 영향받는 사용자: 전체 (미인증 요청 시)
- 위험도: Medium (서비스 중단 없음)

## 추천 조치
1. **즉시**: findById() null 체크 추가
2. **단기**: Security 설정 추가
3. **장기**: E2E 테스트 추가

---
생성 시간: 2026-02-16 14:35:10
```

## 다음 단계
- 자동 수정 실행: `/fix {error-id}`
- 다른 에러 분석: `/analyze-error {another-error-id}`
