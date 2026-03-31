package cn.openaipay.domain.cashier.model;

import org.joda.money.Money;

/**
 * 收银台支付工具模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public class CashierPayTool {

    /** 支付工具类型 */
    private final CashierPayToolType toolType;
    /** 支付工具编码 */
    private final String toolCode;
    /** 支付工具展示名称 */
    private final String toolName;
    /** 支付工具补充说明 */
    private final String toolDescription;
    /** 是否默认选中 */
    private final boolean defaultSelected;
    /** 单笔限额 */
    private final Money singleLimit;
    /** 单日限额 */
    private final Money dailyLimit;
    /** 发卡行编码 */
    private final String bankCode;
    /** 银行卡类型 */
    private final String cardType;
    /** 手机尾号 */
    private final String phoneTailNo;

    public CashierPayTool(CashierPayToolType toolType,
                          String toolCode,
                          String toolName,
                          String toolDescription,
                          boolean defaultSelected,
                          Money singleLimit,
                          Money dailyLimit,
                          String bankCode,
                          String cardType,
                          String phoneTailNo) {
        this.toolType = toolType;
        this.toolCode = toolCode;
        this.toolName = toolName;
        this.toolDescription = toolDescription;
        this.defaultSelected = defaultSelected;
        this.singleLimit = singleLimit;
        this.dailyLimit = dailyLimit;
        this.bankCode = bankCode;
        this.cardType = cardType;
        this.phoneTailNo = phoneTailNo;
    }

    /**
     * 获取业务数据。
     */
    public CashierPayToolType getToolType() {
        return toolType;
    }

    /**
     * 获取编码。
     */
    public String getToolCode() {
        return toolCode;
    }

    /**
     * 获取业务数据。
     */
    public String getToolName() {
        return toolName;
    }

    /**
     * 获取业务数据。
     */
    public String getToolDescription() {
        return toolDescription;
    }

    /**
     * 判断是否业务数据。
     */
    public boolean isDefaultSelected() {
        return defaultSelected;
    }

    /**
     * 获取限额信息。
     */
    public Money getSingleLimit() {
        return singleLimit;
    }

    /**
     * 获取限额信息。
     */
    public Money getDailyLimit() {
        return dailyLimit;
    }

    /**
     * 获取银行编码。
     */
    public String getBankCode() {
        return bankCode;
    }

    /**
     * 获取卡类型信息。
     */
    public String getCardType() {
        return cardType;
    }

    /**
     * 获取NO信息。
     */
    public String getPhoneTailNo() {
        return phoneTailNo;
    }

    /**
     * 处理业务数据。
     */
    public CashierPayTool withDefaultSelected(boolean selected) {
        if (this.defaultSelected == selected) {
            return this;
        }
        return new CashierPayTool(
                toolType,
                toolCode,
                toolName,
                toolDescription,
                selected,
                singleLimit,
                dailyLimit,
                bankCode,
                cardType,
                phoneTailNo
        );
    }
}
