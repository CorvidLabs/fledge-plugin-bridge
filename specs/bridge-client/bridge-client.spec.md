---
module: bridge-client
version: 2
status: active
files:
  - src/main/kotlin/com/corvidlabs/bridge/ws/BridgeClient.kt
  - src/main/kotlin/com/corvidlabs/bridge/ws/BridgeMessage.kt
  - src/main/kotlin/com/corvidlabs/bridge/ws/RequestHandler.kt
  - src/main/kotlin/com/corvidlabs/bridge/commands/ConnectCommand.kt
  - src/main/kotlin/com/corvidlabs/bridge/commands/StatusCommand.kt
  - src/main/kotlin/com/corvidlabs/bridge/commands/DisconnectCommand.kt
  - src/main/kotlin/com/corvidlabs/bridge/audit/AuditLog.kt
db_tables: []
depends_on:
  - specs/protocol/protocol.spec.md
  - specs/security/security.spec.md
tracks: [2285]
---

# Bridge Client

## Purpose

WebSocket client that establishes an outbound connection from a developer's local machine to a corvid-agent server. It handles capability-gated file requests inside a path sandbox and, when explicitly enabled, executes shell commands from a validated working directory. Command execution is not an operating-system sandbox. The connection is always outbound — no inbound ports are opened on the developer's machine.

## Public API

### Exported Classes

| Class | Description |
|-------|-------------|
| `BridgeClient` | WebSocket client that connects to corvid-agent and handles agent requests |
| `RequestHandler` | Dispatches incoming requests to file/exec handlers with capability checks |
| `ConnectCommand` | Clikt command: `bridge connect --server <url> --token <token>` |
| `StatusCommand` | Clikt command: `bridge status` — reports that no persistent session is active |
| `DisconnectCommand` | Clikt command: `bridge disconnect` — reports that there is no persistent session to terminate |

### BridgeClient Methods

| Method | Parameters | Returns | Description |
|--------|-----------|---------|-------------|
| `connect` | — | `Unit` (suspending) | Establishes WebSocket connection, authenticates, and enters request loop |

### RequestHandler Methods

| Method | Parameters | Returns | Description |
|--------|-----------|---------|-------------|
| `handle` | `request: BridgeRequest` | `BridgeResponse` | Routes request to appropriate handler based on type |

### Message Types

| Type | Direction | Description |
|------|-----------|-------------|
| `auth` | client → server | Authentication with token + capability declaration |
| `file.read` | server → client | Read a file at a sandboxed path |
| `file.write` | server → client | Write content to a sandboxed path |
| `file.list` | server → client | List directory contents |
| `exec` | server → client | Execute a shell command in sandbox |
| `ping` / `pong` | bidirectional | Keepalive |

### Export Inventory

| Export | Description |
|--------|-------------|
| `BridgeRequest` | Incoming request envelope. |
| `id` | Correlation identifier. |
| `type` | Protocol request discriminator. |
| `path` | Requested sandbox-relative path. |
| `content` | File-write content. |
| `pattern` | Optional list filter. |
| `command` | Requested shell command. |
| `cwd` | Requested working directory. |
| `timeout` | Command timeout in seconds. |
| `BridgeResponse` | Structured request result. |
| `success` | Result success flag. |
| `data` | Successful result payload. |
| `error` | Failure detail. |
| `AuthMessage` | Authentication request envelope. |
| `token` | Session authentication token. |
| `projectId` | Target project identifier. |
| `capabilities` | Declared session capabilities. |
| `CapabilitySet` | Serializable capability declaration. |
| `read` | Read capability flag. |
| `write` | Write capability flag. |
| `exec` | Execute capability flag. |
| `AuthResponse` | Authentication result. |
| `reason` | Authentication failure reason. |
| `handle` | Validate and dispatch one bridge request. |
| `thread` | Request execution thread reference. |
| `help` | Connect-command help text. |
| `run` | Execute the selected bridge command. |
| `AuditLog` | Append-only structured request audit logger. |
| `record` | Persist one audit entry. |

## Invariants

1. All connections are outbound from the developer's machine — never inbound.
2. Every file operation is validated against the sandbox root before execution.
3. Path traversal attempts (e.g., `../../etc/passwd`) are rejected.
4. Capabilities (read/write/exec) must be explicitly opted in at connect time.
5. The session dies when the CLI process exits — no background daemon.
6. Command execution has a configurable timeout (default 30s).
7. Authentication happens as the first message after WebSocket handshake.

## Behavioral Examples

### Scenario: Developer connects to corvid-agent
- **Given** corvid-agent is running at ws://localhost:3000 with a bridge endpoint
- **When** developer runs `fledge bridge connect --server ws://localhost:3000 --token <token>`
- **Then** an outbound WebSocket connects, authenticates, and enters the request loop

### Scenario: Agent reads a file via bridge
- **Given** an active bridge session with read capability enabled
- **When** agent sends `{"type": "file.read", "path": "src/Main.kt"}`
- **Then** the bridge reads the file within sandbox and returns its content

### Scenario: Path traversal blocked
- **Given** an active bridge session with sandbox at `/Users/kyn/projects/podo-android`
- **When** agent sends `{"type": "file.read", "path": "../../.ssh/id_rsa"}`
- **Then** the bridge returns an error: "Path escapes sandbox"

### Scenario: Exec without capability
- **Given** an active bridge session with exec capability disabled
- **When** agent sends `{"type": "exec", "command": "gradle build"}`
- **Then** the bridge returns an error: "Command execution is not allowed"

## Error Cases

| Condition | Behavior |
|-----------|----------|
| Server unreachable | Connection fails with error message, CLI exits |
| Invalid token | Server closes connection after auth, CLI reports auth failure |
| Path escapes sandbox | Request rejected with "Path escapes sandbox" error |
| Capability not granted | Request rejected with capability-specific error message |
| Command timeout | Process killed after timeout, error response sent |
| Empty or NUL-containing command | Request rejected before shell execution |
| WebSocket disconnect | CLI detects closure, prints "Disconnected", exits cleanly |

## Dependencies

### Consumes

| Module | What is used |
|--------|-------------|
| `security` | `CapabilityGuard` for path validation and capability checks |
| `protocol` | `FledgeProtocol` for fledge-v1 stdin/stdout communication |

### Consumed By

| Module | What is used |
|--------|-------------|
| corvid-agent server | `/api/bridge` endpoint relays agent requests to this client |

## Change Log

| Date | Author | Change |
|------|--------|--------|
| 2026-05-06 | CorvidAgent | Initial spec |
| 2026-07-14 | SpecSync | CHG-0001-adopt-specsync-5-0-1-and-trust-1-0-0-governance-for-the-bridge-fledge-plugin: Adopt SpecSync 5.0.1 and Trust 1.0.0 governance for the Bridge Fledge plugin |
