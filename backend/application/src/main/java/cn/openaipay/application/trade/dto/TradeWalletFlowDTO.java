package cn.openaipay.application.trade.dto;

import java.time.LocalDateTime;

/**
 * 钱包余额页账变摘要传输对象。
 *
 * 业务场景：移动端“我的-余额-余额变动明细”仅需要最近账变展示字段，
 * 包含交易方向、签名金额、时间和对手方信息，避免前端读取整笔交易明细。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record TradeWalletFlowDTO(
        /** 交易主单号 */
        String tradeOrderNo,
        /** 交易类型（TRANSFER/PAY/WITHDRAW/DEPOSIT/REFUND 等） */
        String tradeType,
        /** 业务场景编码，前端可据此做文案兜底 */
        String businessSceneCode,
        /** 方向 */
        String direction,
        /** 带正负号的账变金额字符串，例如 -1200.00 或 88.50 */
        String signedWalletAmount,
        /** 币种编码 */
        String currencyCode,
        /** counterparty用户ID */
        String counterpartyUserId,
        /** 对手方展示昵称，前端可直接用于列表文案与头像兜底 */
        String counterpartyNickname,
        /** 对手方头像地址，前端无需再额外补查资料接口 */
        String counterpartyAvatarUrl,
        /** 后端生成的默认账变标题文案 */
        String displayTitle,
        /** 账变发生时间，优先取交易更新时间 */
        LocalDateTime occurredAt
) {
}
