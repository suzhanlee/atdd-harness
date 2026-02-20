#!/bin/bash
# atdd-stop-hook.sh - Stop hook for ATDD orchestration
#
# Purpose: Track ATDD pipeline state and trigger next skills
# Activation: Stop event + session has atdd state
#
# ultrawork 방식: jq + bash로 JSON 출력하여 다음 스킬 트리거
#
# Hook Input Fields (Stop):
#   - session_id: current session
#   - transcript_path: conversation log path
#   - cwd: current working directory
#
# Output JSON (ultrawork 방식):
#   {"decision": "block", "reason": "Execute: Skill(\"validate\")"} -> 세션 종료 차단, 스킬 실행
#   {"decision": "allow"} 또는 출력 없음 -> 세션 종료 허용

set -euo pipefail

# Read hook input from stdin
HOOK_INPUT=$(cat)

# Use jq for JSON parsing (ultrawork style)
CWD=$(echo "$HOOK_INPUT" | jq -r '.cwd // empty')
SESSION_ID=$(echo "$HOOK_INPUT" | jq -r '.session_id // empty')

# Normalize Windows paths
CWD=$(echo "$CWD" | sed 's|\\|/|g')

# State file path
STATE_FILE="$CWD/.atdd/state.json"

# Exit if no state file
if [[ ! -f "$STATE_FILE" ]]; then
  exit 0
fi

# Check if this session has atdd state
ATDD_STATE=$(jq -e --arg sid "$SESSION_ID" '.sessions[$sid].atdd // empty' "$STATE_FILE" 2>/dev/null || echo "")

if [[ -z "$ATDD_STATE" ]]; then
  exit 0
fi

# Extract atdd state fields
PHASE=$(echo "$ATDD_STATE" | jq -r '.phase // "interview"')
BASE_PATH=$(echo "$ATDD_STATE" | jq -r '.basePath // empty')
TOPIC=$(echo "$ATDD_STATE" | jq -r '.topic // empty')

# Fallback: Try to get basePath from context.json if not in state
if [[ -z "$BASE_PATH" ]]; then
  CONTEXT_FILE="$CWD/.atdd/context.json"
  if [[ -f "$CONTEXT_FILE" ]]; then
    BASE_PATH=$(jq -r '.basePath // empty' "$CONTEXT_FILE" 2>/dev/null || echo "")
  fi
fi

if [[ -z "$BASE_PATH" ]]; then
  exit 0
fi

# Full path for base directory
FULL_BASE_PATH="$CWD/$BASE_PATH"

# Function to update phase in state file
update_phase() {
  local new_phase="$1"
  local tmp_file=$(mktemp)
  jq --arg sid "$SESSION_ID" --arg phase "$new_phase" \
    '.sessions[$sid].atdd.phase = $phase' "$STATE_FILE" > "$tmp_file" && mv "$tmp_file" "$STATE_FILE"
}

# Function to cleanup session
cleanup_session() {
  local tmp_file=$(mktemp)
  jq --arg sid "$SESSION_ID" 'del(.sessions[$sid])' "$STATE_FILE" > "$tmp_file" && mv "$tmp_file" "$STATE_FILE"
}

# Phase transition logic
case "$PHASE" in
  interview)
    # Check if requirements-draft.md exists (interview complete)
    REQUIREMENTS_FILE="$FULL_BASE_PATH/interview/requirements-draft.md"
    if [[ -f "$REQUIREMENTS_FILE" ]]; then
      # Interview complete - trigger validate
      update_phase "validate"
      echo "📋 ATDD: Interview complete, triggering /validate for \"$TOPIC\"" >&2

      # Output JSON to block and trigger next skill (ultrawork style)
      jq -n --arg topic "$TOPIC" '{decision: "block", reason: ("Execute: Skill(\"validate\", args=\"" + $topic + "\")")}'
      exit 0
    else
      # Interview incomplete - just log
      echo "⏸️ ATDD: Interview paused for \"$TOPIC\"" >&2
    fi
    ;;

  validate)
    # Check if validation-report.md exists with PASS
    VALIDATION_REPORT="$FULL_BASE_PATH/validate/validation-report.md"
    if [[ -f "$VALIDATION_REPORT" ]]; then
      # Check for PASS status
      if grep -qiE "(종합 결과.*✅|overall.*pass|결과.*pass)" "$VALIDATION_REPORT" 2>/dev/null; then
        # Validation passed - mark as done
        update_phase "done"
        echo "🎉 ATDD: Pipeline complete for \"$TOPIC\"" >&2
        echo "📁 Results: $BASE_PATH" >&2

        # Cleanup session
        cleanup_session

        # Allow session to end (no output or decision: allow)
        jq -n '{decision: "allow"}'
        exit 0
      else
        # Validation incomplete or failed
        echo "⚠️ ATDD: Validation incomplete or failed for \"$TOPIC\"" >&2
      fi
    else
      echo "📋 ATDD: Validation in progress for \"$TOPIC\"" >&2
    fi
    ;;

  done)
    # Already done - cleanup and allow
    cleanup_session
    jq -n '{decision: "allow"}'
    exit 0
    ;;

  *)
    # Unknown phase - just log
    echo "📋 ATDD: Session at phase \"$PHASE\" for \"$TOPIC\"" >&2
    ;;
esac

# Default: allow session to end
exit 0
