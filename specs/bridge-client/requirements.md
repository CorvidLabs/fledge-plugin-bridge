---
spec: bridge-client.spec.md
---

## Acceptance Criteria

### REQ-bridge-client-001

The client SHALL establish only outbound WebSocket connections and authenticate before processing requests.

Acceptance Criteria
- The client appends `/api/bridge` to the configured server URL and opens a client WebSocket.
- The first client frame is an authentication message.
- Request processing begins only after an explicit `auth-ok` response.

### REQ-bridge-client-002

Every file and command request SHALL pass through capability and sandbox validation before execution.

Acceptance Criteria
- File reads and listings require read capability; file writes require write capability; commands require exec capability.
- File paths and command working directories are rejected when they escape the configured sandbox root.
- Command text is rejected when empty or when it contains a NUL byte.

### REQ-bridge-client-003

The bridge SHALL return structured responses and write an audit record for handled requests.

Acceptance Criteria
- Decoded requests return a `BridgeResponse` with the request correlation ID and success or error state.
- Every decoded request handled by `RequestHandler` attempts an append-only audit record.
- Audit records omit tokens and response bodies.
