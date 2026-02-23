#!/bin/bash
# atdd-init-hook.sh - UserPromptSubmit hook
#
# Purpose: Initialize ATDD state when user types "/atdd {topic}"
# Activation: UserPromptSubmit when user types "/atdd"
#
# Hook Input Fields (UserPromptSubmit):
#   - session_id: actual Claude Code session ID
#   - cwd: current working directory
#   - prompt: user's input text

set -euo pipefail

# Read hook input from stdin
HOOK_INPUT=$(cat)

# Extract fields using jq
CWD=$(echo "$HOOK_INPUT" | jq -r '.cwd // empty')
SESSION_ID=$(echo "$HOOK_INPUT" | jq -r '.session_id // empty')
PROMPT=$(echo "$HOOK_INPUT" | jq -r '.prompt // empty')

# Check if this is an atdd command
if ! echo "$PROMPT" | grep -qiE "^/atdd"; then
  exit 0
fi

# Extract topic from prompt (e.g., "/atdd payment-system" -> "payment-system")
TOPIC=$(echo "$PROMPT" | sed -E 's|^/atdd[[:space:]]*||i' | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')

if [[ -z "$TOPIC" ]]; then
  # No topic provided - let the skill handle it
  exit 0
fi

# Normalize path (convert backslashes to forward slashes for Windows compatibility)
CWD=$(echo "$CWD" | tr '\\' '/')

# State file path
STATE_FILE="$CWD/.atdd/state.json"

# Ensure .atdd directory exists
mkdir -p "$(dirname "$STATE_FILE")"

# Generate date and timestamp
DATE=$(date +%Y-%m-%d)
TIMESTAMP=$(date -Iseconds 2>/dev/null || date +%Y-%m-%dT%H:%M:%S%z)
BASE_PATH=".atdd/$DATE/$TOPIC"

# Default state structure
DEFAULT_STATE=$(jq -n \
  --arg topic "$TOPIC" \
  --arg date "$DATE" \
  '{
    version: "1.0.0",
    project: {
      name: "atdd-harness",
      description: "ATDD Harness for Java/Spring",
      techStack: ["Java 17+", "Spring Boot 3.x", "MySQL", "Cucumber", "RestAssured", "JUnit5"]
    },
    phases: {
      interview: {status: "pending", startedAt: null, completedAt: null, outputs: []},
      validate: {status: "pending", startedAt: null, completedAt: null, outputs: []},
      design: {status: "pending", startedAt: null, completedAt: null, outputs: []},
      gherkin: {status: "pending", startedAt: null, completedAt: null, outputs: []},
      tdd: {status: "pending", startedAt: null, completedAt: null, outputs: []},
      refactor: {status: "pending", startedAt: null, completedAt: null, outputs: []},
      verify: {status: "pending", startedAt: null, completedAt: null, outputs: []}
    },
    currentPhase: null,
    history: [],
    sessions: {}
  }')

# Load existing state or use default
if [[ -f "$STATE_FILE" ]]; then
  CURRENT_STATE=$(cat "$STATE_FILE")
else
  CURRENT_STATE="$DEFAULT_STATE"
fi

# Check if session already has atdd state
EXISTS=$(echo "$CURRENT_STATE" | jq --arg sid "$SESSION_ID" 'has("sessions") and .sessions[$sid].atdd // false' 2>/dev/null || echo "false")

if [[ "$EXISTS" == "true" ]]; then
  echo "🚀 ATDD already initialized for \"$TOPIC\" (session: ${SESSION_ID:0:8}...)" >&2
  exit 0
fi

# Add session and save state
echo "$CURRENT_STATE" | jq \
  --arg sid "$SESSION_ID" \
  --arg ts "$TIMESTAMP" \
  --arg bp "$BASE_PATH" \
  --arg topic "$TOPIC" \
  '.sessions[$sid] = {
    created_at: $ts,
    atdd: {
      phase: "interview",
      iteration: 0,
      max_iterations: 10,
      basePath: $bp,
      topic: $topic
    }
  }' > "$STATE_FILE"

echo "🚀 ATDD initialized for \"$TOPIC\" (session: ${SESSION_ID:0:8}...)" >&2

exit 0
