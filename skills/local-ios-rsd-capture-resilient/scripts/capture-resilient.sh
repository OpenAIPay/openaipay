#!/usr/bin/env bash
set -euo pipefail

if [[ "$(id -u)" -eq 0 ]]; then
  echo "run this wrapper as the current macOS user, not root" >&2
  exit 2
fi

REPO_ROOT="${OPENAIPAY_ROOT:-/Users/<username>/code/openaipay}"
LABEL="${1:-当前页}"
ATTEMPTS="${LOCAL_CAPTURE_RESILIENT_ATTEMPTS:-5}"
SLEEP_SECONDS="${LOCAL_CAPTURE_RESILIENT_SLEEP_SECONDS:-1}"
TOOL="$REPO_ROOT/.tools/capture_from_local_rsd.sh"
CONFIG_FILE="$REPO_ROOT/.tools/local_capture_rsd.env"
KNOWN_GOOD_PYTHON="/Library/Frameworks/Python.framework/Versions/3.11/bin/python3"
LEGACY_FALLBACK_SCRIPT="${LOCAL_CAPTURE_LEGACY_FALLBACK_SCRIPT:-/Users/<username>/.codex/skills/local-ios-rsd-capture-legacy-plain/scripts/capture-legacy-plain.sh}"
ENABLE_LEGACY_FALLBACK="${LOCAL_CAPTURE_RESILIENT_ENABLE_LEGACY_FALLBACK:-1}"
LEGACY_FALLBACK_ATTEMPTS="${LOCAL_CAPTURE_RESILIENT_LEGACY_ATTEMPTS:-3}"
ASKPASS_FILE=""

cleanup() {
  [[ -n "$ASKPASS_FILE" ]] && rm -f "$ASKPASS_FILE"
}
trap cleanup EXIT

choose_python() {
  local candidate
  for candidate in "$KNOWN_GOOD_PYTHON" "$(command -v python3 2>/dev/null || true)"; do
    [[ -n "$candidate" && -x "$candidate" ]] || continue
    if "$candidate" - <<'PY' >/dev/null 2>&1
import importlib.util
import sys
sys.exit(0 if importlib.util.find_spec('pymobiledevice3') else 1)
PY
    then
      echo "$candidate"
      return 0
    fi
  done
  return 1
}

prepare_sudo() {
  if sudo -n true >/dev/null 2>&1; then
    return 0
  fi

  if [[ -n "${LOCAL_CAPTURE_SUDO_PASSWORD:-}" ]]; then
    printf '%s\n' "$LOCAL_CAPTURE_SUDO_PASSWORD" | sudo -S -p '' -v >/dev/null
    return 0
  fi

  if ! command -v osascript >/dev/null 2>&1; then
    echo "sudo credentials required; run sudo -v first" >&2
    return 1
  fi

  ASKPASS_FILE="$(mktemp /tmp/codex-sudo-askpass.XXXXXX)"
  cat > "$ASKPASS_FILE" <<'ASKPASS'
#!/bin/sh
exec osascript -e 'display dialog "Codex 需要管理员权限执行真机截图（local-ios-rsd-capture-resilient）" default answer "" with hidden answer buttons {"取消", "确定"} default button "确定"' -e 'text returned of result'
ASKPASS
  chmod 700 "$ASKPASS_FILE"
  export SUDO_ASKPASS="$ASKPASS_FILE"
  sudo -A -v >/dev/null
  sudo -n true >/dev/null
}

latest_batch() {
  find "$CAPTURE_ROOT" -maxdepth 1 -type d -name '采集批次_*' | sort | tail -n 1
}

validate_batch() {
  local batch_dir="$1"
  local png_file items_file caps_file meta_file

  png_file="$(find "$batch_dir" -maxdepth 1 -type f -name '页面截图_*.png' | head -n 1)"
  items_file="$(find "$batch_dir" -maxdepth 1 -type f -name '页面元素_*.json' | head -n 1)"
  caps_file="$(find "$batch_dir" -maxdepth 1 -type f -name '设备能力_*.json' | head -n 1)"
  meta_file="$(find "$batch_dir" -maxdepth 1 -type f -name '采集元数据_*.txt' | head -n 1)"

  [[ -n "$png_file" && -s "$png_file" ]] || { echo "png output missing: $batch_dir" >&2; return 2; }
  [[ -n "$items_file" && -s "$items_file" ]] || { echo "items json is empty: $batch_dir" >&2; return 3; }
  [[ -n "$caps_file" && -s "$caps_file" ]] || { echo "capabilities json is empty: $batch_dir" >&2; return 4; }

  echo "validated capture batch: $batch_dir"
  echo "python: $PYTHON_BIN"
  echo "PNG: $png_file"
  echo "ITEMS: $items_file"
  echo "CAPS: $caps_file"
  [[ -n "$meta_file" ]] && echo "META: $meta_file"
}

run_official_repo_capture() {
  local attempt_before attempt_after
  attempt_before="$(latest_batch || true)"
  if (
    cd "$REPO_ROOT"
    export LOCAL_CAPTURE_PYTHON="$PYTHON_BIN"
    bash "$TOOL" "$LABEL"
  ); then
    attempt_after="$(latest_batch || true)"
    if [[ -n "$attempt_after" && "$attempt_after" != "$attempt_before" ]]; then
      validate_batch "$attempt_after"
      return 0
    fi
    echo "capture succeeded but no new batch was detected" >&2
    return 5
  fi
  return 1
}

run_legacy_plain_fallback() {
  if [[ "$ENABLE_LEGACY_FALLBACK" != "1" ]]; then
    return 1
  fi

  if [[ ! -x "$LEGACY_FALLBACK_SCRIPT" ]]; then
    echo "legacy fallback script not found: $LEGACY_FALLBACK_SCRIPT" >&2
    return 1
  fi

  echo "=== FALLBACK legacy_log + current-user direct RSD ==="
  (
    cd "$REPO_ROOT"
    export OPENAIPAY_ROOT="$REPO_ROOT"
    export LOCAL_CAPTURE_PYTHON="$PYTHON_BIN"
    export LOCAL_CAPTURE_LEGACY_ATTEMPTS="$LEGACY_FALLBACK_ATTEMPTS"
    bash "$LEGACY_FALLBACK_SCRIPT" "$LABEL"
  )
}

if [[ ! -x "$TOOL" ]]; then
  echo "capture tool not found: $TOOL" >&2
  exit 1
fi

PYTHON_BIN="$(choose_python || true)"
if [[ -z "$PYTHON_BIN" ]]; then
  echo "no usable python with pymobiledevice3 found" >&2
  exit 1
fi

CAPTURE_ROOT="$REPO_ROOT/截图资料/01_设备采集"
if [[ -f "$CONFIG_FILE" ]]; then
  configured_root="$(awk -F= '/LOCAL_CAPTURE_ROOT/ {gsub(/"/,"",$2); print $2}' "$CONFIG_FILE" | tail -n 1)"
  if [[ -n "$configured_root" ]]; then
    CAPTURE_ROOT="$configured_root"
  fi
fi
mkdir -p "$CAPTURE_ROOT"

prepare_sudo

for attempt in $(seq 1 "$ATTEMPTS"); do
  echo "=== ATTEMPT $attempt (official repo tool) ==="
  if run_official_repo_capture; then
    exit 0
  fi
  sleep "$SLEEP_SECONDS"
done

if run_legacy_plain_fallback; then
  exit 0
fi

echo "capture failed after $ATTEMPTS official attempts and legacy fallback" >&2
exit 1
