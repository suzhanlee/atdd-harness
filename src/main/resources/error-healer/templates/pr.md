## Summary

{{summary}}

## Root Cause

{{rootCause}}

## Changes

{{#each changes}}
- **{{file}}**: {{description}}
{{/each}}

## Test Plan

{{#each testScenarios}}
- [ ] {{this}}
{{/each}}

## Verification

- [x] All tests pass
- [x] No regression in existing tests
- [x] Code coverage maintained/improved

## Related

- Error ID: {{errorId}}
- Severity: {{severity}}
- Analysis Report: {{analysisReportUrl}}

---

🤖 Generated with [Claude Code Error Healer](https://github.com/anthropics/claude-code)
