#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
USER_ID="${USER_ID:-880100068483692100}"
SOURCE_FUND_CODE="${SOURCE_FUND_CODE:-AICASH}"
TARGET_FUND_CODE="${TARGET_FUND_CODE:-AICASH_PRO}"
BILL_LIMIT="${BILL_LIMIT:-200}"

if ! command -v jq >/dev/null 2>&1; then
  echo "缺少 jq，请先安装 jq" >&2
  exit 1
fi

ts() {
  date '+%Y-%m-%d %H:%M:%S'
}

log() {
  printf '[%s] %s\n' "$(ts)" "$*"
}

gen_no() {
  local domain="${1:-30}"
  local biz_type="${2:-90}"
  local timestamp millis sequence user_gene
  timestamp="$(date '+%Y%m%d%H%M%S')"
  millis="$(printf '%03d' "$((RANDOM % 1000))")"
  sequence="$(printf '%05d' "$((RANDOM % 100000))")"
  user_gene="${USER_ID:(-4)}"
  printf '%02d%02d%s00%s%s%s' "${domain}" "${biz_type}" "${timestamp}" "${user_gene}" "${millis}" "${sequence}"
}

call_api() {
  local method="$1"
  local path="$2"
  local body="${3:-}"
  local response
  if [[ -n "${body}" ]]; then
    response="$(curl -sS -X "${method}" "${BASE_URL}${path}" \
      -H 'Content-Type: application/json' \
      -d "${body}")"
  else
    response="$(curl -sS -X "${method}" "${BASE_URL}${path}" \
      -H 'Content-Type: application/json')"
  fi

  local success
  success="$(jq -r '.success // false' <<<"${response}")"
  if [[ "${success}" != "true" ]]; then
    echo "接口调用失败: ${method} ${path}" >&2
    echo "${response}" >&2
    return 1
  fi
  printf '%s' "${response}"
}

assert_bill_entry() {
  local bill_json="$1"
  local business_type="$2"
  local status="$3"
  local label="$4"
  local matched
  matched="$(jq -r --arg bt "${business_type}" --arg st "${status}" \
    '.data[] | select(.businessType == $bt and .status == $st) | (.tradeOrderNo // .tradeNo // .orderNo // empty)' <<<"${bill_json}" | head -n 1)"
  if [[ -z "${matched}" ]]; then
    echo "❌ 未命中场景：${label}（businessType=${business_type}, status=${status}）" >&2
    return 1
  fi
  echo "✅ ${label}: ${matched}"
}

log "开始准备爱存账户与测试数据，BASE_URL=${BASE_URL}, USER_ID=${USER_ID}"

call_api POST "/api/fund-accounts" "$(cat <<JSON
{"userId":${USER_ID},"fundCode":"${SOURCE_FUND_CODE}","currencyCode":"CNY"}
JSON
)" >/dev/null

call_api POST "/api/fund-accounts" "$(cat <<JSON
{"userId":${USER_ID},"fundCode":"${TARGET_FUND_CODE}","currencyCode":"CNY"}
JSON
)" >/dev/null

subscribe_order_no="$(gen_no 30 11)"
subscribe_business_no="$(gen_no 10 11)"
call_api POST "/api/fund-accounts/subscribe" "$(cat <<JSON
{"orderNo":"${subscribe_order_no}","userId":${USER_ID},"fundCode":"${SOURCE_FUND_CODE}","amount":"200.0000","businessNo":"${subscribe_business_no}"}
JSON
)" >/dev/null
call_api POST "/api/fund-accounts/subscribe/confirm" "$(cat <<JSON
{"orderNo":"${subscribe_order_no}","confirmedShare":"200.0000","nav":"1.0000"}
JSON
)" >/dev/null
log "已完成申购场景：${subscribe_order_no}"

redeem_order_no="$(gen_no 30 12)"
redeem_business_no="$(gen_no 10 12)"
call_api POST "/api/fund-accounts/redeem" "$(cat <<JSON
{"orderNo":"${redeem_order_no}","userId":${USER_ID},"fundCode":"${SOURCE_FUND_CODE}","share":"20.0000","redeemMode":"NORMAL","businessNo":"${redeem_business_no}"}
JSON
)" >/dev/null
call_api POST "/api/fund-accounts/redeem/confirm" "$(cat <<JSON
{"orderNo":"${redeem_order_no}"}
JSON
)" >/dev/null
log "已完成赎回场景：${redeem_order_no}"

switch_order_no="$(gen_no 30 13)"
switch_business_no="$(gen_no 10 13)"
call_api POST "/api/fund-accounts/switch" "$(cat <<JSON
{"orderNo":"${switch_order_no}","userId":${USER_ID},"sourceFundCode":"${SOURCE_FUND_CODE}","targetFundCode":"${TARGET_FUND_CODE}","sourceShare":"30.0000","businessNo":"${switch_business_no}"}
JSON
)" >/dev/null
call_api POST "/api/fund-accounts/switch/confirm" "$(cat <<JSON
{"orderNo":"${switch_order_no}","sourceNav":"1.0000","targetNav":"1.0000"}
JSON
)" >/dev/null
log "已完成产品切换场景：${switch_order_no}"

income_order_no="$(gen_no 30 14)"
income_business_no="$(gen_no 10 14)"
call_api POST "/api/fund-accounts/income/settle" "$(cat <<JSON
{"orderNo":"${income_order_no}","userId":${USER_ID},"fundCode":"${SOURCE_FUND_CODE}","incomeAmount":"1.2300","nav":"1.0000","businessNo":"${income_business_no}"}
JSON
)" >/dev/null
log "已完成收益发放场景：${income_order_no}"

freeze_confirm_trade_no="$(gen_no 30 15)"
freeze_confirm_business_no="$(gen_no 10 15)"
call_api POST "/api/fund-accounts/pay-freeze" "$(cat <<JSON
{"fundTradeNo":"${freeze_confirm_trade_no}","userId":${USER_ID},"fundCode":"${SOURCE_FUND_CODE}","amount":"8.00","currencyCode":"CNY","businessNo":"${freeze_confirm_business_no}"}
JSON
)" >/dev/null
call_api POST "/api/fund-accounts/pay-freeze/confirm" "$(cat <<JSON
{"userId":${USER_ID},"fundTradeNo":"${freeze_confirm_trade_no}"}
JSON
)" >/dev/null
log "已完成支付冻结确认场景：${freeze_confirm_trade_no}"

freeze_comp_trade_no="$(gen_no 30 16)"
freeze_comp_business_no="$(gen_no 10 16)"
call_api POST "/api/fund-accounts/pay-freeze" "$(cat <<JSON
{"fundTradeNo":"${freeze_comp_trade_no}","userId":${USER_ID},"fundCode":"${SOURCE_FUND_CODE}","amount":"6.00","currencyCode":"CNY","businessNo":"${freeze_comp_business_no}"}
JSON
)" >/dev/null
call_api POST "/api/fund-accounts/pay-freeze/compensate" "$(cat <<JSON
{"userId":${USER_ID},"fundTradeNo":"${freeze_comp_trade_no}","fundCode":"${SOURCE_FUND_CODE}","businessNo":"${freeze_comp_business_no}"}
JSON
)" >/dev/null
log "已完成支付冻结补偿场景：${freeze_comp_trade_no}"

log "触发一次历史回填（switch/freeze）以覆盖旧数据缺口"
call_api POST "/api/fund-accounts/trades/backfill?fundCode=${SOURCE_FUND_CODE}&types=PRODUCT_SWITCH,FREEZE&limit=1000" >/dev/null

log "查询账单并校验六类爱存交易"
bill_response="$(call_api GET "/api/trade/users/${USER_ID}/bill-entries?businessDomainCode=AICASH&limit=${BILL_LIMIT}")"

assert_bill_entry "${bill_response}" "PURCHASE" "SUCCEEDED" "申购入账"
assert_bill_entry "${bill_response}" "REDEEM" "SUCCEEDED" "赎回出账"
assert_bill_entry "${bill_response}" "TRANSFER_OUT" "SUCCEEDED" "产品切换"
assert_bill_entry "${bill_response}" "YIELD_SETTLE" "SUCCEEDED" "收益发放"
assert_bill_entry "${bill_response}" "PAY_FREEZE" "SUCCEEDED" "支付冻结确认"
assert_bill_entry "${bill_response}" "PAY_FREEZE" "FAILED" "支付冻结补偿"

log "🎉 验收通过：6 类爱存交易均可在账单查询到"
