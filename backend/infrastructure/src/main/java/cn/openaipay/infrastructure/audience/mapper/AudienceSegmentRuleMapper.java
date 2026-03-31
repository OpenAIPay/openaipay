package cn.openaipay.infrastructure.audience.mapper;

import cn.openaipay.infrastructure.audience.dataobject.AudienceSegmentRuleDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 人群规则持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Mapper
public interface AudienceSegmentRuleMapper extends BaseMapper<AudienceSegmentRuleDO> {

    /**
     * 按人群编码查询规则
     */
    default List<AudienceSegmentRuleDO> findBySegmentCode(String segmentCode) {
        QueryWrapper<AudienceSegmentRuleDO> wrapper = new QueryWrapper<>();
        wrapper.eq("segment_code", segmentCode);
        wrapper.orderByAsc("rule_code");
        return selectList(wrapper);
    }

    /**
     * 按人群编码集合查询规则
     */
    default List<AudienceSegmentRuleDO> findBySegmentCodes(Collection<String> segmentCodes) {
        if (segmentCodes == null || segmentCodes.isEmpty()) {
            return List.of();
        }
        QueryWrapper<AudienceSegmentRuleDO> wrapper = new QueryWrapper<>();
        wrapper.in("segment_code", segmentCodes);
        wrapper.orderByAsc("segment_code");
        wrapper.orderByAsc("rule_code");
        return selectList(wrapper);
    }
}
