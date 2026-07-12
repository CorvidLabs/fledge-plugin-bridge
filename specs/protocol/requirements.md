---
spec: protocol.spec.md
---

## Acceptance Criteria

### REQ-protocol-001

The plugin SHALL decode fledge-v1 initialization and dispatch its requested command arguments.

### REQ-protocol-002

The plugin SHALL emit newline-delimited JSON and flush stdout after each protocol message.

### REQ-protocol-003

The executable SHALL retain direct command-line operation when no valid initialization message is available.
