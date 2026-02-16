---
name: adr
description: Architecture Decision Record (ADR) 작성 가이드. 설계 의사결정을 문서화하고 추적.
disable-model-invocation: true
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit
references:
  - references/adr-template.md
---

# ADR (Architecture Decision Record) 작성

## 목표
사용자가 직접 Architecture Decision Record를 작성하여 설계 의사결정 능력을 향상시킨다.

## 입력
- `.atdd/requirements/refined-requirements.md`
- `.atdd/validation/validation-report.md`

## 상세 가이드
- ADR 템플릿: [adr-template.md](references/adr-template.md)

## 트리거
- `/adr` 명령어 실행
- `/design` Phase의 서브단계

## ADR이란?

Architecture Decision Record는 아키텍처 의사결정을 기록하는 가벼운 문서 형식이다.
각 ADR은 하나의 결정을 설명하며, 결정의 배경, 대안, 결과를 포함한다.

## 작성 프로세스

### 1. ADR 번호 할당
```
001-데이터베이스-선택.md
002-인증-방식-결정.md
003-API-아키텍처.md
```

### 2. 결정 사항 식별
요구사항에서 아키텍처 결정이 필요한 영역 파악:
- 기술 스택 선택
- 데이터 모델링
- API 설계
- 보안 아키텍처
- 확장성 전략

### 3. ADR 작성
템플릿에 따라 각 섹션 작성:
- **Title**: 결정 사항 요약
- **Status**: Proposed, Accepted, Deprecated, Superseded
- **Context**: 결정이 필요한 상황
- **Decision**: 내린 결정
- **Alternatives**: 고려한 대안들
- **Consequences**: 결정의 결과

### 4. 저장 위치
```
.atdd/design/adr/
├── 001-*.md
├── 002-*.md
└── index.md
```

## ADR 작성 가이드라인

### 좋은 ADR의 특징
1. **단일 결정**: 하나의 ADR은 하나의 결정만 다룬다
2. **명확한 제목**: 결정 내용을 한 줄로 요약
3. **완전한 맥락**: 결정 배경을 충분히 설명
4. **대안 분석**: 고려한 다른 옵션들 포함
5. **결과 명시**: 결정으로 인한 영향 설명

### Status 값
| Status | 설명 |
|--------|------|
| **Proposed** | 제안됨, 아직 승인되지 않음 |
| **Accepted** | 승인됨, 현재 유효함 |
| **Deprecated** | 더 이상 권장되지 않음 |
| **Superseded** | 다른 ADR로 대체됨 |

## 예시: 데이터베이스 선택 ADR

```markdown
# 001. 데이터베이스 선택

## Status
Proposed

## Context
사용자 관리 시스템의 데이터베이스를 선택해야 한다.
요구사항:
- 동시 사용자 1,000명 지원
- 응답 시간 200ms 이하
- 트랜잭션 무결성 필요
- 복잡한 쿼리 필요 없음

## Decision
MySQL 8.0을 사용한다.

## Alternatives Considered

### PostgreSQL
- 장점: JSON 지원 우수, 확장성 높음
- 단점: 팀 경험 부족, 운영 복잡도
- 선택하지 않은 이유: 팀의 MySQL 경험이 풍부함

### MongoDB
- 장점: 스키마 유연성, 수평 확장 용이
- 단점: 트랜잭션 지원 제한적
- 선택하지 않은 이유: ACID 트랜잭션 필요

## Consequences

### 긍정적
- 팀의 MySQL 노하우 활용 가능
- 안정적인 운영 환경
- 풍부한 생태계

### 부정적
- 수평 확장 시 복잡도 증가 가능
- JSON 데이터 처리 시 PostgreSQL 대비 불리

### 위험
- 트래픽 급증 시 샤딩 필요할 수 있음
```

## 다음 단계
ADR 작성 완료 후 `/redteam` 실행하여 설계 비평 수행

## 참조
- ADR 템플릿: [adr-template.md](references/adr-template.md)
- Agent 정의: [AGENTS.md](../../../AGENTS.md)
- 워크플로우: [WORKFLOWS.md](../../../WORKFLOWS.md)
