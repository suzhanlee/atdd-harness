---
name: commit
description: 한국어로 커밋 메시지를 작성하고 커밋을 생성한다. 기능 단위로 커밋 계획을 세운 후 작은 커밋으로 나누어 진행한다.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Bash
aliases:
  - ci
---

# 한국어 커밋

## 목표
Conventional Commits 규칙을 기반으로 **기능 단위의 작은 커밋**을 계획하고 한국어로 명확한 커밋 메시지를 작성한다.

## 트리거
- "커밋해줘"
- "commit"
- "/commit"
- "/ci"
- "변경사항 커밋"

## 핵심 원칙

### 🎯 Atomic Commits (원자적 커밋)
- **한 커밋 = 하나의 논리적 변경**
- 커밋은 독립적으로 되돌릴 수 있어야 함
- 커밋 단위로 리뷰 가능해야 함

### 📏 커밋 크기 가이드라인
| 지표 | 권장 범위 |
|------|----------|
| 파일 수 | 1~5개 |
| 라인 변경 | 50~300줄 |
| 커밋 요약 | 50자 이내 |
| 본문 | 72자 줄바꿈 |

---

## 프로세스

### Step 1: 변경사항 수집
```bash
git status --short
git diff --stat
```

변경된 파일들을 분석하여:
- 새 파일 (A)
- 수정된 파일 (M)
- 삭제된 파일 (D)
- 리네임된 파일 (R)

### Step 2: 커밋 계획 수립 (핵심!)

변경된 파일들을 **기능 단위로 그룹화**하여 커밋 계획을 수립한다.

```
📋 커밋 계획
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

분석된 변경사항:
- 총 파일 수: N개
- 총 라인 변경: +XXX / -YYY
- 변경 유형: [기능 추가 / 버그 수정 / 리팩토링 / ...]

커밋 그룹:
┌─────────────────────────────────────┐
│ 커밋 1/N: <타입>(<스코프>): <요약>  │
│   ├── file1.java                    │
│   └── file2.java                    │
├─────────────────────────────────────┤
│ 커밋 2/N: <타입>(<스코프>): <요약>  │
│   └── file3.java                    │
└─────────────────────────────────────┘

예상 커밋 메시지:
---
feat(auth): 로그인 API 구현

- 로그인 요청/응답 DTO 정의
- 로그인 비즈니스 로직 구현
- 로그인 REST API 엔드포인트 추가
---
```

### Step 3: 사용자 승인
```
위 계획으로 진행할까요?
1. ✅ 예, 진행해주세요
2. 🔄 계획 수정 (파일 그룹 변경)
3. 📝 커밋 메시지 직접 편집
4. ❌ 취소
```

### Step 4: 순차적 커밋 실행
각 커밋을 순서대로 실행:
```bash
git add file1.java file2.java
git commit -m "..."

git add file3.java
git commit -m "..."
```

---

## 커밋 분할 전략

### 파일 기반 분할
변경된 파일이 10개 이상이면 기능별로 분할:

| 변경 유형 | 별도 커밋 |
|----------|----------|
| 새 기능 + 테스트 | 2개 커밋으로 분리 |
| 리팩토링 + 기능 | 2개 커밋으로 분리 |
| 설정 + 코드 | 2개 커밋으로 분리 |
| 여러 도메인 | 도메인별 분리 |

### 분할 예시

**❌ 나쁜 예 (너무 큰 커밋)**
```
feat: 사용자 관리 기능 전체 구현

25 files changed, 1500 insertions(+)
```

**✅ 좋은 예 (기능별 분할)**
```
커밋 1: feat(user): 사용자 Entity 및 Repository 구현
커밋 2: feat(user): 사용자 등록 API 구현
커밋 3: feat(user): 사용자 조회 API 구현
커밋 4: feat(user): 사용자 수정/삭제 API 구현
커밋 5: test(user): 사용자 관리 테스트 추가
```

---

## 커밋 메시지 형식

### 기본 형식
```
<타입>(<스코프>): <요약>

[본문]

[푸터]
```

### 타입 (Type)
| 타입 | 설명 | 한국어 예시 |
|------|------|-------------|
| feat | 새로운 기능 | feat: 사용자 로그인 기능 추가 |
| fix | 버그 수정 | fix: 로그인 실패 시 에러 메시지 수정 |
| docs | 문서 변경 | docs: README 설치 방법 업데이트 |
| style | 코드 포맷팅 | style: 들여쓰기 4칸으로 통일 |
| refactor | 코드 리팩토링 | refactor: UserService 로직 분리 |
| test | 테스트 추가/수정 | test: UserService 단위 테스트 추가 |
| chore | 빌드/도구 변경 | chore: Gradle 버전 업데이트 |
| perf | 성능 개선 | perf: 쿼리 최적화로 응답 속도 개선 |

### 작성 규칙

1. **요약 (Subject)**
   - 50자 이내
   - 명령형 어조 (~함, ~추가, ~수정)
   - 마침표 금지

2. **본문 (Body)**
   - 무엇을, 왜 변경했는지 설명
   - 72자마다 줄바꿈
   - 어떻게보다 무엇과 왜에 집중

3. **푸터 (Footer)**
   - Breaking Changes: `BREAKING CHANGE: 설명`
   - 이슈 참조: `Closes #123`

---

## 커밋 계획 템플릿

```
📋 커밋 계획
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

분석된 변경사항:
- 총 파일 수: N개
- 총 라인 변경: +XXX / -YYY
- 변경 유형: [기능 추가 / 버그 수정 / 리팩토링 / ...]

커밋 그룹:
┌─────────────────────────────────────┐
│ 커밋 1/N: <타입>(<스코프>): <요약>  │
│   ├── file1.java                    │
│   └── file2.java                    │
├─────────────────────────────────────┤
│ 커밋 2/N: <타입>(<스코프>): <요약>  │
│   └── file3.java                    │
└─────────────────────────────────────┘

예상 커밋 메시지:
---
feat(auth): 로그인 API 구현

- 로그인 요청/응답 DTO 정의
- 로그인 비즈니스 로직 구현
- 로그인 REST API 엔드포인트 추가
---

진행하시겠습니까? (y/n/e: 편집)
```

---

## 주의사항

### 🚫 피해야 할 커밋
1. **거대 커밋**: 10개 이상 파일, 1000줄 이상 변경
2. **혼합 커밋**: 기능 + 버그수정 + 리팩토링 섞임
3. **의미 없는 커밋**: "WIP", "수정", "업데이트"
4. **민감 정보 포함**: 비밀번호, API 키, .env 파일

### ✅ 권장 커밋
1. **설명 가능한 커밋**: "이 커밋은 ~를 위해 ~했습니다"
2. **되돌리기 쉬운 커밋**: 독립적인 변경
3. **리뷰 가능한 커밋**: 5분 내에 이해 가능

---

## 실행 예시

### 시나리오: 사용자 CRUD 기능 개발

**변경된 파일 (12개)**:
```
src/main/java/.../user/
  ├── User.java (새 파일)
  ├── UserRepository.java (새 파일)
  ├── UserService.java (새 파일)
  ├── UserController.java (새 파일)
  ├── UserRequest.java (새 파일)
  └── UserResponse.java (새 파일)

src/test/java/.../user/
  ├── UserServiceTest.java (새 파일)
  ├── UserControllerTest.java (새 파일)
  └── UserRepositoryTest.java (새 파일)

src/main/resources/
  └── application.yml (수정)

docs/
  └── API.md (새 파일)
```

**커밋 계획 (5개로 분할)**:
```
📋 커밋 계획
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

커밋 1/5: feat(user): 사용자 도메인 엔티티 및 Repository
  ├── User.java
  └── UserRepository.java

커밋 2/5: feat(user): 사용자 CRUD 서비스 구현
  ├── UserService.java
  ├── UserRequest.java
  └── UserResponse.java

커밋 3/5: feat(user): 사용자 REST API 엔드포인트
  └── UserController.java

커밋 4/5: test(user): 사용자 기능 테스트
  ├── UserServiceTest.java
  ├── UserControllerTest.java
  └── UserRepositoryTest.java

커밋 5/5: docs(user): 사용자 API 문서 작성
  ├── API.md
  └── application.yml

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

---

## 참조
- Conventional Commits: https://www.conventionalcommits.org/
- Atomic Commits: https://www.atomiccommits.dev/
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
