package cn.openaipay.infrastructure.pricing.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.pricing.dataobject.PricingRuleDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * Pricing规则持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
@Mapper
public interface PricingRuleMapper extends BaseMapper<PricingRuleDO> {

    /**
     * 按规则编码查找记录。
     */
    default Optional<PricingRuleDO> findByRuleCode(String ruleCode) {
        QueryWrapper<PricingRuleDO> wrapper = new QueryWrapper<>();
        wrapper.eq("rule_code", ruleCode);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按条件查找记录。
     */
    default List<PricingRuleDO> findByFilters(String businessSceneCode, String paymentMethod, String status) {
        QueryWrapper<PricingRuleDO> wrapper = new QueryWrapper<>();
        if (businessSceneCode != null) {
            wrapper.and(w -> w.eq("business_scene_code", businessSceneCode)
                    .or()
                    .eq("business_scene_code", "ALL"));
        }
        if (paymentMethod != null) {
            wrapper.and(w -> w.eq("payment_method", paymentMethod)
                    .or()
                    .eq("payment_method", "ALL"));
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("priority").orderByDesc("updated_at");
        return selectList(wrapper);
    }
}
