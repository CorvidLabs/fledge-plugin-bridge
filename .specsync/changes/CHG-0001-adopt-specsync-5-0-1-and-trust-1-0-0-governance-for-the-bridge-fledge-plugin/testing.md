---
change: CHG-0001-adopt-specsync-5-0-1-and-trust-1-0-0-governance-for-the-bridge-fledge-plugin
artifact: testing
---

# Testing

- REQ-bridge-client-001: inspect the outbound WebSocket URL, initial auth frame, auth timeout, and `auth-ok` gate; exercise existing BridgeClient tests through Gradle.
- REQ-bridge-client-002: run capability, traversal, symlink, file, and exec tests in `CapabilityGuardTest` and `RequestHandlerTest`.
- REQ-bridge-client-003: run structured response and audit-log tests in `RequestHandlerTest`.
- REQ-protocol-001: verify init decoding and argument dispatch through protocol and CLI tests/build checks.
- REQ-protocol-002: inspect JSON encoding and flush behavior in `FledgeProtocol`; run the Gradle build lifecycle.
- REQ-protocol-003: verify `Main.kt` dispatches process arguments when no valid init message is available.
- REQ-security-001: run independent read, write, and exec capability tests.
- REQ-security-002: run absolute-path, traversal, NUL, and symlink-escape tests.
- REQ-security-003: run blank/NUL command validation tests and confirm other commands remain unchanged.
- Governance: run `specsync check --strict --require-coverage 100 --force`, `specsync agents status`, `fledge lanes run verify`, `fledge trust doctor`, and committed-range `fledge trust verify`.
