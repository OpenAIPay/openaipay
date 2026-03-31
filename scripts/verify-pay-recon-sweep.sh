#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
USER_ID="${USER_ID:-880100068483692100}"
PAYEE_USER_ID="${PAYEE_USER_ID:-${USER_ID}}"
WITHDRAW_AMOUNT="${WITHDRAW_AMOUNT:-88.00}"
WALLET_TOPUP_AMOUNT="${WALLET_TOPUP_AMOUNT:-1000.00}"
WAIT_RECON_SECONDS="${WAIT_RECON_SECONDS:-150}"
POLL_INTERVAL_SECONDS="${POLL_INTERVAL_SECONDS:-2}"

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-${OPENAIPAY_DB_PORT:-3306}}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-}"
DB_NAME="${DB_NAME:-openaipay}"

command -v jq >/dev/null 2>&1 || { echo "缺少 jq，请先安装 jq" >&2; exit 1; }
command -v mysql >/dev/null 2>&1 || { echo "缺少 mysql 客户端，请先安装" >&2; exit 1; }

log() {
  printf '[%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$*"
}

fail() {
  echo "❌ $*" >&2
  exit 1
}

gen_no() {
  printf '%s%03d%05d' "$(date '+%Y%m%d%H%M%S')" "$((RANDOM % 1000))" "$((RANDOM % 100000))"
}

norm2() {
  awk -v v="${1:-0}" 'BEGIN{printf "%.2f", (v+0)}'
}

call_raw() {
  local method="$1" path="$2" body="${3:-}"
  if [[ -n "${body}" ]]; then
    curl -sS -X "${method}" "${BASE_URL}${path}" -H 'Content-Type: application/json' -d "${body}"
  else
    curl -sS -X "${method}" "${BASE_URL}${path}" -H 'Content-Type: application/json'
  fi
}

call_ok() {
  local method="$1" path="$2" body="${3:-}" response
  response="$(call_raw "${method}" "${path}" "${body}")"
  jq -e . >/dev/null 2>&1 <<<"${response}" || fail "非 JSON 响应: ${method} ${path}"
  [[ "$(jq -r '.success // false' <<<"${response}")" == "true" ]] || {
    echo "${response}" >&2
    fail "接口失败: ${method} ${path}"
  }
  printf '%s' "${response}"
}

mysql_query() {
  local sql="$1"
  local -a cmd=(mysql -N -s -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" "${DB_NAME}" -e "${sql}")
  if [[ -n "${DB_PASSWORD}" ]]; then
    MYSQL_PWD="${DB_PASSWORD}" "${cmd[@]}"
    return 0
  fi
  "${cmd[@]}"
}

db_assert_connectable() {
  mysql_query "SELECT 1;" >/dev/null 2>&1 || fail "无法连接数据库 ${DB_HOST}:${DB_PORT}/${DB_NAME}"
}

ensure_wallet_account() {
  local payload resp msg
  payload="$(jq -cn --arg uid "${USER_ID}" '{userId:($uid|tonumber),currencyCode:"CNY"}')"
  resp="$(call_raw POST "/api/wallet-accounts" "${payload}")"
  if jq -e '.success==true' >/dev/null 2>&1 <<<"${resp}"; then
    log "已创建余额账户"
    return 0
  fi
  msg="$(jq -r '.error.message // ""' <<<"${resp}")"
  if [[ "${msg}" == *"already exists"* || "${msg}" == *"已存在"* ]]; then
    log "余额账户已存在，继续执行"
    return 0
  fi
  echo "${resp}" >&2
  fail "创建余额账户失败"
}

ensure_bank_card() {
  local cards card_no
  cards="$(call_ok GET "/api/bankcards/users/${USER_ID}/active")"
  card_no="$(jq -r '(.data[]?|select(.defaultCard==true)|.cardNo)//(.data[0].cardNo//empty)' <<<"${cards}")"
  if [[ -n "${card_no}" ]]; then
    log "复用银行卡: ${card_no}"
    return 0
  fi

  local new_card
  new_card="622202$(date '+%s%N' | tr -cd '0-9' | tail -c 10)"
  call_ok POST "/api/bankcards" "$(jq -cn \
    --arg uid "${USER_ID}" \
    --arg c "${new_card}" \
    '{userId:($uid|tonumber),cardNo:$c,bankCode:"ICBC",bankName:"中国工商银行",cardType:"DEBIT",cardHolderName:"验证用户",reservedMobile:"13920000002",phoneTailNo:"0000",defaultCard:true,singleLimit:"500000.00",dailyLimit:"2000000.00"}')" >/dev/null
  log "已创建银行卡: ${new_card}"
}

ensure_wallet_liquidity() {
  local current required
  current="$(call_ok GET "/api/wallet-accounts/${USER_ID}" | jq -r '(.data.availableBalance.amount // .data.availableBalance // "0.00")')"
  current="$(norm2 "${current}")"
  required="$(awk -v a="${WITHDRAW_AMOUNT}" -v b="${WALLET_TOPUP_AMOUNT}" 'BEGIN{printf "%.2f", a + b*0.1}')"
  if awk -v c="${current}" -v r="${required}" 'BEGIN{exit !(c >= r)}'; then
    log "余额充足: ${current}"
    return 0
  fi

  log "余额不足(${current})，通过 DB 补足余额"
  mysql_query "UPDATE wallet_account
               SET available_balance = available_balance + ${WALLET_TOPUP_AMOUNT},
                   updated_at = NOW()
               WHERE user_id = ${USER_ID};" >/dev/null
  current="$(call_ok GET "/api/wallet-accounts/${USER_ID}" | jq -r '(.data.availableBalance.amount // .data.availableBalance // "0.00")')"
  current="$(norm2 "${current}")"
  awk -v c="${current}" -v r="${required}" 'BEGIN{exit !(c >= r)}' || fail "补余额后仍不足: current=${current}, required=${required}"
  log "补余额完成: ${current}"
}

wait_trade_terminal() {
  local request_no="$1"
  local deadline=$((SECONDS + WAIT_RECON_SECONDS))
  WAIT_TRADE_JSON=""
  WAIT_TRADE_STATUS=""
  while (( SECONDS < deadline )); do
    local response status
    response="$(call_ok GET "/api/trade/by-request/${request_no}")"
    status="$(jq -r '.data.status // ""' <<<"${response}")"
    case "${status}" in
      SUCCEEDED|FAILED|RECON_PENDING)
        WAIT_TRADE_JSON="${response}"
        WAIT_TRADE_STATUS="${status}"
        return 0
        ;;
    esac
    sleep "${POLL_INTERVAL_SECONDS}"
  done
  fail "等待交易终态超时: requestNo=${request_no}"
}

extract_pay_order_no() {
  local response="$1"
  jq -r '.data.payOrderNo // .data.currentPayAttempt.payOrderNo // (.data.payAttempts[0].payOrderNo // empty)' <<<"${response}"
}

inject_recon_pending_stuck_state() {
  local pay_order_no="$1"
  local injected_reason="simulate bank accepted callback lost"
  local outbound_id
  outbound_id="$(mysql_query "SELECT outbound_id FROM outbound_order WHERE pay_order_no='${pay_order_no}' ORDER BY id DESC LIMIT 1;")"
  [[ -n "${outbound_id}" ]] || fail "未找到 outbound_order, payOrderNo=${pay_order_no}"

  log "注入丢消息场景: payOrderNo=${pay_order_no}, outboundId=${outbound_id}"

  mysql_query "UPDATE async_message
               SET status='DEAD',
                   last_error='verify dead-letter before recon sweep',
                   updated_at=NOW()
               WHERE topic='PAY_RECON_REQUESTED'
                 AND message_key='${pay_order_no}:RECON'
                 AND status IN ('PENDING','PROCESSING');" >/dev/null

  mysql_query "UPDATE outbound_order
               SET outbound_status='RECON_PENDING',
                   result_code='BANK_TIMEOUT',
                   result_description='${injected_reason}',
                   gmt_resp=NOW(),
                   gmt_modified=NOW()
               WHERE pay_order_no='${pay_order_no}';" >/dev/null

  mysql_query "UPDATE pay_participant_branch
               SET status='TRY_OK',
                   response_message='${injected_reason}',
                   updated_at=NOW()
               WHERE pay_order_no='${pay_order_no}'
                 AND participant_type='OUTBOUND';" >/dev/null

  mysql_query "UPDATE pay_order
               SET status='RECON_PENDING',
                   result_code='RECON_PENDING',
                   result_message='${injected_reason}',
                   failure_reason='${injected_reason}',
                   status_version = status_version + 1,
                   updated_at=NOW()
               WHERE pay_order_no='${pay_order_no}';" >/dev/null

  local pay_status branch_status outbound_status
  pay_status="$(mysql_query "SELECT status FROM pay_order WHERE pay_order_no='${pay_order_no}' LIMIT 1;")"
  branch_status="$(mysql_query "SELECT status FROM pay_participant_branch WHERE pay_order_no='${pay_order_no}' AND participant_type='OUTBOUND' ORDER BY id DESC LIMIT 1;")"
  outbound_status="$(mysql_query "SELECT outbound_status FROM outbound_order WHERE pay_order_no='${pay_order_no}' ORDER BY id DESC LIMIT 1;")"
  [[ "${pay_status}" == "RECON_PENDING" ]] || fail "pay_order 注入失败: ${pay_status}"
  [[ "${branch_status}" == "TRY_OK" ]] || fail "pay_participant_branch 注入失败: ${branch_status}"
  [[ "${outbound_status}" == "RECON_PENDING" ]] || fail "outbound_order 注入失败: ${outbound_status}"
}

wait_recovered_by_sweep_worker() {
  local pay_order_no="$1"
  local deadline=$((SECONDS + WAIT_RECON_SECONDS))
  while (( SECONDS < deadline )); do
    local pay_status branch_status outbound_status
    pay_status="$(mysql_query "SELECT status FROM pay_order WHERE pay_order_no='${pay_order_no}' LIMIT 1;")"
    branch_status="$(mysql_query "SELECT status FROM pay_participant_branch WHERE pay_order_no='${pay_order_no}' AND participant_type='OUTBOUND' ORDER BY id DESC LIMIT 1;")"
    outbound_status="$(mysql_query "SELECT outbound_status FROM outbound_order WHERE pay_order_no='${pay_order_no}' ORDER BY id DESC LIMIT 1;")"

    if [[ "${pay_status}" == "COMMITTED" && "${branch_status}" == "CONFIRM_OK" && "${outbound_status}" == "SUCCEEDED" ]]; then
      log "兜底续跑已收敛: pay=${pay_status}, branch=${branch_status}, outbound=${outbound_status}"
      return 0
    fi
    sleep "${POLL_INTERVAL_SECONDS}"
  done
  fail "等待 RECON_PENDING 收敛超时（请检查 PayReconSweepWorker）"
}

main() {
  log "开始验证 pay RECON_PENDING 兜底续跑链路"
  log "BASE_URL=${BASE_URL}, USER_ID=${USER_ID}, DB=${DB_HOST}:${DB_PORT}/${DB_NAME}"

  db_assert_connectable
  ensure_wallet_account
  ensure_bank_card
  ensure_wallet_liquidity

  local request_no withdraw_payload
  request_no="VERIFY-PAY-RECON-$(gen_no)"
  withdraw_payload="$(jq -cn \
    --arg requestNo "${request_no}" \
    --arg scene "TRADE_WITHDRAW" \
    --arg uid "${USER_ID}" \
    --arg payee "${PAYEE_USER_ID}" \
    --arg amount "${WITHDRAW_AMOUNT}" \
    '{
      requestNo:$requestNo,
      businessSceneCode:$scene,
      payerUserId:($uid|tonumber),
      payeeUserId:($payee|tonumber),
      paymentMethod:"WITHDRAW",
      amount:$amount
    }')"

  log "发起提现交易，requestNo=${request_no}, scene=TRADE_WITHDRAW"
  call_ok POST "/api/trade/withdraw" "${withdraw_payload}" >/dev/null
  wait_trade_terminal "${request_no}"
  [[ "${WAIT_TRADE_STATUS}" != "FAILED" ]] || fail "提现交易失败: ${WAIT_TRADE_JSON}"

  local pay_order_no
  pay_order_no="$(extract_pay_order_no "${WAIT_TRADE_JSON}")"
  [[ -n "${pay_order_no}" ]] || fail "无法从交易响应提取 payOrderNo: ${WAIT_TRADE_JSON}"
  log "命中支付单: payOrderNo=${pay_order_no}, tradeStatus=${WAIT_TRADE_STATUS}"

  inject_recon_pending_stuck_state "${pay_order_no}"
  log "已注入 RECON_PENDING + 丢消息，等待定时 sweep 收敛（默认间隔约 45s）"

  wait_recovered_by_sweep_worker "${pay_order_no}"

  local final_trade
  final_trade="$(call_ok GET "/api/trade/by-request/${request_no}")"
  local final_status
  final_status="$(jq -r '.data.status // ""' <<<"${final_trade}")"
  [[ "${final_status}" == "SUCCEEDED" ]] || fail "交易状态异常: ${final_status}"

  log "🎉 验证通过：银行受理/回调丢失场景可由 sweep 兜底收敛"
}

main "$@"
