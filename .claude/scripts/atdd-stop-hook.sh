#!/bin/bash
# atdd-stop-hook.sh - Stop hook
#
# Purpose: Manage ATDD pipeline transitions when Claude stops
# Activation: Stop event + session has atdd state
#
# Flow:
#   phase: interview + requirements-draft.md exists → epic-split or validate
#   phase: epic-split + epics.md exists             → validate
#   phase: validate + validation-report.md (PASS)   → cleanup
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
iteration = atdd.get('iteration', 0)
max_iterations = atdd.get('max_iterations', 10)
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

# Check max iterations
if max_iterations > 0 and iteration >= max_iterations:
    print(f'🛑 ATDD: Max iterations ({max_iterations}) reached.', file=sys.stderr)
    del state['sessions'][session_id]
    with open(state_file, 'w') as f:
        json.dump(state, f, indent=2)
    sys.exit(0)

def update_phase(new_phase):
    state['sessions'][session_id]['atdd']['phase'] = new_phase
    state['sessions'][session_id]['atdd']['iteration'] = iteration + 1
    with open(state_file, 'w') as f:
        json.dump(state, f, indent=2)

def cleanup_session():
    del state['sessions'][session_id]
    with open(state_file, 'w') as f:
        json.dump(state, f, indent=2)

def count_features(file_path):
    if not os.path.exists(file_path):
        return 0
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    # Count checkbox items
    return content.count('- [')

# Phase transition logic
if phase == 'interview':
    requirements_file = os.path.join(cwd, base_path, 'interview', 'requirements-draft.md')

    if not os.path.exists(requirements_file):
        # No requirements yet - interview still in progress
        sys.exit(0)

    feature_count = count_features(requirements_file)

    if feature_count <= 3:
        # Skip epic-split, go directly to validate
        update_phase('validate')
        print(f'✅ ATDD: Interview complete ({feature_count} features). Skipping epic-split → /validate', file=sys.stderr)
        print(json.dumps({
            'decision': 'block',
            'reason': f'Interview complete! Requirements are small enough ({feature_count} features).\nExecute: Skill("validate")'
        }))
    else:
        # Need epic-split
        update_phase('epic-split')
        print(f'📋 ATDD: Interview complete ({feature_count} features). Need epic-split.', file=sys.stderr)
        print(json.dumps({
            'decision': 'block',
            'reason': f'Interview complete! Requirements have {feature_count} features.\nExecute: Skill("epic-split")'
        }))

elif phase == 'epic-split':
    epics_file = os.path.join(cwd, base_path, 'interview', 'epics.md')

    if not os.path.exists(epics_file):
        # No epics yet - epic-split still in progress
        sys.exit(0)

    # Epic-split complete - go to validate
    update_phase('validate')
    print('✅ ATDD: Epic-split complete → /validate', file=sys.stderr)
    print(json.dumps({
        'decision': 'block',
        'reason': 'Epic-split complete! Now validate the requirements.\nExecute: Skill("validate")'
    }))

elif phase == 'validate':
    report_file = os.path.join(cwd, base_path, 'validate', 'validation-report.md')

    if not os.path.exists(report_file):
        # No report yet - validation still in progress
        sys.exit(0)

    # Check if validation passed
    with open(report_file, 'r', encoding='utf-8') as f:
        content = f.read().lower()

    has_pass = 'pass' in content and '✅' in content

    if has_pass:
        # Validation passed - cleanup
        cleanup_session()
        print('🎉 ATDD: Validation PASSED! Complete.', file=sys.stderr)
    else:
        # Check for failure
        if 'fail' in content and '❌' in content:
            print('⚠️ ATDD: Validation FAILED. Needs revision.', file=sys.stderr)

elif phase == 'done':
    cleanup_session()
else:
    print(f'⚠️ ATDD: Unknown phase "{phase}"', file=sys.stderr)
    cleanup_session()
PYTHON_SCRIPT

exit 0
