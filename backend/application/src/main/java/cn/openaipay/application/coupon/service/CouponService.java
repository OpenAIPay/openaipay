package cn.openaipay.application.coupon.service;

import cn.openaipay.application.coupon.command.ChangeCouponTemplateStatusCommand;
import cn.openaipay.application.coupon.command.CreateCouponTemplateCommand;
import cn.openaipay.application.coupon.command.IssueCouponCommand;
import cn.openaipay.application.coupon.command.RedeemCouponCommand;
import cn.openaipay.application.coupon.command.UpdateCouponTemplateCommand;
import cn.openaipay.application.coupon.dto.CouponIssueDTO;
import cn.openaipay.application.coupon.dto.CouponTemplateDTO;

import java.util.List;
/**
 * 优惠券应用服务接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface CouponService {

    /**
     * 创建模板信息。
     */
    CouponTemplateDTO createTemplate(CreateCouponTemplateCommand command);

    /**
     * 更新模板信息。
     */
    CouponTemplateDTO updateTemplate(UpdateCouponTemplateCommand command);

    /**
     * 处理模板状态。
     */
    CouponTemplateDTO changeTemplateStatus(ChangeCouponTemplateStatusCommand command);

    /**
     * 获取模板信息。
     */
    CouponTemplateDTO getTemplate(Long templateId);

    /**
     * 查询模板信息列表。
     */
    List<CouponTemplateDTO> listTemplates(String sceneType, String status);

    /**
     * 处理优惠券信息。
     */
    CouponIssueDTO issueCoupon(IssueCouponCommand command);

    /**
     * 处理优惠券信息。
     */
    CouponIssueDTO redeemCoupon(RedeemCouponCommand command);

    /**
     * 查询用户优惠券信息列表。
     */
    List<CouponIssueDTO> listUserCoupons(Long userId);

    /**
     * 获取业务数据。
     */
    CouponIssueDTO getIssue(String couponNo);

    /**
     * 查询业务数据列表。
     */
    List<CouponIssueDTO> listIssues(Long templateId, Long userId, String status, Integer limit);
}
