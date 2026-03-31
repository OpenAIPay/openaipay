package cn.openaipay.domain.pay.client;

import java.time.LocalDateTime;
import org.joda.money.Money;

/**
 * PayOutboundOrderSnapshot 记录模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record PayOutboundOrderSnapshot(
        /** 出金单号 */
        String outboundId,
        /** 请求业务单号 */
        String requestBizNo,
        /** 业务单号 */
        String bizOrderNo,
        /** 交易单号 */
        String tradeOrderNo,
        /** 支付单号 */
        String payOrderNo,
        /** 收款方账号 */
        String payeeAccountNo,
        /** 出金金额 */
        Money outboundAmount,
        /** 出金状态 */
        String outboundStatus,
        /** 结果编码 */
        String resultCode,
        /** 结果信息 */
        String resultDescription,
        /** 机构ID */
        String instId,
        /** 机构渠道编码 */
        String instChannelCode,
        /** 出金订单单号 */
        String outboundOrderNo,
        /** 支付渠道编码 */
        String payChannelCode,
        /** GMTsubmit信息 */
        LocalDateTime gmtSubmit,
        /** GMTresp信息 */
        LocalDateTime gmtResp,
        /** GMT结算信息 */
        LocalDateTime gmtSettle,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
