---
name: local-ios-rsd-capture-resilient
description: "Capture the current page from my USB-connected iPhone on this Mac using the proven OpenAiPay real-device RSD chain: first run the repo capture tool as the current user with pre-warmed sudo and retries, then fall back to the proven legacy-log plus current-user direct RSD path when the official route keeps failing with CoreDevice pairing churn or OSError 65 No route to host. Use when the user wants a real iPhone screenshot plus runtime accessibility data from /Users/<username>/code/openaipay and the capture path must stay resilient after cable reconnects or endpoint changes."
---

# Local iOS RSD Capture Resilient

## Overview

Use this skill for the environment-specific capture flow that has already been proven on my Mac.
It keeps the original official repo-tool workflow in place and adds the successful `legacy_log + current-user direct RSD` fallback that worked for the loan success and loan detail pages on March 9, 2026.

## Workflow

1. Confirm the iPhone is connected by USB, unlocked, awake, and already on the target page.
2. Run `scripts/capture-resilient.sh <label>` from `/Users/<username>/code/openaipay`.
3. Let the wrapper refresh `sudo` credentials, but keep the main capture process running as the current macOS user.
4. Let it retry the official repo capture flow first.
5. If the official flow still fails, let it fall back to the proven `legacy_log + current-user direct RSD` capture path.
6. On success, open the newest batch under `截图资料/01_设备采集` and use these files first:
   - `页面截图_*.png`
   - `页面元素_*.json`
   - `设备能力_*.json`
   - `采集元数据_*.txt`

## Rules

- Never run the whole capture wrapper as root.
- Never wrap the whole repo script with `osascript do shell script ... with administrator privileges`; that can switch pairing context and trigger `The device must be paired before it can be connected`.
- Prefer `/Library/Frameworks/Python.framework/Versions/3.11/bin/python3` when it has `pymobiledevice3`.
- Keep the original repo-tool route as the first path; the legacy direct-RSD route is a fallback, not a replacement.
- Treat capture success as: non-empty PNG, non-empty accessibility items JSON, and non-empty accessibility capabilities JSON.
- If the wrapper still fails, report the exact endpoint and error instead of claiming success.

## Troubleshooting

- If the device was just rebooted, replug the cable, unlock the phone, and keep the target page visible before retrying.
- If you see `OSError: [Errno 65] No route to host`, rerun the whole wrapper instead of reusing the old endpoint; the tunnel host and port can change between attempts.
- If the official route keeps failing, allow the wrapper to continue into the legacy fallback instead of switching to simulator capture.
- If you see `com.apple.dt.CoreDeviceError error 2` or `device must be paired`, check whether the capture was accidentally started as root and rerun it as the logged-in user.
- If the wrapper needs admin permission, allow the `sudo` dialog or run `sudo -v` first.

## Resources

### scripts/

- `scripts/capture-resilient.sh`: current-user wrapper that first retries the official repo-tool flow, then falls back to the proven `legacy_log + current-user direct RSD` path, and validates that PNG plus both JSON outputs are non-empty.
