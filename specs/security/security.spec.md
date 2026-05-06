---
module: security
version: 1
status: active
files:
  - src/main/kotlin/com/corvidlabs/bridge/security/CapabilityGuard.kt
db_tables: []
depends_on: []
---

# Security

## Purpose

Enforces the bridge's security model: capability-gated operations (read/write/exec each opt-in), path sandboxing to prevent traversal attacks, and command safety filtering. Every request passes through CapabilityGuard before execution.

## Public API

### Exported Classes

| Class | Description |
|-------|-------------|
| `CapabilityGuard` | Validates capabilities, paths, and commands for every bridge request |

### CapabilityGuard Methods

| Method | Parameters | Returns | Description |
|--------|-----------|---------|-------------|
| `assertRead` | — | `Unit` | Throws if read not allowed |
| `assertWrite` | — | `Unit` | Throws if write not allowed |
| `assertExec` | — | `Unit` | Throws if exec not allowed |
| `validatePath` | `requestedPath: String` | `Path` | Resolves path within sandbox, rejects traversal |
| `validateCommand` | `command: String` | `String` | Checks command against blocklist |

### Constructor Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `allowRead` | `Boolean` | Whether file read operations are permitted |
| `allowWrite` | `Boolean` | Whether file write operations are permitted |
| `allowExec` | `Boolean` | Whether command execution is permitted |
| `sandboxRoot` | `String` | Root directory for path sandboxing |

## Invariants

1. Capabilities default to read-only — write and exec must be explicitly enabled.
2. Path validation normalizes paths and rejects any path that resolves outside the sandbox root.
3. Symbolic links that resolve outside the sandbox are rejected.
4. The command blocklist prevents catastrophic system commands (rm -rf /, fork bombs, etc.).
5. CapabilityGuard is immutable after construction — capabilities cannot be escalated mid-session.

## Behavioral Examples

### Scenario: Read-only session
- **Given** a CapabilityGuard with allowRead=true, allowWrite=false, allowExec=false
- **When** `assertRead()` is called
- **Then** it succeeds

### Scenario: Write blocked in read-only session
- **Given** a CapabilityGuard with allowWrite=false
- **When** `assertWrite()` is called
- **Then** it throws IllegalStateException

### Scenario: Path traversal attack
- **Given** sandbox root is `/Users/kyn/project`
- **When** `validatePath("../../../etc/passwd")` is called
- **Then** it throws IllegalArgumentException with "Path escapes sandbox"

### Scenario: Safe path within sandbox
- **Given** sandbox root is `/Users/kyn/project`
- **When** `validatePath("src/Main.kt")` is called
- **Then** it returns `/Users/kyn/project/src/Main.kt`

### Scenario: Blocked command
- **Given** a CapabilityGuard with allowExec=true
- **When** `validateCommand("rm -rf /")` is called
- **Then** it throws IllegalArgumentException

## Error Cases

| Condition | Behavior |
|-----------|----------|
| Capability not granted | `IllegalStateException` with descriptive message |
| Path escapes sandbox | `IllegalArgumentException` with "Path escapes sandbox" |
| Blocked command | `IllegalArgumentException` with "Command blocked by safety filter" |

## Dependencies

### Consumes

| Module | What is used |
|--------|-------------|
| `java.nio.file.Path` | Path resolution and normalization |

### Consumed By

| Module | What is used |
|--------|-------------|
| `bridge-client` | `RequestHandler` validates every request through `CapabilityGuard` |

## Change Log

| Date | Author | Change |
|------|--------|--------|
| 2026-05-06 | CorvidAgent | Initial spec |
