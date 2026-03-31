package cn.openaipay.infrastructure.audience.mapper;

import cn.openaipay.infrastructure.audience.dataobject.AudienceTagDefinitionDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签定义持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Mapper
public interface AudienceTagDefinitionMapper extends BaseMapper<AudienceTagDefinitionDO> {

    /**
     * 按编码查询标签定义
     */
    default Optional<AudienceTagDefinitionDO> findByTagCode(String tagCode) {
        QueryWrapper<AudienceTagDefinitionDO> wrapper = new QueryWrapper<>();
        wrapper.eq("tag_code", tagCode);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
