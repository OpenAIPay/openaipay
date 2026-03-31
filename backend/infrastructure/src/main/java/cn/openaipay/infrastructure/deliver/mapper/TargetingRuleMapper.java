package cn.openaipay.infrastructure.deliver.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.deliver.dataobject.TargetingRuleDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * TargetingRuleMapper 对象映射器
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface TargetingRuleMapper extends BaseMapper<TargetingRuleDO> {

    /**
     * 按与编码查找记录。
     */
    default List<TargetingRuleDO> findEnabledByEntityTypeAndCodes(String entityType, Collection<String> entityCodes) {
        if (entityCodes == null || entityCodes.isEmpty()) {
            return List.of();
        }
        QueryWrapper<TargetingRuleDO> wrapper = new QueryWrapper<>();
        wrapper.eq("entity_type", entityType)
                .eq("enabled", true)
                .in("entity_code", entityCodes);
        return selectList(wrapper);
    }
}
