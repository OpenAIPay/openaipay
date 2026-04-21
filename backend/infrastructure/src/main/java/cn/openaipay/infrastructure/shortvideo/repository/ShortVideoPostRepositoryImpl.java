package cn.openaipay.infrastructure.shortvideo.repository;

import cn.openaipay.domain.shortvideo.model.FeedCursor;
import cn.openaipay.domain.shortvideo.model.ShortVideoPost;
import cn.openaipay.domain.shortvideo.repository.ShortVideoPostRepository;
import cn.openaipay.infrastructure.shortvideo.dataobject.ShortVideoPostDO;
import cn.openaipay.infrastructure.shortvideo.mapper.ShortVideoPostMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * 短视频内容仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@Repository
public class ShortVideoPostRepositoryImpl implements ShortVideoPostRepository {

    /** 持久化接口。 */
    private final ShortVideoPostMapper shortVideoPostMapper;

    public ShortVideoPostRepositoryImpl(ShortVideoPostMapper shortVideoPostMapper) {
        this.shortVideoPostMapper = shortVideoPostMapper;
    }

    /**
     * 按视频标识查询。
     */
    @Override
    public Optional<ShortVideoPost> findByVideoId(String videoId) {
        return shortVideoPostMapper.findByVideoId(videoId).map(this::toDomain);
    }

    /**
     * 查询默认公开信息流。
     */
    @Override
    public List<ShortVideoPost> listPublicFeed(FeedCursor cursor, int limit) {
        Integer lastFeedPriority = cursor == null ? null : cursor.lastFeedPriority();
        Long lastId = cursor == null ? null : cursor.lastSequenceId();
        return shortVideoPostMapper.listPublicFeed(lastFeedPriority, lastId, limit)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private ShortVideoPost toDomain(ShortVideoPostDO entity) {
        return new ShortVideoPost(
                entity.getId(),
                entity.getVideoId(),
                entity.getCreatorUserId(),
                entity.getCaption(),
                entity.getCoverMediaId(),
                entity.getPlaybackMediaId(),
                entity.getDurationMs(),
                entity.getAspectRatio(),
                entity.getPublishStatus(),
                entity.getVisibilityStatus(),
                entity.getFeedPriority(),
                entity.getPublishedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
