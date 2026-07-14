---
spec: security.spec.md
---

## Acceptance Criteria

### REQ-security-001

Read, write, and execute operations SHALL be independently capability-gated.

Acceptance Criteria
- Each operation checks only its corresponding immutable capability flag.
- A denied operation throws a descriptive `IllegalStateException`.

### REQ-security-002

Resolved file paths SHALL remain inside the configured sandbox, including through symbolic links.

Acceptance Criteria
- Absolute paths and NUL bytes are rejected.
- Normalized paths must start with the canonical sandbox root.
- Existing ancestors and target symlinks are rejected when their real paths escape the sandbox.

### REQ-security-003

Command validation SHALL reject empty input and NUL bytes without pretending a substring blocklist provides shell isolation.

Acceptance Criteria
- Blank commands and commands containing NUL bytes are rejected.
- Other command text is returned unchanged after exec capability is granted.
