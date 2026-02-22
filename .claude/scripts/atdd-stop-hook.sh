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
#
# ATDD Pipeline: interview → [epic-split] → validate → adr ↔ redteam → design ↔ redteam-design → compound → gherkin → tdd → refactor → verify
#
# State Management:
#   - Skills update context.json: { "phase": "xxx", "status": "completed" }
#   - Hook checks context.json status to determine completion

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
CONTEXT_FILE="$CWD/.atdd/context.json"

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

# Helper function to trigger next skill
trigger_next_skill() {
  local next_skill="$1"
  echo "📋 ATDD: $PHASE complete, triggering /$next_skill for \"$TOPIC\"" >&2
  jq -n --arg skill "$next_skill" --arg topic "$TOPIC" \
    '{decision: "block", reason: ("Execute: Skill(\"" + $skill + "\", args=\"" + $topic + "\")")}'
}

# Helper function for pipeline completion
pipeline_complete() {
  echo "🎉 ATDD: Pipeline complete for \"$TOPIC\"" >&2
  echo "📁 Results: $BASE_PATH" >&2
  cleanup_session
  jq -n '{decision: "allow"}'
}

# Helper function to check if phase is completed in context.json
# Returns 0 (true) if completed, 1 (false) otherwise
is_phase_completed() {
  local expected_phase="$1"

  if [[ ! -f "$CONTEXT_FILE" ]]; then
    return 1
  fi

  local context_phase=$(jq -r '.phase // empty' "$CONTEXT_FILE" 2>/dev/null)
  local context_status=$(jq -r '.status // empty' "$CONTEXT_FILE" 2>/dev/null)

  [[ "$context_phase" == "$expected_phase" ]] && [[ "$context_status" == "completed" ]]
}

# Phase transition logic
# Pipeline: interview → [epic-split] → validate → adr ↔ redteam → design ↔ redteam-design → compound → gherkin → tdd → refactor → verify
case "$PHASE" in
  interview)
    # Check if interview phase is completed via context.json
    if is_phase_completed "interview"; then
      update_phase "validate"
      trigger_next_skill "validate"
      exit 0
    else
      echo "⏸️ ATDD: Interview paused for \"$TOPIC\"" >&2
    fi
    ;;

  validate)
    # Check if validate phase is completed via context.json
    if is_phase_completed "validate"; then
      update_phase "adr"
      trigger_next_skill "adr"
      exit 0
    else
      echo "📋 ATDD: Validation in progress for \"$TOPIC\"" >&2
    fi
    ;;

  adr)
    # Check if adr phase is completed via context.json
    if is_phase_completed "adr"; then
      update_phase "redteam"
      trigger_next_skill "redteam"
      exit 0
    else
      echo "📋 ATDD: ADR in progress for \"$TOPIC\"" >&2
    fi
    ;;

  redteam)
    # Check if redteam phase is completed via context.json
    if is_phase_completed "redteam"; then
      # Check if revision needed via redteam report
      REDTEAM_REPORT="$FULL_BASE_PATH/redteam/critique-report.md"
      if [[ -f "$REDTEAM_REPORT" ]]; then
        if grep -qiE "(revision.*needed|수정.*필요|reject)" "$REDTEAM_REPORT" 2>/dev/null; then
          update_phase "adr"
          trigger_next_skill "adr"
          exit 0
        fi
      fi
      update_phase "design"
      trigger_next_skill "design"
      exit 0
    else
      echo "📋 ATDD: Red Team review in progress for \"$TOPIC\"" >&2
    fi
    ;;

  design)
    # Check if design phase is completed via context.json
    if is_phase_completed "design"; then
      update_phase "redteam-design"
      trigger_next_skill "redteam-design"
      exit 0
    else
      echo "📋 ATDD: Design in progress for \"$TOPIC\"" >&2
    fi
    ;;

  redteam-design)
    # Check if redteam-design phase is completed via context.json
    if is_phase_completed "redteam-design"; then
      # Check if revision needed via redteam-design report
      REDTEAM_DESIGN_REPORT="$FULL_BASE_PATH/redteam-design/critique-report.md"
      if [[ -f "$REDTEAM_DESIGN_REPORT" ]]; then
        if grep -qiE "(revision.*needed|수정.*필요|reject)" "$REDTEAM_DESIGN_REPORT" 2>/dev/null; then
          update_phase "design"
          trigger_next_skill "design"
          exit 0
        fi
      fi
      update_phase "compound"
      trigger_next_skill "compound"
      exit 0
    else
      echo "📋 ATDD: Red Team Design review in progress for \"$TOPIC\"" >&2
    fi
    ;;

  compound)
    # Check if compound phase is completed via context.json
    if is_phase_completed "compound"; then
      update_phase "gherkin"
      trigger_next_skill "gherkin"
      exit 0
    else
      echo "📋 ATDD: Compound learning in progress for \"$TOPIC\"" >&2
    fi
    ;;

  gherkin)
    # Check if gherkin phase is completed via context.json
    if is_phase_completed "gherkin"; then
      update_phase "tdd"
      trigger_next_skill "tdd"
      exit 0
    else
      echo "📋 ATDD: Gherkin in progress for \"$TOPIC\"" >&2
    fi
    ;;

  tdd)
    # Check if tdd phase is completed via context.json
    if is_phase_completed "tdd"; then
      update_phase "refactor"
      trigger_next_skill "refactor"
      exit 0
    else
      echo "📋 ATDD: TDD in progress for \"$TOPIC\"" >&2
    fi
    ;;

  refactor)
    # Check if refactor phase is completed via context.json
    if is_phase_completed "refactor"; then
      update_phase "verify"
      trigger_next_skill "verify"
      exit 0
    else
      echo "📋 ATDD: Refactoring in progress for \"$TOPIC\"" >&2
    fi
    ;;

  verify)
    # Check if verify phase is completed via context.json
    if is_phase_completed "verify"; then
      update_phase "done"
      pipeline_complete
      exit 0
    else
      echo "📋 ATDD: Verification in progress for \"$TOPIC\"" >&2
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
    echo "📋 ATDD: Session at unknown phase \"$PHASE\" for \"$TOPIC\"" >&2
    ;;
esac

# Default: allow session to end
exit 0
