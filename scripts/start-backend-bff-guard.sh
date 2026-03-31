#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="${OPENAIPAY_RUN_DIR:-${ROOT_DIR}/.run_pids}"
LOG_DIR="${OPENAIPAY_LOG_DIR:-${ROOT_DIR}/.run_logs}"

SUPERVISOR_PID_FILE="${RUN_DIR}/backend_bff_guard.pid"
BACKEND_PID_FILE="${RUN_DIR}/backend.pid"
BFF_PID_FILE="${RUN_DIR}/bff.pid"
WEBSITE_PID_FILE="${RUN_DIR}/website.pid"

SUPERVISOR_LOG_FILE="${LOG_DIR}/backend_bff_guard.log"
BACKEND_LOG_ALIAS_FILE="${LOG_DIR}/backend.log"
BFF_LOG_ALIAS_FILE="${LOG_DIR}/bff.log"
WEBSITE_LOG_ALIAS_FILE="${LOG_DIR}/website.log"
BACKEND_LOG_FILE="${LOG_DIR}/backend.$(date '+%Y%m%d').log"
BFF_LOG_FILE="${LOG_DIR}/bff.$(date '+%Y%m%d').log"
WEBSITE_LOG_FILE="${LOG_DIR}/website.$(date '+%Y%m%d').log"

BACKEND_START_SCRIPT="${ROOT_DIR}/scripts/start-backend-mysql.sh"
BFF_DIR="${ROOT_DIR}/app-bff"
WEBSITE_DIR="${ROOT_DIR}/website"
BACKEND_PORT="${OPENAIPAY_BACKEND_PORT:-8080}"
BFF_PORT="${OPENAIPAY_BFF_PORT:-3000}"
WEBSITE_PORT="${OPENAIPAY_WEBSITE_PORT:-5173}"
DEFAULT_DB_PASSWORD="${OPENAIPAY_DB_PASSWORD_DEFAULT:-openaipay}"
DEFAULT_TOKEN_SIGNING_SECRET="${OPENAIPAY_TOKEN_SIGNING_SECRET_DEFAULT:-openaipay-local-signing-secret-please-change}"
DEFAULT_INTERNAL_CONFIG_TOKEN="${OPENAIPAY_INTERNAL_CONFIG_TOKEN_DEFAULT:-openaipay-local-internal-config-token}"

BACKEND_HEALTH_URL="${OPENAIPAY_BACKEND_HEALTH_URL:-http://127.0.0.1:${BACKEND_PORT}/actuator/health}"
BFF_HEALTH_URL="${OPENAIPAY_BFF_HEALTH_URL:-http://127.0.0.1:${BFF_PORT}/health}"
WEBSITE_HEALTH_URL="${OPENAIPAY_WEBSITE_HEALTH_URL:-http://127.0.0.1:${WEBSITE_PORT}/}"

HEALTH_CHECK_INTERVAL="${OPENAIPAY_GUARD_CHECK_INTERVAL:-5}"
HEALTH_MAX_FAILS="${OPENAIPAY_GUARD_MAX_FAILS:-3}"
STARTUP_WAIT_SECONDS="${OPENAIPAY_GUARD_STARTUP_WAIT_SECONDS:-90}"
QUICK_HEALTH_SECONDS="${OPENAIPAY_GUARD_QUICK_HEALTH_SECONDS:-6}"
CLEAN_CONFLICT_PORTS="${OPENAIPAY_CLEAN_CONFLICT_PORTS:-1}"
RESTART_ON_START="${OPENAIPAY_RESTART_ON_START:-1}"
LOG_STDOUT="${OPENAIPAY_LOG_STDOUT:-auto}"
BACKEND_FLYWAY_AUTO_FALLBACK="${OPENAIPAY_BACKEND_FLYWAY_AUTO_FALLBACK:-1}"
BACKEND_RUNTIME_FLYWAY_ENABLED="${OPENAIPAY_BACKEND_RUNTIME_FLYWAY_ENABLED:-${SPRING_FLYWAY_ENABLED:-}}"

mode="${1:-}"

ensure_dirs() {
  mkdir -p "${RUN_DIR}" "${LOG_DIR}"
  touch "${SUPERVISOR_LOG_FILE}"
  prepare_service_log_file "backend"
  prepare_service_log_file "bff"
  prepare_service_log_file "website"
}

current_log_date() {
  date '+%Y%m%d'
}

prepare_service_log_file() {
  local service_name="$1"
  local dated_file=""
  local alias_file=""

  case "${service_name}" in
    backend)
      dated_file="${LOG_DIR}/backend.$(current_log_date).log"
      alias_file="${BACKEND_LOG_ALIAS_FILE}"
      BACKEND_LOG_FILE="${dated_file}"
      ;;
    bff)
      dated_file="${LOG_DIR}/bff.$(current_log_date).log"
      alias_file="${BFF_LOG_ALIAS_FILE}"
      BFF_LOG_FILE="${dated_file}"
      ;;
    website)
      dated_file="${LOG_DIR}/website.$(current_log_date).log"
      alias_file="${WEBSITE_LOG_ALIAS_FILE}"
      WEBSITE_LOG_FILE="${dated_file}"
      ;;
    *)
      log "未知服务日志类型: ${service_name}"
      return 1
      ;;
  esac

  touch "${dated_file}"
  rm -f "${alias_file}"
  ln -s "$(basename "${dated_file}")" "${alias_file}"
}

log() {
  local message="$1"
  local line
  line="[$(date '+%Y-%m-%d %H:%M:%S')] ${message}"
  printf '%s\n' "${line}" >> "${SUPERVISOR_LOG_FILE}"
  if [[ "${LOG_STDOUT}" == "1" ]] || [[ "${LOG_STDOUT}" == "auto" && -t 1 ]]; then
    printf '%s\n' "${line}"
  fi
}

spawn_detached() {
  local log_file="$1"
  shift
  if command -v perl >/dev/null 2>&1; then
    perl -MPOSIX=setsid -e '
      my $log = shift @ARGV;
      my $pid = fork();
      die "fork failed: $!" unless defined $pid;
      if ($pid) {
        print "$pid\n";
        exit 0;
      }
      setsid() or die "setsid failed: $!";
      open STDIN, "<", "/dev/null" or die "open stdin failed: $!";
      open STDOUT, ">>", $log or die "open stdout failed: $!";
      open STDERR, ">>", $log or die "open stderr failed: $!";
      exec @ARGV or die "exec failed: $!";
    ' "${log_file}" "$@"
    return 0
  fi

  nohup "$@" >>"${log_file}" 2>&1 </dev/null &
  echo $!
}

progress() {
  local current="$1"
  local total="$2"
  local message="$3"
  log "[${current}/${total}] ${message}"
}

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
    || pid_command_contains "${pid}" "node server.mjs" \
    || pid_command_contains "${pid}" "node ${WEBSITE_DIR}/server.mjs"
}

validate_pid_file() {
  local pid_file="$1"
  local service_name="$2"
  local checker="$3"
  local pid=""

  if ! pid="$(read_pid "${pid_file}")"; then
    return 1
  fi
  if ! is_pid_alive "${pid}"; then
    rm -f "${pid_file}"
    return 1
  fi
  if ! "${checker}" "${pid}"; then
    local cmd=""
    cmd="$(pid_command "${pid}" || true)"
    log "${service_name} pid 文件命中非预期进程 (pid=${pid}, cmd=${cmd:-unknown})，清理陈旧 pid。"
    rm -f "${pid_file}"
    return 1
  fi

  printf '%s\n' "${pid}"
  return 0
}

listener_pid() {
  local port="$1"
  lsof -tiTCP:"${port}" -sTCP:LISTEN 2>/dev/null | head -n 1 || true
}

select_runtime_pid() {
  local pid_file="$1"
  local port="$2"
  local checker="$3"
  local fallback_pid="${4:-}"
  local listener=""

  listener="$(listener_pid "${port}")"
  if [[ -n "${listener}" ]] && "${checker}" "${listener}"; then
    if [[ "${listener}" != "${fallback_pid}" ]]; then
      echo "${listener}" > "${pid_file}"
    fi
    printf '%s\n' "${listener}"
    return 0
  fi

  printf '%s\n' "${fallback_pid}"
  return 1
}

is_healthy() {
  local url="$1"
  curl -fsS --max-time 2 "${url}" >/dev/null 2>&1
}

stop_pid_direct() {
  local pid="$1"
  local service_name="$2"

  if ! is_pid_alive "${pid}"; then
    return 0
  fi

  log "停止 ${service_name} (pid=${pid})..."
  kill "${pid}" >/dev/null 2>&1 || true
  for _ in {1..20}; do
    if ! is_pid_alive "${pid}"; then
      break
    fi
    sleep 0.5
  done
  if is_pid_alive "${pid}"; then
    log "${service_name} 未在预期时间内退出，执行强制停止 (pid=${pid})"
    kill -9 "${pid}" >/dev/null 2>&1 || true
  fi
}

stop_service_by_pid_file() {
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
    local cmd=""
    cmd="$(pid_command "${pid}" || true)"
    log "${service_name} pid 文件指向非预期进程 (pid=${pid}, cmd=${cmd:-unknown})，仅清理 pid 文件。"
    rm -f "${pid_file}"
    return 0
  fi

  stop_pid_direct "${pid}" "${service_name}"
  rm -f "${pid_file}"
}

cleanup_listener_port() {
  local port="$1"
  local service_name="$2"
  local checker="$3"
  local listener=""
  local cmd=""

  listener="$(listener_pid "${port}")"
  if [[ -z "${listener}" ]]; then
    return 0
  fi

  cmd="$(pid_command "${listener}" || true)"
  if "${checker}" "${listener}"; then
    log "检测到 ${service_name} 旧监听进程占用 :${port} (pid=${listener})，执行清理。"
    stop_pid_direct "${listener}" "${service_name}"
    return 0
  fi

  if [[ "${CLEAN_CONFLICT_PORTS}" == "1" ]]; then
    log "警告: :${port} 被非预期进程占用 (pid=${listener}, cmd=${cmd:-unknown})，按配置执行清理。"
    stop_pid_direct "${listener}" "${service_name} 端口占用进程"
    return 0
  fi

  log "错误: :${port} 被非预期进程占用 (pid=${listener}, cmd=${cmd:-unknown})，请手动处理或设置 OPENAIPAY_CLEAN_CONFLICT_PORTS=1"
  return 1
}

reuse_or_cleanup_existing_service() {
  local pid_file="$1"
  local service_name="$2"
  local checker="$3"
  local port="$4"
  local health_url="$5"
  local pid=""

  if pid="$(validate_pid_file "${pid_file}" "${service_name}" "${checker}")"; then
    if is_healthy "${health_url}"; then
      local runtime_pid=""
      runtime_pid="$(select_runtime_pid "${pid_file}" "${port}" "${checker}" "${pid}" || true)"
      if [[ -n "${runtime_pid}" && "${runtime_pid}" != "${pid}" ]]; then
        log "${service_name} 已切换为实际监听进程 pid (pid=${runtime_pid}, port=:${port})"
        pid="${runtime_pid}"
      fi
      log "${service_name} 已运行且健康，直接复用 (pid=${pid}, port=:${port})"
      return 0
    fi
    log "${service_name} pid 文件存在，但健康检查失败，准备重启 (pid=${pid}, port=:${port})"
    stop_service_by_pid_file "${pid_file}" "${service_name}" "${checker}"
  fi

  pid="$(listener_pid "${port}")"
  if [[ -n "${pid}" ]]; then
    if "${checker}" "${pid}" && is_healthy "${health_url}"; then
      echo "${pid}" > "${pid_file}"
      log "${service_name} 监听进程健康可用，直接复用 (pid=${pid}, port=:${port})"
      return 0
    fi
    if "${checker}" "${pid}"; then
      log "${service_name} 监听进程存在但不健康，准备清理 (pid=${pid}, port=:${port})"
    fi
    cleanup_listener_port "${port}" "${service_name}" "${checker}" || return 1
    rm -f "${pid_file}"
  fi

  return 1
}

wait_until_healthy() {
  local service_name="$1"
  local url="$2"
  local timeout_seconds="$3"
  local pid_file="$4"
  local port="$5"
  local service_log_file="$6"
  local checker="${7:-}"
  local deadline=$((SECONDS + timeout_seconds))
  local last_reported=-1

  log "等待 ${service_name} 健康检查通过 (${url}, timeout=${timeout_seconds}s)..."

  while (( SECONDS < deadline )); do
    if is_healthy "${url}"; then
      local pid=""
      local runtime_pid=""
      pid="$(read_pid "${pid_file}" || true)"
      if [[ -n "${checker}" ]]; then
        runtime_pid="$(select_runtime_pid "${pid_file}" "${port}" "${checker}" "${pid}" || true)"
        if [[ -n "${runtime_pid}" && "${runtime_pid}" != "${pid}" ]]; then
          log "${service_name} 已切换为实际监听进程 pid (pid=${runtime_pid}, port=:${port})"
          pid="${runtime_pid}"
        fi
      fi
      log "${service_name} 健康检查通过 (${url}, pid=${pid:-unknown}, port=:${port})"
      return 0
    fi

    local elapsed=$((timeout_seconds - (deadline - SECONDS)))
    if (( elapsed == 0 || elapsed >= last_reported + 5 )); then
      local pid=""
      local listener=""
      pid="$(read_pid "${pid_file}" || true)"
      listener="$(listener_pid "${port}")"
      log "${service_name} 启动中... elapsed=${elapsed}s/${timeout_seconds}s, pid=${pid:-none}, listener=${listener:-none}, port=:${port}"
      last_reported=${elapsed}
    fi
    sleep 1
  done

  log "${service_name} 健康检查超时 (${timeout_seconds}s): ${url}"
  if [[ -f "${service_log_file}" ]]; then
    local summary=""
    summary="$(grep -E 'Detected failed migration|Script V[0-9]+__|Message    :|BUILD FAILURE|Application run failed|Error starting ApplicationContext' "${service_log_file}" | tail -n 8 || true)"
    if [[ -n "${summary}" ]]; then
      log "${service_name} 错误摘要如下:"
      while IFS= read -r line; do
        log "${service_name}! ${line}"
      done <<< "${summary}"
    fi
    log "${service_name} 最近日志如下 (${service_log_file}):"
    tail -n 20 "${service_log_file}" | while IFS= read -r line; do
      log "${service_name}> ${line}"
    done
  fi
  return 1
}

ensure_backend_password() {
  if [[ -n "${OPENAIPAY_DB_PASSWORD:-}" ]]; then
    return 0
  fi
  if [[ -n "${SPRING_DATASOURCE_PASSWORD:-}" ]]; then
    export OPENAIPAY_DB_PASSWORD="${SPRING_DATASOURCE_PASSWORD}"
    return 0
  fi
  if [[ -n "${DEFAULT_DB_PASSWORD}" ]]; then
    export OPENAIPAY_DB_PASSWORD="${DEFAULT_DB_PASSWORD}"
    return 0
  fi
  if [[ -t 0 ]]; then
    read -r -s -p "OpenAiPay local MySQL password: " OPENAIPAY_DB_PASSWORD
    echo
    export OPENAIPAY_DB_PASSWORD
    return 0
  fi
  log "未设置 OPENAIPAY_DB_PASSWORD，且当前为非交互终端，无法启动 backend。"
  return 1
}

report_db_config_status() {
  local db_host="${OPENAIPAY_DB_HOST:-127.0.0.1}"
  local db_port="${OPENAIPAY_DB_PORT:-3306}"
  local db_name="${OPENAIPAY_DB_NAME:-portal}"
  local db_user="${OPENAIPAY_DB_USERNAME:-openaipay}"
  log "数据库配置: ${db_user}@${db_host}:${db_port}/${db_name}"
  return 0
}

contains_flyway_validate_mismatch() {
  local service_log_file="$1"
  if [[ ! -f "${service_log_file}" ]]; then
    return 1
  fi
  grep -Eqi \
    'migrations have failed validation|validate failed|migration checksum mismatch|checksum mismatch for migration version|flywayexception|detected failed migration' \
    "${service_log_file}"
}

should_try_backend_flyway_fallback() {
  if [[ "${BACKEND_FLYWAY_AUTO_FALLBACK}" != "1" ]]; then
    return 1
  fi
  if [[ "${BACKEND_RUNTIME_FLYWAY_ENABLED:-}" == "false" ]]; then
    return 1
  fi
  contains_flyway_validate_mismatch "${BACKEND_LOG_FILE}"
}

start_backend_with_health_check() {
  if ! start_backend; then
    return 1
  fi

  if wait_until_healthy "backend" "${BACKEND_HEALTH_URL}" "${STARTUP_WAIT_SECONDS}" "${BACKEND_PID_FILE}" "${BACKEND_PORT}" "${BACKEND_LOG_FILE}" is_backend_pid_expected; then
    return 0
  fi

  if ! should_try_backend_flyway_fallback; then
    return 1
  fi

  log "检测到 Flyway 历史校验差异，自动切换为 SPRING_FLYWAY_ENABLED=false 重新拉起 backend。"
  cleanup_service_before_start "${BACKEND_PID_FILE}" "backend" is_backend_pid_expected "${BACKEND_PORT}"
  BACKEND_RUNTIME_FLYWAY_ENABLED="false"

  if ! start_backend; then
    return 1
  fi
  if ! wait_until_healthy "backend" "${BACKEND_HEALTH_URL}" "${STARTUP_WAIT_SECONDS}" "${BACKEND_PID_FILE}" "${BACKEND_PORT}" "${BACKEND_LOG_FILE}" is_backend_pid_expected; then
    return 1
  fi

  log "backend 已以 SPRING_FLYWAY_ENABLED=false 成功启动（联调兜底模式）。建议后续修复 Flyway 历史校验差异后恢复默认校验。"
  return 0
}

start_backend() {
  if reuse_or_cleanup_existing_service \
    "${BACKEND_PID_FILE}" "backend" is_backend_pid_expected \
    "${BACKEND_PORT}" "${BACKEND_HEALTH_URL}"; then
    return 0
  fi

  prepare_service_log_file "backend"
  local flyway_enabled_display=""
  flyway_enabled_display="${BACKEND_RUNTIME_FLYWAY_ENABLED:-default}"
  log "启动 backend (maven install + spring-boot:run, SPRING_FLYWAY_ENABLED=${flyway_enabled_display})..."
  local pid=""
  local backend_env=(
    "OPENAIPAY_DB_PASSWORD=${OPENAIPAY_DB_PASSWORD:-}"
    "SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD:-}"
    "OPENAIPAY_FLYWAY_LOCATIONS=${OPENAIPAY_FLYWAY_LOCATIONS:-filesystem:${ROOT_DIR}/local/flyway-migration}"
    "OPENAIPAY_TOKEN_SIGNING_SECRET=${OPENAIPAY_TOKEN_SIGNING_SECRET:-${DEFAULT_TOKEN_SIGNING_SECRET}}"
    "OPENAIPAY_INTERNAL_CONFIG_TOKEN=${OPENAIPAY_INTERNAL_CONFIG_TOKEN:-${DEFAULT_INTERNAL_CONFIG_TOKEN}}"
    "OPENAIPAY_ALLOW_INSECURE_DEFAULTS=${OPENAIPAY_ALLOW_INSECURE_DEFAULTS:-true}"
  )
  if [[ -n "${BACKEND_RUNTIME_FLYWAY_ENABLED:-}" ]]; then
    backend_env+=("SPRING_FLYWAY_ENABLED=${BACKEND_RUNTIME_FLYWAY_ENABLED}")
  fi
  pid="$(
    cd "${ROOT_DIR}"
    spawn_detached "${BACKEND_LOG_FILE}" env \
      "${backend_env[@]}" \
      bash "${BACKEND_START_SCRIPT}"
  )"
  echo "${pid}" > "${BACKEND_PID_FILE}"
  log "backend 启动命令已提交 (pid=${pid:-unknown}, log=${BACKEND_LOG_FILE})"
}

start_bff() {
  if reuse_or_cleanup_existing_service \
    "${BFF_PID_FILE}" "bff" is_bff_pid_expected \
    "${BFF_PORT}" "${BFF_HEALTH_URL}"; then
    return 0
  fi

  local start_cmd="${OPENAIPAY_BFF_START_CMD:-}"
  if [[ -z "${start_cmd}" ]]; then
    if [[ -f "${BFF_DIR}/dist/main.js" ]]; then
      start_cmd="npm run start:prod"
    else
      start_cmd="npm run start"
    fi
  fi

  prepare_service_log_file "bff"
  log "启动 bff (${start_cmd})..."
  local pid=""
  pid="$(
    cd "${BFF_DIR}"
    spawn_detached "${BFF_LOG_FILE}" env \
      OPENAIPAY_TOKEN_SIGNING_SECRET="${OPENAIPAY_TOKEN_SIGNING_SECRET:-${DEFAULT_TOKEN_SIGNING_SECRET}}" \
      OPENAIPAY_INTERNAL_CONFIG_TOKEN="${OPENAIPAY_INTERNAL_CONFIG_TOKEN:-${DEFAULT_INTERNAL_CONFIG_TOKEN}}" \
      bash -lc "${start_cmd}"
  )"
  echo "${pid}" > "${BFF_PID_FILE}"
  log "bff 启动命令已提交 (pid=${pid:-unknown}, log=${BFF_LOG_FILE})"
}

start_website() {
  if reuse_or_cleanup_existing_service \
    "${WEBSITE_PID_FILE}" "website" is_website_pid_expected \
    "${WEBSITE_PORT}" "${WEBSITE_HEALTH_URL}"; then
    return 0
  fi

  if [[ ! -d "${WEBSITE_DIR}" ]]; then
    log "website 目录不存在: ${WEBSITE_DIR}"
    return 1
  fi

  if [[ ! -f "${WEBSITE_DIR}/server.mjs" ]]; then
    log "website 启动文件不存在: ${WEBSITE_DIR}/server.mjs"
    return 1
  fi

  prepare_service_log_file "website"
  log "启动 website (node server.mjs, port=${WEBSITE_PORT})..."
  local pid=""
  pid="$(
    cd "${WEBSITE_DIR}"
    spawn_detached "${WEBSITE_LOG_FILE}" env \
      PORT="${WEBSITE_PORT}" \
      node server.mjs
  )"
  echo "${pid}" > "${WEBSITE_PID_FILE}"
  log "website 启动命令已提交 (pid=${pid:-unknown}, log=${WEBSITE_LOG_FILE})"
}

stop_existing_supervisor() {
  local pid=""
  if ! pid="$(read_pid "${SUPERVISOR_PID_FILE}")"; then
    return 0
  fi

  if ! is_pid_alive "${pid}"; then
    rm -f "${SUPERVISOR_PID_FILE}"
    return 0
  fi

  if is_guard_pid_expected "${pid}"; then
    stop_pid_direct "${pid}" "backend+bff+website guard"
  else
    local cmd=""
    cmd="$(pid_command "${pid}" || true)"
    log "守护 pid 文件命中非预期进程 (pid=${pid}, cmd=${cmd:-unknown})，仅清理 pid 文件。"
  fi
  rm -f "${SUPERVISOR_PID_FILE}"
}

cleanup_service_before_start() {
  local pid_file="$1"
  local service_name="$2"
  local checker="$3"
  local port="$4"

  stop_service_by_pid_file "${pid_file}" "${service_name}" "${checker}"
  cleanup_listener_port "${port}" "${service_name}" "${checker}"
}

cleanup_all() {
  log "收到退出信号，开始清理 backend/bff/website..."
  stop_service_by_pid_file "${BACKEND_PID_FILE}" "backend" is_backend_pid_expected
  stop_service_by_pid_file "${BFF_PID_FILE}" "bff" is_bff_pid_expected
  stop_service_by_pid_file "${WEBSITE_PID_FILE}" "website" is_website_pid_expected
  cleanup_listener_port "${BACKEND_PORT}" "backend" is_backend_pid_expected || true
  cleanup_listener_port "${BFF_PORT}" "bff" is_bff_pid_expected || true
  cleanup_listener_port "${WEBSITE_PORT}" "website" is_website_pid_expected || true
  rm -f "${SUPERVISOR_PID_FILE}"
  log "守护进程退出"
}

run_foreground_supervisor() {
  ensure_dirs
  trap cleanup_all INT TERM

  local existing_pid=""
  if existing_pid="$(read_pid "${SUPERVISOR_PID_FILE}" 2>/dev/null || true)" && [[ -n "${existing_pid}" ]] && is_pid_alive "${existing_pid}"; then
    if is_guard_pid_expected "${existing_pid}" && [[ "${existing_pid}" != "$$" ]]; then
      log "守护进程已在运行 (pid=${existing_pid})，无需重复启动。"
      exit 0
    fi
  fi

  echo $$ > "${SUPERVISOR_PID_FILE}"
  log "backend+bff+website 守护进程启动 (pid=$$)"

  local backend_fail_count=0
  local bff_fail_count=0
  local website_fail_count=0

  while true; do
    sleep "${HEALTH_CHECK_INTERVAL}"

    local backend_ready=1
    local backend_pid=""
    if ! backend_pid="$(validate_pid_file "${BACKEND_PID_FILE}" "backend" is_backend_pid_expected)"; then
      local backend_listener=""
      backend_listener="$(listener_pid "${BACKEND_PORT}")"
      if [[ -n "${backend_listener}" ]] && is_backend_pid_expected "${backend_listener}" && is_healthy "${BACKEND_HEALTH_URL}"; then
        echo "${backend_listener}" > "${BACKEND_PID_FILE}"
        backend_fail_count=0
        backend_ready=1
        log "backend pid 文件失效，但监听进程健康，已重新绑定 (pid=${backend_listener}, port=:${BACKEND_PORT})"
      else
        backend_fail_count=0
        log "检测到 backend 进程不存在，尝试重启..."
        if start_backend_with_health_check; then
          backend_ready=1
        else
          backend_ready=0
        fi
      fi
    elif is_healthy "${BACKEND_HEALTH_URL}"; then
      backend_fail_count=0
    else
      backend_fail_count=$((backend_fail_count + 1))
      backend_ready=0
      log "backend 健康检查失败 (${backend_fail_count}/${HEALTH_MAX_FAILS})"
      if (( backend_fail_count >= HEALTH_MAX_FAILS )); then
        log "backend 连续健康检查失败，执行重启"
        cleanup_service_before_start "${BACKEND_PID_FILE}" "backend" is_backend_pid_expected "${BACKEND_PORT}"
        if start_backend_with_health_check; then
          backend_ready=1
        else
          backend_ready=0
        fi
        backend_fail_count=0
      fi
    fi

    if (( backend_ready == 0 )); then
      local bff_pid=""
      if bff_pid="$(read_pid "${BFF_PID_FILE}" 2>/dev/null || true)" && [[ -n "${bff_pid}" ]] && is_pid_alive "${bff_pid}"; then
        log "backend 未就绪，停止 bff 以避免对外提供假可用状态"
        stop_service_by_pid_file "${BFF_PID_FILE}" "bff" is_bff_pid_expected
      fi
      continue
    fi

    local bff_pid=""
    if ! bff_pid="$(validate_pid_file "${BFF_PID_FILE}" "bff" is_bff_pid_expected)"; then
      local bff_listener=""
      bff_listener="$(listener_pid "${BFF_PORT}")"
      if [[ -n "${bff_listener}" ]] && is_bff_pid_expected "${bff_listener}" && is_healthy "${BFF_HEALTH_URL}"; then
        echo "${bff_listener}" > "${BFF_PID_FILE}"
        bff_fail_count=0
        log "bff pid 文件失效，但监听进程健康，已重新绑定 (pid=${bff_listener}, port=:${BFF_PORT})"
      else
        bff_fail_count=0
        log "检测到 bff 进程不存在，尝试重启..."
        if start_bff; then
          wait_until_healthy "bff" "${BFF_HEALTH_URL}" "${STARTUP_WAIT_SECONDS}" "${BFF_PID_FILE}" "${BFF_PORT}" "${BFF_LOG_FILE}" is_bff_pid_expected || true
        fi
      fi
    elif is_healthy "${BFF_HEALTH_URL}"; then
      bff_fail_count=0
    else
      bff_fail_count=$((bff_fail_count + 1))
      log "bff 健康检查失败 (${bff_fail_count}/${HEALTH_MAX_FAILS})"
      if (( bff_fail_count >= HEALTH_MAX_FAILS )); then
        log "bff 连续健康检查失败，执行重启"
        cleanup_service_before_start "${BFF_PID_FILE}" "bff" is_bff_pid_expected "${BFF_PORT}"
        if start_bff; then
          wait_until_healthy "bff" "${BFF_HEALTH_URL}" "${STARTUP_WAIT_SECONDS}" "${BFF_PID_FILE}" "${BFF_PORT}" "${BFF_LOG_FILE}" is_bff_pid_expected || true
        fi
        bff_fail_count=0
      fi
    fi

    local website_pid=""
    if ! website_pid="$(validate_pid_file "${WEBSITE_PID_FILE}" "website" is_website_pid_expected)"; then
      local website_listener=""
      website_listener="$(listener_pid "${WEBSITE_PORT}")"
      if [[ -n "${website_listener}" ]] && is_website_pid_expected "${website_listener}" && is_healthy "${WEBSITE_HEALTH_URL}"; then
        echo "${website_listener}" > "${WEBSITE_PID_FILE}"
        website_fail_count=0
        log "website pid 文件失效，但监听进程健康，已重新绑定 (pid=${website_listener}, port=:${WEBSITE_PORT})"
      else
        website_fail_count=0
        log "检测到 website 进程不存在，尝试重启..."
        if start_website; then
          wait_until_healthy "website" "${WEBSITE_HEALTH_URL}" "${STARTUP_WAIT_SECONDS}" "${WEBSITE_PID_FILE}" "${WEBSITE_PORT}" "${WEBSITE_LOG_FILE}" is_website_pid_expected || true
        fi
      fi
    elif is_healthy "${WEBSITE_HEALTH_URL}"; then
      website_fail_count=0
    else
      website_fail_count=$((website_fail_count + 1))
      log "website 健康检查失败 (${website_fail_count}/${HEALTH_MAX_FAILS})"
      if (( website_fail_count >= HEALTH_MAX_FAILS )); then
        log "website 连续健康检查失败，执行重启"
        cleanup_service_before_start "${WEBSITE_PID_FILE}" "website" is_website_pid_expected "${WEBSITE_PORT}"
        if start_website; then
          wait_until_healthy "website" "${WEBSITE_HEALTH_URL}" "${STARTUP_WAIT_SECONDS}" "${WEBSITE_PID_FILE}" "${WEBSITE_PORT}" "${WEBSITE_LOG_FILE}" is_website_pid_expected || true
        fi
        website_fail_count=0
      fi
    fi
  done
}

start_daemon() {
  ensure_dirs
  ensure_backend_password

  progress 1 7 "检查数据库配置"
  report_db_config_status

  progress 2 7 "清理旧守护进程"
  stop_existing_supervisor

  if [[ "${RESTART_ON_START}" == "1" ]]; then
    progress 3 7 "清理旧 backend / bff / website 进程"
    cleanup_service_before_start "${BACKEND_PID_FILE}" "backend" is_backend_pid_expected "${BACKEND_PORT}"
    cleanup_service_before_start "${BFF_PID_FILE}" "bff" is_bff_pid_expected "${BFF_PORT}"
    cleanup_service_before_start "${WEBSITE_PID_FILE}" "website" is_website_pid_expected "${WEBSITE_PORT}"
  else
    progress 3 7 "保留现有服务，启动时按需复用"
  fi

  progress 4 7 "启动 backend"
  if ! start_backend_with_health_check; then
    echo "❌ backend 启动或健康检查失败"
    echo "   查看日志: ${BACKEND_LOG_FILE} / ${SUPERVISOR_LOG_FILE}"
    exit 1
  fi

  progress 5 7 "启动 bff"
  if ! start_bff; then
    echo "❌ bff 启动命令提交失败"
    echo "   查看日志: ${BFF_LOG_FILE} / ${SUPERVISOR_LOG_FILE}"
    exit 1
  fi
  if ! wait_until_healthy "bff" "${BFF_HEALTH_URL}" "${STARTUP_WAIT_SECONDS}" "${BFF_PID_FILE}" "${BFF_PORT}" "${BFF_LOG_FILE}" is_bff_pid_expected; then
    echo "❌ bff 健康检查失败"
    echo "   查看日志: ${BFF_LOG_FILE} / ${SUPERVISOR_LOG_FILE}"
    exit 1
  fi

  progress 6 7 "启动 website"
  if ! start_website; then
    echo "❌ website 启动命令提交失败"
    echo "   查看日志: ${WEBSITE_LOG_FILE} / ${SUPERVISOR_LOG_FILE}"
    exit 1
  fi
  if ! wait_until_healthy "website" "${WEBSITE_HEALTH_URL}" "${STARTUP_WAIT_SECONDS}" "${WEBSITE_PID_FILE}" "${WEBSITE_PORT}" "${WEBSITE_LOG_FILE}" is_website_pid_expected; then
    echo "❌ website 健康检查失败"
    echo "   查看日志: ${WEBSITE_LOG_FILE} / ${SUPERVISOR_LOG_FILE}"
    exit 1
  fi

  progress 7 7 "启动 backend+bff+website 守护进程"
  local spawned_guard_pid=""
  spawned_guard_pid="$(spawn_detached "${SUPERVISOR_LOG_FILE}" env \
    OPENAIPAY_DB_PASSWORD="${OPENAIPAY_DB_PASSWORD:-}" \
    SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-}" \
    OPENAIPAY_BACKEND_FLYWAY_AUTO_FALLBACK="${BACKEND_FLYWAY_AUTO_FALLBACK}" \
    OPENAIPAY_BACKEND_RUNTIME_FLYWAY_ENABLED="${BACKEND_RUNTIME_FLYWAY_ENABLED:-}" \
    SPRING_FLYWAY_ENABLED="${SPRING_FLYWAY_ENABLED:-}" \
    OPENAIPAY_BFF_START_CMD="${OPENAIPAY_BFF_START_CMD:-}" \
    OPENAIPAY_WEBSITE_PORT="${WEBSITE_PORT}" \
    OPENAIPAY_WEBSITE_HEALTH_URL="${WEBSITE_HEALTH_URL}" \
    OPENAIPAY_BACKEND_HEALTH_URL="${BACKEND_HEALTH_URL}" \
    OPENAIPAY_BFF_HEALTH_URL="${BFF_HEALTH_URL}" \
    OPENAIPAY_GUARD_CHECK_INTERVAL="${HEALTH_CHECK_INTERVAL}" \
    OPENAIPAY_GUARD_MAX_FAILS="${HEALTH_MAX_FAILS}" \
    OPENAIPAY_GUARD_STARTUP_WAIT_SECONDS="${STARTUP_WAIT_SECONDS}" \
    OPENAIPAY_GUARD_QUICK_HEALTH_SECONDS="${QUICK_HEALTH_SECONDS}" \
    OPENAIPAY_CLEAN_CONFLICT_PORTS="${CLEAN_CONFLICT_PORTS}" \
    OPENAIPAY_LOG_STDOUT="0" \
    OPENAIPAY_RESTART_ON_START="0" \
    bash "${BASH_SOURCE[0]}" --foreground)"

  sleep 1
  local guard_pid=""
  guard_pid="$(read_pid "${SUPERVISOR_PID_FILE}" || true)"
  if [[ -z "${guard_pid}" ]] || ! is_pid_alive "${guard_pid}"; then
    echo "❌ 守护进程启动失败"
    echo "   查看日志: ${SUPERVISOR_LOG_FILE}"
    exit 1
  fi

  echo "✅ backend+bff+website 已启动完成"
  echo "   backend  : ${BACKEND_HEALTH_URL}"
  echo "   bff      : ${BFF_HEALTH_URL}"
  echo "   website  : ${WEBSITE_HEALTH_URL}"
  echo "   guard pid: ${guard_pid}"
  if [[ "${BACKEND_RUNTIME_FLYWAY_ENABLED:-}" == "false" ]]; then
    echo "   backend flyway: disabled (SPRING_FLYWAY_ENABLED=false, auto fallback)"
  fi
  echo "   logs     : ${SUPERVISOR_LOG_FILE} / ${BACKEND_LOG_FILE} / ${BFF_LOG_FILE} / ${WEBSITE_LOG_FILE}"
  echo "   stop     : ${ROOT_DIR}/scripts/stop-backend-bff-guard.sh"
}

case "${mode}" in
  --foreground)
    ensure_backend_password
    run_foreground_supervisor
    ;;
  "")
    start_daemon
    ;;
  *)
    echo "Usage: $(basename "$0") [--foreground]"
    exit 1
    ;;
esac
