package cn.openaipay.domain.cashier.model;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 收银台场景配置模型，定义某个 sceneCode 下可展示的付款渠道以及银行卡准入策略。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public record CashierSceneConfiguration(
        /** 收银台场景编码，例如 TRANSFER / PAY / WITHDRAW。 */
        String sceneCode,
        /** 当前场景允许展示的付款渠道集合。 */
        List<CashierChannelCode> supportedChannels,
        /** 当前场景的银行卡准入策略。 */
        CashierBankCardPolicy bankCardPolicy,
        /** 当银行卡渠道无可用卡时返回给前端展示的文案。 */
        String emptyBankCardText
) {

    /** 默认场景编码。 */
    public static final String DEFAULT_SCENE_CODE = "TRANSFER";
    /** 默认无卡提示文案。 */
    public static final String DEFAULT_EMPTY_BANK_CARD_TEXT = "暂无可用银行卡";

    public CashierSceneConfiguration {
        sceneCode = normalizeSceneCode(sceneCode);
        supportedChannels = List.copyOf(Objects.requireNonNull(supportedChannels, "supportedChannels must not be null"));
        bankCardPolicy = Objects.requireNonNull(bankCardPolicy, "bankCardPolicy must not be null");
        emptyBankCardText = normalizeEmptyBankCardText(emptyBankCardText);
    }

    /**
     * 判断当前场景是否允许展示某个付款渠道。
     *
     * @param channelCode 渠道编码
     * @return true 表示当前场景允许该渠道出现在收银台
     */
    public boolean supportsChannel(CashierChannelCode channelCode) {
        return supportedChannels.contains(channelCode);
    }

    /**
     * 判断当前场景是否允许展示某类银行卡。
     *
     * @param cardType 银行卡类型原始值
     * @return true 表示该银行卡符合当前场景准入策略
     */
    public boolean supportsBankCard(String cardType) {
        return bankCardPolicy.supports(cardType);
    }

    /**
     * 解析业务数据。
     *
     * @param sceneCode 业务场景编码
     * @return 当前场景对应的收银台配置
     */
    public static CashierSceneConfiguration resolve(String sceneCode) {
        String normalizedSceneCode = normalizeSceneCode(sceneCode);
        return switch (normalizedSceneCode) {
            case "TRANSFER" -> new CashierSceneConfiguration(
                    normalizedSceneCode,
                    List.of(CashierChannelCode.WALLET, CashierChannelCode.BANK_CARD),
                    CashierBankCardPolicy.ALL_CARDS,
                    DEFAULT_EMPTY_BANK_CARD_TEXT
            );
            case "PAY", "APP_CREDIT_REPAY", "CREDIT_REPAY", "AICREDIT_REPAY" -> new CashierSceneConfiguration(
                    normalizedSceneCode,
                    List.of(CashierChannelCode.WALLET, CashierChannelCode.FUND, CashierChannelCode.BANK_CARD),
                    CashierBankCardPolicy.DEBIT_ONLY,
                    "暂无可用借记卡"
            );
            case "DEPOSIT" -> new CashierSceneConfiguration(
                    normalizedSceneCode,
                    List.of(CashierChannelCode.BANK_CARD),
                    CashierBankCardPolicy.ALL_CARDS,
                    DEFAULT_EMPTY_BANK_CARD_TEXT
            );
            case "WITHDRAW" -> new CashierSceneConfiguration(
                    normalizedSceneCode,
                    List.of(CashierChannelCode.BANK_CARD),
                    CashierBankCardPolicy.DEBIT_ONLY,
                    "暂无可用借记卡"
            );
            case "TRADE_PAY_LOAN_ACCOUNT" -> new CashierSceneConfiguration(
                    normalizedSceneCode,
                    List.of(CashierChannelCode.WALLET, CashierChannelCode.BANK_CARD),
                    CashierBankCardPolicy.DEBIT_ONLY,
                    "暂无可用借记卡"
            );
            default -> new CashierSceneConfiguration(
                    normalizedSceneCode,
                    List.of(CashierChannelCode.WALLET, CashierChannelCode.BANK_CARD),
                    CashierBankCardPolicy.ALL_CARDS,
                    DEFAULT_EMPTY_BANK_CARD_TEXT
            );
        };
    }

    /**
     * 规范化场景编码。
     *
     * @param sceneCode 原始场景编码
     * @return 标准化后的场景编码
     */
    public static String normalizeSceneCode(String sceneCode) {
        if (sceneCode == null || sceneCode.isBlank()) {
            return DEFAULT_SCENE_CODE;
        }
        String normalized = sceneCode.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() > 64) {
            throw new IllegalArgumentException("sceneCode length must be <= 64");
        }
        return normalized;
    }

    private static String normalizeEmptyBankCardText(String emptyBankCardText) {
        if (emptyBankCardText == null || emptyBankCardText.isBlank()) {
            return DEFAULT_EMPTY_BANK_CARD_TEXT;
        }
        return emptyBankCardText.trim();
    }
}
