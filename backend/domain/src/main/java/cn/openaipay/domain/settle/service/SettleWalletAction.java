package cn.openaipay.domain.settle.service;

import org.joda.money.Money;

/**
 * 钱包入账动作。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record SettleWalletAction(
        /** 用户ID */
        Long userId,
        /** 金额 */
        Money amount,
        /** 业务类型 */
        String operationType,
        /** 交易业务类型 */
        String tradeBizType,
        /** 结算业务单号 */
        String settleBizNo,
        /** 失败消息信息 */
        String failureMessagePrefix
) {
}
