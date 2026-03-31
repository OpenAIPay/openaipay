package cn.openaipay.infrastructure.audience.mapper;

import cn.openaipay.infrastructure.audience.dataobject.AudienceUserTagSnapshotDO;
import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户标签快照持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
@Mapper
public interface AudienceUserTagSnapshotMapper extends BaseMapper<AudienceUserTagSnapshotDO> {

    /**
     * 按用户查询标签快照
     */
    default List<AudienceUserTagSnapshotDO> findByUserId(Long userId) {
        QueryWrapper<AudienceUserTagSnapshotDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByAsc("tag_code");
        return selectList(wrapper);
    }

    /**
     * 按用户和标签编码查询快照
     */
    default Optional<AudienceUserTagSnapshotDO> findByUserIdAndTagCode(Long userId, String tagCode) {
        QueryWrapper<AudienceUserTagSnapshotDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("tag_code", tagCode);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }
}
