package cn.openaipay.infrastructure.shortvideo.repository;

import cn.openaipay.domain.shortvideo.model.VideoComment;
import cn.openaipay.domain.shortvideo.repository.ShortVideoCommentRepository;
import cn.openaipay.infrastructure.shortvideo.dataobject.ShortVideoCommentDO;
import cn.openaipay.infrastructure.shortvideo.mapper.ShortVideoCommentMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 短视频评论仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@Repository
public class ShortVideoCommentRepositoryImpl implements ShortVideoCommentRepository {

    /** 持久化接口。 */
    private final ShortVideoCommentMapper shortVideoCommentMapper;

    public ShortVideoCommentRepositoryImpl(ShortVideoCommentMapper shortVideoCommentMapper) {
        this.shortVideoCommentMapper = shortVideoCommentMapper;
    }

    /**
     * 查询指定评论。
     */
    @Override
    public Optional<VideoComment> findByCommentId(String commentId) {
        return shortVideoCommentMapper.findByCommentId(commentId).map(this::toDomain);
    }

    /**
     * 查询指定视频的有效顶级评论。
     */
    @Override
    public List<VideoComment> listActiveTopLevelByVideoId(String videoId, LocalDateTime lastCreatedAt, Long lastId, int limit) {
        return shortVideoCommentMapper.listActiveTopLevelByVideoId(videoId, lastCreatedAt, lastId, limit)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * 查询指定根评论下的有效回复。
     */
    @Override
    public List<VideoComment> listActiveRepliesByRootCommentId(String rootCommentId,
                                                               LocalDateTime lastCreatedAt,
                                                               Long lastId,
                                                               int limit) {
        return shortVideoCommentMapper.listActiveRepliesByRootCommentId(rootCommentId, lastCreatedAt, lastId, limit)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * 保存评论。
     */
    @Override
    @Transactional
    public VideoComment save(VideoComment videoComment) {
        ShortVideoCommentDO entity = shortVideoCommentMapper.findByCommentId(videoComment.getCommentId())
                .orElse(new ShortVideoCommentDO());
        LocalDateTime now = LocalDateTime.now();
        entity.setCommentId(videoComment.getCommentId());
        entity.setVideoId(videoComment.getVideoId());
        entity.setParentCommentId(videoComment.getParentCommentId());
        entity.setRootCommentId(videoComment.getRootCommentId());
        entity.setUserId(videoComment.getUserId());
        entity.setContent(videoComment.getContent());
        entity.setImageMediaId(videoComment.getImageMediaId());
        entity.setLikeCount(videoComment.getLikeCount());
        entity.setReplyCount(videoComment.getReplyCount());
        entity.setStatus(videoComment.getStatus());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(videoComment.getCreatedAt() == null ? now : videoComment.getCreatedAt());
        }
        entity.setUpdatedAt(videoComment.getUpdatedAt() == null ? now : videoComment.getUpdatedAt());
        return toDomain(shortVideoCommentMapper.save(entity));
    }

    /**
     * 调整评论点赞数。
     */
    @Override
    @Transactional
    public void adjustLikeCount(String commentId, long delta) {
        shortVideoCommentMapper.adjustLikeCount(commentId, delta);
    }

    /**
     * 调整评论回复数。
     */
    @Override
    @Transactional
    public void adjustReplyCount(String commentId, long delta) {
        shortVideoCommentMapper.adjustReplyCount(commentId, delta);
    }

    private VideoComment toDomain(ShortVideoCommentDO entity) {
        return new VideoComment(
                entity.getId(),
                entity.getCommentId(),
                entity.getVideoId(),
                entity.getParentCommentId(),
                entity.getRootCommentId(),
                entity.getUserId(),
                entity.getContent(),
                entity.getImageMediaId(),
                entity.getLikeCount() == null ? 0L : entity.getLikeCount(),
                entity.getReplyCount() == null ? 0L : entity.getReplyCount(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
