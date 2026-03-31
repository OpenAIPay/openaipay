package cn.openaipay.application.fundaccount.dto;

import java.time.LocalDateTime;

/**
 * 基金交易明细查询结果。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record FundTransactionDetailDTO(
        /** 订单号 */
        String orderNo,
        /** 用户ID */
        Long userId,
        /** 基金编码 */
        String fundCode,
        /** 交易类型 */
        String transactionType,
        /** 交易状态 */
        String transactionStatus,
        /** 请求金额 */
        String requestAmount,
        /** 请求份额 */
        String requestShare,
        /** 确认金额 */
        String confirmedAmount,
        /** 确认份额 */
        String confirmedShare,
        /** 业务单号 */
        String businessNo,
        /** 扩展信息 */
        String extInfo,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}

