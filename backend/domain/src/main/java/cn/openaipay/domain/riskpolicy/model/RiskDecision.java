package cn.openaipay.domain.riskpolicy.model;

/**
 * 风控决策结果。
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record RiskDecision(
        /** 是否通过。 */
        boolean passed,
        /** 决策编码。 */
        String code,
        /** 决策文案。 */
        String message
) {

    /**
     * 通过决策。
     */
    public static RiskDecision pass() {
        return new RiskDecision(true, "PASS", "risk policy passed");
    }

    /**
     * 拒绝决策。
     */
    public static RiskDecision reject(String code, String message) {
        return new RiskDecision(false, normalize(code), normalize(message));
    }

    private static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }
}
