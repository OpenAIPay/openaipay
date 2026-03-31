package cn.openaipay.infrastructure.deliver.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.deliver.dataobject.FatigueControlRuleDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * FatigueControlRuleMapper 对象映射器
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface FatigueControlRuleMapper extends BaseMapper<FatigueControlRuleDO> {

    /**
     * 按与编码查找记录。
     */
    default List<FatigueControlRuleDO> findEnabledByEntityTypeAndCodes(String entityType, Collection<String> entityCodes) {
        if (entityCodes == null || entityCodes.isEmpty()) {
            return List.of();
        }
        QueryWrapper<FatigueControlRuleDO> wrapper = new QueryWrapper<>();
        wrapper.eq("entity_type", entityType)
                .eq("enabled", true)
                .in("entity_code", entityCodes);
        return selectList(wrapper);
    }
}
