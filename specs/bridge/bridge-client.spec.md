---
module: bridge-client
version: 1
status: active
files:
  - src/main/kotlin/com/corvidlabs/bridge/ws/BridgeClient.kt
  - src/main/kotlin/com/corvidlabs/bridge/ws/BridgeMessage.kt
  - src/main/kotlin/com/corvidlabs/bridge/ws/RequestHandler.kt
  - src/main/kotlin/com/corvidlabs/bridge/commands/ConnectCommand.kt
  - src/main/kotlin/com/corvidlabs/bridge/commands/StatusCommand.kt
  - src/main/kotlin/com/corvidlabs/bridge/commands/DisconnectCommand.kt
db_tables: []
depends_on:
  - specs/protocol/protocol.spec.md
  - specs/security/security.spec.md
tracks: [2285]
---

# Bridge Client

## Purpose

WebSocket client that establishes an outbound connection from a developer's local machine to a corvid-agent server. Receives file and exec requests from agents, executes them locally within a sandboxed scope, and returns results. The connection is always outbound — no inbound ports are opened on the developer's machine.

## Public API

### Exported Classes

| Class | Description |
|-------|-------------|
| `BridgeClient` | WebSocket client that connects to corvid-agent and handles agent requests |
| `RequestHandler` | Dispatches incoming requests to file/exec handlers with capability checks |
| `ConnectCommand` | Clikt command: `bridge connect --server <url> --token <token>` |
| `StatusCommand` | Clikt command: `bridge status` — shows connection state |
| `DisconnectCommand` | Clikt command: `bridge disconnect` — terminates active session |

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
| Blocked command | Request rejected by safety filter |
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
