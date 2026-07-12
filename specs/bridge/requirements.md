---
spec: bridge-client.spec.md
---

## Acceptance Criteria

### REQ-bridge-client-001

The client SHALL establish only outbound WebSocket connections and authenticate before processing requests.

### REQ-bridge-client-002

Every file and command request SHALL pass through capability and sandbox validation before execution.

### REQ-bridge-client-003

The bridge SHALL return structured responses and write an audit record for handled requests.
