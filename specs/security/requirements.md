---
spec: security.spec.md
---

## Acceptance Criteria

### REQ-security-001

Read, write, and execute operations SHALL be independently capability-gated.

### REQ-security-002

Resolved file paths SHALL remain inside the configured sandbox, including through symbolic links.

### REQ-security-003

Command validation SHALL reject empty input and NUL bytes without pretending a substring blocklist provides shell isolation.
