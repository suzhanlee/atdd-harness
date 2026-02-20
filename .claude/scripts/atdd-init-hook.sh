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

# Use Python for JSON parsing (more portable on Windows)
parse_json() {
  python -c "import json,sys; d=json.load(sys.stdin); print(d.get('$1', ''))"
}

# Extract fields using Python
CWD=$(echo "$HOOK_INPUT" | parse_json 'cwd')
SESSION_ID=$(echo "$HOOK_INPUT" | parse_json 'session_id')
PROMPT=$(echo "$HOOK_INPUT" | parse_json 'prompt')

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

# Initialize state file using Python
python - "$CWD" "$SESSION_ID" "$TOPIC" << 'PYTHON_SCRIPT'
import json
import os
import sys
from datetime import datetime

cwd = sys.argv[1].replace('\\', '/')
session_id = sys.argv[2]
topic = sys.argv[3]

# Normalize path
state_file = os.path.join(cwd, '.atdd', 'state.json')

# Ensure .atdd directory exists
os.makedirs(os.path.dirname(state_file), exist_ok=True)

# Load or create state
if os.path.exists(state_file):
    with open(state_file, 'r') as f:
        state = json.load(f)
else:
    state = {
        'version': '1.0.0',
        'project': {
            'name': 'atdd-harness',
            'description': 'ATDD Harness for Java/Spring',
            'techStack': ['Java 17+', 'Spring Boot 3.x', 'MySQL', 'Cucumber', 'RestAssured', 'JUnit5']
        },
        'phases': {
            'interview': {'status': 'pending', 'startedAt': None, 'completedAt': None, 'outputs': []},
            'validate': {'status': 'pending', 'startedAt': None, 'completedAt': None, 'outputs': []},
            'design': {'status': 'pending', 'startedAt': None, 'completedAt': None, 'outputs': []},
            'gherkin': {'status': 'pending', 'startedAt': None, 'completedAt': None, 'outputs': []},
            'tdd': {'status': 'pending', 'startedAt': None, 'completedAt': None, 'outputs': []},
            'refactor': {'status': 'pending', 'startedAt': None, 'completedAt': None, 'outputs': []},
            'verify': {'status': 'pending', 'startedAt': None, 'completedAt': None, 'outputs': []}
        },
        'currentPhase': None,
        'history': [],
        'sessions': {}
    }

# Ensure sessions field exists
if 'sessions' not in state:
    state['sessions'] = {}

# Check if session already has atdd state
if session_id in state.get('sessions', {}) and 'atdd' in state['sessions'].get(session_id, {}):
    # Already initialized
    sys.exit(0)

# Calculate basePath
date = datetime.now().strftime('%Y-%m-%d')
base_path = f'.atdd/{date}/{topic}'
timestamp = datetime.now().astimezone().isoformat()

# Initialize atdd state
state['sessions'][session_id] = {
    'created_at': timestamp,
    'atdd': {
        'phase': 'interview',
        'iteration': 0,
        'max_iterations': 10,
        'basePath': base_path,
        'topic': topic
    }
}

with open(state_file, 'w') as f:
    json.dump(state, f, indent=2)

print(f'🚀 ATDD initialized for "{topic}" (session: {session_id[:8]}...)', file=sys.stderr)
PYTHON_SCRIPT

exit 0
