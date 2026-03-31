package cn.openaipay.application.adminfund.dto;

/**
 * 资金中心概览
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record AdminFundOverviewDTO(
        /** 钱包账户数量 */
        long walletAccountCount,
        /** 基金账户数量 */
        long fundAccountCount,
        /** 信用账户数量 */
        long creditAccountCount,
        /** 借款账户数量 */
        long loanAccountCount,
        /** 银行卡数量 */
        long bankCardCount
) {
}
