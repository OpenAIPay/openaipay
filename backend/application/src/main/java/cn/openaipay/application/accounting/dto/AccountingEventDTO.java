package cn.openaipay.application.accounting.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会计事件DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record AccountingEventDTO(
        /** 事件ID */
        String eventId,
        /** 事件类型 */
        String eventType,
        /** 事件版本号 */
        Integer eventVersion,
        /** 业务ID */
        String bookId,
        /** 来源信息 */
        String sourceSystem,
        /** 来源业务类型 */
        String sourceBizType,
        /** 来源业务单号 */
        String sourceBizNo,
        /** 业务单号 */
        String bizOrderNo,
        /** 请求幂等号 */
        String requestNo,
        /** 交易主单号 */
        String tradeOrderNo,
        /** 支付单号 */
        String payOrderNo,
        /** 业务场景编码 */
        String businessSceneCode,
        /** 业务域编码 */
        String businessDomainCode,
        /** 付款方用户ID */
        Long payerUserId,
        /** 收款方用户ID */
        Long payeeUserId,
        /** 币种编码 */
        String currencyCode,
        /** 业务时间 */
        LocalDateTime occurredAt,
        /** 业务日期 */
        LocalDate postingDate,
        /** 业务键 */
        String idempotencyKey,
        /** TXID */
        String globalTxId,
        /** 业务ID */
        String traceId,
        /** 载荷内容 */
        String payload,
        /** 状态编码 */
        String status,
        /** 业务单号 */
        String postedVoucherNo,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt,
        /** 分录列表 */
        List<AccountingLegDTO> legs
) {
}
