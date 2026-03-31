package cn.openaipay.adapter.common;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 对外返回的错误信息统一做中文收口，避免领域层/应用层英文异常直接暴露给终端。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
final class ErrorMessageLocalizer {

    /** 业务说明 */
    private static final String DEFAULT_MESSAGE = "操作失败，请稍后重试";

    private static final Pattern MUST_NOT_BE_BLANK =
            Pattern.compile("^(.+?) must not be blank$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MUST_NOT_BE_NULL =
            Pattern.compile("^(.+?) must not be null$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MUST_BE_GREATER_THAN_ZERO =
            Pattern.compile("^(.+?) must be greater than 0$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO =
            Pattern.compile("^(.+?) must be greater than or equal to 0$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MUST_NOT_BE_LESS_THAN_ZERO =
            Pattern.compile("^(.+?) must not be less than 0$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MUST_BE_LESS_THAN_OR_EQUAL_TO =
            Pattern.compile("^(.+?) must be <= (.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern LENGTH_AT_MOST =
            Pattern.compile("^(.+?) length must be <= (\\d+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern LENGTH_LESS_THAN_OR_EQUAL_TO =
            Pattern.compile("^(.+?) length must be less than or equal to (\\d+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SIZE_LESS_THAN_OR_EQUAL_TO =
            Pattern.compile("^(.+?) size must be less than or equal to (\\d+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MUST_USE_FORMAT =
            Pattern.compile("^(.+?) must use format (.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MUST_BE_BETWEEN =
            Pattern.compile("^(.+?) must be between (\\d+) and (\\d+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern UNSUPPORTED =
            Pattern.compile("^unsupported (.+?):\\s*(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern NOT_FOUND =
            Pattern.compile("^(.+?) not found(?:[:].*| for .*)?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern ALREADY_EXISTS =
            Pattern.compile("^(.+?) already exists(?:[:].*)?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MUST_BE_BEFORE =
            Pattern.compile("^(.+?) must be before (.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern CAN_NOT_EXCEED =
            Pattern.compile("^(.+?) can not exceed (.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MUST_NOT_BE =
            Pattern.compile("^(.+?) must not be (.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MUST_BE_AT_LEAST_CHARACTERS =
            Pattern.compile("^(.+?) must be at least (\\d+) characters$", Pattern.CASE_INSENSITIVE);
    private static final Pattern UNKNOWN_CODES =
            Pattern.compile("^unknown (.+?) codes:\\s*(.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern CURRENCY_MUST_EQUAL =
            Pattern.compile("^(.+?) currency must equal (.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MUST_BE_DIFFERENT =
            Pattern.compile("^(.+?) and (.+?) must be different$", Pattern.CASE_INSENSITIVE);

    private static final Map<String, String> FIELD_LABELS = Map.ofEntries(
            Map.entry("adminId", "管理员标识"),
            Map.entry("accumulatedIncome", "累计收益"),
            Map.entry("amount", "金额"),
            Map.entry("appCode", "应用编码"),
            Map.entry("appDeviceStatus", "设备状态"),
            Map.entry("appReleaseStatus", "应用发布状态"),
            Map.entry("appUpdateType", "升级类型"),
            Map.entry("availableBalance", "可用余额"),
            Map.entry("availableShare", "可用份额"),
            Map.entry("attachmentUrls", "附件"),
            Map.entry("avatarUrl", "头像地址"),
            Map.entry("branchId", "分支事务号"),
            Map.entry("businessDomainCode", "业务域"),
            Map.entry("bizOrderNo", "业务单号"),
            Map.entry("businessSceneCode", "业务场景"),
            Map.entry("confirmedShare", "确认份额"),
            Map.entry("content", "内容"),
            Map.entry("conversationId", "会话标识"),
            Map.entry("couponNo", "券编号"),
            Map.entry("creditDebitAmount", "信用扣款金额"),
            Map.entry("currencyCode", "币种"),
            Map.entry("currencyUnit", "币种"),
            Map.entry("currentAppVersionId", "当前版本标识"),
            Map.entry("currentIosPackageId", "当前安装包标识"),
            Map.entry("deviceId", "设备标识"),
            Map.entry("deviceModel", "设备型号"),
            Map.entry("deviceName", "设备名称"),
            Map.entry("event", "事件"),
            Map.entry("feeAmount", "手续费"),
            Map.entry("feeBearer", "费用承担方"),
            Map.entry("feeMode", "计费模式"),
            Map.entry("feeRate", "费率"),
            Map.entry("feedbackNo", "反馈单号"),
            Map.entry("feedbackStatus", "反馈状态"),
            Map.entry("feedbackType", "反馈类型"),
            Map.entry("file", "文件"),
            Map.entry("fineBalance", "罚息余额"),
            Map.entry("fixedFee", "固定手续费"),
            Map.entry("frozenShare", "冻结份额"),
            Map.entry("fundCode", "基金代码"),
            Map.entry("fundDebitAmount", "爱存扣款金额"),
            Map.entry("id", "标识"),
            Map.entry("inboundDebitAmount", "入金扣款金额"),
            Map.entry("interestBalance", "利息余额"),
            Map.entry("lastPayStatusVersion", "支付状态版本号"),
            Map.entry("latestNav", "最新净值"),
            Map.entry("limit", "条数"),
            Map.entry("lineNo", "行号"),
            Map.entry("loginId", "登录账号"),
            Map.entry("maxFee", "最高手续费"),
            Map.entry("messageId", "消息标识"),
            Map.entry("minFee", "最低手续费"),
            Map.entry("nav", "净值"),
            Map.entry("nickname", "昵称"),
            Map.entry("operationType", "操作类型"),
            Map.entry("originalAmount", "原始金额"),
            Map.entry("originalTradeOrderNo", "原交易单号"),
            Map.entry("originalVoucher", "原凭证"),
            Map.entry("ownerUser", "所属用户"),
            Map.entry("overduePrincipalBalance", "逾期本金余额"),
            Map.entry("overduePrincipalUnreachAmount", "逾期未入账本金"),
            Map.entry("password", "密码"),
            Map.entry("payableAmount", "应付金额"),
            Map.entry("payeeUserId", "收款方用户标识"),
            Map.entry("payerUserId", "付款方用户标识"),
            Map.entry("payFundDetailTool", "支付资金明细工具"),
            Map.entry("payOrderNo", "支付单号"),
            Map.entry("paymentMethod", "支付方式"),
            Map.entry("payOrder", "支付单"),
            Map.entry("pendingRedeemShare", "待确认转出份额"),
            Map.entry("pendingSubscribeAmount", "待确认转入金额"),
            Map.entry("peerUserId", "对方用户标识"),
            Map.entry("permission", "权限"),
            Map.entry("permissionCode", "权限编码"),
            Map.entry("principalBalance", "本金余额"),
            Map.entry("principalUnreachAmount", "未入账本金"),
            Map.entry("pricingQuoteNo", "报价单号"),
            Map.entry("priority", "优先级"),
            Map.entry("quoteNo", "报价单号"),
            Map.entry("request", "请求参数"),
            Map.entry("requestNo", "请求号"),
            Map.entry("reservedBalance", "冻结余额"),
            Map.entry("role", "角色"),
            Map.entry("roleCode", "角色编码"),
            Map.entry("settleAmount", "结算金额"),
            Map.entry("share", "份额"),
            Map.entry("sourceBizNo", "来源业务单号"),
            Map.entry("sourceBizType", "来源业务类型"),
            Map.entry("status", "状态"),
            Map.entry("submission", "提交参数"),
            Map.entry("targetStatus", "目标状态"),
            Map.entry("templateCode", "模板编码"),
            Map.entry("totalLimit", "总额度"),
            Map.entry("tradeOrderNo", "交易单号"),
            Map.entry("userId", "用户标识"),
            Map.entry("validFrom", "生效时间"),
            Map.entry("validTo", "失效时间"),
            Map.entry("walletDebitAmount", "余额扣款金额"),
            Map.entry("xid", "全局事务号"),
            Map.entry("yesterdayIncome", "昨日收益")
    );

    private static final Map<String, String> ENTITY_LABELS = Map.ofEntries(
            Map.entry("admin account", "管理员账户"),
            Map.entry("credit account", "信用账户"),
            Map.entry("coupon", "券"),
            Map.entry("coupon template", "券模板"),
            Map.entry("feedback ticket", "反馈单"),
            Map.entry("fund account", "爱存账户"),
            Map.entry("message", "消息"),
            Map.entry("original trade", "原交易"),
            Map.entry("product", "产品"),
            Map.entry("redeem transaction", "转出交易"),
            Map.entry("red packet", "红包"),
            Map.entry("subscribe transaction", "转入交易"),
            Map.entry("trade", "交易"),
            Map.entry("wallet account", "余额账户")
    );

    private ErrorMessageLocalizer() {
    }

    static String localize(String raw) {
        if (raw == null) {
            return DEFAULT_MESSAGE;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return DEFAULT_MESSAGE;
        }

        String lower = trimmed.toLowerCase(Locale.ROOT);
        String byKeyword = localizeByKeyword(lower);
        if (byKeyword != null) {
            return byKeyword;
        }

        String byPattern = localizeByPattern(trimmed);
        if (byPattern != null) {
            return cleanup(byPattern);
        }

        return cleanup(replaceFieldTokens(trimmed));
    }

    private static String localizeByKeyword(String lower) {
        if (lower.contains("participant split amount must equal payableamount")
                || lower.contains("walletdebitamount + funddebitamount + creditdebitamount + inbounddebitamount must equal payableamount")) {
            return "支付拆分金额必须等于应付金额";
        }
        if (lower.contains("insufficient available balance")) {
            return "可用余额不足";
        }
        if (lower.contains("debit amount must not be less than 0")) {
            return "支付扣款金额不能小于0";
        }
        if (lower.contains("insufficient available share")) {
            return "可用份额不足";
        }
        if (lower.contains("user fast redeem quota exceeded")) {
            return "今日快速转出额度不足，请明天再试";
        }
        if (lower.contains("fund fast redeem quota exceeded")) {
            return "爱存快速转出总额度不足，请稍后再试";
        }
        if (lower.contains("pending redeem share is not enough")) {
            return "待确认转出份额不足，请稍后重试";
        }
        if (lower.contains("pending subscribe amount is not enough")) {
            return "待确认转入金额不足，请稍后重试";
        }
        if (lower.contains("product switch is disabled")) {
            return "当前产品暂不支持切换";
        }
        if (lower.contains("fund product status is not active")) {
            return "当前爱存产品状态不可用";
        }
        if (lower.contains("fund account status is not active")) {
            return "当前爱存账户状态不可用";
        }
        if (lower.contains("sender and receiver must be friends")) {
            return "当前收款人不是好友，暂时不能发红包";
        }
        if (lower.contains("current user is not the red packet receiver")) {
            return "当前用户不能领取这个红包";
        }
        if (lower.contains("red packet status does not allow claim")) {
            return "这个红包当前不可领取";
        }
        if (lower.contains("orderno must match aipay 32-digit id rule")) {
            return "请求单号格式不正确，请稍后重试";
        }
        if (lower.contains("orderno already used by another transaction type")) {
            return "请求已提交，请勿重复操作";
        }
        if (lower.contains("orderno is not a subscribe transaction")) {
            return "转入请求状态异常，请稍后重试";
        }
        if (lower.contains("orderno is not a redeem transaction")) {
            return "转出请求状态异常，请稍后重试";
        }
        if (lower.contains("currency mismatch")) {
            return "币种不一致";
        }
        if (lower.contains("nav or confirmedshare must be provided")) {
            return "净值或确认份额至少需要提供一个";
        }
        if (lower.contains("operationtype must be debit or credit")) {
            return "操作类型必须为借记或贷记";
        }
        if (lower.contains("payeeuserid must not equal payeruserid for transfer")) {
            return "转账收款方不能是自己";
        }
        if (lower.contains("fee exceeds originalamount when feebearer is payee")) {
            return "收款方承担手续费时，手续费不能大于原始金额";
        }
        if (lower.contains("invalid request")) {
            return "请求参数不合法";
        }
        return null;
    }

    private static String localizeByPattern(String message) {
        Matcher matcher = MUST_NOT_BE_BLANK.matcher(message);
        if (matcher.matches()) {
            return fieldLabel(matcher.group(1)) + "不能为空";
        }
        matcher = MUST_NOT_BE_NULL.matcher(message);
        if (matcher.matches()) {
            return fieldLabel(matcher.group(1)) + "不能为空";
        }
        matcher = MUST_BE_GREATER_THAN_ZERO.matcher(message);
        if (matcher.matches()) {
            return fieldLabel(matcher.group(1)) + "必须大于0";
        }
        matcher = MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO.matcher(message);
        if (matcher.matches()) {
            return fieldLabel(matcher.group(1)) + "不能小于0";
        }
        matcher = MUST_NOT_BE_LESS_THAN_ZERO.matcher(message);
        if (matcher.matches()) {
            return fieldLabel(matcher.group(1)) + "不能小于0";
        }
        matcher = MUST_BE_LESS_THAN_OR_EQUAL_TO.matcher(message);
        if (matcher.matches()) {
            return fieldLabel(matcher.group(1)) + "不能大于" + valueLabel(matcher.group(2));
        }
        matcher = LENGTH_AT_MOST.matcher(message);
        if (matcher.matches()) {
            return fieldLabel(matcher.group(1)) + "长度不能超过" + matcher.group(2);
        }
        matcher = LENGTH_LESS_THAN_OR_EQUAL_TO.matcher(message);
        if (matcher.matches()) {
            return fieldLabel(matcher.group(1)) + "长度不能超过" + matcher.group(2);
        }
        matcher = SIZE_LESS_THAN_OR_EQUAL_TO.matcher(message);
        if (matcher.matches()) {
            return fieldLabel(matcher.group(1)) + "数量不能超过" + matcher.group(2);
        }
        matcher = MUST_USE_FORMAT.matcher(message);
        if (matcher.matches()) {
            return fieldLabel(matcher.group(1)) + "格式必须为" + matcher.group(2);
        }
        matcher = MUST_BE_BETWEEN.matcher(message);
        if (matcher.matches()) {
            return fieldLabel(matcher.group(1)) + "必须在" + matcher.group(2) + "到" + matcher.group(3) + "之间";
        }
        matcher = UNSUPPORTED.matcher(message);
        if (matcher.matches()) {
            return "不支持的" + fieldLabel(matcher.group(1)) + "：" + matcher.group(2);
        }
        matcher = NOT_FOUND.matcher(message);
        if (matcher.matches()) {
            return "未找到" + entityLabel(matcher.group(1));
        }
        matcher = ALREADY_EXISTS.matcher(message);
        if (matcher.matches()) {
            return entityLabel(matcher.group(1)) + "已存在";
        }
        matcher = MUST_BE_BEFORE.matcher(message);
        if (matcher.matches()) {
            return fieldLabel(matcher.group(1)) + "必须早于" + fieldLabel(matcher.group(2));
        }
        matcher = CAN_NOT_EXCEED.matcher(message);
        if (matcher.matches()) {
            return fieldLabel(matcher.group(1)) + "不能大于" + fieldLabel(matcher.group(2));
        }
        matcher = MUST_NOT_BE.matcher(message);
        if (matcher.matches()) {
            return fieldLabel(matcher.group(1)) + "不能为" + valueLabel(matcher.group(2));
        }
        matcher = MUST_BE_AT_LEAST_CHARACTERS.matcher(message);
        if (matcher.matches()) {
            return fieldLabel(matcher.group(1)) + "至少需要" + matcher.group(2) + "个字符";
        }
        matcher = UNKNOWN_CODES.matcher(message);
        if (matcher.matches()) {
            return "存在未知" + fieldLabel(matcher.group(1)) + "编码：" + matcher.group(2);
        }
        matcher = CURRENCY_MUST_EQUAL.matcher(message);
        if (matcher.matches()) {
            String left = fieldLabel(matcher.group(1));
            String right = fieldLabel(stripSuffixIgnoreCase(matcher.group(2), " currency"));
            return left + "币种必须与" + right + "一致";
        }
        matcher = MUST_BE_DIFFERENT.matcher(message);
        if (matcher.matches()) {
            return fieldLabel(matcher.group(1)) + "和" + fieldLabel(matcher.group(2)) + "不能相同";
        }
        return null;
    }

    private static String fieldLabel(String raw) {
        String trimmed = normalizeToken(raw);
        if (trimmed.isEmpty()) {
            return "";
        }
        String exact = FIELD_LABELS.get(trimmed);
        if (exact != null) {
            return exact;
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        String lowerMatch = FIELD_LABELS.get(lower);
        if (lowerMatch != null) {
            return lowerMatch;
        }
        return ENTITY_LABELS.getOrDefault(lower, trimmed);
    }

    private static String entityLabel(String raw) {
        String normalized = normalizeToken(raw).toLowerCase(Locale.ROOT);
        return ENTITY_LABELS.getOrDefault(normalized, fieldLabel(raw));
    }

    private static String valueLabel(String raw) {
        String normalized = normalizeToken(raw).toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "submitted" -> "已提交";
            case "debit" -> "借记";
            case "credit" -> "贷记";
            case "payee" -> "收款方";
            case "payer" -> "付款方";
            default -> fieldLabel(raw);
        };
    }

    private static String normalizeToken(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim();
    }

    private static String stripSuffixIgnoreCase(String raw, String suffix) {
        if (raw == null) {
            return "";
        }
        String normalized = raw.trim();
        if (normalized.toLowerCase(Locale.ROOT).endsWith(suffix.toLowerCase(Locale.ROOT))) {
            return normalized.substring(0, normalized.length() - suffix.length()).trim();
        }
        return normalized;
    }

    private static String replaceFieldTokens(String message) {
        String localized = message;
        for (Map.Entry<String, String> entry : FIELD_LABELS.entrySet()) {
            localized = localized.replaceAll("\\b" + Pattern.quote(entry.getKey()) + "\\b", entry.getValue());
        }
        return localized;
    }

    private static String cleanup(String message) {
        return replaceLegacyBrandNames(message)
                .replaceAll("([\\p{IsHan}A-Za-z0-9])\\s+([\\p{IsHan}])", "$1$2")
                .replace("： ", "：")
                .trim();
    }

    private static String replaceLegacyBrandNames(String message) {
        if (message == null || message.isBlank()) {
            return message;
        }
        return message
                .replace("爱付", "爱付")
                .replace("AiPay", "爱付")
                .replace("aipay", "爱付")
                .replace("AiPay", "爱付")
                .replace("爱存", "爱存")
                .replace("AiCash", "爱存")
                .replace("AiCash", "爱存")
                .replace("aicash", "爱存")
                .replace("AiCash", "爱存")
                .replace("爱花", "爱花")
                .replace("AiCredit", "爱花")
                .replace("aicredit", "爱花")
                .replace("AiCredit", "爱花")
                .replace("爱借", "爱借")
                .replace("AiLoan", "爱借")
                .replace("ailoan", "爱借")
                .replace("AiLoan", "爱借");
    }
}
