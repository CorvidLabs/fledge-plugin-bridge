---
change: CHG-0001-adopt-specsync-5-0-1-and-trust-1-0-0-governance-for-the-bridge-fledge-plugin
artifact: research
---

# Research

SpecSync 5 found unmapped production sources and stale canonical claims across the existing companions. The implementation has no persistent session behind `status` or `disconnect`; explicit exec grants an unsandboxed shell with only its working directory path-validated; `FledgeProtocol` output is JSON but Clikt owns standalone output; and command validation rejects blank/NUL input without a substring blocklist. The repository also configured a ktlint task for a plugin the build does not apply. Gradle `build` already runs the test lifecycle, so one native build step is the non-duplicative verification lane.
