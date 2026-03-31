package cn.openaipay.infrastructure.gateway.bank;

import cn.openaipay.application.gateway.bank.BankGatewaySignatureException;
import cn.openaipay.application.gateway.bank.BankGatewayTimeoutException;
import cn.openaipay.application.gateway.port.BankDepositCancelRequest;
import cn.openaipay.application.gateway.port.BankDepositConfirmRequest;
import cn.openaipay.application.gateway.port.BankDepositInitiateRequest;
import cn.openaipay.application.gateway.port.BankDepositQueryRequest;
import cn.openaipay.application.gateway.port.BankDepositResult;
import cn.openaipay.application.gateway.port.BankGatewayPort;
import cn.openaipay.application.gateway.port.BankWithdrawCancelRequest;
import cn.openaipay.application.gateway.port.BankWithdrawConfirmRequest;
import cn.openaipay.application.gateway.port.BankWithdrawInitiateRequest;
import cn.openaipay.application.gateway.port.BankWithdrawQueryRequest;
import cn.openaipay.application.gateway.port.BankWithdrawResult;
import cn.openaipay.application.shared.id.AiPayBizTypeRegistry;
import cn.openaipay.application.shared.id.AiPayIdGenerator;
import org.joda.money.Money;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
 * 默认银行网关客户端
 *
 * 优先走网联风格Mock API（HTTP调用）；保留本地模拟兜底。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Component
public class DefaultBankGatewayClient implements BankGatewayPort {
    /** 签名算法 */
    private static final String SIGN_ALGORITHM = "HmacSHA256";
    /** 通道模式：MOCK_API / LOCAL */
    private static final String MODE_MOCK_API = "MOCK_API";

    /** 订单号生成器 */
    private final AiPayIdGenerator aiPayIdGenerator;
    /** 业务码注册器 */
    private final AiPayBizTypeRegistry aiPayBizTypeRegistry;
    /** 环境配置 */
    private final Environment environment;
    /** 银行超时时间 */
    private final long timeoutMs;
    /** 模拟网络耗时 */
    private final long simulatedLatencyMs;
    /** 默认签名密钥 */
    private final String defaultSecret;
    /** 是否模拟响应签名错误 */
    private final boolean simulateBadResponseSignature;
    /** 银行调用模式 */
    private final String gatewayMode;
    /** 网联Mock基础地址 */
    private final String mockBaseUrl;
    /** 商户标识 */
    private final String merchantId;
    /** HTTP客户端 */
    private final HttpClient httpClient;
    /** 本地模式入金状态 */
    private final ConcurrentMap<String, DepositMockState> localDepositStates = new ConcurrentHashMap<>();
    /** 本地模式出金状态 */
    private final ConcurrentMap<String, WithdrawMockState> localWithdrawStates = new ConcurrentHashMap<>();

    public DefaultBankGatewayClient(AiPayIdGenerator aiPayIdGenerator,
                                    AiPayBizTypeRegistry aiPayBizTypeRegistry,
                                    Environment environment,
                                    @Value("${aipay.gateway.bank.timeout-ms:3000}") long timeoutMs,
                                    @Value("${aipay.gateway.bank.simulated-latency-ms:120}") long simulatedLatencyMs,
                                    @Value("${aipay.gateway.bank.default-secret:OPENAIPAY_BANK_SECRET}") String defaultSecret,
                                    @Value("${aipay.gateway.bank.simulate-bad-response-signature:false}") boolean simulateBadResponseSignature,
                                    @Value("${aipay.gateway.bank.mode:MOCK_API}") String gatewayMode,
                                    @Value("${aipay.gateway.bank.mock.base-url:http://127.0.0.1:8080/api/mock/nucc}") String mockBaseUrl,
                                    @Value("${aipay.gateway.bank.mock.default-merchant-id:OPENAIPAY}") String merchantId) {
        this.aiPayIdGenerator = aiPayIdGenerator;
        this.aiPayBizTypeRegistry = aiPayBizTypeRegistry;
        this.environment = environment;
        this.timeoutMs = Math.max(1L, timeoutMs);
        this.simulatedLatencyMs = Math.max(0L, simulatedLatencyMs);
        this.defaultSecret = normalizeRequired(defaultSecret, "defaultSecret");
        this.simulateBadResponseSignature = simulateBadResponseSignature;
        this.gatewayMode = normalizeRequired(gatewayMode, "gatewayMode").toUpperCase(Locale.ROOT);
        this.mockBaseUrl = normalizeRequired(mockBaseUrl, "mockBaseUrl");
        this.merchantId = normalizeRequired(merchantId, "merchantId");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(500L, this.timeoutMs)))
                .build();
    }

    /**
     * 处理业务数据。
     */
    @Override
    public BankDepositResult initiateDeposit(BankDepositInitiateRequest command) {
        String instId = normalizeInstId(command.instChannelCode());
        String secret = resolveSecret(instId);

        Map<String, String> requestMap = new LinkedHashMap<>();
        requestMap.put("action", "DEPOSIT_INIT");
        requestMap.put("merchantId", merchantId);
        requestMap.put("instId", instId);
        requestMap.put("inboundId", normalizeRequired(command.inboundId(), "inboundId"));
        requestMap.put("instChannelCode", normalizeRequired(command.instChannelCode(), "instChannelCode"));
        requestMap.put("payerUserId", String.valueOf(command.payerUserId()));
        requestMap.put("payerAccountNo", normalizeRequired(command.payerAccountNo(), "payerAccountNo"));
        requestMap.put("amount", normalizeAmount(command.amount()));
        requestMap.put("currencyCode", command.amount().getCurrencyUnit().getCode().toUpperCase(Locale.ROOT));
        requestMap.put("payChannelCode", normalizeRequired(command.payChannelCode(), "payChannelCode"));
        requestMap.put("requestIdentify", normalizeRequired(command.requestIdentify(), "requestIdentify"));
        requestMap.put("bizIdentity", normalizeRequired(command.bizIdentity(), "bizIdentity"));
        requestMap.put("requestTime", LocalDateTime.now().toString());

        if (MODE_MOCK_API.equals(gatewayMode)) {
            Map<String, String> responseMap = callMockApi("/deposit/initiate", requestMap, secret);
            return toInitiateResult(command, instId, responseMap);
        }
        return localInitiateDeposit(command, requestMap, instId, secret);
    }

    /**
     * 查询业务数据。
     */
    @Override
    public BankDepositResult queryDeposit(BankDepositQueryRequest command) {
        String instId = normalizeInstId(command.instChannelCode());
        String secret = resolveSecret(instId);

        Map<String, String> requestMap = new LinkedHashMap<>();
        requestMap.put("action", "DEPOSIT_QUERY");
        requestMap.put("merchantId", merchantId);
        requestMap.put("instId", instId);
        requestMap.put("inboundId", normalizeRequired(command.inboundId(), "inboundId"));
        requestMap.put("instChannelCode", normalizeRequired(command.instChannelCode(), "instChannelCode"));
        requestMap.put("requestTime", LocalDateTime.now().toString());

        if (MODE_MOCK_API.equals(gatewayMode)) {
            Map<String, String> responseMap = callMockApi("/deposit/query", requestMap, secret);
            return toQueryDepositResult(command, instId, responseMap);
        }
        return localQueryDeposit(command, requestMap, instId, secret);
    }

    /**
     * 确认业务数据。
     */
    @Override
    public BankDepositResult confirmDeposit(BankDepositConfirmRequest command) {
        String instId = normalizeInstId(command.instChannelCode());
        String secret = resolveSecret(instId);

        Map<String, String> requestMap = new LinkedHashMap<>();
        requestMap.put("action", "DEPOSIT_CONFIRM");
        requestMap.put("merchantId", merchantId);
        requestMap.put("instId", instId);
        requestMap.put("inboundId", normalizeRequired(command.inboundId(), "inboundId"));
        requestMap.put("instChannelCode", normalizeRequired(command.instChannelCode(), "instChannelCode"));
        requestMap.put("requestTime", LocalDateTime.now().toString());

        if (MODE_MOCK_API.equals(gatewayMode)) {
            Map<String, String> responseMap = callMockApi("/deposit/confirm", requestMap, secret);
            return toConfirmResult(command, instId, responseMap);
        }
        return localConfirmDeposit(command, requestMap, instId, secret);
    }

    /**
     * 取消业务数据。
     */
    @Override
    public BankDepositResult cancelDeposit(BankDepositCancelRequest command) {
        String instId = normalizeInstId(command.instChannelCode());
        String secret = resolveSecret(instId);

        Map<String, String> requestMap = new LinkedHashMap<>();
        requestMap.put("action", "DEPOSIT_CANCEL");
        requestMap.put("merchantId", merchantId);
        requestMap.put("instId", instId);
        requestMap.put("inboundId", normalizeRequired(command.inboundId(), "inboundId"));
        requestMap.put("instChannelCode", normalizeRequired(command.instChannelCode(), "instChannelCode"));
        if (normalizeOptional(command.reason()) != null) {
            requestMap.put("reason", normalizeOptional(command.reason()));
        }
        requestMap.put("requestTime", LocalDateTime.now().toString());

        if (MODE_MOCK_API.equals(gatewayMode)) {
            Map<String, String> responseMap = callMockApi("/deposit/cancel", requestMap, secret);
            return toCancelResult(command, instId, responseMap);
        }
        return localCancelDeposit(command, requestMap, instId, secret);
    }

    /**
     * 处理业务数据。
     */
    @Override
    public BankWithdrawResult initiateWithdraw(BankWithdrawInitiateRequest command) {
        String instId = normalizeInstId(command.instChannelCode());
        String secret = resolveSecret(instId);

        Map<String, String> requestMap = new LinkedHashMap<>();
        requestMap.put("action", "WITHDRAW_INIT");
        requestMap.put("merchantId", merchantId);
        requestMap.put("instId", instId);
        requestMap.put("outboundId", normalizeRequired(command.outboundId(), "outboundId"));
        requestMap.put("instChannelCode", normalizeRequired(command.instChannelCode(), "instChannelCode"));
        requestMap.put("payerUserId", String.valueOf(command.payerUserId()));
        requestMap.put("payeeAccountNo", normalizeRequired(command.payeeAccountNo(), "payeeAccountNo"));
        requestMap.put("amount", normalizeAmount(command.amount()));
        requestMap.put("currencyCode", command.amount().getCurrencyUnit().getCode().toUpperCase(Locale.ROOT));
        requestMap.put("payChannelCode", normalizeRequired(command.payChannelCode(), "payChannelCode"));
        requestMap.put("requestIdentify", normalizeRequired(command.requestIdentify(), "requestIdentify"));
        requestMap.put("bizIdentity", normalizeRequired(command.bizIdentity(), "bizIdentity"));
        requestMap.put("requestTime", LocalDateTime.now().toString());

        if (MODE_MOCK_API.equals(gatewayMode)) {
            Map<String, String> responseMap = callMockApi("/withdraw/initiate", requestMap, secret);
            return toWithdrawInitiateResult(command, instId, responseMap);
        }
        return localInitiateWithdraw(command, requestMap, instId, secret);
    }

    /**
     * 查询业务数据。
     */
    @Override
    public BankWithdrawResult queryWithdraw(BankWithdrawQueryRequest command) {
        String instId = normalizeInstId(command.instChannelCode());
        String secret = resolveSecret(instId);

        Map<String, String> requestMap = new LinkedHashMap<>();
        requestMap.put("action", "WITHDRAW_QUERY");
        requestMap.put("merchantId", merchantId);
        requestMap.put("instId", instId);
        requestMap.put("outboundId", normalizeRequired(command.outboundId(), "outboundId"));
        requestMap.put("instChannelCode", normalizeRequired(command.instChannelCode(), "instChannelCode"));
        requestMap.put("requestTime", LocalDateTime.now().toString());

        if (MODE_MOCK_API.equals(gatewayMode)) {
            Map<String, String> responseMap = callMockApi("/withdraw/query", requestMap, secret);
            return toQueryWithdrawResult(command, instId, responseMap);
        }
        return localQueryWithdraw(command, requestMap, instId, secret);
    }

    /**
     * 确认业务数据。
     */
    @Override
    public BankWithdrawResult confirmWithdraw(BankWithdrawConfirmRequest command) {
        String instId = normalizeInstId(command.instChannelCode());
        String secret = resolveSecret(instId);

        Map<String, String> requestMap = new LinkedHashMap<>();
        requestMap.put("action", "WITHDRAW_CONFIRM");
        requestMap.put("merchantId", merchantId);
        requestMap.put("instId", instId);
        requestMap.put("outboundId", normalizeRequired(command.outboundId(), "outboundId"));
        requestMap.put("instChannelCode", normalizeRequired(command.instChannelCode(), "instChannelCode"));
        requestMap.put("requestTime", LocalDateTime.now().toString());

        if (MODE_MOCK_API.equals(gatewayMode)) {
            Map<String, String> responseMap = callMockApi("/withdraw/confirm", requestMap, secret);
            return toWithdrawConfirmResult(command, instId, responseMap);
        }
        return localConfirmWithdraw(command, requestMap, instId, secret);
    }

    /**
     * 取消业务数据。
     */
    @Override
    public BankWithdrawResult cancelWithdraw(BankWithdrawCancelRequest command) {
        String instId = normalizeInstId(command.instChannelCode());
        String secret = resolveSecret(instId);

        Map<String, String> requestMap = new LinkedHashMap<>();
        requestMap.put("action", "WITHDRAW_CANCEL");
        requestMap.put("merchantId", merchantId);
        requestMap.put("instId", instId);
        requestMap.put("outboundId", normalizeRequired(command.outboundId(), "outboundId"));
        requestMap.put("instChannelCode", normalizeRequired(command.instChannelCode(), "instChannelCode"));
        if (normalizeOptional(command.reason()) != null) {
            requestMap.put("reason", normalizeOptional(command.reason()));
        }
        requestMap.put("requestTime", LocalDateTime.now().toString());

        if (MODE_MOCK_API.equals(gatewayMode)) {
            Map<String, String> responseMap = callMockApi("/withdraw/cancel", requestMap, secret);
            return toWithdrawCancelResult(command, instId, responseMap);
        }
        return localCancelWithdraw(command, requestMap, instId, secret);
    }

    private BankDepositResult toInitiateResult(BankDepositInitiateRequest command,
                                               String instId,
                                               Map<String, String> responseMap) {
        String resultCode = defaultValue(responseMap.get("resultCode"), "BANK_CALL_ERROR");
        boolean success = "SUCCESS".equalsIgnoreCase(resultCode);
        LocalDateTime gmtResp = parseDateTime(responseMap.get("gmtResp"));
        return new BankDepositResult(
                success,
                resultCode,
                defaultValue(responseMap.get("resultMessage"), success ? "银行受理成功" : "银行受理失败"),
                instId,
                normalizeOptional(responseMap.get("instSerialNo")),
                normalizeOptional(responseMap.get("instRefNo")),
                defaultValue(responseMap.get("instChannelCode"), command.instChannelCode()),
                null,
                gmtResp == null ? LocalDateTime.now() : gmtResp,
                null
        );
    }

    private BankDepositResult toConfirmResult(BankDepositConfirmRequest command,
                                              String instId,
                                              Map<String, String> responseMap) {
        String resultCode = defaultValue(responseMap.get("resultCode"), "BANK_CALL_ERROR");
        boolean success = "SUCCESS".equalsIgnoreCase(resultCode);
        LocalDateTime gmtResp = parseDateTime(responseMap.get("gmtResp"));
        LocalDateTime gmtSettle = parseDateTime(responseMap.get("gmtSettle"));
        return new BankDepositResult(
                success,
                resultCode,
                defaultValue(responseMap.get("resultMessage"), success ? "银行入账成功" : "银行入账失败"),
                instId,
                null,
                null,
                defaultValue(responseMap.get("instChannelCode"), command.instChannelCode()),
                normalizeOptional(responseMap.get("inboundOrderNo")),
                gmtResp == null ? LocalDateTime.now() : gmtResp,
                gmtSettle
        );
    }

    private BankDepositResult toQueryDepositResult(BankDepositQueryRequest command,
                                                   String instId,
                                                   Map<String, String> responseMap) {
        String resultCode = defaultValue(responseMap.get("resultCode"), "BANK_CALL_ERROR");
        LocalDateTime gmtResp = parseDateTime(responseMap.get("gmtResp"));
        LocalDateTime gmtSettle = parseDateTime(responseMap.get("gmtSettle"));
        return new BankDepositResult(
                isQuerySucceeded(resultCode),
                resultCode,
                defaultValue(responseMap.get("resultMessage"), defaultDepositQueryMessage(resultCode)),
                instId,
                normalizeOptional(responseMap.get("instSerialNo")),
                normalizeOptional(responseMap.get("instRefNo")),
                defaultValue(responseMap.get("instChannelCode"), command.instChannelCode()),
                normalizeOptional(responseMap.get("inboundOrderNo")),
                gmtResp == null ? LocalDateTime.now() : gmtResp,
                gmtSettle
        );
    }

    private BankDepositResult toCancelResult(BankDepositCancelRequest command,
                                             String instId,
                                             Map<String, String> responseMap) {
        String resultCode = defaultValue(responseMap.get("resultCode"), "BANK_CALL_ERROR");
        boolean success = "SUCCESS".equalsIgnoreCase(resultCode);
        LocalDateTime gmtResp = parseDateTime(responseMap.get("gmtResp"));
        return new BankDepositResult(
                success,
                resultCode,
                defaultValue(responseMap.get("resultMessage"), success ? "银行撤销成功" : "银行撤销失败"),
                instId,
                null,
                null,
                defaultValue(responseMap.get("instChannelCode"), command.instChannelCode()),
                null,
                gmtResp == null ? LocalDateTime.now() : gmtResp,
                null
        );
    }

    private BankWithdrawResult toWithdrawInitiateResult(BankWithdrawInitiateRequest command,
                                                        String instId,
                                                        Map<String, String> responseMap) {
        String resultCode = defaultValue(responseMap.get("resultCode"), "BANK_CALL_ERROR");
        boolean success = "SUCCESS".equalsIgnoreCase(resultCode);
        LocalDateTime gmtResp = parseDateTime(responseMap.get("gmtResp"));
        return new BankWithdrawResult(
                success,
                resultCode,
                defaultValue(responseMap.get("resultMessage"), success ? "银行受理成功" : "银行受理失败"),
                instId,
                normalizeOptional(responseMap.get("instSerialNo")),
                normalizeOptional(responseMap.get("instRefNo")),
                defaultValue(responseMap.get("instChannelCode"), command.instChannelCode()),
                null,
                gmtResp == null ? LocalDateTime.now() : gmtResp,
                null
        );
    }

    private BankWithdrawResult toWithdrawConfirmResult(BankWithdrawConfirmRequest command,
                                                       String instId,
                                                       Map<String, String> responseMap) {
        String resultCode = defaultValue(responseMap.get("resultCode"), "BANK_CALL_ERROR");
        boolean success = "SUCCESS".equalsIgnoreCase(resultCode);
        LocalDateTime gmtResp = parseDateTime(responseMap.get("gmtResp"));
        LocalDateTime gmtSettle = parseDateTime(responseMap.get("gmtSettle"));
        return new BankWithdrawResult(
                success,
                resultCode,
                defaultValue(responseMap.get("resultMessage"), success ? "银行出款成功" : "银行出款失败"),
                instId,
                null,
                null,
                defaultValue(responseMap.get("instChannelCode"), command.instChannelCode()),
                normalizeOptional(responseMap.get("outboundOrderNo")),
                gmtResp == null ? LocalDateTime.now() : gmtResp,
                gmtSettle
        );
    }

    private BankWithdrawResult toQueryWithdrawResult(BankWithdrawQueryRequest command,
                                                     String instId,
                                                     Map<String, String> responseMap) {
        String resultCode = defaultValue(responseMap.get("resultCode"), "BANK_CALL_ERROR");
        LocalDateTime gmtResp = parseDateTime(responseMap.get("gmtResp"));
        LocalDateTime gmtSettle = parseDateTime(responseMap.get("gmtSettle"));
        return new BankWithdrawResult(
                isQuerySucceeded(resultCode),
                resultCode,
                defaultValue(responseMap.get("resultMessage"), defaultWithdrawQueryMessage(resultCode)),
                instId,
                normalizeOptional(responseMap.get("instSerialNo")),
                normalizeOptional(responseMap.get("instRefNo")),
                defaultValue(responseMap.get("instChannelCode"), command.instChannelCode()),
                normalizeOptional(responseMap.get("outboundOrderNo")),
                gmtResp == null ? LocalDateTime.now() : gmtResp,
                gmtSettle
        );
    }

    private BankWithdrawResult toWithdrawCancelResult(BankWithdrawCancelRequest command,
                                                      String instId,
                                                      Map<String, String> responseMap) {
        String resultCode = defaultValue(responseMap.get("resultCode"), "BANK_CALL_ERROR");
        boolean success = "SUCCESS".equalsIgnoreCase(resultCode);
        LocalDateTime gmtResp = parseDateTime(responseMap.get("gmtResp"));
        return new BankWithdrawResult(
                success,
                resultCode,
                defaultValue(responseMap.get("resultMessage"), success ? "银行撤销成功" : "银行撤销失败"),
                instId,
                null,
                null,
                defaultValue(responseMap.get("instChannelCode"), command.instChannelCode()),
                null,
                gmtResp == null ? LocalDateTime.now() : gmtResp,
                null
        );
    }

    private Map<String, String> callMockApi(String path, Map<String, String> requestMap, String secret) {
        String requestPayload = canonicalPayload(requestMap);
        String requestSignature = sign(requestPayload, secret);
        String requestBody = requestPayload + "&sign=" + requestSignature;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildMockUrl(path)))
                .timeout(Duration.ofMillis(timeoutMs))
                .header("Content-Type", "text/plain;charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("mock bank api status=" + response.statusCode());
            }
            Map<String, String> responseMap = parsePayload(response.body());
            String responseSignature = normalizeOptional(responseMap.remove("sign"));
            if (responseSignature == null) {
                throw new BankGatewaySignatureException("响应签名缺失");
            }
            String responsePayload = canonicalPayload(responseMap);
            verifySignature(responsePayload, responseSignature, secret, "响应签名");
            return responseMap;
        } catch (HttpTimeoutException ex) {
            throw new BankGatewayTimeoutException("mock bank api timeout");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("bank call interrupted");
        } catch (IOException ex) {
            throw new IllegalStateException("mock bank api io error: " + ex.getMessage(), ex);
        }
    }

    private String buildMockUrl(String path) {
        String normalized = mockBaseUrl.endsWith("/") ? mockBaseUrl.substring(0, mockBaseUrl.length() - 1) : mockBaseUrl;
        return normalized + path;
    }

    private BankDepositResult localInitiateDeposit(BankDepositInitiateRequest command,
                                                   Map<String, String> requestMap,
                                                   String instId,
                                                   String secret) {
        String requestPayload = canonicalPayload(requestMap);
        String requestSignature = sign(requestPayload, secret);
        ensureWithinTimeout("DEPOSIT_INIT");
        verifySignature(requestPayload, requestSignature, secret, "请求签名");

        String instSerialNo = aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_GATEWAY,
                aiPayBizTypeRegistry.gatewayDepositInitBizType(),
                String.valueOf(command.payerUserId())
        );
        String instRefNo = "REF" + instSerialNo.substring(instSerialNo.length() - 12);
        LocalDateTime now = LocalDateTime.now();

        Map<String, String> responseMap = new LinkedHashMap<>();
        responseMap.put("resultCode", "SUCCESS");
        responseMap.put("resultMessage", "银行受理成功");
        responseMap.put("instChannelCode", command.instChannelCode());
        responseMap.put("instSerialNo", instSerialNo);
        responseMap.put("instRefNo", instRefNo);
        responseMap.put("gmtResp", now.toString());
        String responsePayload = canonicalPayload(responseMap);
        String responseSignature = sign(responsePayload, secret);
        if (simulateBadResponseSignature) {
            responseSignature = responseSignature + "X";
        }
        verifySignature(responsePayload, responseSignature, secret, "响应签名");

        localDepositStates.put(command.inboundId(), new DepositMockState(
                "ACCEPTED",
                instId,
                command.instChannelCode(),
                instSerialNo,
                instRefNo,
                null,
                now,
                null
        ));

        return new BankDepositResult(
                true,
                "SUCCESS",
                "银行受理成功",
                instId,
                instSerialNo,
                instRefNo,
                command.instChannelCode(),
                null,
                now,
                null
        );
    }

    private BankDepositResult localConfirmDeposit(BankDepositConfirmRequest command,
                                                  Map<String, String> requestMap,
                                                  String instId,
                                                  String secret) {
        String requestPayload = canonicalPayload(requestMap);
        String requestSignature = sign(requestPayload, secret);
        ensureWithinTimeout("DEPOSIT_CONFIRM");
        verifySignature(requestPayload, requestSignature, secret, "请求签名");

        LocalDateTime now = LocalDateTime.now();
        String inboundOrderNo = aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_GATEWAY,
                aiPayBizTypeRegistry.gatewayDepositConfirmBizType(),
                "0"
        );
        Map<String, String> responseMap = new LinkedHashMap<>();
        responseMap.put("resultCode", "SUCCESS");
        responseMap.put("resultMessage", "银行入账成功");
        responseMap.put("instChannelCode", command.instChannelCode());
        responseMap.put("inboundOrderNo", inboundOrderNo);
        responseMap.put("gmtResp", now.toString());
        responseMap.put("gmtSettle", now.toString());
        String responsePayload = canonicalPayload(responseMap);
        String responseSignature = sign(responsePayload, secret);
        if (simulateBadResponseSignature) {
            responseSignature = responseSignature + "X";
        }
        verifySignature(responsePayload, responseSignature, secret, "响应签名");

        DepositMockState state = localDepositStates.get(command.inboundId());
        if (state != null && "CANCELED".equalsIgnoreCase(state.state())) {
            return new BankDepositResult(
                    false,
                    "FAILED",
                    "银行入账失败，订单已撤销",
                    instId,
                    state.instSerialNo(),
                    state.instRefNo(),
                    command.instChannelCode(),
                    null,
                    now,
                    null
            );
        }
        localDepositStates.put(command.inboundId(), new DepositMockState(
                "SUCCESS",
                instId,
                command.instChannelCode(),
                state == null ? null : state.instSerialNo(),
                state == null ? null : state.instRefNo(),
                inboundOrderNo,
                state == null ? now : defaultTime(state.gmtResp(), now),
                now
        ));

        return new BankDepositResult(
                true,
                "SUCCESS",
                "银行入账成功",
                instId,
                null,
                null,
                command.instChannelCode(),
                inboundOrderNo,
                now,
                now
        );
    }

    private BankDepositResult localCancelDeposit(BankDepositCancelRequest command,
                                                 Map<String, String> requestMap,
                                                 String instId,
                                                 String secret) {
        String requestPayload = canonicalPayload(requestMap);
        String requestSignature = sign(requestPayload, secret);
        ensureWithinTimeout("DEPOSIT_CANCEL");
        verifySignature(requestPayload, requestSignature, secret, "请求签名");

        LocalDateTime now = LocalDateTime.now();
        Map<String, String> responseMap = new LinkedHashMap<>();
        responseMap.put("resultCode", "SUCCESS");
        responseMap.put("resultMessage", "银行撤销成功");
        responseMap.put("instChannelCode", command.instChannelCode());
        responseMap.put("gmtResp", now.toString());
        String responsePayload = canonicalPayload(responseMap);
        String responseSignature = sign(responsePayload, secret);
        if (simulateBadResponseSignature) {
            responseSignature = responseSignature + "X";
        }
        verifySignature(responsePayload, responseSignature, secret, "响应签名");

        DepositMockState state = localDepositStates.get(command.inboundId());
        if (state != null && "SUCCESS".equalsIgnoreCase(state.state())) {
            return new BankDepositResult(
                    false,
                    "FAILED",
                    "银行撤销失败，订单已入账",
                    instId,
                    state.instSerialNo(),
                    state.instRefNo(),
                    command.instChannelCode(),
                    state.inboundOrderNo(),
                    now,
                    state.gmtSettle()
            );
        }
        localDepositStates.put(command.inboundId(), new DepositMockState(
                "CANCELED",
                instId,
                command.instChannelCode(),
                state == null ? null : state.instSerialNo(),
                state == null ? null : state.instRefNo(),
                state == null ? null : state.inboundOrderNo(),
                state == null ? now : defaultTime(state.gmtResp(), now),
                state == null ? null : state.gmtSettle()
        ));

        return new BankDepositResult(
                true,
                "SUCCESS",
                "银行撤销成功",
                instId,
                null,
                null,
                command.instChannelCode(),
                null,
                now,
                null
        );
    }

    private BankDepositResult localQueryDeposit(BankDepositQueryRequest command,
                                                Map<String, String> requestMap,
                                                String instId,
                                                String secret) {
        String requestPayload = canonicalPayload(requestMap);
        String requestSignature = sign(requestPayload, secret);
        ensureWithinTimeout("DEPOSIT_QUERY");
        verifySignature(requestPayload, requestSignature, secret, "请求签名");

        DepositMockState state = localDepositStates.get(command.inboundId());
        if (state == null) {
            return new BankDepositResult(
                    true,
                    "NOT_FOUND",
                    "银行侧无此订单",
                    instId,
                    null,
                    null,
                    command.instChannelCode(),
                    null,
                    LocalDateTime.now(),
                    null
            );
        }
        return toDepositQueryStateResult(command, state, instId);
    }

    private BankWithdrawResult localInitiateWithdraw(BankWithdrawInitiateRequest command,
                                                     Map<String, String> requestMap,
                                                     String instId,
                                                     String secret) {
        String requestPayload = canonicalPayload(requestMap);
        String requestSignature = sign(requestPayload, secret);
        ensureWithinTimeout("WITHDRAW_INIT");
        verifySignature(requestPayload, requestSignature, secret, "请求签名");

        if (shouldFailWithdraw(command.requestIdentify())) {
            LocalDateTime now = LocalDateTime.now();
            localWithdrawStates.put(command.outboundId(), new WithdrawMockState(
                    "FAILED",
                    instId,
                    command.instChannelCode(),
                    null,
                    null,
                    null,
                    now,
                    null
            ));
            return new BankWithdrawResult(
                    false,
                    "FAILED",
                    "银行受理失败",
                    instId,
                    null,
                    null,
                    command.instChannelCode(),
                    null,
                    now,
                    null
            );
        }

        String instSerialNo = aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_GATEWAY,
                aiPayBizTypeRegistry.gatewayWithdrawInitBizType(),
                String.valueOf(command.payerUserId())
        );
        String instRefNo = "REF" + instSerialNo.substring(instSerialNo.length() - 12);
        LocalDateTime now = LocalDateTime.now();

        Map<String, String> responseMap = new LinkedHashMap<>();
        responseMap.put("resultCode", "SUCCESS");
        responseMap.put("resultMessage", "银行受理成功");
        responseMap.put("instChannelCode", command.instChannelCode());
        responseMap.put("instSerialNo", instSerialNo);
        responseMap.put("instRefNo", instRefNo);
        responseMap.put("gmtResp", now.toString());
        String responsePayload = canonicalPayload(responseMap);
        String responseSignature = sign(responsePayload, secret);
        if (simulateBadResponseSignature) {
            responseSignature = responseSignature + "X";
        }
        verifySignature(responsePayload, responseSignature, secret, "响应签名");

        localWithdrawStates.put(command.outboundId(), new WithdrawMockState(
                "ACCEPTED",
                instId,
                command.instChannelCode(),
                instSerialNo,
                instRefNo,
                null,
                now,
                null
        ));

        return new BankWithdrawResult(
                true,
                "SUCCESS",
                "银行受理成功",
                instId,
                instSerialNo,
                instRefNo,
                command.instChannelCode(),
                null,
                now,
                null
        );
    }

    private BankWithdrawResult localConfirmWithdraw(BankWithdrawConfirmRequest command,
                                                    Map<String, String> requestMap,
                                                    String instId,
                                                    String secret) {
        String requestPayload = canonicalPayload(requestMap);
        String requestSignature = sign(requestPayload, secret);
        ensureWithinTimeout("WITHDRAW_CONFIRM");
        verifySignature(requestPayload, requestSignature, secret, "请求签名");

        LocalDateTime now = LocalDateTime.now();
        String outboundOrderNo = aiPayIdGenerator.generate(
                AiPayIdGenerator.DOMAIN_GATEWAY,
                aiPayBizTypeRegistry.gatewayWithdrawConfirmBizType(),
                "0"
        );
        Map<String, String> responseMap = new LinkedHashMap<>();
        responseMap.put("resultCode", "SUCCESS");
        responseMap.put("resultMessage", "银行出款成功");
        responseMap.put("instChannelCode", command.instChannelCode());
        responseMap.put("outboundOrderNo", outboundOrderNo);
        responseMap.put("gmtResp", now.toString());
        responseMap.put("gmtSettle", now.toString());
        String responsePayload = canonicalPayload(responseMap);
        String responseSignature = sign(responsePayload, secret);
        if (simulateBadResponseSignature) {
            responseSignature = responseSignature + "X";
        }
        verifySignature(responsePayload, responseSignature, secret, "响应签名");

        WithdrawMockState state = localWithdrawStates.get(command.outboundId());
        if (state != null && ("CANCELED".equalsIgnoreCase(state.state()) || "FAILED".equalsIgnoreCase(state.state()))) {
            return new BankWithdrawResult(
                    false,
                    "FAILED",
                    "银行出款失败",
                    instId,
                    state.instSerialNo(),
                    state.instRefNo(),
                    command.instChannelCode(),
                    null,
                    now,
                    null
            );
        }
        localWithdrawStates.put(command.outboundId(), new WithdrawMockState(
                "SUCCESS",
                instId,
                command.instChannelCode(),
                state == null ? null : state.instSerialNo(),
                state == null ? null : state.instRefNo(),
                outboundOrderNo,
                state == null ? now : defaultTime(state.gmtResp(), now),
                now
        ));

        return new BankWithdrawResult(
                true,
                "SUCCESS",
                "银行出款成功",
                instId,
                null,
                null,
                command.instChannelCode(),
                outboundOrderNo,
                now,
                now
        );
    }

    private BankWithdrawResult localCancelWithdraw(BankWithdrawCancelRequest command,
                                                   Map<String, String> requestMap,
                                                   String instId,
                                                   String secret) {
        String requestPayload = canonicalPayload(requestMap);
        String requestSignature = sign(requestPayload, secret);
        ensureWithinTimeout("WITHDRAW_CANCEL");
        verifySignature(requestPayload, requestSignature, secret, "请求签名");

        LocalDateTime now = LocalDateTime.now();
        Map<String, String> responseMap = new LinkedHashMap<>();
        responseMap.put("resultCode", "SUCCESS");
        responseMap.put("resultMessage", "银行撤销成功");
        responseMap.put("instChannelCode", command.instChannelCode());
        responseMap.put("gmtResp", now.toString());
        String responsePayload = canonicalPayload(responseMap);
        String responseSignature = sign(responsePayload, secret);
        if (simulateBadResponseSignature) {
            responseSignature = responseSignature + "X";
        }
        verifySignature(responsePayload, responseSignature, secret, "响应签名");

        WithdrawMockState state = localWithdrawStates.get(command.outboundId());
        if (state != null && "SUCCESS".equalsIgnoreCase(state.state())) {
            return new BankWithdrawResult(
                    false,
                    "FAILED",
                    "银行撤销失败，订单已出款",
                    instId,
                    state.instSerialNo(),
                    state.instRefNo(),
                    command.instChannelCode(),
                    state.outboundOrderNo(),
                    now,
                    state.gmtSettle()
            );
        }
        localWithdrawStates.put(command.outboundId(), new WithdrawMockState(
                "CANCELED",
                instId,
                command.instChannelCode(),
                state == null ? null : state.instSerialNo(),
                state == null ? null : state.instRefNo(),
                state == null ? null : state.outboundOrderNo(),
                state == null ? now : defaultTime(state.gmtResp(), now),
                state == null ? null : state.gmtSettle()
        ));

        return new BankWithdrawResult(
                true,
                "SUCCESS",
                "银行撤销成功",
                instId,
                null,
                null,
                command.instChannelCode(),
                null,
                now,
                null
        );
    }

    private BankWithdrawResult localQueryWithdraw(BankWithdrawQueryRequest command,
                                                  Map<String, String> requestMap,
                                                  String instId,
                                                  String secret) {
        String requestPayload = canonicalPayload(requestMap);
        String requestSignature = sign(requestPayload, secret);
        ensureWithinTimeout("WITHDRAW_QUERY");
        verifySignature(requestPayload, requestSignature, secret, "请求签名");

        WithdrawMockState state = localWithdrawStates.get(command.outboundId());
        if (state == null) {
            return new BankWithdrawResult(
                    true,
                    "NOT_FOUND",
                    "银行侧无此订单",
                    instId,
                    null,
                    null,
                    command.instChannelCode(),
                    null,
                    LocalDateTime.now(),
                    null
            );
        }
        return toWithdrawQueryStateResult(command, state, instId);
    }

    private BankDepositResult toDepositQueryStateResult(BankDepositQueryRequest command,
                                                        DepositMockState state,
                                                        String instId) {
        String resultCode = normalizeQueryState(state.state());
        return new BankDepositResult(
                true,
                resultCode,
                defaultDepositQueryMessage(resultCode),
                defaultValue(state.instId(), instId),
                state.instSerialNo(),
                state.instRefNo(),
                defaultValue(state.instChannelCode(), command.instChannelCode()),
                state.inboundOrderNo(),
                defaultTime(state.gmtResp(), LocalDateTime.now()),
                state.gmtSettle()
        );
    }

    private BankWithdrawResult toWithdrawQueryStateResult(BankWithdrawQueryRequest command,
                                                          WithdrawMockState state,
                                                          String instId) {
        String resultCode = normalizeQueryState(state.state());
        return new BankWithdrawResult(
                true,
                resultCode,
                defaultWithdrawQueryMessage(resultCode),
                defaultValue(state.instId(), instId),
                state.instSerialNo(),
                state.instRefNo(),
                defaultValue(state.instChannelCode(), command.instChannelCode()),
                state.outboundOrderNo(),
                defaultTime(state.gmtResp(), LocalDateTime.now()),
                state.gmtSettle()
        );
    }

    private boolean isQuerySucceeded(String resultCode) {
        String normalized = normalizeOptional(resultCode);
        if (normalized == null) {
            return false;
        }
        String upperCode = normalized.toUpperCase(Locale.ROOT);
        return !"BANK_TIMEOUT".equals(upperCode)
                && !"BANK_CALL_ERROR".equals(upperCode)
                && !"BANK_SIGN_ERROR".equals(upperCode);
    }

    private String defaultDepositQueryMessage(String resultCode) {
        return switch (normalizeQueryState(resultCode)) {
            case "SUCCESS" -> "银行入账成功";
            case "ACCEPTED" -> "银行已受理";
            case "CANCELED" -> "银行已撤销";
            case "FAILED" -> "银行处理失败";
            case "NOT_FOUND" -> "银行侧无此订单";
            default -> "银行查单失败";
        };
    }

    private String defaultWithdrawQueryMessage(String resultCode) {
        return switch (normalizeQueryState(resultCode)) {
            case "SUCCESS" -> "银行出款成功";
            case "ACCEPTED" -> "银行已受理";
            case "CANCELED" -> "银行已撤销";
            case "FAILED" -> "银行处理失败";
            case "NOT_FOUND" -> "银行侧无此订单";
            default -> "银行查单失败";
        };
    }

    private String normalizeQueryState(String resultCode) {
        String normalized = normalizeOptional(resultCode);
        if (normalized == null) {
            return "UNKNOWN";
        }
        String upperCode = normalized.toUpperCase(Locale.ROOT);
        return switch (upperCode) {
            case "SUCCEEDED" -> "SUCCESS";
            case "SUCCESS", "ACCEPTED", "CANCELED", "FAILED", "NOT_FOUND" -> upperCode;
            default -> upperCode;
        };
    }

    private LocalDateTime defaultTime(LocalDateTime value, LocalDateTime fallback) {
        return value == null ? fallback : value;
    }

    private boolean shouldFailWithdraw(String requestIdentify) {
        String normalized = normalizeOptional(requestIdentify);
        return normalized != null && normalized.toUpperCase(Locale.ROOT).contains("FAIL");
    }

    private void ensureWithinTimeout(String action) {
        if (simulatedLatencyMs > timeoutMs) {
            throw new BankGatewayTimeoutException(action + " timeout, cost=" + simulatedLatencyMs + "ms");
        }
        if (simulatedLatencyMs <= 0) {
            return;
        }
        try {
            Thread.sleep(simulatedLatencyMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("bank call interrupted");
        }
    }

    private void verifySignature(String payload, String signature, String secret, String scene) {
        String expected = sign(payload, secret);
        if (!expected.equals(signature)) {
            throw new BankGatewaySignatureException(scene + "不通过");
        }
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

    private Map<String, String> parsePayload(String payloadRaw) {
        Map<String, String> values = new LinkedHashMap<>();
        String normalized = normalizeOptional(payloadRaw);
        if (normalized == null) {
            return values;
        }
        String[] parts = normalized.split("&");
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            String[] kv = part.split("=", 2);
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

    private String resolveSecret(String instId) {
        String key = "aipay.gateway.bank.inst-secrets." + instId.toLowerCase(Locale.ROOT);
        String configured = environment.getProperty(key);
        return normalizeOptional(configured) == null ? defaultSecret : normalizeOptional(configured);
    }

    private String normalizeAmount(Money amount) {
        if (amount == null || amount.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        return amount.rounded(2, RoundingMode.HALF_UP).getAmount().toPlainString();
    }

    private String normalizeInstId(String instChannelCode) {
        String normalized = normalizeRequired(instChannelCode, "instChannelCode").toUpperCase(Locale.ROOT);
        int separator = normalized.indexOf(':');
        String candidate = separator > 0 ? normalized.substring(0, separator) : normalized;
        int firstDigitIndex = -1;
        for (int i = 0; i < candidate.length(); i++) {
            if (Character.isDigit(candidate.charAt(i))) {
                firstDigitIndex = i;
                break;
            }
        }
        String instId = firstDigitIndex <= 0 ? candidate : candidate.substring(0, firstDigitIndex);
        instId = instId.replaceAll("[^A-Z0-9]", "");
        if (instId.isBlank()) {
            throw new IllegalArgumentException("instChannelCode does not contain institution code");
        }
        return instId;
    }

    private LocalDateTime parseDateTime(String raw) {
        String normalized = normalizeOptional(raw);
        if (normalized == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(normalized);
        } catch (RuntimeException ex) {
            return null;
        }
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
}

record DepositMockState(
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
        /** 入金订单单号 */
        String inboundOrderNo,
        /** GMTresp信息 */
        LocalDateTime gmtResp,
        /** GMT结算信息 */
        LocalDateTime gmtSettle
) {
}

record WithdrawMockState(
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
