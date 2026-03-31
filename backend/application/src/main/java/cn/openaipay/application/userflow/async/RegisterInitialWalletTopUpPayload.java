package cn.openaipay.application.userflow.async;

import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.json.JsonWriter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 注册成功后发放初始余额消息载荷。
 *
 * @author: tenggk.ai
 * @date: 2026/03/21
 */
public record RegisterInitialWalletTopUpPayload(
        /** 用户ID */
        String userId,
        /** 登录账号 */
        String loginId,
        /** 账号来源 */
        String accountSource
) {

    /**
     * 转换为消息体。
     */
    public String toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", userId);
        payload.put("loginId", loginId);
        payload.put("accountSource", accountSource);
        return JsonWriter.standard().writeToString(payload);
    }

    /**
     * 从消息体解析。
     */
    public static RegisterInitialWalletTopUpPayload fromPayload(String payload) {
        Map<String, Object> raw = JsonParserFactory.getJsonParser().parseMap(payload);
        Object userIdValue = raw.get("userId");
        Object loginIdValue = raw.get("loginId");
        Object accountSourceValue = raw.get("accountSource");
        return new RegisterInitialWalletTopUpPayload(
                userIdValue == null ? null : String.valueOf(userIdValue),
                loginIdValue == null ? null : String.valueOf(loginIdValue),
                accountSourceValue == null ? null : String.valueOf(accountSourceValue)
        );
    }
}
