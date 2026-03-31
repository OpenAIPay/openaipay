package cn.openaipay.application.pay.async;

import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.json.JsonWriter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付结果变更消息载荷。
 *
 * @author: tenggk.ai
 * @date: 2026/03/14
 */
public record PayResultChangedPayload(
        /** 支付单号 */
        String payOrderNo,
        /** 来源业务类型 */
        String sourceBizType,
        /** 来源业务单号 */
        String sourceBizNo,
        /** 支付状态 */
        String payStatus,
        /** 状态版本号 */
        Integer statusVersion,
        /** 结果编码 */
        String resultCode,
        /** 结果说明 */
        String resultMessage
) {
    /**
     * 转换为业务数据。
     */
    public String toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("payOrderNo", payOrderNo);
        payload.put("sourceBizType", sourceBizType);
        payload.put("sourceBizNo", sourceBizNo);
        payload.put("payStatus", payStatus);
        payload.put("statusVersion", statusVersion);
        if (resultCode != null && !resultCode.isBlank()) {
            payload.put("resultCode", resultCode.trim());
        }
        if (resultMessage != null && !resultMessage.isBlank()) {
            payload.put("resultMessage", resultMessage.trim());
        }
        return JsonWriter.standard().writeToString(payload);
    }

    /**
     * 处理业务数据。
     */
    public static PayResultChangedPayload fromPayload(String payload) {
        Map<String, Object> raw = JsonParserFactory.getJsonParser().parseMap(payload);
        return new PayResultChangedPayload(
                stringValue(raw.get("payOrderNo")),
                stringValue(raw.get("sourceBizType")),
                stringValue(raw.get("sourceBizNo")),
                stringValue(raw.get("payStatus")),
                integerValue(raw.get("statusVersion")),
                stringValue(raw.get("resultCode")),
                stringValue(raw.get("resultMessage"))
        );
    }

    private static String stringValue(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        String value = String.valueOf(rawValue).trim();
        return value.isEmpty() ? null : value;
    }

    private static Integer integerValue(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        if (rawValue instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(rawValue));
    }
}
