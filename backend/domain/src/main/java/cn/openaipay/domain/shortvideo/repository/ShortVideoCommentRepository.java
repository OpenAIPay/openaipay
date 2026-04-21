package cn.openaipay.domain.shortvideo.repository;

import cn.openaipay.domain.shortvideo.model.VideoComment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 短视频评论仓储接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public interface ShortVideoCommentRepository {

    /**
     * 查询指定评论。
     */
    Optional<VideoComment> findByCommentId(String commentId);

    /**
     * 查询指定视频的有效顶级评论。
     */
    List<VideoComment> listActiveTopLevelByVideoId(String videoId, LocalDateTime lastCreatedAt, Long lastId, int limit);

    /**
     * 查询指定根评论下的有效回复。
     */
    List<VideoComment> listActiveRepliesByRootCommentId(String rootCommentId, LocalDateTime lastCreatedAt, Long lastId, int limit);

    /**
     * 保存评论。
     */
    VideoComment save(VideoComment videoComment);

    /**
     * 调整评论点赞数。
     */
    void adjustLikeCount(String commentId, long delta);

    /**
     * 调整评论回复数。
     */
    void adjustReplyCount(String commentId, long delta);
}
