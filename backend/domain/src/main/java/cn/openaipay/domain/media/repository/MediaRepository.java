package cn.openaipay.domain.media.repository;

import cn.openaipay.domain.media.model.MediaAsset;
import java.util.List;
import java.util.Optional;

/**
 * 媒体仓储接口
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public interface MediaRepository {

    /**
     * 按媒体ID查找记录。
     */
    Optional<MediaAsset> findByMediaId(String mediaId);

    /**
     * 保存业务数据。
     */
    MediaAsset save(MediaAsset mediaAsset);

    /**
     * 按用户ID查询记录列表。
     */
    List<MediaAsset> listByOwnerUserId(Long ownerUserId, int limit);
}
