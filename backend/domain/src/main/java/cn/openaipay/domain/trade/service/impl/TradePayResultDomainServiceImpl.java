package cn.openaipay.domain.trade.service.impl;

import cn.openaipay.domain.trade.service.TradePayResultDecision;
import cn.openaipay.domain.trade.service.TradePayResultDomainService;
import cn.openaipay.domain.trade.service.TradePayResultHandling;
import java.util.Locale;

/**
 * 交易域支付结果决策服务实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public class TradePayResultDomainServiceImpl implements TradePayResultDomainService {

    /**
     * 处理业务数据。
     */
    @Override
    public TradePayResultDecision decide(String payStatus, String resultCode, String resultMessage) {
        String normalizedPayStatus = normalizeRequired(payStatus, "payStatus").toUpperCase(Locale.ROOT);
        return switch (normalizedPayStatus) {
            case "COMMITTED" -> new TradePayResultDecision(normalizedPayStatus, TradePayResultHandling.COMMITTED, null);
            case "SUBMITTED", "TRYING", "PREPARED", "COMMITTING" ->
                    new TradePayResultDecision(normalizedPayStatus, TradePayResultHandling.PROCESSING, null);
            case "ROLLED_BACK" -> new TradePayResultDecision(
                    normalizedPayStatus,
                    TradePayResultHandling.ROLLED_BACK,
                    buildFailureReason(resultCode, resultMessage, "pay rolled back")
            );
            case "RECON_PENDING" -> new TradePayResultDecision(
                    normalizedPayStatus,
                    TradePayResultHandling.RECON_PENDING,
                    buildFailureReason(resultCode, resultMessage, "pay recon pending")
            );
            default -> new TradePayResultDecision(
                    normalizedPayStatus,
                    TradePayResultHandling.FAILED,
                    buildFailureReason(resultCode, resultMessage, "pay failed")
            );
        };
    }

    private String buildFailureReason(String resultCode, String resultMessage, String fallbackPrefix) {
        String normalizedMessage = normalizeOptional(resultMessage);
        String normalizedCode = normalizeOptional(resultCode);
        if (normalizedMessage != null && normalizedCode != null) {
            return fallbackPrefix + ": " + normalizedMessage + " (" + normalizedCode + ")";
        }
        if (normalizedMessage != null) {
            return fallbackPrefix + ": " + normalizedMessage;
        }
        if (normalizedCode != null) {
            return fallbackPrefix + ": " + normalizedCode;
        }
        return fallbackPrefix;
    }

    private String normalizeRequired(String raw, String fieldName) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private String normalizeOptional(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
