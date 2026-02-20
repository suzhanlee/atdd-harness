#!/bin/bash
# atdd-stop-hook.sh - Stop hook
#
# Purpose: Track ATDD pipeline state and cleanup when complete
# Activation: Stop event + session has atdd state
#
# Note: Skill orchestration is handled by the atdd skill, not this hook.
# This hook only tracks state and cleans up.
#
# Hook Input Fields (Stop):
#   - session_id: current session
#   - transcript_path: conversation log path
#   - cwd: current working directory

set -euo pipefail

# Read hook input from stdin
HOOK_INPUT=$(cat)

# Use Python for JSON parsing
parse_json() {
  python -c "import json,sys; d=json.load(sys.stdin); print(d.get('$1', ''))"
}

# Extract fields using Python
CWD=$(echo "$HOOK_INPUT" | parse_json 'cwd')
SESSION_ID=$(echo "$HOOK_INPUT" | parse_json 'session_id')

# State file path
STATE_FILE="$CWD/.atdd/state.json"

# Exit if no state file
if [[ ! -f "$STATE_FILE" ]]; then
  exit 0
fi

# Check and process state using Python
python - "$CWD" "$SESSION_ID" << 'PYTHON_SCRIPT'
import json
import os
import sys

cwd = sys.argv[1].replace('\\', '/')
session_id = sys.argv[2]
state_file = os.path.join(cwd, '.atdd', 'state.json')

if not os.path.exists(state_file):
    sys.exit(0)

with open(state_file, 'r') as f:
    state = json.load(f)

# Check if this session has atdd state
sessions = state.get('sessions', {})
if session_id not in sessions:
    sys.exit(0)

atdd = sessions[session_id].get('atdd', {})
if not atdd:
    sys.exit(0)

phase = atdd.get('phase', 'interview')
base_path = atdd.get('basePath', '')
topic = atdd.get('topic', '')

# Fallback: Try to get basePath from context.json if not in state
if not base_path:
    context_file = os.path.join(cwd, '.atdd', 'context.json')
    if os.path.exists(context_file):
        with open(context_file, 'r') as f:
            context = json.load(f)
            base_path = context.get('basePath', '')

if not base_path:
    sys.exit(0)

def cleanup_session():
    del state['sessions'][session_id]
    with open(state_file, 'w') as f:
        json.dump(state, f, indent=2)

def update_phase(new_phase):
    state['sessions'][session_id]['atdd']['phase'] = new_phase
    with open(state_file, 'w') as f:
        json.dump(state, f, indent=2)

# Check completion status
validation_report = os.path.join(cwd, base_path, 'validate', 'validation-report.md')

if os.path.exists(validation_report):
    with open(validation_report, 'r', encoding='utf-8') as f:
        content = f.read().lower()

    # Check if validation passed
    has_pass = 'pass' in content and ('✅' in content or '종합 결과: ✅' in content)

    if has_pass:
        # Update phase to done
        update_phase('done')
        print(f'🎉 ATDD: Pipeline complete for "{topic}"', file=sys.stderr)
        print(f'📁 Results: {base_path}', file=sys.stderr)
        # Cleanup session
        cleanup_session()
    else:
        print(f'⚠️ ATDD: Validation incomplete or failed for "{topic}"', file=sys.stderr)
else:
    print(f'📋 ATDD: Session stopped at phase "{phase}" for "{topic}"', file=sys.stderr)
    # Keep session state for potential resume

PYTHON_SCRIPT

exit 0
