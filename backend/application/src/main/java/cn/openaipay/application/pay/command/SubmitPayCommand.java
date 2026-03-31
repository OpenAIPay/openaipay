package cn.openaipay.application.pay.command;

/**
 * 异步提交支付请求命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public record SubmitPayCommand(
        /** 来源业务类型 */
        String sourceBizType,
        /** 来源业务单号 */
        String sourceBizNo,
        /** 交易单号 */
        String tradeOrderNo,
        /** 来源业务快照 */
        SourceBizSnapshot sourceBizSnapshot,
        /** 订单单号 */
        String bizOrderNo,
        /** 业务场景编码 */
        String businessSceneCode,
        /** 付款方用户ID */
        Long payerUserId,
        /** 收款方用户ID */
        Long payeeUserId,
        /** 计划信息 */
        SettlementPlanSnapshot settlementPlan
) {
}
