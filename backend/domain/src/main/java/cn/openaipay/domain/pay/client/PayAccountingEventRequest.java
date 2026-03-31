package cn.openaipay.domain.pay.client;

import java.time.LocalDateTime;
import java.util.List;
import org.joda.money.Money;

/**
 * PayAccountingEventRequest 请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record PayAccountingEventRequest(
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
        /** 业务键 */
        String idempotencyKey,
        /** TXID */
        String globalTxId,
        /** 业务ID */
        String traceId,
        /** 载荷内容 */
        String payload,
        /** 分录列表 */
        List<PayAccountingLegRequest> legs
) {
    public record PayAccountingLegRequest(
            /** LEG单号 */
            Integer legNo,
            /** account域信息 */
            String accountDomain,
            /** account类型 */
            String accountType,
            /** account单号 */
            String accountNo,
            /** 所属类型 */
            String ownerType,
            /** 所属ID */
            Long ownerId,
            /** 金额 */
            Money amount,
            /** 方向 */
            String direction,
            /** 业务角色信息 */
            String bizRole,
            /** 科目hint信息 */
            String subjectHint,
            /** reference单号 */
            String referenceNo,
            /** 扩展信息 */
            String metadata
    ) {
    }
}
