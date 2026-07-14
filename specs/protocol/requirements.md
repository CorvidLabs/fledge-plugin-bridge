---
spec: protocol.spec.md
---

## Acceptance Criteria

### REQ-protocol-001

The plugin SHALL decode fledge-v1 initialization and dispatch its requested command arguments.

Acceptance Criteria
- A JSON line with `type: init` is decoded as `InitMessage`.
- Arguments from a valid init message are passed to `BridgeCli`.

### REQ-protocol-002

The protocol adapter SHALL emit newline-delimited JSON and flush stdout after each output or log message.

Acceptance Criteria
- Output messages encode text in a JSON object with `type: output`.
- Log messages encode level and message fields in a JSON object with `type: log`.
- stdout is flushed after each emitted protocol message.

### REQ-protocol-003

The executable SHALL retain direct command-line operation when no valid initialization message is available.

Acceptance Criteria
- Empty, malformed, or non-init input yields no `InitMessage`.
- Process arguments are passed to `BridgeCli` when initialization is unavailable.
