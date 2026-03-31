package cn.openaipay.application.trade.dto;

import org.joda.money.Money;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 统一交易主单数据传输对象。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record TradeOrderDTO(
        /** 交易主单号 */
        String tradeOrderNo,
        /** 请求幂等号 */
        String requestNo,
        /** 交易类型 */
        String tradeType,
        /** 交易编排场景编码，例如 TRADE_TRANSFER、TRADE_PAY */
        String businessSceneCode,
        /** 业务查询域编码，例如 AICREDIT、AILOAN、AICASH */
        String businessDomainCode,
        /** 业务交易单号，在对应业务域内唯一 */
        String bizOrderNo,
        /** 原交易单号，退款等逆向交易场景使用 */
        String originalTradeOrderNo,
        /** 付款方用户ID */
        Long payerUserId,
        /** 收款方用户ID */
        Long payeeUserId,
        /** 支付方式编码 */
        String paymentMethod,
        /** 原始金额 */
        Money originalAmount,
        /** 手续费金额 */
        Money feeAmount,
        /** 应付金额 */
        Money payableAmount,
        /** 结算金额 */
        Money settleAmount,
        /** 扣款拆分计划 */
        TradeSplitPlanDTO splitPlan,
        /** 计费报价单号 */
        String pricingQuoteNo,
        /** 支付单号 */
        String payOrderNo,
        /** 当前生效支付尝试序号 */
        Integer currentPayAttemptNo,
        /** 当前生效支付状态版本号 */
        Integer currentPayStatusVersion,
        /** 当前生效支付结果码 */
        String currentPayResultCode,
        /** 当前生效支付结果描述 */
        String currentPayResultMessage,
        /** 当前交易关联的支付尝试次数 */
        Integer payAttemptCount,
        /** 状态编码 */
        String status,
        /** 失败原因 */
        String failureReason,
        /** 扩展信息 */
        String metadata,
        /** 支付工具列表快照 */
        String paymentToolSnapshot,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt,
        /** 支付尝试列表 */
        List<TradePayAttemptDTO> payAttempts,
        /** 交易流程步骤列表 */
        List<TradeFlowStepDTO> flowSteps
) {
}
