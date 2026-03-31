#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="${OPENAIPAY_RUN_DIR:-${ROOT_DIR}/.run_pids}"
BACKEND_PORT="${OPENAIPAY_BACKEND_PORT:-8080}"
BFF_PORT="${OPENAIPAY_BFF_PORT:-3000}"
WEBSITE_PORT="${OPENAIPAY_WEBSITE_PORT:-5173}"
CLEAN_CONFLICT_PORTS="${OPENAIPAY_CLEAN_CONFLICT_PORTS:-1}"

SUPERVISOR_PID_FILE="${RUN_DIR}/backend_bff_guard.pid"
BACKEND_PID_FILE="${RUN_DIR}/backend.pid"
BFF_PID_FILE="${RUN_DIR}/bff.pid"
WEBSITE_PID_FILE="${RUN_DIR}/website.pid"

read_pid() {
  local pid_file="$1"
  if [[ ! -f "${pid_file}" ]]; then
    return 1
  fi
  tr -d '[:space:]' < "${pid_file}"
}

is_pid_alive() {
  local pid="${1:-}"
  if [[ -z "${pid}" ]]; then
    return 1
  fi
  kill -0 "${pid}" >/dev/null 2>&1
}

pid_command() {
  local pid="${1:-}"
  if [[ -z "${pid}" ]]; then
    return 1
  fi
  ps -p "${pid}" -o command= 2>/dev/null | head -n 1
}

pid_command_contains() {
  local pid="$1"
  local fragment="$2"
  local cmd=""
  cmd="$(pid_command "${pid}" || true)"
  [[ -n "${cmd}" && "${cmd}" == *"${fragment}"* ]]
}

is_guard_pid_expected() {
  local pid="$1"
  pid_command_contains "${pid}" "start-backend-bff-guard.sh" \
    && pid_command_contains "${pid}" "--foreground"
}

is_backend_pid_expected() {
  local pid="$1"
  pid_command_contains "${pid}" "start-backend-mysql.sh" \
    || pid_command_contains "${pid}" "spring-boot:run" \
    || pid_command_contains "${pid}" "AiPayApplication" \
    || pid_command_contains "${pid}" "backend/adapter-web"
}

is_bff_pid_expected() {
  local pid="$1"
  pid_command_contains "${pid}" "app-bff" \
    || pid_command_contains "${pid}" "node dist/main" \
    || pid_command_contains "${pid}" "npm run start:prod" \
    || pid_command_contains "${pid}" "npm run start"
}

is_website_pid_expected() {
  local pid="$1"
  pid_command_contains "${pid}" "website/server.mjs" \
    || pid_command_contains "${pid}" "node server.mjs"
}

listener_pid() {
  local port="$1"
  lsof -tiTCP:"${port}" -sTCP:LISTEN 2>/dev/null | head -n 1 || true
}

stop_pid_direct() {
  local pid="$1"
  local service_name="$2"

  if ! is_pid_alive "${pid}"; then
    return 0
  fi

  echo "Stopping ${service_name} (pid=${pid})..."
  kill "${pid}" >/dev/null 2>&1 || true
  for _ in {1..20}; do
    if ! is_pid_alive "${pid}"; then
      break
    fi
    sleep 0.5
  done
  if is_pid_alive "${pid}"; then
    echo "Force killing ${service_name} (pid=${pid})..."
    kill -9 "${pid}" >/dev/null 2>&1 || true
  fi
}

stop_pid_file() {
  local pid_file="$1"
  local service_name="$2"
  local checker="${3:-}"
  local pid=""

  if ! pid="$(read_pid "${pid_file}")"; then
    return 0
  fi

  if ! is_pid_alive "${pid}"; then
    rm -f "${pid_file}"
    return 0
  fi

  if [[ -n "${checker}" ]] && ! "${checker}" "${pid}"; then
    echo "Skip ${service_name}: pid file points to unexpected process (pid=${pid}, cmd=$(pid_command "${pid}" || true))"
    rm -f "${pid_file}"
    return 0
  fi

  stop_pid_direct "${pid}" "${service_name}"
  rm -f "${pid_file}"
}

stop_listener_if_needed() {
  local port="$1"
  local service_name="$2"
  local checker="$3"
  local pid=""
  local cmd=""

  pid="$(listener_pid "${port}")"
  if [[ -z "${pid}" ]]; then
    return 0
  fi

  cmd="$(pid_command "${pid}" || true)"
  if "${checker}" "${pid}"; then
    stop_pid_direct "${pid}" "${service_name} listener"
    return 0
  fi

  if [[ "${CLEAN_CONFLICT_PORTS}" == "1" ]]; then
    echo "Port :${port} occupied by unexpected process, force cleaning (pid=${pid}, cmd=${cmd:-unknown})..."
    stop_pid_direct "${pid}" "${service_name} listener"
  else
    echo "Port :${port} occupied by unexpected process, skip cleaning (pid=${pid}, cmd=${cmd:-unknown})"
  fi
}

stop_pid_file "${SUPERVISOR_PID_FILE}" "backend+bff+website guard" is_guard_pid_expected
stop_pid_file "${BACKEND_PID_FILE}" "backend" is_backend_pid_expected
stop_pid_file "${BFF_PID_FILE}" "bff" is_bff_pid_expected
stop_pid_file "${WEBSITE_PID_FILE}" "website" is_website_pid_expected
stop_listener_if_needed "${BACKEND_PORT}" "backend" is_backend_pid_expected
stop_listener_if_needed "${BFF_PORT}" "bff" is_bff_pid_expected
stop_listener_if_needed "${WEBSITE_PORT}" "website" is_website_pid_expected

echo "✅ backend+bff+website guard stopped"
