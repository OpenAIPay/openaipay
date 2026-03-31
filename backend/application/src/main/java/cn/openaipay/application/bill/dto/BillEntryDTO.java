package cn.openaipay.application.bill.dto;

import java.time.LocalDateTime;

/**
 * 统一账单读模型条目传输对象。
 *
 * 业务场景：账单中心需要统一展示 trade 与 fundTrade 的业务流水，
 * 通过 trade_bill_index 聚合视图直接返回展示字段，避免前端拼表。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record BillEntryDTO(
        /** 交易主单号 */
        String tradeOrderNo,
        /** 业务域编码（TRADE/AICREDIT/AILOAN/AICASH/WALLET 等） */
        String businessDomainCode,
        /** 业务交易单号 */
        String bizOrderNo,
        /** 产品类型 */
        String productType,
        /** 业务类型 */
        String businessType,
        /** 方向（DEBIT/CREDIT） */
        String direction,
        /** 交易类型 */
        String tradeType,
        /** 业务账户号 */
        String accountNo,
        /** 账单号 */
        String billNo,
        /** billmonth信息 */
        String billMonth,
        /** 展示标题 */
        String displayTitle,
        /** 展示副标题 */
        String displaySubtitle,
        /** 金额 */
        String amount,
        /** 红包优惠金额（兼容移动端字段） */
        String couponDiscountAmount,
        /** 优惠金额 */
        String discountAmount,
        /** 优惠券编号 */
        String couponNo,
        /** 币种编码 */
        String currencyCode,
        /** 状态编码 */
        String status,
        /** 业务排序时间 */
        LocalDateTime tradeTime
) {
}
