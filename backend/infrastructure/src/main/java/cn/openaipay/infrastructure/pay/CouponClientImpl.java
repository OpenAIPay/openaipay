package cn.openaipay.infrastructure.pay;

import cn.openaipay.application.coupon.facade.CouponFacade;
import cn.openaipay.domain.pay.client.CouponClient;
import org.joda.money.Money;
import org.springframework.stereotype.Component;

/**
 * CouponClientImpl 业务模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/15
 */
@Component
public class CouponClientImpl implements CouponClient {

    /** 优惠券信息 */
    private final CouponFacade couponFacade;

    public CouponClientImpl(CouponFacade couponFacade) {
        this.couponFacade = couponFacade;
    }

    /**
     * 解析金额。
     */
    @Override
    public Money resolveDiscountAmount(String couponNo) {
        return couponFacade.resolveDiscountAmount(couponNo);
    }

    /**
     * 处理优惠券信息。
     */
    @Override
    public void reserveCoupon(String couponNo) {
        couponFacade.reserveCoupon(couponNo);
    }

    /**
     * 处理优惠券信息。
     */
    @Override
    public void releaseCoupon(String couponNo) {
        couponFacade.releaseCoupon(couponNo);
    }

    /**
     * 处理优惠券信息。
     */
    @Override
    public void consumeCoupon(String couponNo, String bizOrderNo, String tradeOrderNo, String payOrderNo) {
        couponFacade.consumeCoupon(couponNo, bizOrderNo, tradeOrderNo, payOrderNo);
    }
}
