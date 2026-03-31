package cn.openaipay.application.fundaccount.dto;
/**
 * 基金交易数据传输对象
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record FundTransactionDTO(
        /** 订单号 */
        String orderNo,
        /** 交易类型 */
        String transactionType,
        /** 交易状态 */
        String transactionStatus,
        /** 消息内容 */
        String message
) {
}
