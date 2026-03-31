package cn.openaipay.infrastructure.deliver.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.deliver.dataobject.DeliverPositionDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.time.LocalDateTime;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * DeliverPositionMapper 对象映射器
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
@Mapper
public interface DeliverPositionMapper extends BaseMapper<DeliverPositionDO> {

    /**
     * 按编码查找记录。
     */
    default Optional<DeliverPositionDO> findPublishedByCode(String positionCode, LocalDateTime now) {
        QueryWrapper<DeliverPositionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("position_code", positionCode)
                .eq("status", "PUBLISHED")
                .and(query -> query.isNull("active_from").or().le("active_from", now))
                .and(query -> query.isNull("active_to").or().ge("active_to", now))
                .last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
