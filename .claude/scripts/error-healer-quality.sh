#!/bin/bash
# Error Healer Quality Gate Hook
# PR 생성 전 품질 검증을 수행합니다.

set -e

# 입력 파싱 (stdin에서 JSON 입력)
INPUT=$(cat)
TASK_SUBJECT=$(echo "$INPUT" | jq -r '.task_subject // ""')
TASK_STATUS=$(echo "$INPUT" | jq -r '.task_status // ""')
TASK_METADATA=$(echo "$INPUT" | jq -r '.task_metadata // "{}"')

# 로그 함수
log() {
    echo "[error-healer-quality] $1" >&2
}

# PR task가 완료되려 할 때 검증
if [[ "$TASK_SUBJECT" == *"PR"* ]] || [[ "$TASK_SUBJECT" == *"Fix"* && "$TASK_SUBJECT" == *"create"* ]]; then
    if [[ "$TASK_STATUS" == "completing" ]]; then
        log "PR task detected, running quality checks..."

        # 1. Git 상태 확인
        if git diff --quiet && git diff --cached --quiet; then
            log "No changes detected"
        else
            log "Changes detected, proceeding with validation..."
        fi

        # 2. 프로젝트 타입 감지 및 테스트 실행
        PROJECT_ROOT="${PROJECT_ROOT:-$(pwd)}"

        # Java/Gradle 프로젝트
        if [ -f "$PROJECT_ROOT/gradlew" ] || [ -f "$PROJECT_ROOT/build.gradle" ]; then
            log "Detected Gradle project, running tests..."

            cd "$PROJECT_ROOT"

            # Unit Tests
            log "Running unit tests..."
            if ./gradlew test --quiet 2>&1; then
                log "Unit tests passed"
            else
                echo '{"block": true, "message": "Unit tests failed. Fix failing tests before completing PR task."}'
                exit 2
            fi

            # Integration Tests (존재하는 경우)
            if ./gradlew tasks --all 2>/dev/null | grep -q "integrationTest"; then
                log "Running integration tests..."
                if ./gradlew integrationTest --quiet 2>&1; then
                    log "Integration tests passed"
                else
                    echo '{"block": true, "message": "Integration tests failed. Fix failing tests before completing PR task."}'
                    exit 2
                fi
            fi

            # Cucumber Tests (존재하는 경우)
            if ./gradlew tasks --all 2>/dev/null | grep -q "cucumber"; then
                log "Running cucumber tests..."
                if ./gradlew cucumber --quiet 2>&1; then
                    log "Cucumber tests passed"
                else
                    echo '{"block": true, "message": "Cucumber tests failed. Fix failing tests before completing PR task."}'
                    exit 2
                fi
            fi

        # Node.js/npm 프로젝트
        elif [ -f "$PROJECT_ROOT/package.json" ]; then
            log "Detected Node.js project, running tests..."

            cd "$PROJECT_ROOT"

            if npm test 2>&1; then
                log "Tests passed"
            else
                echo '{"block": true, "message": "Tests failed. Fix failing tests before completing PR task."}'
                exit 2
            fi

        # Python 프로젝트
        elif [ -f "$PROJECT_ROOT/pytest.ini" ] || [ -f "$PROJECT_ROOT/setup.py" ]; then
            log "Detected Python project, running tests..."

            cd "$PROJECT_ROOT"

            if python -m pytest 2>&1; then
                log "Tests passed"
            else
                echo '{"block": true, "message": "Tests failed. Fix failing tests before completing PR task."}'
                exit 2
            fi

        else
            log "No test framework detected, skipping test validation"
        fi

        # 3. 커버리지 확인 (Java/Gradle만)
        if [ -f "$PROJECT_ROOT/gradlew" ]; then
            COVERAGE_THRESHOLD="${COVERAGE_THRESHOLD:-80}"

            log "Checking coverage (threshold: ${COVERAGE_THRESHOLD}%)..."

            # JaCoCo 리포트 생성
            ./gradlew jacocoTestReport --quiet 2>/dev/null || true

            # 커버리지 추출 (간단한 구현)
            COVERAGE_FILE="$PROJECT_ROOT/build/reports/jacoco/test/html/index.html"
            if [ -f "$COVERAGE_FILE" ]; then
                # HTML에서 커버리지 추출 (간단한 방식)
                COVERAGE=$(grep -oP 'Total.*?(\d+)%' "$COVERAGE_FILE" | head -1 | grep -oP '\d+' | head -1 || echo "0")

                if [ "$COVERAGE" -lt "$COVERAGE_THRESHOLD" ]; then
                    log "Warning: Coverage ($COVERAGE%) is below threshold ($COVERAGE_THRESHOLD%)"
                    # 커버리지는 warning만 (block하지 않음)
                fi
            fi
        fi

        # 4. 커밋 확인
        if ! git diff --cached --quiet; then
            log "Staged changes detected, ready to commit"
            echo '{"block": false, "message": "Quality checks passed. Ready to commit and create PR."}'
        elif ! git diff --quiet; then
            log "Unstaged changes detected"
            echo '{"block": false, "message": "Quality checks passed. Stage changes before committing."}'
        else
            log "No changes to commit"
            echo '{"block": false, "message": "Quality checks passed. No changes detected."}'
        fi

        exit 0
    fi
fi

# PR task가 아니면 통과
echo '{"block": false, "message": "Not a PR task, skipping quality checks."}'
exit 0
