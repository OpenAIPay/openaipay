package cn.openaipay.infrastructure.audience.mapper;

import cn.openaipay.infrastructure.audience.dataobject.AudienceSegmentDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 人群定义持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Mapper
public interface AudienceSegmentMapper extends BaseMapper<AudienceSegmentDO> {

    /**
     * 按编码查询人群
     */
    default Optional<AudienceSegmentDO> findBySegmentCode(String segmentCode) {
        QueryWrapper<AudienceSegmentDO> wrapper = new QueryWrapper<>();
        wrapper.eq("segment_code", segmentCode);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按状态查询人群
     */
    default List<AudienceSegmentDO> findByStatus(String status) {
        QueryWrapper<AudienceSegmentDO> wrapper = new QueryWrapper<>();
        wrapper.eq("status", status);
        wrapper.orderByAsc("segment_code");
        return selectList(wrapper);
    }
}
