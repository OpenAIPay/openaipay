package cn.openaipay.application.pay.async;

import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.json.JsonWriter;

import java.util.Map;

/**
 * 支付对账/补偿请求消息载荷。
 *
 * @author: tenggk.ai
 * @date: 2026/03/18
 */
public record PayReconRequestedPayload(
    /** 支付单号 */
    String payOrderNo
) {

    /**
     * 转换为业务数据。
     */
    public String toPayload() {
        return JsonWriter.standard().writeToString(Map.of("payOrderNo", payOrderNo));
    }

    /**
     * 处理业务数据。
     */
    public static PayReconRequestedPayload fromPayload(String payload) {
        Map<String, Object> raw = JsonParserFactory.getJsonParser().parseMap(payload);
        Object value = raw.get("payOrderNo");
        return new PayReconRequestedPayload(value == null ? null : String.valueOf(value));
    }
}
