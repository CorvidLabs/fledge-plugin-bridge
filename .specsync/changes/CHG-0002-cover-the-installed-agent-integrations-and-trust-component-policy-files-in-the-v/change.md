---
id: CHG-0002-cover-the-installed-agent-integrations-and-trust-component-policy-files-in-the-v
state: accepted
type: operations
base_commit: 68db237920b6f0035236d1d07c9397fb26d0bce2
---

# Cover the installed agent integrations and Trust component policy files in the verified SDD delivery without changing the canonical contract

## Intent

Cover the installed agent integrations and Trust component policy files in the verified SDD delivery without changing the canonical contract

## Affected Canonical Specs

- None

## Acceptance Criteria

- All four agent integrations report installed; managed Trust rules are present; Augur and Attest policies parse; strict SpecSync coverage remains 100 percent; native Gradle and unified Trust verification pass

## No-spec Rationale

These files configure the approved governance toolchain and agent integrations; they do not change Kotlin runtime behavior or the canonical bridge contract.
