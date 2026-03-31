package cn.openaipay.domain.coupon.repository;

import cn.openaipay.domain.coupon.model.CouponIssue;
import cn.openaipay.domain.coupon.model.CouponTemplate;
import cn.openaipay.domain.coupon.model.CouponTemplateStatus;

import java.util.List;
import java.util.Optional;
/**
 * 优惠券仓储接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface CouponRepository {

    /**
     * 保存优惠券模板。
     */
    CouponTemplate saveTemplate(CouponTemplate template);

    /**
     * 按模板ID查询模板。
     */
    Optional<CouponTemplate> findTemplateById(Long templateId);

    /**
     * 按模板编码查询模板。
     */
    Optional<CouponTemplate> findTemplateByCode(String templateCode);

    /**
     * 按场景与状态查询模板列表。
     */
    List<CouponTemplate> findTemplates(String sceneType, String status);

    /**
     * 统计用户已领取某模板次数。
     */
    long countUserClaims(Long templateId, Long userId);

    /**
     * 保存优惠券发放记录。
     */
    CouponIssue saveIssue(CouponIssue issue);

    /**
     * 按券号查询发放记录。
     */
    Optional<CouponIssue> findIssueByCouponNo(String couponNo);

    /**
     * 按模板、用户与业务单号查询发放记录。
     */
    Optional<CouponIssue> findIssueByTemplateUserAndBusinessNo(Long templateId, Long userId, String businessNo);

    /**
     * 按用户ID查询发放记录列表。
     */
    List<CouponIssue> findIssuesByUserId(Long userId);

    /**
     * 按模板/用户/状态筛选查询发放记录列表。
     */
    List<CouponIssue> findIssues(Long templateId, Long userId, String status, Integer limit);

    /**
     * 按模板状态统计模板数量。
     */
    long countTemplatesByStatus(CouponTemplateStatus status);

    /**
     * 统计全量已发放优惠券数量。
     */
    long countTotalIssuedCoupons();
}
