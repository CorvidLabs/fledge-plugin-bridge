## MODIFIED

### SPEC SECTION Invariants

1. Capabilities default to read-only — write and exec must be explicitly enabled.
2. Path validation normalizes paths and rejects any path that resolves outside the sandbox root.
3. Symbolic links that resolve outside the sandbox are rejected.
4. Command validation rejects NUL bytes and empty input but does not maintain a command blocklist. With `--allow-exec true` the operator grants shell access; trust is their responsibility.
5. CapabilityGuard is immutable after construction — capabilities cannot be escalated mid-session.

### REQUIREMENT REQ-security-001

Read, write, and execute operations SHALL be independently capability-gated.

Acceptance Criteria
- Each operation checks only its corresponding immutable capability flag.
- A denied operation throws a descriptive `IllegalStateException`.

### REQUIREMENT REQ-security-002

Resolved file paths SHALL remain inside the configured sandbox, including through symbolic links.

Acceptance Criteria
- Absolute paths and NUL bytes are rejected.
- Normalized paths must start with the canonical sandbox root.
- Existing ancestors and target symlinks are rejected when their real paths escape the sandbox.

### REQUIREMENT REQ-security-003

Command validation SHALL reject empty input and NUL bytes without pretending a substring blocklist provides shell isolation.

Acceptance Criteria
- Blank commands and commands containing NUL bytes are rejected.
- Other command text is returned unchanged after exec capability is granted.
