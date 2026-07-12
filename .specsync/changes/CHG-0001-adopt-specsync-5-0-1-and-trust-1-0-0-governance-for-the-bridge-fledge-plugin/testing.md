---
change: CHG-0001-adopt-specsync-5-0-1-and-trust-1-0-0-governance-for-the-bridge-fledge-plugin
artifact: testing
---

# Testing

- `specsync check --strict --require-coverage 100 --force`
- `fledge lanes run verify` with JDK 21
- `specsync agents status`
- `fledge trust doctor`
- `fledge trust verify`
- Hosted native `test` and unified `trust` jobs
