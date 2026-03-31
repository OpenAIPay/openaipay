package cn.openaipay.infrastructure.risk.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.risk.dataobject.RiskRuleDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 风控规则持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Mapper
public interface RiskRuleMapper extends BaseMapper<RiskRuleDO> {

    /**
     * 按规则编码查询。
     */
    default Optional<RiskRuleDO> findByRuleCode(String ruleCode) {
        QueryWrapper<RiskRuleDO> wrapper = new QueryWrapper<>();
        wrapper.eq("rule_code", ruleCode).last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}

