# ADR Template

---

# [번호]. [제목]

## Status
[Proposed | Accepted | Deprecated | Superseded by [ADR-번호]]

## Context
[결정이 필요한 상황과 배경을 설명한다]
- 비즈니스 요구사항
- 기술적 제약사항
- 팀/조직 상황

## Decision
[내린 결정을 명확하게 기술한다]
- 무엇을 결정했는가?
- 어떤 기술/방식을 선택했는가?

## Alternatives Considered

### [대안 1 이름]
- **장점**:
- **단점**:
- **선택하지 않은 이유**:

### [대안 2 이름]
- **장점**:
- **단점**:
- **선택하지 않은 이유**:

### [대안 N 이름]
- **장점**:
- **단점**:
- **선택하지 않은 이유**:

## Consequences

### 긍정적
- [결정으로 얻는 이점들]

### 부정적
- [결정으로 인한 단점들]
- [추후 해결이 필요한 문제들]

### 위험
- [잠재적인 위험 요소들]
- [완화 전략]

---

## Related
- Related ADRs: [ADR 번호 목록]
- Related Requirements: [요구사항 ID]

## References
- [참고 문서/링크]

---

# ADR Index Template

```markdown
# ADR Index

## Active ADRs

| Number | Title | Status | Date |
|--------|-------|--------|------|
| 001 | 데이터베이스 선택 | Accepted | 2024-01-15 |
| 002 | 인증 방식 결정 | Proposed | 2024-01-16 |

## Deprecated ADRs

| Number | Title | Superseded By | Date |
|--------|-------|---------------|------|
| - | - | - | - |

## ADR Creation Guide
1. 새 ADR 번호 할당 (순차적 증가)
2. 템플릿 복사하여 작성
3. Status: Proposed로 시작
4. `/redteam` 실행하여 비평 수행
5. 비평 반영 후 Status: Accepted로 변경
```
