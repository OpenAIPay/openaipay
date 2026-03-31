package cn.openaipay.application.gateway.dto;

import java.time.LocalDateTime;

/**
 * 网关入金结果
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record GatewayDepositResultDTO(
        /** 是否成功 */
        boolean success,
        /** 结果编码 */
        String resultCode,
        /** 结果信息 */
        String resultDescription,
        /** 机构ID */
        String instId,
        /** 机构单号 */
        String instSerialNo,
        /** 机构REF单号 */
        String instRefNo,
        /** 机构渠道编码 */
        String instChannelCode,
        /** 入金订单单号 */
        String inboundOrderNo,
        /** GMTresp信息 */
        LocalDateTime gmtResp,
        /** GMT结算信息 */
        LocalDateTime gmtSettle
) {
}
