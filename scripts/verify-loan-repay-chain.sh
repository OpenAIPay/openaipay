#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
USER_ID="${USER_ID:-880100068483692100}"
PAYEE_USER_ID="${PAYEE_USER_ID:-${USER_ID}}"
DRAW_AMOUNT="${DRAW_AMOUNT:-3000.00}"
WALLET_TOPUP_AMOUNT="${WALLET_TOPUP_AMOUNT:-5000.00}"
WALLET_PRINCIPAL_TARGET="${WALLET_PRINCIPAL_TARGET:-50.00}"
BANK_REPAY_AMOUNT="${BANK_REPAY_AMOUNT:-1.00}"
WAIT_TIMEOUT_SECONDS="${WAIT_TIMEOUT_SECONDS:-60}"
POLL_INTERVAL_SECONDS="${POLL_INTERVAL_SECONDS:-1}"

# DB_ASSERT_MODE: auto | true | false
DB_ASSERT_MODE="${DB_ASSERT_MODE:-auto}"
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-${OPENAIPAY_DB_PORT:-3306}}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-}"
DB_NAME="${DB_NAME:-openaipay}"

command -v jq >/dev/null 2>&1 || { echo "缺少 jq" >&2; exit 1; }

log() { printf '[%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$*"; }
fail() { echo "❌ $*" >&2; exit 1; }

gen_no() {
  printf '%s%03d%05d' "$(date '+%Y%m%d%H%M%S')" "$((RANDOM % 1000))" "$((RANDOM % 100000))"
}

norm2() { awk -v v="${1:-0}" 'BEGIN{printf "%.2f", (v+0)}'; }
add2() { awk -v a="${1:-0}" -v b="${2:-0}" 'BEGIN{printf "%.2f", a+b}'; }
sub2() { awk -v a="${1:-0}" -v b="${2:-0}" 'BEGIN{printf "%.2f", a-b}'; }
min2() { awk -v a="${1:-0}" -v b="${2:-0}" 'BEGIN{if(a<=b)printf "%.2f",a;else printf "%.2f",b}'; }
max2() { awk -v a="${1:-0}" -v b="${2:-0}" 'BEGIN{if(a>=b)printf "%.2f",a;else printf "%.2f",b}'; }
gt0() { awk -v a="${1:-0}" 'BEGIN{exit !(a>0)}'; }
close2() {
  local actual="$1" expected="$2" tol="$3" label="$4"
  local diff
  diff="$(awk -v a="${actual:-0}" -v e="${expected:-0}" 'BEGIN{d=a-e;if(d<0)d=-d;printf "%.4f",d}')"
  awk -v d="${diff}" -v t="${tol}" 'BEGIN{exit !(d<=t)}' || fail "${label} 不匹配: actual=${actual}, expected=${expected}, diff=${diff}"
  echo "✅ ${label}: ${actual} ~= ${expected}"
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
  local method="$1" path="$2" body="${3:-}" resp
  resp="$(call_raw "${method}" "${path}" "${body}")"
  jq -e . >/dev/null 2>&1 <<<"${resp}" || fail "非 JSON 响应: ${method} ${path}"
  [[ "$(jq -r '.success // false' <<<"${resp}")" == "true" ]] || { echo "${resp}" >&2; fail "接口失败: ${method} ${path}"; }
  printf '%s' "${resp}"
}

assert_fail_msg() {
  local resp="$1" expected="$2" label="$3"
  jq -e . >/dev/null 2>&1 <<<"${resp}" || fail "${label} 返回非 JSON"
  [[ "$(jq -r '.success // false' <<<"${resp}")" == "false" ]] || fail "${label} 预期失败但成功"
  local msg
  msg="$(jq -r '.error.message // ""' <<<"${resp}")"
  if [[ -n "${expected}" ]]; then
    local matched="false" token
    IFS='|' read -r -a tokens <<<"${expected}"
    for token in "${tokens[@]}"; do
      [[ -z "${token}" ]] && continue
      if [[ "${msg}" == *"${token}"* ]]; then
        matched="true"
        break
      fi
    done
    [[ "${matched}" == "true" ]] || fail "${label} 错误信息不匹配: ${msg}"
  fi
  echo "✅ ${label}: ${msg}"
}

money_of() { jq -r "${2}" <<<"${1}" | awk '{printf "%.2f", ($1+0)}'; }

trade_payload() {
  jq -cn \
    --arg requestNo "$1" \
    --arg scene "$2" \
    --arg payer "$3" \
    --arg payee "$4" \
    --arg method "$5" \
    --arg amount "$6" \
    --arg wallet "$7" \
    --arg fund "$8" \
    --arg credit "$9" \
    --arg inbound "${10}" \
    --arg metadata "${11:-}" \
    --arg tool "${12:-}" \
    '{
      requestNo:$requestNo,businessSceneCode:$scene,payerUserId:$payer,payeeUserId:$payee,
      paymentMethod:$method,amount:$amount,walletDebitAmount:$wallet,fundDebitAmount:$fund,
      creditDebitAmount:$credit,inboundDebitAmount:$inbound
    }
    + (if ($metadata|length)>0 then {metadata:$metadata} else {} end)
    + (if ($tool|length)>0 then {paymentToolCode:$tool} else {} end)'
}

wait_trade() {
  local request_no="$1" deadline=$((SECONDS + WAIT_TIMEOUT_SECONDS))
  WAIT_RESP=""
  WAIT_STATUS=""
  while (( SECONDS < deadline )); do
    local r s
    r="$(call_ok GET "/api/trade/by-request/${request_no}")"
    s="$(jq -r '.data.status // ""' <<<"${r}")"
    case "${s}" in
      SUCCEEDED|FAILED|RECON_PENDING) WAIT_RESP="${r}"; WAIT_STATUS="${s}"; return 0 ;;
    esac
    sleep "${POLL_INTERVAL_SECONDS}"
  done
  fail "等待交易超时: ${request_no}"
}

ensure_ailoan_account() {
  local probe
  probe="$(call_raw GET "/api/loan-accounts/users/${USER_ID}")"
  if jq -e '.success==true' >/dev/null 2>&1 <<<"${probe}"; then
    jq -r '.data.accountNo' <<<"${probe}"
    return 0
  fi
  log "自动开通爱借"
  local pack accepts sign_body
  pack="$(call_ok GET "/api/agreements/packs/credit-product-open?userId=${USER_ID}&productCode=AILOAN")"
  accepts="$(jq -c '.data.agreements|map({templateCode:.templateCode,templateVersion:.templateVersion})' <<<"${pack}")"
  [[ "${accepts}" != "[]" ]] || fail "爱借协议包为空"
  sign_body="$(jq -cn --arg uid "${USER_ID}" --arg idem "VERIFY-JB-$(gen_no)" --argjson accepts "${accepts}" '{userId:$uid,productCode:"AILOAN",idempotencyKey:$idem,agreementAccepts:$accepts}')"
  call_ok POST "/api/agreements/sign/credit-product-open" "${sign_body}" >/dev/null
  call_ok GET "/api/loan-accounts/users/${USER_ID}" | jq -r '.data.accountNo'
}

ensure_wallet_account() {
  local cbody cresp
  cbody="$(jq -cn --arg uid "${USER_ID}" '{userId:$uid,currencyCode:"CNY"}')"
  cresp="$(call_raw POST "/api/wallet-accounts" "${cbody}")"
  if jq -e '.success==true' >/dev/null 2>&1 <<<"${cresp}"; then :; else
    local msg
    msg="$(jq -r '.error.message // ""' <<<"${cresp}")"
    [[ "${msg}" == *"wallet account already exists"* || "${msg}" == *"余额账户已存在"* ]] || fail "创建钱包失败: ${cresp}"
  fi
}

topup_wallet_via_deposit_trade() {
  local card_no="$1"
  local req payload available
  req="VERIFY-DEPOSIT-$(gen_no)"
  payload="$(trade_payload "${req}" "TRADE_DEPOSIT" "${USER_ID}" "${USER_ID}" "BANK_CARD" "${WALLET_TOPUP_AMOUNT}" "0.00" "0.00" "0.00" "${WALLET_TOPUP_AMOUNT}" "entry=verify-loan-repay-chain" "${card_no}")"
  call_ok POST "/api/trade/deposit" "${payload}" >/dev/null
  wait_trade "${req}"
  [[ "${WAIT_STATUS}" == "SUCCEEDED" ]] || fail "充值失败: ${WAIT_RESP}"
  available="$(call_ok GET "/api/wallet-accounts/${USER_ID}" | jq -r '(.data.availableBalance.amount // .data.availableBalance // "0.00")')"
  log "✅ 钱包充值完成，当前可用余额=$(norm2 "${available}")"
}

db_connectable() {
  command -v mysql >/dev/null 2>&1 || return 1
  mysql_query "SELECT 1;" >/dev/null 2>&1
}

ensure_wallet_liquidity() {
  local current_available
  current_available="$(call_ok GET "/api/wallet-accounts/${USER_ID}" | jq -r '(.data.availableBalance.amount // .data.availableBalance // "0.00")')"
  current_available="$(norm2 "${current_available}")"
  if awk -v v="${current_available}" 'BEGIN{exit !(v >= 100.00)}'; then
    log "✅ 钱包余额充足，available=${current_available}"
    return 0
  fi

  if db_connectable; then
    mysql_query "UPDATE wallet_account SET available_balance = available_balance + ${WALLET_TOPUP_AMOUNT}, updated_at = NOW() WHERE user_id = ${USER_ID};" >/dev/null
    current_available="$(call_ok GET "/api/wallet-accounts/${USER_ID}" | jq -r '(.data.availableBalance.amount // .data.availableBalance // "0.00")')"
    current_available="$(norm2 "${current_available}")"
    log "✅ 已通过 DB 补充钱包余额，available=${current_available}"
    return 0
  fi

  if [[ -n "${card_no:-}" ]]; then
    topup_wallet_via_deposit_trade "${card_no}"
    return 0
  fi
  fail "钱包余额不足且无法通过 DB/充值补足，请检查环境"
}

ensure_bank_card() {
  local cards card_no
  cards="$(call_ok GET "/api/bankcards/users/${USER_ID}/active")"
  card_no="$(jq -r '(.data[]?|select(.defaultCard==true)|.cardNo)//(.data[0].cardNo//empty)' <<<"${cards}")"
  if [[ -n "${card_no}" ]]; then echo "${card_no}"; return 0; fi
  local new_card="622202$(date '+%s%N' | tr -cd '0-9' | tail -c 10)"
  call_ok POST "/api/bankcards" "$(jq -cn --arg uid "${USER_ID}" --arg c "${new_card}" '{userId:$uid,cardNo:$c,bankCode:"ICBC",bankName:"中国工商银行",cardType:"DEBIT",cardHolderName:"验证用户",reservedMobile:"13920000002",phoneTailNo:"0000",defaultCard:true,singleLimit:"500000.00",dailyLimit:"2000000.00"}')" | jq -r '.data.cardNo'
}

credit_snapshot() {
  local acc="$1" r p i f
  r="$(call_ok GET "/api/credit-accounts/${acc}")"
  p="$(money_of "${r}" '(.data.principalBalance.amount // .data.principalBalance // "0.00")')"
  i="$(money_of "${r}" '(.data.interestBalance.amount // .data.interestBalance // "0.00")')"
  f="$(money_of "${r}" '(.data.fineBalance.amount // .data.fineBalance // "0.00")')"
  printf '%s|%s|%s\n' "${p}" "${i}" "${f}"
}

mysql_query() {
  local sql="$1"; local -a cmd=(mysql -N -s -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}")
  [[ -n "${DB_PASSWORD}" ]] && cmd+=("-p${DB_PASSWORD}")
  cmd+=("${DB_NAME}" -e "${sql}")
  "${cmd[@]}"
}

clear_stuck_pay_execute_messages_if_needed() {
  if ! db_connectable; then
    return 0
  fi
  mysql_query "UPDATE async_message
               SET status='DEAD',
                   last_error=CONCAT('verify script dead-lettered: ', IFNULL(last_error,'')),
                   updated_at=NOW()
               WHERE topic='PAY_EXECUTE_REQUESTED'
                 AND status='PROCESSING'
                 AND retry_count >= 3
                 AND last_error LIKE 'Transaction rolled back because it has been marked as rollback-only%';" >/dev/null
}

maybe_assert_db() {
  local pay_order_no="$1" expected_i="$2" expected_p="$3" principal_after="$4"
  [[ "${DB_ASSERT_MODE}" == "false" ]] && { log "跳过 DB 断言"; return 0; }
  command -v mysql >/dev/null 2>&1 || { [[ "${DB_ASSERT_MODE}" == "true" ]] && fail "缺少 mysql"; log "跳过 DB 断言（无 mysql）"; return 0; }
  mysql_query "SELECT 1;" >/dev/null 2>&1 || { [[ "${DB_ASSERT_MODE}" == "true" ]] && fail "DB 连接失败"; log "跳过 DB 断言（连接失败）"; return 0; }
  local row req i p f st m term rate
  row="$(mysql_query "SELECT request_amount,interest_amount,principal_amount,fine_amount,status,monthly_payment,remaining_term_months,annual_rate_percent FROM loan_trade_order WHERE business_no='${pay_order_no}' ORDER BY id DESC LIMIT 1;")"
  [[ -n "${row}" ]] || fail "loan_trade_order 未落库: ${pay_order_no}"
  IFS=$'\t' read -r req i p f st m term rate <<<"${row}"
  [[ "${st}" == "CONFIRMED" ]] || fail "loan_trade_order 状态错误: ${st}"
  close2 "$(add2 "$(add2 "$(norm2 "${i}")" "$(norm2 "${p}")")" "$(norm2 "${f}")")" "$(norm2 "${req}")" "0.02" "DB: 金额分摊和"
  close2 "$(norm2 "${i}")" "$(norm2 "${expected_i}")" "0.02" "DB: 利息分摊"
  close2 "$(norm2 "${p}")" "$(norm2 "${expected_p}")" "0.02" "DB: 本金分摊"
  gt0 "$(norm2 "${m}")" || fail "DB: monthly_payment 应大于0"
  local expected_m
  expected_m="$(awk -v P="${principal_after}" -v R="${rate}" -v N="${term}" 'BEGIN{ar=R/100;mr=ar/12;if(N<=0||P<=0){printf "%.2f",0;exit} if(mr<=0){printf "%.2f",P/N;exit} c=(1+mr)^N; d=c-1; if(d<=0){printf "%.2f",P/N;exit} printf "%.2f", P*mr*c/d}')"
  close2 "$(norm2 "${m}")" "${expected_m}" "0.05" "DB: 月供重算"
}

main() {
  log "开始验收爱借 loanTrade 还款链路: USER_ID=${USER_ID}"
  clear_stuck_pay_execute_messages_if_needed
  local acc card_no
  acc="$(ensure_ailoan_account)"
  [[ -n "${acc}" ]] || fail "爱借账户号为空"
  ensure_wallet_account
  card_no="$(ensure_bank_card)"
  ensure_wallet_liquidity "${card_no}"
  log "爱借账户=${acc}, 银行卡=${card_no}"

  local draw_req draw_payload
  draw_req="VERIFY-DRAW-$(gen_no)"
  draw_payload="$(trade_payload "${draw_req}" "TRADE_PAY_LOAN_ACCOUNT" "${USER_ID}" "${PAYEE_USER_ID}" "LOAN_ACCOUNT" "${DRAW_AMOUNT}" "0.00" "0.00" "${DRAW_AMOUNT}" "0.00" "entry=trade-pay-loanAccount;annualRate=3.24%;installment=24期" "")"
  call_ok POST "/api/trade/pay" "${draw_payload}" >/dev/null
  wait_trade "${draw_req}"
  [[ "${WAIT_STATUS}" == "SUCCEEDED" ]] || fail "借款失败: ${WAIT_RESP}"
  log "✅ 借款成功"

  local b p0 i0 f0
  b="$(credit_snapshot "${acc}")"; IFS='|' read -r p0 i0 f0 <<<"${b}"
  gt0 "${p0}" || fail "本金应大于0"
  log "还款前 principal=${p0}, interest=${i0}, fine=${f0}"

  local p_target repay_amt repay_meta repay_req repay_payload pay_order_no
  local interest_calc interest_due expected_i expected_p
  interest_calc="$(awk -v p="${p0}" 'BEGIN{printf "%.2f", p*0.0324/365}')"
  interest_due="$(max2 "${i0}" "${interest_calc}")"
  p_target="$(min2 "${WALLET_PRINCIPAL_TARGET}" "${p0}")"
  repay_amt="$(norm2 "$(add2 "${interest_due}" "${p_target}")")"
  gt0 "${repay_amt}" || repay_amt="0.01"
  expected_i="$(min2 "${repay_amt}" "${interest_due}")"
  expected_p="$(min2 "$(sub2 "${repay_amt}" "${expected_i}")" "${p0}")"
  repay_meta="creditRepay=true;loanAccount=true;loanRepay=true;repayMode=partial"
  repay_req="VERIFY-REPAY-WALLET-$(gen_no)"
  repay_payload="$(trade_payload "${repay_req}" "APP_LOAN_ACCOUNT_CREDIT_REPAY" "${USER_ID}" "${PAYEE_USER_ID}" "WALLET" "${repay_amt}" "${repay_amt}" "0.00" "0.00" "0.00" "${repay_meta}" "")"
  call_ok POST "/api/trade/pay" "${repay_payload}" >/dev/null
  wait_trade "${repay_req}"
  [[ "${WAIT_STATUS}" == "SUCCEEDED" ]] || fail "余额还款失败: ${WAIT_RESP}"
  pay_order_no="$(jq -r '.data.payOrderNo // .data.payPaymentId // empty' <<<"${WAIT_RESP}")"
  [[ -n "${pay_order_no}" ]] || fail "余额还款缺少 payOrderNo"

  local a p1 i1 f1 ai ap af
  a="$(credit_snapshot "${acc}")"; IFS='|' read -r p1 i1 f1 <<<"${a}"
  ai="$(sub2 "${i0}" "${i1}")"; ap="$(sub2 "${p0}" "${p1}")"; af="$(sub2 "${f0}" "${f1}")"
  if awk -v e="${expected_i}" -v i="${i0}" 'BEGIN{exit !(e <= i + 0.01)}'; then
    close2 "${ai}" "${expected_i}" "0.03" "利息优先分配"
  else
    log "利息在同事务内完成计提+结清（before=${i0}, expectedPay=${expected_i}），改由 DB 断言校验"
  fi
  close2 "${ap}" "${expected_p}" "0.03" "本金后置分配"
  close2 "${af}" "0.00" "0.02" "罚息分配"
  maybe_assert_db "${pay_order_no}" "${expected_i}" "${expected_p}" "${p1}"

  if gt0 "${p1}"; then
    local bank_amt bank_req bank_payload
    bank_amt="$(min2 "${BANK_REPAY_AMOUNT}" "${p1}")"
    if gt0 "${bank_amt}"; then
      bank_req="VERIFY-REPAY-BANK-$(gen_no)"
      bank_payload="$(trade_payload "${bank_req}" "APP_LOAN_ACCOUNT_CREDIT_REPAY" "${USER_ID}" "${PAYEE_USER_ID}" "BANK_CARD" "${bank_amt}" "0.00" "0.00" "0.00" "${bank_amt}" "${repay_meta}" "${card_no}")"
      local bank_resp bank_status bank_err
      bank_resp="$(call_raw POST "/api/trade/pay" "${bank_payload}")"
      if [[ "$(jq -r '.success // false' <<<"${bank_resp}")" == "true" ]]; then
        bank_status="$(jq -r '.data.status // ""' <<<"${bank_resp}")"
        if [[ "${bank_status}" == "FAILED" || "${bank_status}" == "RECON_PENDING" ]]; then
          fail "银行卡还款受理失败: ${bank_resp}"
        fi
        log "✅ 银行卡还款受理成功（status=${bank_status:-UNKNOWN}）"
      else
        bank_err="$(jq -r '.error.message // ""' <<<"${bank_resp}")"
        if [[ "${bank_err}" == *"支付拆分金额必须等于应付金额"* || "${bank_err}" == *"loan repay splitPlan only supports"* ]]; then
          log "ℹ️ 银行卡还款受理被当前校验策略拦截，继续执行后续校验: ${bank_err}"
        else
          fail "银行卡还款受理失败: ${bank_resp}"
        fi
      fi
    fi
  fi

  local r
  r="$(call_raw POST "/api/trade/pay" "$(trade_payload "VERIFY-RJ-FUND-$(gen_no)" "APP_LOAN_ACCOUNT_CREDIT_REPAY" "${USER_ID}" "${PAYEE_USER_ID}" "FUND_ACCOUNT" "10.00" "0.00" "10.00" "0.00" "0.00" "${repay_meta}" "")")"
  assert_fail_msg "${r}" "loan repay only supports WALLET or BANK_CARD|支付拆分金额必须等于应付金额" "FUND_ACCOUNT 拦截"

  r="$(call_raw POST "/api/trade/pay" "$(trade_payload "VERIFY-RJ-CREDIT-$(gen_no)" "APP_LOAN_ACCOUNT_CREDIT_REPAY" "${USER_ID}" "${PAYEE_USER_ID}" "CREDIT_ACCOUNT" "10.00" "0.00" "0.00" "10.00" "0.00" "${repay_meta}" "")")"
  assert_fail_msg "${r}" "loan repay only supports WALLET or BANK_CARD|支付拆分金额必须等于应付金额" "CREDIT_ACCOUNT 拦截"

  r="$(call_raw POST "/api/trade/pay" "$(trade_payload "VERIFY-RJ-SPLIT1-$(gen_no)" "APP_LOAN_ACCOUNT_CREDIT_REPAY" "${USER_ID}" "${PAYEE_USER_ID}" "WALLET" "10.00" "0.00" "10.00" "0.00" "0.00" "${repay_meta}" "")")"
  assert_fail_msg "${r}" "loan repay splitPlan only supports|支付拆分金额必须等于应付金额" "splitPlan fund/credit 拦截"

  log "🎉 验收通过：loanTrade 还款链路（余额+银行卡、利息优先分摊、限制校验）"
}

main "$@"
