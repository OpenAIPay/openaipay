package cn.openaipay.infrastructure.coupon.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.coupon.dataobject.CouponTemplateDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券模板持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Mapper
public interface CouponTemplateMapper extends BaseMapper<CouponTemplateDO> {

    /**
     * 按模板编码查找记录。
     */
    default Optional<CouponTemplateDO> findByTemplateCode(String templateCode) {
        QueryWrapper<CouponTemplateDO> wrapper = new QueryWrapper<>();
        wrapper.eq("template_code", templateCode);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按条件查找记录。
     */
    default List<CouponTemplateDO> findByFilters(String sceneType, String status) {
        QueryWrapper<CouponTemplateDO> wrapper = new QueryWrapper<>();
        if (sceneType != null) {
            wrapper.eq("scene_type", sceneType);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("updated_at");
        return selectList(wrapper);
    }

    /**
     * 按状态处理数量信息。
     */
    default long countByStatus(String status) {
        QueryWrapper<CouponTemplateDO> wrapper = new QueryWrapper<>();
        wrapper.eq("status", status);
        Long total = selectCount(wrapper);
        return total == null ? 0L : total;
    }
}
