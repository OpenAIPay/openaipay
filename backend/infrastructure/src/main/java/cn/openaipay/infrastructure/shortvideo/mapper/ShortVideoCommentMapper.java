package cn.openaipay.infrastructure.shortvideo.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.shortvideo.dataobject.ShortVideoCommentDO;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短视频评论持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@Mapper
public interface ShortVideoCommentMapper extends BaseMapper<ShortVideoCommentDO> {

    /**
     * 按评论标识查询。
     */
    default Optional<ShortVideoCommentDO> findByCommentId(String commentId) {
        QueryWrapper<ShortVideoCommentDO> wrapper = new QueryWrapper<>();
        wrapper.eq("comment_id", commentId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 查询视频下的有效顶级评论。
     */
    default List<ShortVideoCommentDO> listActiveTopLevelByVideoId(String videoId,
                                                                  LocalDateTime lastCreatedAt,
                                                                  Long lastId,
                                                                  int limit) {
        QueryWrapper<ShortVideoCommentDO> wrapper = new QueryWrapper<>();
        wrapper.eq("video_id", videoId);
        wrapper.eq("status", "ACTIVE");
        wrapper.isNull("parent_comment_id");
        if (lastCreatedAt != null && lastId != null && lastId > 0) {
            wrapper.and(nested -> nested.lt("created_at", lastCreatedAt)
                    .or()
                    .eq("created_at", lastCreatedAt)
                    .lt("id", lastId));
        }
        wrapper.orderByDesc("created_at");
        wrapper.orderByDesc("id");
        wrapper.last("LIMIT " + Math.max(1, limit));
        return selectList(wrapper);
    }

    /**
     * 查询根评论下的有效回复。
     */
    default List<ShortVideoCommentDO> listActiveRepliesByRootCommentId(String rootCommentId,
                                                                       LocalDateTime lastCreatedAt,
                                                                       Long lastId,
                                                                       int limit) {
        QueryWrapper<ShortVideoCommentDO> wrapper = new QueryWrapper<>();
        wrapper.eq("root_comment_id", rootCommentId);
        wrapper.eq("status", "ACTIVE");
        wrapper.isNotNull("parent_comment_id");
        if (lastCreatedAt != null && lastId != null && lastId > 0) {
            wrapper.and(nested -> nested.gt("created_at", lastCreatedAt)
                    .or()
                    .eq("created_at", lastCreatedAt)
                    .gt("id", lastId));
        }
        wrapper.orderByAsc("created_at");
        wrapper.orderByAsc("id");
        wrapper.last("LIMIT " + Math.max(1, limit));
        return selectList(wrapper);
    }

    /**
     * 调整评论点赞数。
     */
    default int adjustLikeCount(String commentId, long delta) {
        if (commentId == null || commentId.isBlank() || delta == 0L) {
            return 0;
        }
        UpdateWrapper<ShortVideoCommentDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("comment_id", commentId.trim());
        if (delta > 0L) {
            wrapper.setSql("like_count = like_count + " + delta);
        } else {
            wrapper.setSql("like_count = GREATEST(like_count - " + Math.abs(delta) + ", 0)");
        }
        return update(null, wrapper);
    }

    /**
     * 调整评论回复数。
     */
    default int adjustReplyCount(String commentId, long delta) {
        if (commentId == null || commentId.isBlank() || delta == 0L) {
            return 0;
        }
        UpdateWrapper<ShortVideoCommentDO> wrapper = new UpdateWrapper<>();
        wrapper.eq("comment_id", commentId.trim());
        if (delta > 0L) {
            wrapper.setSql("reply_count = reply_count + " + delta);
        } else {
            wrapper.setSql("reply_count = GREATEST(reply_count - " + Math.abs(delta) + ", 0)");
        }
        return update(null, wrapper);
    }
}
