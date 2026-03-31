package cn.openaipay.application.fundaccount.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 基金交易扩展单回填结果。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record FundTradeBackfillResultDTO(
        /** 基金编码（为空表示不限） */
        String fundCode,
        /** 本次扫描的交易类型 */
        List<String> transactionTypes,
        /** 扫描上限 */
        int scanLimit,
        /** 扫描笔数 */
        int scannedCount,
        /** 修复笔数 */
        int repairedCount,
        /** 跳过笔数（已存在扩展单） */
        int skippedCount,
        /** 失败数 */
        int failedCount,
        /** 失败交易号（最多返回20条） */
        List<String> failedOrderNos,
        /** 结果生成时间 */
        LocalDateTime generatedAt
) {
}

