package cn.openaipay.infrastructure.shortvideo.mapper;

import cn.openaipay.infrastructure.common.persistence.BaseMapper;
import cn.openaipay.infrastructure.shortvideo.dataobject.ShortVideoCommentLikeDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短视频评论点赞持久化接口。
 *
 * @author: tenggk.ai
 * @date: 2026/04/03
 */
@Mapper
public interface ShortVideoCommentLikeMapper extends BaseMapper<ShortVideoCommentLikeDO> {

    /**
     * 按用户和评论查询。
     */
    default Optional<ShortVideoCommentLikeDO> findByUserIdAndCommentId(Long userId, String commentId) {
        QueryWrapper<ShortVideoCommentLikeDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("comment_id", commentId);
        wrapper.last("LIMIT 1");
        return Optional.ofNullable(selectOne(wrapper));
    }

    /**
     * 批量按用户和评论查询。
     */
    default List<ShortVideoCommentLikeDO> findByUserIdAndCommentIds(Long userId, Collection<String> commentIds) {
        if (userId == null || userId <= 0 || commentIds == null || commentIds.isEmpty()) {
            return List.of();
        }
        QueryWrapper<ShortVideoCommentLikeDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.in("comment_id", commentIds);
        return selectList(wrapper);
    }

    /**
     * 删除用户对评论的点赞关系。
     */
    default int deleteByUserIdAndCommentId(Long userId, String commentId) {
        QueryWrapper<ShortVideoCommentLikeDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("comment_id", commentId);
        return delete(wrapper);
    }
}
