package cn.openaipay.domain.pay.client;

import org.joda.money.Money;

/**
 * CouponClient 客户端
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
public interface CouponClient {
    /**
     * 解析金额。
     */
    Money resolveDiscountAmount(String couponNo);

    /**
     * 处理优惠券信息。
     */
    void reserveCoupon(String couponNo);

    /**
     * 处理优惠券信息。
     */
    void releaseCoupon(String couponNo);

    /**
     * 处理优惠券信息。
     */
    void consumeCoupon(String couponNo, String bizOrderNo, String tradeOrderNo, String payOrderNo);
}
