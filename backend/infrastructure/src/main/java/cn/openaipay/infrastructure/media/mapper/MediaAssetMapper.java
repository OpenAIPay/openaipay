package cn.openaipay.infrastructure.media.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.media.dataobject.MediaAssetDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 媒体资源持久化接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
@Mapper
public interface MediaAssetMapper extends BaseMapper<MediaAssetDO> {

    /**
     * 按媒体ID查找记录。
     */
    default Optional<MediaAssetDO> findByMediaId(String mediaId) {
        QueryWrapper<MediaAssetDO> wrapper = new QueryWrapper<>();
        wrapper.eq("media_id", mediaId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 按用户ID查询记录列表。
     */
    default List<MediaAssetDO> listByOwnerUserId(Long ownerUserId, int limit) {
        QueryWrapper<MediaAssetDO> wrapper = new QueryWrapper<>();
        wrapper.eq("owner_user_id", ownerUserId);
        wrapper.orderByDesc("id");
        wrapper.last("LIMIT " + Math.max(1, limit));
        return selectList(wrapper);
    }
}
