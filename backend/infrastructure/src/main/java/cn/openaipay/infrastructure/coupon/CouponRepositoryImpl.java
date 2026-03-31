package cn.openaipay.infrastructure.coupon;

import cn.openaipay.domain.coupon.model.CouponIssue;
import cn.openaipay.domain.coupon.model.CouponIssueStatus;
import cn.openaipay.domain.coupon.model.CouponSceneType;
import cn.openaipay.domain.coupon.model.CouponTemplate;
import cn.openaipay.domain.coupon.model.CouponTemplateStatus;
import cn.openaipay.domain.coupon.model.CouponValueType;
import cn.openaipay.domain.coupon.repository.CouponRepository;
import cn.openaipay.infrastructure.coupon.dataobject.CouponIssueDO;
import cn.openaipay.infrastructure.coupon.dataobject.CouponTemplateDO;
import cn.openaipay.infrastructure.coupon.mapper.CouponIssueMapper;
import cn.openaipay.infrastructure.coupon.mapper.CouponTemplateMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 优惠券仓储实现
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Repository
public class CouponRepositoryImpl implements CouponRepository {

    /** Coupon模板Persistence组件 */
    private final CouponTemplateMapper couponTemplateMapper;
    /** Coupon发放Persistence组件 */
    private final CouponIssueMapper couponIssueMapper;

    public CouponRepositoryImpl(CouponTemplateMapper couponTemplateMapper,
                                CouponIssueMapper couponIssueMapper) {
        this.couponTemplateMapper = couponTemplateMapper;
        this.couponIssueMapper = couponIssueMapper;
    }

    /**
     * 保存模板信息。
     */
    @Override
    @Transactional
    public CouponTemplate saveTemplate(CouponTemplate template) {
        CouponTemplateDO entity = template.getTemplateId() == null
                ? new CouponTemplateDO()
                : couponTemplateMapper.findById(template.getTemplateId()).orElse(new CouponTemplateDO());
        fillTemplateDO(entity, template);
        CouponTemplateDO saved = couponTemplateMapper.save(entity);
        return toDomainTemplate(saved);
    }

    /**
     * 按ID查找模板信息。
     */
    @Override
    public Optional<CouponTemplate> findTemplateById(Long templateId) {
        return couponTemplateMapper.findById(templateId).map(this::toDomainTemplate);
    }

    /**
     * 按编码查找模板信息。
     */
    @Override
    public Optional<CouponTemplate> findTemplateByCode(String templateCode) {
        return couponTemplateMapper.findByTemplateCode(templateCode).map(this::toDomainTemplate);
    }

    /**
     * 查找模板信息。
     */
    @Override
    public List<CouponTemplate> findTemplates(String sceneType, String status) {
        return couponTemplateMapper.findByFilters(sceneType, status)
                .stream()
                .map(this::toDomainTemplate)
                .toList();
    }

    /**
     * 处理数量用户信息。
     */
    @Override
    public long countUserClaims(Long templateId, Long userId) {
        return couponIssueMapper.countByTemplateIdAndUserId(templateId, userId);
    }

    /**
     * 保存业务数据。
     */
    @Override
    @Transactional
    public CouponIssue saveIssue(CouponIssue issue) {
        CouponIssueDO entity = issue.getIssueId() == null
                ? couponIssueMapper.findByCouponNo(issue.getCouponNo()).orElse(new CouponIssueDO())
                : couponIssueMapper.findById(issue.getIssueId()).orElse(new CouponIssueDO());
        fillIssueDO(entity, issue);
        CouponIssueDO saved = couponIssueMapper.save(entity);
        return toDomainIssue(saved);
    }

    /**
     * 按优惠券单号查找记录。
     */
    @Override
    public Optional<CouponIssue> findIssueByCouponNo(String couponNo) {
        return couponIssueMapper.findByCouponNo(couponNo).map(this::toDomainIssue);
    }

    /**
     * 按模板、用户与业务单号查询发放记录。
     */
    @Override
    public Optional<CouponIssue> findIssueByTemplateUserAndBusinessNo(Long templateId, Long userId, String businessNo) {
        return couponIssueMapper.findByTemplateUserAndBusinessNo(templateId, userId, businessNo)
                .map(this::toDomainIssue);
    }

    /**
     * 按用户ID查找记录。
     */
    @Override
    public List<CouponIssue> findIssuesByUserId(Long userId) {
        return couponIssueMapper.findByUserIdOrderByClaimedAtDesc(userId)
                .stream()
                .map(this::toDomainIssue)
                .toList();
    }

    /**
     * 查找业务数据。
     */
    @Override
    public List<CouponIssue> findIssues(Long templateId, Long userId, String status, Integer limit) {
        return couponIssueMapper.findByFilters(templateId, userId, status, limit)
                .stream()
                .map(this::toDomainIssue)
                .toList();
    }

    /**
     * 按状态处理数量模板信息。
     */
    @Override
    public long countTemplatesByStatus(CouponTemplateStatus status) {
        return couponTemplateMapper.countByStatus(status.name());
    }

    /**
     * 处理数量优惠券信息。
     */
    @Override
    public long countTotalIssuedCoupons() {
        return couponIssueMapper.count();
    }

    private CouponTemplate toDomainTemplate(CouponTemplateDO entity) {
        return new CouponTemplate(
                entity.getId(),
                entity.getTemplateCode(),
                entity.getTemplateName(),
                CouponSceneType.from(entity.getSceneType()),
                CouponValueType.from(entity.getValueType()),
                entity.getAmount(),
                entity.getMinAmount(),
                entity.getMaxAmount(),
                entity.getThresholdAmount(),
                entity.getTotalBudget(),
                entity.getTotalStock(),
                entity.getClaimedCount(),
                entity.getPerUserLimit(),
                entity.getClaimStartTime(),
                entity.getClaimEndTime(),
                entity.getUseStartTime(),
                entity.getUseEndTime(),
                entity.getFundingSource(),
                entity.getRulePayload(),
                CouponTemplateStatus.from(entity.getStatus()),
                entity.getCreatedBy(),
                entity.getUpdatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private CouponIssue toDomainIssue(CouponIssueDO entity) {
        return new CouponIssue(
                entity.getId(),
                entity.getCouponNo(),
                entity.getTemplateId(),
                entity.getUserId(),
                entity.getCouponAmount(),
                CouponIssueStatus.from(entity.getStatus()),
                entity.getClaimChannel(),
                entity.getBusinessNo(),
                entity.getOrderNo(),
                entity.getBizOrderNo(),
                entity.getTradeOrderNo(),
                entity.getPayOrderNo(),
                entity.getClaimedAt(),
                entity.getExpireAt(),
                entity.getUsedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void fillTemplateDO(CouponTemplateDO entity, CouponTemplate template) {
        LocalDateTime now = LocalDateTime.now();
        entity.setTemplateCode(template.getTemplateCode());
        entity.setTemplateName(template.getTemplateName());
        entity.setSceneType(template.getSceneType().name());
        entity.setValueType(template.getValueType().name());
        entity.setAmount(template.getAmount());
        entity.setMinAmount(template.getMinAmount());
        entity.setMaxAmount(template.getMaxAmount());
        entity.setThresholdAmount(template.getThresholdAmount());
        entity.setTotalBudget(template.getTotalBudget());
        entity.setTotalStock(template.getTotalStock());
        entity.setClaimedCount(template.getClaimedCount());
        entity.setPerUserLimit(template.getPerUserLimit());
        entity.setClaimStartTime(template.getClaimStartTime());
        entity.setClaimEndTime(template.getClaimEndTime());
        entity.setUseStartTime(template.getUseStartTime());
        entity.setUseEndTime(template.getUseEndTime());
        entity.setFundingSource(template.getFundingSource());
        entity.setRulePayload(template.getRulePayload());
        entity.setStatus(template.getStatus().name());
        entity.setCreatedBy(template.getCreatedBy());
        entity.setUpdatedBy(template.getUpdatedBy());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(template.getCreatedAt() == null ? now : template.getCreatedAt());
        }
        entity.setUpdatedAt(template.getUpdatedAt() == null ? now : template.getUpdatedAt());
    }

    private void fillIssueDO(CouponIssueDO entity, CouponIssue issue) {
        LocalDateTime now = LocalDateTime.now();
        entity.setCouponNo(issue.getCouponNo());
        entity.setTemplateId(issue.getTemplateId());
        entity.setUserId(issue.getUserId());
        entity.setCouponAmount(issue.getCouponAmount());
        entity.setStatus(issue.getStatus().name());
        entity.setClaimChannel(issue.getClaimChannel());
        entity.setBusinessNo(issue.getBusinessNo());
        entity.setOrderNo(issue.getOrderNo());
        entity.setBizOrderNo(issue.getBizOrderNo());
        entity.setTradeOrderNo(issue.getTradeOrderNo());
        entity.setPayOrderNo(issue.getPayOrderNo());
        entity.setClaimedAt(issue.getClaimedAt());
        entity.setExpireAt(issue.getExpireAt());
        entity.setUsedAt(issue.getUsedAt());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(issue.getCreatedAt() == null ? now : issue.getCreatedAt());
        }
        entity.setUpdatedAt(issue.getUpdatedAt() == null ? now : issue.getUpdatedAt());
    }
}
