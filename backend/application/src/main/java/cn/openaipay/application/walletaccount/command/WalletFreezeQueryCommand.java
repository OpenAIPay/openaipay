package cn.openaipay.application.walletaccount.command;

/**
 * 钱包冻结查询命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record WalletFreezeQueryCommand(
        /** 用户ID */
        Long userId,
        /** 币种编码 */
        String currencyCode,
        /** 冻结类型 */
        String freezeType,
        /** 冻结状态 */
        String freezeStatus,
        /** 返回上限 */
        Integer limit
) {
}
