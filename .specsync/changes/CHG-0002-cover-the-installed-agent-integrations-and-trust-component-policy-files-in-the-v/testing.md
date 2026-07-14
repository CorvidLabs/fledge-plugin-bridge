---
change: CHG-0002-cover-the-installed-agent-integrations-and-trust-component-policy-files-in-the-v
artifact: testing
---

# Testing

- Run `specsync agents status` and require Claude, Cursor, Codex, and Gemini to report installed.
- Run `specsync check --strict --require-coverage 100 --force`.
- Run the JDK 21 Gradle build lifecycle through `fledge lanes run verify`.
- Run `fledge trust doctor` and committed-range `fledge trust verify`.
