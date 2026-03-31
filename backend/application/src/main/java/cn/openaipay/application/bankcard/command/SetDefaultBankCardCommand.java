package cn.openaipay.application.bankcard.command;

/**
 * 设置默认银行卡命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record SetDefaultBankCardCommand(
        /** 用户ID */
        Long userId,
        /** 银行卡号 */
        String cardNo
) {
}
