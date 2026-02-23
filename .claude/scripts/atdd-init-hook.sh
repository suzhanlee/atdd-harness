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
#
# SSoT: context.json (Single Source of Truth)
# This file is the only state file for ATDD workflow.

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

# Context file path (SSoT)
CONTEXT_FILE="$CWD/.atdd/context.json"

# Ensure .atdd directory exists
mkdir -p "$(dirname "$CONTEXT_FILE")"

# Check if context.json already exists for this topic
if [[ -f "$CONTEXT_FILE" ]]; then
  EXISTING_TOPIC=$(jq -r '.topic // empty' "$CONTEXT_FILE" 2>/dev/null || echo "")
  EXISTING_STATUS=$(jq -r '.status // empty' "$CONTEXT_FILE" 2>/dev/null || echo "")

  # If same topic and still in progress, skip initialization
  if [[ "$EXISTING_TOPIC" == "$TOPIC" ]] && [[ "$EXISTING_STATUS" == "in_progress" ]]; then
    echo "🚀 ATDD already initialized for \"$TOPIC\"" >&2
    exit 0
  fi

  # If different topic or completed, archive old context by renaming
  EXISTING_DATE=$(jq -r '.date // empty' "$CONTEXT_FILE" 2>/dev/null || date +%Y-%m-%d)
  EXISTING_BASE=$(jq -r '.basePath // empty' "$CONTEXT_FILE" 2>/dev/null || echo "")
  if [[ -n "$EXISTING_TOPIC" ]] && [[ -n "$EXISTING_BASE" ]]; then
    ARCHIVE_NAME="context-${EXISTING_DATE}-${EXISTING_TOPIC}.json"
    mv "$CONTEXT_FILE" "$(dirname "$CONTEXT_FILE")/$ARCHIVE_NAME" 2>/dev/null || true
    echo "📦 Archived previous ATDD context: $ARCHIVE_NAME" >&2
  fi
fi

# Generate date and timestamp
DATE=$(date +%Y-%m-%d)
TIMESTAMP=$(date -Iseconds 2>/dev/null || date +%Y-%m-%dT%H:%M:%S%z)
BASE_PATH=".atdd/$DATE/$TOPIC"

# Create context.json (SSoT)
jq -n \
  --arg topic "$TOPIC" \
  --arg date "$DATE" \
  --arg ts "$TIMESTAMP" \
  --arg bp "$BASE_PATH" \
  '{
    topic: $topic,
    date: $date,
    status: "in_progress",
    phase: "interview",
    featurePath: null,
    module: null,
    basePath: $bp,
    created_at: $ts,
    updated_at: $ts
  }' > "$CONTEXT_FILE"

# Ensure the base directory exists for outputs
mkdir -p "$CWD/$BASE_PATH"

echo "🚀 ATDD initialized for \"$TOPIC\"" >&2
echo "📁 Base path: $BASE_PATH" >&2

exit 0
