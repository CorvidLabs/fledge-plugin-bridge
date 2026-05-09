# fledge-plugin-bridge

A Kotlin fledge plugin that bridges your local dev environment to [corvid-agent](https://github.com/CorvidLabs/corvid-agent) via outbound WebSocket. Agents can read/write files and run commands on your machine: securely, with capability gating and path sandboxing.

## Architecture

```
Your machine                      corvid-agent server
────────────                      ──────────────────
fledge bridge connect             /api/bridge endpoint
  └─ WebSocket (outbound) ──────→ WebSocket server
     Ktor + Clikt                 relays agent commands
```

**Key design decisions:**
- **Outbound-only**: no ports opened on your machine
- **Capability-gated**: read, write, exec each opt-in per session
- **Path-sandboxed**: all file ops confined to project directory
- **Session-scoped**: dies when you close the CLI

## Installation

```bash
fledge plugin install corvidlabs/fledge-plugin-bridge
```

Or clone and build manually:

```bash
git clone https://github.com/CorvidLabs/fledge-plugin-bridge.git
cd fledge-plugin-bridge
./gradlew jar
bash hooks/build.sh
```

Requires JDK 21+.

## Usage

### Connect (read-only)

```bash
fledge bridge connect \
  --server ws://localhost:3000 \
  --token <your-token>
```

### Connect with write + exec

```bash
fledge bridge connect \
  --server ws://localhost:3000 \
  --token <your-token> \
  --sandbox /Users/kyn/projects/podo-android \
  --allow-write true \
  --allow-exec true
```

### Check status

```bash
fledge bridge status
```

### Disconnect

```bash
fledge bridge disconnect
```

## Security Model

| Layer | Protection |
|-------|-----------|
| Transport | Outbound WebSocket — no inbound ports |
| Auth | Token-scoped to agent + project |
| Capabilities | Read/write/exec each opt-in at connect |
| Path sandbox | All file ops confined to `--sandbox` directory |
| Exec gating | `--allow-exec` is opt-in; operator accepts risk of granting shell access |
| Audit log | Every request logged to `~/.fledge/bridge-audit.log` (NDJSON) |
| Session scope | Connection dies when CLI exits |

## Protocol

JSON messages over WebSocket:

| Type | Direction | Description |
|------|-----------|-------------|
| `auth` | client → server | Authenticate with token + declare capabilities |
| `file.read` | server → client | Read file contents |
| `file.write` | server → client | Write file contents |
| `file.list` | server → client | List directory entries |
| `exec` | server → client | Execute a shell command |
| `ping` / `pong` | bidirectional | Keepalive |

## Specs

This project uses [spec-sync](https://github.com/CorvidLabs/spec-sync) for spec-to-code validation:

- `specs/bridge/bridge-client.spec.md` — WebSocket client and request handling
- `specs/protocol/protocol.spec.md` — Fledge-v1 plugin protocol
- `specs/security/security.spec.md` — Capability guard and path sandboxing

## Development

```bash
./gradlew build          # Build
./gradlew test           # Run tests
fledge lanes run verify  # Full verification pipeline
```

## Related

- [corvid-agent#2285](https://github.com/CorvidLabs/corvid-agent/issues/2285) — Server-side bridge endpoint
- [kt-algochat](https://github.com/CorvidLabs/kt-algochat) — Kotlin AlgoChat library
- [podo-android](https://github.com/CorvidLabs/podo-android) — Primary consumer of this plugin

## License

MIT
