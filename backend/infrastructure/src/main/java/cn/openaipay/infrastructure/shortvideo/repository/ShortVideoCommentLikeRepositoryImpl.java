package cn.openaipay.infrastructure.shortvideo.repository;

import cn.openaipay.domain.shortvideo.repository.ShortVideoCommentLikeRepository;
import cn.openaipay.infrastructure.shortvideo.dataobject.ShortVideoCommentLikeDO;
import cn.openaipay.infrastructure.shortvideo.mapper.ShortVideoCommentLikeMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 短视频评论点赞关系仓储实现。
 *
 * @author: tenggk.ai
 * @date: 2026/04/03
 */
@Repository
public class ShortVideoCommentLikeRepositoryImpl implements ShortVideoCommentLikeRepository {

    /** 持久化接口。 */
    private final ShortVideoCommentLikeMapper shortVideoCommentLikeMapper;

    public ShortVideoCommentLikeRepositoryImpl(ShortVideoCommentLikeMapper shortVideoCommentLikeMapper) {
        this.shortVideoCommentLikeMapper = shortVideoCommentLikeMapper;
    }

    /**
     * 判断用户是否点赞过评论。
     */
    @Override
    public boolean existsByUserIdAndCommentId(Long userId, String commentId) {
        if (userId == null || userId <= 0 || commentId == null || commentId.isBlank()) {
            return false;
        }
        return shortVideoCommentLikeMapper.findByUserIdAndCommentId(userId, commentId.trim()).isPresent();
    }

    /**
     * 批量查询用户点赞过的评论标识。
     */
    @Override
    public Set<String> findLikedCommentIds(Long userId, List<String> commentIds) {
        Set<String> result = new LinkedHashSet<>();
        shortVideoCommentLikeMapper.findByUserIdAndCommentIds(userId, commentIds)
                .forEach(entity -> result.add(entity.getCommentId()));
        return result;
    }

    /**
     * 保存点赞关系。
     */
    @Override
    @Transactional
    public void saveLike(Long userId, String commentId) {
        if (existsByUserIdAndCommentId(userId, commentId)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        ShortVideoCommentLikeDO entity = new ShortVideoCommentLikeDO();
        entity.setUserId(userId);
        entity.setCommentId(commentId == null ? null : commentId.trim());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        shortVideoCommentLikeMapper.save(entity);
    }

    /**
     * 删除点赞关系。
     */
    @Override
    @Transactional
    public void deleteLike(Long userId, String commentId) {
        if (userId == null || userId <= 0 || commentId == null || commentId.isBlank()) {
            return;
        }
        shortVideoCommentLikeMapper.deleteByUserIdAndCommentId(userId, commentId.trim());
    }
}
