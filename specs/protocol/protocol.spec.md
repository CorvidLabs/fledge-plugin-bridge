---
module: protocol
version: 1
status: active
files:
  - src/main/kotlin/com/corvidlabs/bridge/protocol/FledgeProtocol.kt
  - src/main/kotlin/com/corvidlabs/bridge/Main.kt
  - src/main/kotlin/com/corvidlabs/bridge/cli/BridgeCli.kt
db_tables: []
depends_on: []
---

# Fledge Protocol

## Purpose

Implements the fledge-v1 plugin protocol for communication between the fledge CLI and the bridge plugin. Handles initialization (reading the `init` message from stdin), output (writing JSON messages to stdout), and subcommand dispatch via Clikt.

## Public API

### Exported Classes

| Class | Description |
|-------|-------------|
| `FledgeProtocol` | Singleton handling fledge-v1 JSON-RPC over stdin/stdout |
| `BridgeCli` | Root Clikt command with `connect`, `status`, `disconnect` subcommands |

### FledgeProtocol Methods

| Method | Parameters | Returns | Description |
|--------|-----------|---------|-------------|
| `readInit` | — | `InitMessage?` | Reads and parses the fledge init message from stdin |
| `output` | `text: String` | `Unit` | Sends user-facing output to fledge |
| `log` | `level: String, message: String` | `Unit` | Sends a log message to fledge |
| `error` | `message: String` | `Unit` | Sends an error-level log to fledge |
| `info` | `message: String` | `Unit` | Sends an info-level log to fledge |

### Exported Types

| Type | Description |
|------|-------------|
| `InitMessage` | Deserialized fledge-v1 init payload |
| `ProjectInfo` | Project metadata from init message |
| `PluginInfo` | Plugin metadata from init message |
| `Capabilities` | Fledge capability flags (exec, store, metadata) |

## Invariants

1. `readInit` returns null if stdin is empty or the message is not a valid init message.
2. All output messages are newline-delimited JSON written to stdout.
3. The plugin can also run standalone (without fledge) by passing CLI args directly.
4. stdout is flushed after every message to prevent buffering deadlocks.

## Behavioral Examples

### Scenario: Plugin launched by fledge
- **Given** fledge sends an init message on stdin with args `["connect", "--server", "ws://localhost:3000", "--token", "abc"]`
- **When** the plugin starts
- **Then** `readInit` parses the init message and passes args to BridgeCli

### Scenario: Plugin launched standalone
- **Given** no init message on stdin (user runs binary directly)
- **When** the plugin starts
- **Then** `readInit` returns null, and CLI args from the process are used instead

## Error Cases

| Condition | Behavior |
|-----------|----------|
| Malformed init JSON | `readInit` returns null, falls back to standalone mode |
| Missing init type field | `readInit` returns null |
| stdin closed immediately | `readInit` returns null |

## Dependencies

### Consumes

| Module | What is used |
|--------|-------------|
| `kotlinx-serialization-json` | JSON parsing and encoding |
| `clikt` | CLI argument parsing and subcommand dispatch |

### Consumed By

| Module | What is used |
|--------|-------------|
| `bridge-client` | Uses `FledgeProtocol.output` and `FledgeProtocol.error` for user feedback |

## Change Log

| Date | Author | Change |
|------|--------|--------|
| 2026-05-06 | CorvidAgent | Initial spec |
