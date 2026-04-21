package cn.openaipay.domain.shortvideo.repository;

import java.util.List;
import java.util.Set;

/**
 * 短视频评论点赞关系仓储。
 *
 * @author: tenggk.ai
 * @date: 2026/04/03
 */
public interface ShortVideoCommentLikeRepository {

    /**
     * 判断用户是否点赞过评论。
     */
    boolean existsByUserIdAndCommentId(Long userId, String commentId);

    /**
     * 批量查询用户点赞过的评论标识。
     */
    Set<String> findLikedCommentIds(Long userId, List<String> commentIds);

    /**
     * 保存点赞关系。
     */
    void saveLike(Long userId, String commentId);

    /**
     * 删除点赞关系。
     */
    void deleteLike(Long userId, String commentId);
}
