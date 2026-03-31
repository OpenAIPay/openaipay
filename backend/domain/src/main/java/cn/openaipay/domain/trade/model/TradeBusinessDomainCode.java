package cn.openaipay.domain.trade.model;

import cn.openaipay.domain.creditaccount.model.CreditProductCodes;
import cn.openaipay.domain.fundaccount.model.FundProductCodes;

import java.util.Locale;

/**
 * 交易业务域编码。
 *
 * 业务场景：统一交易主单负责流程编排，业务域编码负责把交易映射到爱花、爱借、爱存、余额等业务查询视角。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public enum TradeBusinessDomainCode {
    /** 通用交易域：未归类到具体产品时的默认业务域。 */
    TRADE,
    /** 余额账户交易域：账户余额收支、内部转账等资金业务。 */
    WALLET,
    /** 爱花交易域：爱花消费、爱花还款等信用业务。 */
    AICREDIT,
    /** 爱借交易域：爱借放款、爱借还款等信贷业务。 */
    AILOAN,
    /** 爱存交易域：爱存申购、赎回、收益结转等基金业务。 */
    AICASH,
    /** 红包交易域：红包发放、领取、退款等营销资金业务。 */
    RED_PACKET,
    /** 入金业务域：银行卡向系统资金账户入金。 */
    INBOUND,
    /** 出金业务域：系统余额向银行卡出金。 */
    OUTBOUND;

    /**
     * 处理业务数据。
     */
    public static TradeBusinessDomainCode from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("businessDomainCode must not be blank");
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if (FundProductCodes.isPrimaryFundCode(normalized)) {
            return AICASH;
        }
        if (CreditProductCodes.isAiCredit(normalized)) {
            return AICREDIT;
        }
        if (CreditProductCodes.isAiLoan(normalized)) {
            return AILOAN;
        }
        try {
            return TradeBusinessDomainCode.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported businessDomainCode: " + raw);
        }
    }

    /**
     * 按交易场景和支付方式推断业务域。
     *
     * 业务场景：统一交易服务创建主单时，根据场景和支付方式生成标准业务域。
     */
    public static TradeBusinessDomainCode detect(String businessSceneCode, String paymentMethod) {
        String scene = normalizeUpper(businessSceneCode);
        String method = normalizeUpper(paymentMethod);
        if (containsKeyword(scene, method, "AILOAN", "LOAN_ACCOUNT", "LOAN")) {
            return AILOAN;
        }
        if (containsKeyword(scene, method, "AICREDIT", "CREDIT_ACCOUNT", "CREDIT")) {
            return AICREDIT;
        }
        if (isCreditRepayScene(scene)) {
            return AICREDIT;
        }
        if (containsKeyword(scene, method,
                FundProductCodes.AICASH)) {
            return AICASH;
        }
        if (containsKeyword(scene, method, "OUTBOUND", "WITHDRAW")) {
            return OUTBOUND;
        }
        if (containsKeyword(scene, method, "WALLET", "BALANCE")) {
            return WALLET;
        }
        if (containsKeyword(scene, method, "RED_PACKET")) {
            return RED_PACKET;
        }
        if (containsKeyword(scene, method, "INBOUND", "DEPOSIT")) {
            return INBOUND;
        }
        return TRADE;
    }

    private static boolean isCreditRepayScene(String scene) {
        return "APP_CREDIT_REPAY".equals(scene)
                || (scene.contains("CREDIT") && scene.contains("REPAY"))
                || (scene.contains("AICREDIT") && scene.contains("REPAY"))
                || (scene.contains("LOAN") && scene.contains("REPAY"))
                || (scene.contains("AILOAN") && scene.contains("REPAY"));
    }

    private static boolean containsKeyword(String scene, String method, String... keywords) {
        for (String keyword : keywords) {
            if (scene.contains(keyword) || method.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeUpper(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().toUpperCase(Locale.ROOT);
    }
}
