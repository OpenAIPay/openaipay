package cn.openaipay.application.pay.async;

import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.json.JsonWriter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付成功后触发会计过账的异步消息载荷。
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public record PayAccountingEventRequestedPayload(
        /** 支付单号 */
        String payOrderNo
) {
    /**
     * 转换为业务数据。
     */
    public String toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("payOrderNo", payOrderNo);
        return JsonWriter.standard().writeToString(payload);
    }

    /**
     * 处理业务数据。
     */
    public static PayAccountingEventRequestedPayload fromPayload(String payload) {
        Map<String, Object> raw = JsonParserFactory.getJsonParser().parseMap(payload);
        return new PayAccountingEventRequestedPayload(stringValue(raw.get("payOrderNo")));
    }

    private static String stringValue(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        String value = String.valueOf(rawValue).trim();
        return value.isEmpty() ? null : value;
    }
}
