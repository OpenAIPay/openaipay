package cn.openaipay.infrastructure.coupon.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.coupon.dataobject.CouponIssueDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券发放持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Mapper
public interface CouponIssueMapper extends BaseMapper<CouponIssueDO> {

    /**
     * 按优惠券单号查找记录。
     */
    default Optional<CouponIssueDO> findByCouponNo(String couponNo) {
        QueryWrapper<CouponIssueDO> wrapper = new QueryWrapper<>();
        wrapper.eq("coupon_no", couponNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按模板、用户与业务单号查找记录。
     */
    default Optional<CouponIssueDO> findByTemplateUserAndBusinessNo(Long templateId, Long userId, String businessNo) {
        QueryWrapper<CouponIssueDO> wrapper = new QueryWrapper<>();
        wrapper.eq("template_id", templateId);
        wrapper.eq("user_id", userId);
        wrapper.eq("business_no", businessNo);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按模板ID与用户ID处理数量信息。
     */
    default long countByTemplateIdAndUserId(Long templateId, Long userId) {
        QueryWrapper<CouponIssueDO> wrapper = new QueryWrapper<>();
        wrapper.eq("template_id", templateId);
        wrapper.eq("user_id", userId);
        Long total = selectCount(wrapper);
        return total == null ? 0L : total;
    }

    /**
     * 按用户ID订单AT查找记录。
     */
    default List<CouponIssueDO> findByUserIdOrderByClaimedAtDesc(Long userId) {
        QueryWrapper<CouponIssueDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("claimed_at");
        return selectList(wrapper);
    }

    /**
     * 按条件查找记录。
     */
    default List<CouponIssueDO> findByFilters(Long templateId, Long userId, String status, Integer limit) {
        QueryWrapper<CouponIssueDO> wrapper = new QueryWrapper<>();
        if (templateId != null) {
            wrapper.eq("template_id", templateId);
        }
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("claimed_at").orderByDesc("id");
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }
        return selectList(wrapper);
    }
}
