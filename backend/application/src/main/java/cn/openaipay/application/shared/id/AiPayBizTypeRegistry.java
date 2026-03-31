package cn.openaipay.application.shared.id;

import cn.openaipay.domain.pay.model.PayParticipantType;
import cn.openaipay.domain.trade.model.TradeType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * AiPay业务类型码统一映射中心。
 * 通过配置文件维护映射，避免业务团队调整类型码时改动代码。
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Component
@ConfigurationProperties(prefix = "aipay.id.biz-type")
public class AiPayBizTypeRegistry {

    /** 业务类型码格式校验正则。 */
    private static final Pattern BIZ_TYPE_PATTERN = Pattern.compile("^\\d{2}$");

    /**
     * TradeType -> BizType。key建议直接使用TradeType枚举名。
     */
    private Map<String, String> trade = defaultTradeMapping();

    /**
     * Pay相关场景 -> BizType。
     * 建议key：ORDER_CREATE、BRANCH_COUPON、BRANCH_WALLET_ACCOUNT、BRANCH_FUND_ACCOUNT、BRANCH_CREDIT_ACCOUNT、BRANCH_OUTBOUND。
     */
    private Map<String, String> pay = defaultPayMapping();

    /**
     * Coupon相关场景 -> BizType。建议key：ISSUE。
     */
    private Map<String, String> coupon = defaultCouponMapping();

    /**
     * Inbound相关场景 -> BizType。建议key：ORDER_CREATE、SETTLE。
     */
    private Map<String, String> inbound = defaultInboundMapping();

    /**
     * Outbound相关场景 -> BizType。建议key：ORDER_CREATE。
     */
    private Map<String, String> outbound = defaultOutboundMapping();

    /**
     * Gateway相关场景 -> BizType。建议key：DEPOSIT_INIT、DEPOSIT_CONFIRM、DEPOSIT_CANCEL、WITHDRAW_INIT、WITHDRAW_CONFIRM、WITHDRAW_CANCEL。
     */
    private Map<String, String> gateway = defaultGatewayMapping();

    /**
     * 处理交易业务信息。
     */
    public String tradeBizType(TradeType tradeType) {
        return resolve(trade, tradeType.name(), "aipay.id.biz-type.trade");
    }

    /**
     * 处理交易业务信息。
     */
    public String tradeBizType(String tradeType) {
        return resolve(trade, tradeType, "aipay.id.biz-type.trade");
    }

    /**
     * 处理支付订单业务信息。
     */
    public String payOrderCreateBizType() {
        return resolve(pay, "ORDER_CREATE", "aipay.id.biz-type.pay");
    }

    /**
     * 处理支付业务信息。
     */
    public String payBranchBizType(PayParticipantType participantType) {
        return resolve(pay, "BRANCH_" + participantType.name(), "aipay.id.biz-type.pay");
    }

    /**
     * 处理优惠券业务信息。
     */
    public String couponIssueBizType() {
        return resolve(coupon, "ISSUE", "aipay.id.biz-type.coupon");
    }

    /**
     * 处理入金订单业务信息。
     */
    public String inboundOrderCreateBizType() {
        return resolve(inbound, "ORDER_CREATE", "aipay.id.biz-type.inbound");
    }

    /**
     * 处理入金结算业务信息。
     */
    public String inboundSettleBizType() {
        return resolve(inbound, "SETTLE", "aipay.id.biz-type.inbound");
    }

    /**
     * 处理出金订单业务信息。
     */
    public String outboundOrderCreateBizType() {
        return resolve(outbound, "ORDER_CREATE", "aipay.id.biz-type.outbound");
    }

    /**
     * 处理网关初始化业务信息。
     */
    public String gatewayDepositInitBizType() {
        return resolve(gateway, "DEPOSIT_INIT", "aipay.id.biz-type.gateway");
    }

    /**
     * 处理网关业务信息。
     */
    public String gatewayDepositConfirmBizType() {
        return resolve(gateway, "DEPOSIT_CONFIRM", "aipay.id.biz-type.gateway");
    }

    /**
     * 处理网关业务信息。
     */
    public String gatewayDepositCancelBizType() {
        return resolve(gateway, "DEPOSIT_CANCEL", "aipay.id.biz-type.gateway");
    }

    /**
     * 处理网关初始化业务信息。
     */
    public String gatewayWithdrawInitBizType() {
        return resolve(gateway, "WITHDRAW_INIT", "aipay.id.biz-type.gateway");
    }

    /**
     * 处理网关业务信息。
     */
    public String gatewayWithdrawConfirmBizType() {
        return resolve(gateway, "WITHDRAW_CONFIRM", "aipay.id.biz-type.gateway");
    }

    /**
     * 处理网关业务信息。
     */
    public String gatewayWithdrawCancelBizType() {
        return resolve(gateway, "WITHDRAW_CANCEL", "aipay.id.biz-type.gateway");
    }

    /**
     * 获取交易信息。
     */
    public Map<String, String> getTrade() {
        return trade;
    }

    /**
     * 处理SET交易信息。
     */
    public void setTrade(Map<String, String> trade) {
        this.trade = mergeMapping(defaultTradeMapping(), trade, "aipay.id.biz-type.trade");
    }

    /**
     * 获取支付信息。
     */
    public Map<String, String> getPay() {
        return pay;
    }

    /**
     * 处理SET支付信息。
     */
    public void setPay(Map<String, String> pay) {
        this.pay = mergeMapping(defaultPayMapping(), pay, "aipay.id.biz-type.pay");
    }

    /**
     * 获取优惠券信息。
     */
    public Map<String, String> getCoupon() {
        return coupon;
    }

    /**
     * 处理SET优惠券信息。
     */
    public void setCoupon(Map<String, String> coupon) {
        this.coupon = mergeMapping(defaultCouponMapping(), coupon, "aipay.id.biz-type.coupon");
    }

    /**
     * 获取入金信息。
     */
    public Map<String, String> getInbound() {
        return inbound;
    }

    /**
     * 处理SET入金信息。
     */
    public void setInbound(Map<String, String> inbound) {
        this.inbound = mergeMapping(defaultInboundMapping(), inbound, "aipay.id.biz-type.inbound");
    }

    /**
     * 获取出金信息。
     */
    public Map<String, String> getOutbound() {
        return outbound;
    }

    /**
     * 处理SET出金信息。
     */
    public void setOutbound(Map<String, String> outbound) {
        this.outbound = mergeMapping(defaultOutboundMapping(), outbound, "aipay.id.biz-type.outbound");
    }

    /**
     * 获取网关信息。
     */
    public Map<String, String> getGateway() {
        return gateway;
    }

    /**
     * 处理SET网关信息。
     */
    public void setGateway(Map<String, String> gateway) {
        this.gateway = mergeMapping(defaultGatewayMapping(), gateway, "aipay.id.biz-type.gateway");
    }

    private String resolve(Map<String, String> mapping, String key, String propertyPrefix) {
        String normalizedKey = normalizeKey(key, propertyPrefix);
        String value = mapping.get(normalizedKey);
        if (value == null) {
            throw new IllegalStateException(propertyPrefix + "." + normalizedKey + " is not configured");
        }
        return normalizeBizType(value, propertyPrefix + "." + normalizedKey);
    }

    private Map<String, String> normalizeMap(Map<String, String> raw, String propertyPrefix) {
        if (raw == null || raw.isEmpty()) {
            return new LinkedHashMap<>();
        }
        Map<String, String> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : raw.entrySet()) {
            String key = normalizeKey(entry.getKey(), propertyPrefix);
            String value = normalizeBizType(entry.getValue(), propertyPrefix + "." + key);
            normalized.put(key, value);
        }
        return normalized;
    }

    private Map<String, String> mergeMapping(Map<String, String> defaults, Map<String, String> overrides, String propertyPrefix) {
        Map<String, String> merged = new LinkedHashMap<>(defaults);
        Map<String, String> normalizedOverrides = normalizeMap(overrides, propertyPrefix);
        merged.putAll(normalizedOverrides);
        return merged;
    }

    private String normalizeKey(String key, String propertyPrefix) {
        if (key == null || key.isBlank()) {
            throw new IllegalStateException(propertyPrefix + " contains blank mapping key");
        }
        return key.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace('.', '_');
    }

    private String normalizeBizType(String rawBizType, String propertyKey) {
        if (rawBizType == null || !BIZ_TYPE_PATTERN.matcher(rawBizType.trim()).matches()) {
            throw new IllegalStateException(propertyKey + " must be two-digit numeric string");
        }
        return rawBizType.trim();
    }

    private static Map<String, String> defaultTradeMapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("DEPOSIT", "10");
        mapping.put("WITHDRAW", "11");
        mapping.put("PAY", "20");
        mapping.put("TRANSFER", "21");
        mapping.put("REFUND", "30");
        return mapping;
    }

    private static Map<String, String> defaultPayMapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("ORDER_CREATE", "20");
        mapping.put("BRANCH_COUPON", "20");
        mapping.put("BRANCH_WALLET_ACCOUNT", "20");
        mapping.put("BRANCH_FUND_ACCOUNT", "20");
        mapping.put("BRANCH_CREDIT_ACCOUNT", "20");
        mapping.put("BRANCH_INBOUND", "20");
        mapping.put("BRANCH_OUTBOUND", "20");
        return mapping;
    }

    private static Map<String, String> defaultCouponMapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("ISSUE", "10");
        return mapping;
    }

    private static Map<String, String> defaultInboundMapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("ORDER_CREATE", "10");
        mapping.put("SETTLE", "20");
        return mapping;
    }

    private static Map<String, String> defaultOutboundMapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("ORDER_CREATE", "10");
        return mapping;
    }

    private static Map<String, String> defaultGatewayMapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("DEPOSIT_INIT", "10");
        mapping.put("DEPOSIT_CONFIRM", "20");
        mapping.put("DEPOSIT_CANCEL", "30");
        mapping.put("WITHDRAW_INIT", "40");
        mapping.put("WITHDRAW_CONFIRM", "50");
        mapping.put("WITHDRAW_CANCEL", "60");
        return mapping;
    }
}
