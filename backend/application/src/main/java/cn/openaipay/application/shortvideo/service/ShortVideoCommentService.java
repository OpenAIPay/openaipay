package cn.openaipay.application.shortvideo.service;

import cn.openaipay.application.shortvideo.command.CreateShortVideoCommentCommand;
import cn.openaipay.application.shortvideo.command.LikeShortVideoCommentCommand;
import cn.openaipay.application.shortvideo.dto.ShortVideoCommentDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoCommentLikeDTO;
import cn.openaipay.application.shortvideo.dto.ShortVideoCommentPageDTO;
import cn.openaipay.application.shortvideo.query.ListShortVideoCommentRepliesQuery;
import cn.openaipay.application.shortvideo.query.ListShortVideoCommentsQuery;

/**
 * 短视频评论应用服务。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public interface ShortVideoCommentService {

    /**
     * 查询评论列表。
     */
    ShortVideoCommentPageDTO listComments(Long userId, ListShortVideoCommentsQuery query);

    /**
     * 查询回复列表。
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
