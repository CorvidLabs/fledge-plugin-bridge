## MODIFIED

### SPEC SECTION Purpose

WebSocket client that establishes an outbound connection from a developer's local machine to a corvid-agent server. It handles capability-gated file requests inside a path sandbox and, when explicitly enabled, executes shell commands from a validated working directory. Command execution is not an operating-system sandbox. The connection is always outbound — no inbound ports are opened on the developer's machine.

### SPEC SECTION Error Cases

| Condition | Behavior |
|-----------|----------|
| Server unreachable | Connection fails with error message, CLI exits |
| Invalid token | Server closes connection after auth, CLI reports auth failure |
| Path escapes sandbox | Request rejected with "Path escapes sandbox" error |
| Capability not granted | Request rejected with capability-specific error message |
| Command timeout | Process killed after timeout, error response sent |
| Empty or NUL-containing command | Request rejected before shell execution |
| WebSocket disconnect | CLI detects closure, prints "Disconnected", exits cleanly |

### REQUIREMENT REQ-bridge-client-001

The client SHALL establish only outbound WebSocket connections and authenticate before processing requests.

Acceptance Criteria
- The client appends `/api/bridge` to the configured server URL and opens a client WebSocket.
- The first client frame is an authentication message.
- Request processing begins only after an explicit `auth-ok` response.

### REQUIREMENT REQ-bridge-client-002

Every file and command request SHALL pass through capability and sandbox validation before execution.

Acceptance Criteria
- File reads and listings require read capability; file writes require write capability; commands require exec capability.
- File paths and command working directories are rejected when they escape the configured sandbox root.
- Command text is rejected when empty or when it contains a NUL byte.

### REQUIREMENT REQ-bridge-client-003

The bridge SHALL return structured responses and write an audit record for handled requests.

Acceptance Criteria
- Decoded requests return a `BridgeResponse` with the request correlation ID and success or error state.
- Every decoded request handled by `RequestHandler` attempts an append-only audit record.
- Audit records omit tokens and response bodies.
