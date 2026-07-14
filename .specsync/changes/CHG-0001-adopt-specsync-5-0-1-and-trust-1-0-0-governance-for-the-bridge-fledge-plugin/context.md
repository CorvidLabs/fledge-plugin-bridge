---
change: CHG-0001-adopt-specsync-5-0-1-and-trust-1-0-0-governance-for-the-bridge-fledge-plugin
artifact: context
---

# Context

The Kotlin bridge already has three active security-sensitive specs and native Gradle CI. Migration must preserve outbound-only transport, capability gating, path sandboxing, audit behavior, product code, and packaging. Trust-managed Atlas publication remains disabled because this repository has no separate Atlas workflow to replace.
