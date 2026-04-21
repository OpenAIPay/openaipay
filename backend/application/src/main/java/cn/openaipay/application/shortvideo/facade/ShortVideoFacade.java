package cn.openaipay.application.shortvideo.facade;

import cn.openaipay.application.shortvideo.command.CreateShortVideoCommentCommand;
import cn.openaipay.application.shortvideo.command.FavoriteShortVideoCommand;
import cn.openaipay.application.shortvideo.command.LikeShortVideoCommentCommand;
import cn.openaipay.application.shortvideo.command.LikeShortVideoCommand;
import cn.openaipay.application.shortvideo.dto.ShortVideoCommentDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoCommentLikeDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoCommentPageDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoEngagementDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoFeedPageDTO;
import cn.openaipay.application.shortvideo.query.ListShortVideoCommentRepliesQuery;
import cn.openaipay.application.shortvideo.query.ListShortVideoFeedQuery;
import cn.openaipay.application.shortvideo.query.ListShortVideoCommentsQuery;

/**
 * 短视频门面接口。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public interface ShortVideoFacade {

    /**
     * 查询短视频信息流。
     */
    ShortVideoFeedPageDTO listFeed(Long userId, ListShortVideoFeedQuery query);

    /**
     * 点赞视频。
     */
    ShortVideoEngagementDTO like(Long userId, LikeShortVideoCommand command);

    /**
     * 取消点赞。
     */
    ShortVideoEngagementDTO unlike(Long userId, LikeShortVideoCommand command);

    /**
     * 收藏视频。
     */
    ShortVideoEngagementDTO favorite(Long userId, FavoriteShortVideoCommand command);

    /**
     * 取消收藏。
     */
    ShortVideoEngagementDTO unfavorite(Long userId, FavoriteShortVideoCommand command);

    /**
     * 查询评论列表。
     */
    ShortVideoCommentPageDTO listComments(Long userId, ListShortVideoCommentsQuery query);

    /**
     * 查询评论回复列表。
     */
    ShortVideoCommentPageDTO listReplies(Long userId, ListShortVideoCommentRepliesQuery query);

    /**
     * 发布评论。
     */
    ShortVideoCommentDTO createComment(Long userId, CreateShortVideoCommentCommand command);

    /**
     * 点赞评论。
     */
    ShortVideoCommentLikeDTO likeComment(Long userId, LikeShortVideoCommentCommand command);

    /**
     * 取消点赞评论。
     */
    ShortVideoCommentLikeDTO unlikeComment(Long userId, LikeShortVideoCommentCommand command);
}
