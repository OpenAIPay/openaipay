package cn.openaipay.application.coupon.facade.impl;

import cn.openaipay.application.coupon.command.ChangeCouponTemplateStatusCommand;
import cn.openaipay.application.coupon.command.CreateCouponTemplateCommand;
import cn.openaipay.application.coupon.command.IssueCouponCommand;
import cn.openaipay.application.coupon.command.RedeemCouponCommand;
import cn.openaipay.application.coupon.command.UpdateCouponTemplateCommand;
import cn.openaipay.application.coupon.dto.CouponIssueDTO;
import cn.openaipay.application.coupon.dto.CouponOpsSummaryDTO;
import cn.openaipay.application.coupon.dto.CouponTemplateDTO;
import cn.openaipay.application.coupon.facade.CouponFacade;
import cn.openaipay.application.coupon.service.CouponService;
import cn.openaipay.domain.coupon.model.CouponIssue;
import cn.openaipay.domain.coupon.model.CouponTemplateStatus;
import cn.openaipay.domain.coupon.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.joda.money.Money;
import org.joda.money.CurrencyUnit;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 优惠券门面实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Service
public class CouponFacadeImpl implements CouponFacade {

    /** CouponService组件 */
    private final CouponService couponService;
    /** CouponRepository组件 */
    private final CouponRepository couponRepository;

    public CouponFacadeImpl(CouponService couponService,
                            CouponRepository couponRepository) {
        this.couponService = couponService;
        this.couponRepository = couponRepository;
    }

    /**
     * 查询OPS汇总信息。
     */
    @Override
    @Transactional(readOnly = true)
    public CouponOpsSummaryDTO queryOpsSummary() {
        return new CouponOpsSummaryDTO(
                couponRepository.countTemplatesByStatus(CouponTemplateStatus.ACTIVE),
                couponRepository.countTemplatesByStatus(CouponTemplateStatus.PAUSED),
                couponRepository.countTemplatesByStatus(CouponTemplateStatus.DRAFT),
                couponRepository.countTemplatesByStatus(CouponTemplateStatus.EXPIRED),
                couponRepository.countTotalIssuedCoupons()
        );
    }

    /**
     * 解析金额。
     */
    @Override
    @Transactional(readOnly = true)
    public Money resolveDiscountAmount(String couponNo) {
        String normalizedCouponNo = normalize(couponNo);
        if (normalizedCouponNo == null) {
            return Money.zero(CurrencyUnit.of("CNY")).rounded(2, RoundingMode.HALF_UP);
        }
        CouponIssue couponIssue = couponRepository.findIssueByCouponNo(normalizedCouponNo)
                .orElseThrow(() -> new NoSuchElementException("coupon not found: " + normalizedCouponNo));
        return couponIssue.getCouponAmount().rounded(2, RoundingMode.HALF_UP);
    }

    /**
     * 处理优惠券信息。
     */
    @Override
    @Transactional
    public void reserveCoupon(String couponNo) {
        CouponIssue couponIssue = mustGetCoupon(couponNo);
        couponIssue.reserveForPayment(LocalDateTime.now());
        couponRepository.saveIssue(couponIssue);
    }

    /**
     * 处理优惠券信息。
     */
    @Override
    @Transactional
    public void releaseCoupon(String couponNo) {
        String normalizedCouponNo = normalize(couponNo);
        if (normalizedCouponNo == null) {
            return;
        }
        CouponIssue couponIssue = couponRepository.findIssueByCouponNo(normalizedCouponNo)
                .orElseThrow(() -> new NoSuchElementException("coupon not found: " + normalizedCouponNo));
        couponIssue.releaseReservation(LocalDateTime.now());
        couponRepository.saveIssue(couponIssue);
    }

    /**
     * 处理优惠券信息。
     */
    @Override
    @Transactional
    public void consumeCoupon(String couponNo, String bizOrderNo, String tradeOrderNo, String payOrderNo) {
        String normalizedCouponNo = normalize(couponNo);
        if (normalizedCouponNo == null) {
            return;
        }
        CouponIssue couponIssue = couponRepository.findIssueByCouponNo(normalizedCouponNo)
                .orElseThrow(() -> new NoSuchElementException("coupon not found: " + normalizedCouponNo));
        String normalizedBizOrderNo = normalizeRequired(bizOrderNo, "bizOrderNo");
        couponIssue.consumeAfterReservation(
                normalizedBizOrderNo,
                normalizedBizOrderNo,
                normalize(tradeOrderNo),
                normalize(payOrderNo),
                LocalDateTime.now(),
                null,
                couponIssue.getExpireAt()
        );
        couponRepository.saveIssue(couponIssue);
    }

    /**
     * 创建模板信息。
     */
    @Override
    public CouponTemplateDTO createTemplate(CreateCouponTemplateCommand command) {
        return couponService.createTemplate(command);
    }

    /**
     * 更新模板信息。
     */
    @Override
    public CouponTemplateDTO updateTemplate(UpdateCouponTemplateCommand command) {
        return couponService.updateTemplate(command);
    }

    /**
     * 处理模板状态。
     */
    @Override
    public CouponTemplateDTO changeTemplateStatus(ChangeCouponTemplateStatusCommand command) {
        return couponService.changeTemplateStatus(command);
    }

    /**
     * 获取模板信息。
     */
    @Override
    public CouponTemplateDTO getTemplate(Long templateId) {
        return couponService.getTemplate(templateId);
    }

    /**
     * 查询模板信息列表。
     */
    @Override
    public List<CouponTemplateDTO> listTemplates(String sceneType, String status) {
        return couponService.listTemplates(sceneType, status);
    }

    /**
     * 处理优惠券信息。
     */
    @Override
    public CouponIssueDTO issueCoupon(IssueCouponCommand command) {
        return couponService.issueCoupon(command);
    }

    /**
     * 处理优惠券信息。
     */
    @Override
    public CouponIssueDTO redeemCoupon(RedeemCouponCommand command) {
        return couponService.redeemCoupon(command);
    }

    /**
     * 查询用户优惠券信息列表。
     */
    @Override
    public List<CouponIssueDTO> listUserCoupons(Long userId) {
        return couponService.listUserCoupons(userId);
    }

    /**
     * 获取业务数据。
     */
    @Override
    public CouponIssueDTO getIssue(String couponNo) {
        return couponService.getIssue(couponNo);
    }

    /**
     * 查询业务数据列表。
     */
    @Override
    public List<CouponIssueDTO> listIssues(Long templateId, Long userId, String status, Integer limit) {
        return couponService.listIssues(templateId, userId, status, limit);
    }

    private CouponIssue mustGetCoupon(String couponNo) {
        String normalizedCouponNo = normalizeRequired(couponNo, "couponNo");
        return couponRepository.findIssueByCouponNo(normalizedCouponNo)
                .orElseThrow(() -> new NoSuchElementException("coupon not found: " + normalizedCouponNo));
    }

    private String normalizeRequired(String value, String field) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return normalized;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
