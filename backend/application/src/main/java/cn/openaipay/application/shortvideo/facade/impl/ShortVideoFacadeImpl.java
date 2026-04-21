package cn.openaipay.application.shortvideo.facade.impl;

import cn.openaipay.application.shortvideo.command.CreateShortVideoCommentCommand;
import cn.openaipay.application.shortvideo.command.FavoriteShortVideoCommand;
import cn.openaipay.application.shortvideo.command.LikeShortVideoCommentCommand;
import cn.openaipay.application.shortvideo.command.LikeShortVideoCommand;
import cn.openaipay.application.shortvideo.dto.ShortVideoCommentDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoCommentLikeDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoCommentPageDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoEngagementDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoFeedPageDTO;
import cn.openaipay.application.shortvideo.facade.ShortVideoFacade;
import cn.openaipay.application.shortvideo.query.ListShortVideoCommentRepliesQuery;
import cn.openaipay.application.shortvideo.query.ListShortVideoFeedQuery;
import cn.openaipay.application.shortvideo.query.ListShortVideoCommentsQuery;
import cn.openaipay.application.shortvideo.service.ShortVideoCommentService;
import cn.openaipay.application.shortvideo.service.ShortVideoEngagementService;
import cn.openaipay.application.shortvideo.service.ShortVideoFeedService;
import org.springframework.stereotype.Service;

/**
 * 短视频门面实现。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@Service
public class ShortVideoFacadeImpl implements ShortVideoFacade {

    /** 信息流应用服务。 */
    private final ShortVideoFeedService shortVideoFeedService;
    /** 互动应用服务。 */
    private final ShortVideoEngagementService shortVideoEngagementService;
    /** 评论应用服务。 */
    private final ShortVideoCommentService shortVideoCommentService;

    public ShortVideoFacadeImpl(ShortVideoFeedService shortVideoFeedService,
                                ShortVideoEngagementService shortVideoEngagementService,
                                ShortVideoCommentService shortVideoCommentService) {
        this.shortVideoFeedService = shortVideoFeedService;
        this.shortVideoEngagementService = shortVideoEngagementService;
        this.shortVideoCommentService = shortVideoCommentService;
    }

    /**
     * 查询短视频信息流。
     */
    @Override
    public ShortVideoFeedPageDTO listFeed(Long userId, ListShortVideoFeedQuery query) {
        return shortVideoFeedService.listFeed(userId, query);
    }

    /**
     * 点赞视频。
     */
    @Override
    public ShortVideoEngagementDTO like(Long userId, LikeShortVideoCommand command) {
        return shortVideoEngagementService.like(userId, command);
    }

    /**
     * 取消点赞。
     */
    @Override
    public ShortVideoEngagementDTO unlike(Long userId, LikeShortVideoCommand command) {
        return shortVideoEngagementService.unlike(userId, command);
    }

    /**
     * 收藏视频。
     */
    @Override
    public ShortVideoEngagementDTO favorite(Long userId, FavoriteShortVideoCommand command) {
        return shortVideoEngagementService.favorite(userId, command);
    }

    /**
     * 取消收藏。
     */
    @Override
    public ShortVideoEngagementDTO unfavorite(Long userId, FavoriteShortVideoCommand command) {
        return shortVideoEngagementService.unfavorite(userId, command);
    }

    /**
     * 查询评论列表。
     */
    @Override
    public ShortVideoCommentPageDTO listComments(Long userId, ListShortVideoCommentsQuery query) {
        return shortVideoCommentService.listComments(userId, query);
    }

    /**
     * 查询评论回复列表。
     */
    @Override
    public ShortVideoCommentPageDTO listReplies(Long userId, ListShortVideoCommentRepliesQuery query) {
        return shortVideoCommentService.listReplies(userId, query);
    }

    /**
     * 发布评论。
     */
    @Override
    public ShortVideoCommentDTO createComment(Long userId, CreateShortVideoCommentCommand command) {
        return shortVideoCommentService.createComment(userId, command);
    }

    /**
     * 点赞评论。
     */
    @Override
    public ShortVideoCommentLikeDTO likeComment(Long userId, LikeShortVideoCommentCommand command) {
        return shortVideoCommentService.likeComment(userId, command);
    }

    /**
     * 取消点赞评论。
     */
    @Override
    public ShortVideoCommentLikeDTO unlikeComment(Long userId, LikeShortVideoCommentCommand command) {
        return shortVideoCommentService.unlikeComment(userId, command);
    }
}
