package cn.openaipay.adapter.gateway.web.mock;

import cn.openaipay.application.shared.id.AiPayBizTypeRegistry;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 网联风格银行侧Mock接口（提现）。
 *
 * 报文采用key=value&key=value文本格式，并带sign字段进行HMAC验签。
 *
 * @author: tenggk.ai
 * @date: 2026/03/06
 */
@RestController
@RequestMapping("/api/mock/nucc/withdraw")
public class NetsBankMockWithdrawController {
    /** 签约信息 */
    private static final String SIGN_ALGORITHM = "HmacSHA256";

    /** AiPay ID 生成器。 */
    private final AiPayIdGenerator aiPayIdGenerator;
    /** AiPay 业务类型码注册表。 */
    private final AiPayBizTypeRegistry aiPayBizTypeRegistry;
    /** 运行环境标识。 */
    private final Environment environment;
    /** 默认签名密钥。 */
    private final String defaultSecret;
    /** 默认商户号。 */
    private final String defaultMerchantId;
    /** 模拟延迟毫秒数。 */
    private final long simulatedLatencyMs;
    /** 出金订单状态。 */
    private final ConcurrentMap<String, WithdrawState> withdrawStates = new ConcurrentHashMap<>();

    public NetsBankMockWithdrawController(AiPayIdGenerator aiPayIdGenerator,
                                          AiPayBizTypeRegistry aiPayBizTypeRegistry,
                                          Environment environment,
                                          @Value("${aipay.gateway.bank.default-secret:OPENAIPAY_BANK_SECRET}") String defaultSecret,
                                          @Value("${aipay.gateway.bank.mock.default-merchant-id:OPENAIPAY}") String defaultMerchantId,
                                          @Value("${aipay.gateway.bank.mock.simulated-latency-ms:80}") long simulatedLatencyMs) {
        this.aiPayIdGenerator = aiPayIdGenerator;
        this.aiPayBizTypeRegistry = aiPayBizTypeRegistry;
        this.environment = environment;
        this.defaultSecret = normalizeRequired(defaultSecret, "defaultSecret");
        this.defaultMerchantId = normalizeRequired(defaultMerchantId, "defaultMerchantId");
        this.simulatedLatencyMs = Math.max(0L, simulatedLatencyMs);
    }

    @PostMapping(
            value = "/initiate",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    /**
     * 处理业务数据。
     */
    public String initiate(@RequestBody String requestBody) {
        Map<String, String> request = parsePayload(requestBody);
        String instId = normalizeInstId(request.get("instId"));
        String secret = resolveSecret(instId);
        if (!verifyRequestSignature(request, secret)) {
            return signResponse(new LinkedHashMap<>(Map.of(
                    "resultCode", "BANK_SIGN_ERROR",
                    "resultMessage", "request signature invalid",
                    "instId", instId,
                    "gmtResp", LocalDateTime.now().toString()
            )), secret);
        }

        ensureMerchantId(request);
        simulateLatency();

        String payerUserId = defaultValue(request.get("payerUserId"), "0");
        String outboundId = normalizeRequired(request.get("outboundId"), "outboundId");
        String instChannelCode = normalizeRequired(request.get("instChannelCode"), "instChannelCode");
        if (shouldFail(request)) {
            LocalDateTime now = LocalDateTime.now();
            withdrawStates.put(outboundId, new WithdrawState(
                    "FAILED",
                    instId,
                    instChannelCode,
                    null,
                    null,
                    null,
                    now,
                    null
            ));
            Map<String, String> response = new LinkedHashMap<>();
            response.put("resultCode", "FAILED");
            response.put("resultMessage", "银行受理失败");
            response.put("instId", instId);
            response.put("instChannelCode", instChannelCode);
            response.put("gmtResp", now.toString());
            return signResponse(response, secret);
        }
        String instSerialNo = aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_GATEWAY,
                aiPayBizTypeRegistry.gatewayWithdrawInitBizType(),
                payerUserId
        );
        String instRefNo = "REF" + instSerialNo.substring(instSerialNo.length() - 12);

        Map<String, String> response = new LinkedHashMap<>();
        response.put("resultCode", "SUCCESS");
        response.put("resultMessage", "银行受理成功");
        response.put("instId", instId);
        response.put("instChannelCode", instChannelCode);
        response.put("instSerialNo", instSerialNo);
        response.put("instRefNo", instRefNo);
        LocalDateTime now = LocalDateTime.now();
        response.put("gmtResp", now.toString());
        withdrawStates.put(outboundId, new WithdrawState(
                "ACCEPTED",
                instId,
                instChannelCode,
                instSerialNo,
                instRefNo,
                null,
                now,
                null
        ));
        return signResponse(response, secret);
    }

    private boolean shouldFail(Map<String, String> request) {
        String requestIdentify = normalizeOptional(request.get("requestIdentify"));
        if (requestIdentify == null) {
            return false;
        }
        return requestIdentify.toUpperCase(Locale.ROOT).contains("FAIL");
    }

    @PostMapping(
            value = "/query",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    /**
     * 查询业务数据。
     */
    public String query(@RequestBody String requestBody) {
        Map<String, String> request = parsePayload(requestBody);
        String instId = normalizeInstId(request.get("instId"));
        String secret = resolveSecret(instId);
        if (!verifyRequestSignature(request, secret)) {
            return signResponse(new LinkedHashMap<>(Map.of(
                    "resultCode", "BANK_SIGN_ERROR",
                    "resultMessage", "request signature invalid",
                    "instId", instId,
                    "gmtResp", LocalDateTime.now().toString()
            )), secret);
        }

        ensureMerchantId(request);
        simulateLatency();

        String outboundId = normalizeRequired(request.get("outboundId"), "outboundId");
        String instChannelCode = normalizeRequired(request.get("instChannelCode"), "instChannelCode");
        WithdrawState state = withdrawStates.get(outboundId);
        Map<String, String> response = new LinkedHashMap<>();
        response.put("instId", instId);
        response.put("instChannelCode", instChannelCode);
        if (state == null) {
            response.put("resultCode", "NOT_FOUND");
            response.put("resultMessage", "银行侧无此订单");
            response.put("gmtResp", LocalDateTime.now().toString());
            return signResponse(response, secret);
        }
        response.put("resultCode", normalizeQueryState(state.state()));
        response.put("resultMessage", queryMessage(state.state()));
        if (state.instSerialNo() != null) {
            response.put("instSerialNo", state.instSerialNo());
        }
        if (state.instRefNo() != null) {
            response.put("instRefNo", state.instRefNo());
        }
        if (state.outboundOrderNo() != null) {
            response.put("outboundOrderNo", state.outboundOrderNo());
        }
        response.put("gmtResp", defaultTime(state.gmtResp()).toString());
        if (state.gmtSettle() != null) {
            response.put("gmtSettle", state.gmtSettle().toString());
        }
        return signResponse(response, secret);
    }

    @PostMapping(
            value = "/confirm",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    /**
     * 确认业务数据。
     */
    public String confirm(@RequestBody String requestBody) {
        Map<String, String> request = parsePayload(requestBody);
        String instId = normalizeInstId(request.get("instId"));
        String secret = resolveSecret(instId);
        if (!verifyRequestSignature(request, secret)) {
            return signResponse(new LinkedHashMap<>(Map.of(
                    "resultCode", "BANK_SIGN_ERROR",
                    "resultMessage", "request signature invalid",
                    "instId", instId,
                    "gmtResp", LocalDateTime.now().toString()
            )), secret);
        }

        ensureMerchantId(request);
        simulateLatency();

        String outboundId = normalizeRequired(request.get("outboundId"), "outboundId");
        String instChannelCode = normalizeRequired(request.get("instChannelCode"), "instChannelCode");
        WithdrawState state = withdrawStates.get(outboundId);
        if (state != null && ("CANCELED".equalsIgnoreCase(state.state()) || "FAILED".equalsIgnoreCase(state.state()))) {
            return signResponse(new LinkedHashMap<>(Map.of(
                    "resultCode", "FAILED",
                    "resultMessage", "银行出款失败",
                    "instId", instId,
                    "instChannelCode", instChannelCode,
                    "gmtResp", LocalDateTime.now().toString()
            )), secret);
        }
        String outboundOrderNo = aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_GATEWAY,
                aiPayBizTypeRegistry.gatewayWithdrawConfirmBizType(),
                "0"
        );
        LocalDateTime now = LocalDateTime.now();

        Map<String, String> response = new LinkedHashMap<>();
        response.put("resultCode", "SUCCESS");
        response.put("resultMessage", "银行出款成功");
        response.put("instId", instId);
        response.put("instChannelCode", instChannelCode);
        response.put("outboundOrderNo", outboundOrderNo);
        response.put("gmtResp", now.toString());
        response.put("gmtSettle", now.toString());
        withdrawStates.put(outboundId, new WithdrawState(
                "SUCCESS",
                instId,
                instChannelCode,
                state == null ? null : state.instSerialNo(),
                state == null ? null : state.instRefNo(),
                outboundOrderNo,
                state == null ? now : defaultTime(state.gmtResp()),
                now
        ));
        return signResponse(response, secret);
    }

    @PostMapping(
            value = "/cancel",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    /**
     * 取消业务数据。
     */
    public String cancel(@RequestBody String requestBody) {
        Map<String, String> request = parsePayload(requestBody);
        String instId = normalizeInstId(request.get("instId"));
        String secret = resolveSecret(instId);
        if (!verifyRequestSignature(request, secret)) {
            return signResponse(new LinkedHashMap<>(Map.of(
                    "resultCode", "BANK_SIGN_ERROR",
                    "resultMessage", "request signature invalid",
                    "instId", instId,
                    "gmtResp", LocalDateTime.now().toString()
            )), secret);
        }

        ensureMerchantId(request);
        simulateLatency();

        String outboundId = normalizeRequired(request.get("outboundId"), "outboundId");
        String instChannelCode = normalizeRequired(request.get("instChannelCode"), "instChannelCode");
        WithdrawState state = withdrawStates.get(outboundId);
        if (state != null && "SUCCESS".equalsIgnoreCase(state.state())) {
            return signResponse(new LinkedHashMap<>(Map.of(
                    "resultCode", "FAILED",
                    "resultMessage", "银行撤销失败，订单已出款",
                    "instId", instId,
                    "instChannelCode", instChannelCode,
                    "gmtResp", LocalDateTime.now().toString()
            )), secret);
        }
        withdrawStates.put(outboundId, new WithdrawState(
                "CANCELED",
                instId,
                instChannelCode,
                state == null ? null : state.instSerialNo(),
                state == null ? null : state.instRefNo(),
                state == null ? null : state.outboundOrderNo(),
                state == null ? LocalDateTime.now() : defaultTime(state.gmtResp()),
                state == null ? null : state.gmtSettle()
        ));
        Map<String, String> response = new LinkedHashMap<>();
        response.put("resultCode", "SUCCESS");
        response.put("resultMessage", "银行撤销成功");
        response.put("instId", instId);
        response.put("instChannelCode", instChannelCode);
        response.put("gmtResp", LocalDateTime.now().toString());
        return signResponse(response, secret);
    }

    private void ensureMerchantId(Map<String, String> request) {
        String merchantId = normalizeRequired(request.get("merchantId"), "merchantId");
        if (!defaultMerchantId.equalsIgnoreCase(merchantId)) {
            throw new IllegalArgumentException("unsupported merchantId: " + merchantId);
        }
    }

    private void simulateLatency() {
        if (simulatedLatencyMs <= 0L) {
            return;
        }
        try {
            Thread.sleep(simulatedLatencyMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("mock bank call interrupted");
        }
    }

    private boolean verifyRequestSignature(Map<String, String> request, String secret) {
        Map<String, String> signSource = new LinkedHashMap<>(request);
        String actualSign = normalizeOptional(signSource.remove("sign"));
        if (actualSign == null) {
            return false;
        }
        String expectedSign = sign(canonicalPayload(signSource), secret);
        return expectedSign.equals(actualSign);
    }

    private String signResponse(Map<String, String> responseMap, String secret) {
        String payload = canonicalPayload(responseMap);
        return payload + "&sign=" + sign(payload, secret);
    }

    private Map<String, String> parsePayload(String payload) {
        Map<String, String> values = new LinkedHashMap<>();
        String normalized = normalizeOptional(payload);
        if (normalized == null) {
            return values;
        }
        String[] segments = normalized.split("&");
        for (String segment : segments) {
            if (segment == null || segment.isBlank()) {
                continue;
            }
            String[] kv = segment.split("=", 2);
            if (kv.length != 2) {
                continue;
            }
            String key = normalizeOptional(kv[0]);
            if (key == null) {
                continue;
            }
            values.put(key, kv[1].trim());
        }
        return values;
    }

    private String canonicalPayload(Map<String, String> values) {
        List<Map.Entry<String, String>> entries = new ArrayList<>(values.entrySet());
        entries.sort(Comparator.comparing(Map.Entry::getKey));
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : entries) {
            String key = normalizeOptional(entry.getKey());
            String value = normalizeOptional(entry.getValue());
            if (key == null || value == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(key).append('=').append(value);
        }
        return builder.toString();
    }

    private String sign(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance(SIGN_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), SIGN_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return hex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("sign failed: " + ex.getMessage(), ex);
        }
    }

    private String hex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(String.format(Locale.ROOT, "%02x", b));
        }
        return builder.toString();
    }

    private String resolveSecret(String instId) {
        String key = "aipay.gateway.bank.inst-secrets." + instId.toLowerCase(Locale.ROOT);
        String configured = environment.getProperty(key);
        return normalizeOptional(configured) == null ? defaultSecret : normalizeOptional(configured);
    }

    private String normalizeInstId(String raw) {
        String normalized = normalizeRequired(raw, "instId");
        int separator = normalized.indexOf(':');
        String inst = separator > 0 ? normalized.substring(0, separator) : normalized;
        return inst.toUpperCase(Locale.ROOT);
    }

    private String defaultValue(String raw, String fallback) {
        String normalized = normalizeOptional(raw);
        return normalized == null ? fallback : normalized;
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

    private String normalizeQueryState(String state) {
        String normalized = defaultValue(state, "UNKNOWN").toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "SUCCEEDED" -> "SUCCESS";
            case "ACCEPTED", "SUCCESS", "CANCELED", "FAILED", "NOT_FOUND" -> normalized;
            default -> "UNKNOWN";
        };
    }

    private String queryMessage(String state) {
        return switch (normalizeQueryState(state)) {
            case "SUCCESS" -> "银行出款成功";
            case "ACCEPTED" -> "银行已受理";
            case "CANCELED" -> "银行已撤销";
            case "FAILED" -> "银行处理失败";
            case "NOT_FOUND" -> "银行侧无此订单";
            default -> "银行查单失败";
        };
    }

    private LocalDateTime defaultTime(LocalDateTime value) {
        return value == null ? LocalDateTime.now() : value;
    }

    private record WithdrawState(
            /** state信息 */
            String state,
            /** 机构ID */
            String instId,
            /** 机构渠道编码 */
            String instChannelCode,
            /** 机构serial单号 */
            String instSerialNo,
            /** 机构REF单号 */
            String instRefNo,
            /** 出金订单单号 */
            String outboundOrderNo,
            /** GMTresp信息 */
            LocalDateTime gmtResp,
            /** GMT结算信息 */
            LocalDateTime gmtSettle
    ) {
    }
}
