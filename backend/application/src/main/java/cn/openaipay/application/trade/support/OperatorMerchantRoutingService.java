package cn.openaipay.application.trade.support;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 运营商商户路由服务
 *
 * 话费充值等运营商代收场景下，根据 operator 元数据把收款方路由到对应运营商商户大账号。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Component
public class OperatorMerchantRoutingService {
    /** 手机号用户ID */
    public static final long CHINA_MOBILE_MERCHANT_USER_ID = 880201069206400001L;
    /** 用户ID */
    public static final long CHINA_TELECOM_MERCHANT_USER_ID = 880214069206400014L;
    /** 用户ID */
    public static final long CHINA_UNICOM_MERCHANT_USER_ID = 880222069206400022L;

    /** 手机号TOPUP场景信息 */
    private static final String MOBILE_HALL_TOP_UP_SCENE = "APP_MOBILE_HALL_TOP_UP";
    /** 手机号产品信息 */
    private static final String PHONE_BILL_PRODUCT = "PHONE_BILL";
    /** 手机号信息 */
    private static final String MOBILE_HALL_ENTRY = "mobile-hall-top-up";

    /**
     * 解析收款方用户ID。
     */
    public Long resolvePayeeUserId(String businessSceneCode, Long fallbackPayeeUserId, String metadata) {
        Map<String, String> metadataMap = parseMetadata(metadata);
        if (!isPhoneBillScene(businessSceneCode, metadataMap)) {
            return fallbackPayeeUserId;
        }
        Long merchantUserId = resolveOperatorMerchantUserId(metadataMap.get("operator"));
        return merchantUserId == null ? fallbackPayeeUserId : merchantUserId;
    }

    /**
     * 判断是否应信用收款方信息。
     */
    public boolean shouldCreditPayee(Long payerUserId, Long payeeUserId) {
        return payerUserId != null
                && payeeUserId != null
                && payerUserId > 0
                && payeeUserId > 0
                && !payerUserId.equals(payeeUserId);
    }

    private boolean isPhoneBillScene(String businessSceneCode, Map<String, String> metadataMap) {
        String normalizedSceneCode = normalize(businessSceneCode);
        if (normalizedSceneCode != null && MOBILE_HALL_TOP_UP_SCENE.equalsIgnoreCase(normalizedSceneCode)) {
            return true;
        }
        String product = normalize(metadataMap.get("product"));
        if (product != null && PHONE_BILL_PRODUCT.equalsIgnoreCase(product)) {
            return true;
        }
        String entry = normalize(metadataMap.get("entry"));
        return entry != null && MOBILE_HALL_ENTRY.equalsIgnoreCase(entry);
    }

    private Long resolveOperatorMerchantUserId(String rawOperator) {
        String operator = normalize(rawOperator);
        if (operator == null) {
            return null;
        }
        return switch (operator) {
            case "CHINA_MOBILE", "MOBILE", "CMCC", "中国移动", "移动" -> CHINA_MOBILE_MERCHANT_USER_ID;
            case "CHINA_TELECOM", "TELECOM", "CTCC", "中国电信", "电信" -> CHINA_TELECOM_MERCHANT_USER_ID;
            case "CHINA_UNICOM", "UNICOM", "CUCC", "中国联通", "联通" -> CHINA_UNICOM_MERCHANT_USER_ID;
            default -> null;
        };
    }

    private Map<String, String> parseMetadata(String metadata) {
        Map<String, String> metadataMap = new LinkedHashMap<>();
        String normalizedMetadata = normalize(metadata);
        if (normalizedMetadata == null) {
            return metadataMap;
        }
        String[] segments = normalizedMetadata.split(";");
        for (String segment : segments) {
            if (segment == null || segment.isBlank()) {
                continue;
            }
            String[] kv = segment.split("=", 2);
            if (kv.length != 2) {
                continue;
            }
            String key = normalize(kv[0]);
            String value = normalize(kv[1]);
            if (key == null || value == null) {
                continue;
            }
            metadataMap.put(key.toLowerCase(Locale.ROOT), value);
        }
        return metadataMap;
    }

    private String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed.toUpperCase(Locale.ROOT);
    }
}
