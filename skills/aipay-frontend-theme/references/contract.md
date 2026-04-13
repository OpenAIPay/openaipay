# AiPay Theme Contract Reference

Primary contract (source of truth):

- `/Users/<username>/code/openaipay/docs/frontend-theme-component-contract.md`

Quick rules:

1. Runtime components use `AppTheme.palette` tokens.
2. Reference pages must be recolored by asset pixel replacement.
3. Do not use overlay/blend/tint layers to fake recolor.
4. Run `scripts/check-theme-contract.sh` before finishing.
