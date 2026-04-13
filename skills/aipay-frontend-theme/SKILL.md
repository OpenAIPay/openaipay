---
name: aipay-frontend-theme
description: Use when changing AiPay iOS frontend color style, replacing blue UI to purple, updating screenshot-based Reference pages, or enforcing component theme consistency across parallel threads. Applies to SwiftUI components and Assets.xcassets *Reference.imageset image replacements.
---

# AiPay Frontend Theme

## Overview

This skill standardizes AiPay frontend theme changes so all threads produce the same result.
It enforces token-first runtime styling and direct asset replacement for screenshot-based pages.

## Workflow

1. Read contract:
- `/Users/<username>/code/openaipay/docs/frontend-theme-component-contract.md`

2. If changing runtime components:
- Use `AppTheme.palette` tokens from:
  `/Users/<username>/code/openaipay/ios-app/OpenAiPay/Core/AppTheme.swift`
- Do not add new blue hardcoded colors.

3. If changing Reference pages:
- Replace pixels directly in `Assets.xcassets/*Reference.imageset/*.png`.
- Do not add overlay/blend/tint layers to fake recolor.

4. Validate before finishing:
- Run:
  `scripts/check-theme-contract.sh`
- Build iOS app to verify no regressions.

5. Report output:
- List changed files.
- Explain whether change was token-based or asset replacement.
- Provide preview screenshot path when UI changed.

## Guardrails

- Prefer the smallest possible change area.
- Never introduce a new full-screen color overlay for recoloring.
- If a rule must be bypassed, add `theme-check:allow` on that line and explain why.

## References

- `references/contract.md`
