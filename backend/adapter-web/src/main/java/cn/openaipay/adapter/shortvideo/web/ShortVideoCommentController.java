package cn.openaipay.adapter.shortvideo.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.security.UserRequestContext;
import cn.openaipay.adapter.shortvideo.web.request.CreateShortVideoCommentRequest;
import cn.openaipay.adapter.shortvideo.web.response.ShortVideoCommentLikeResponse;
import cn.openaipay.adapter.shortvideo.web.response.ShortVideoCommentResponse;
import cn.openaipay.application.shortvideo.command.CreateShortVideoCommentCommand;
import cn.openaipay.application.shortvideo.command.LikeShortVideoCommentCommand;
import cn.openaipay.application.shortvideo.facade.ShortVideoFacade;
import cn.openaipay.application.shortvideo.query.ListShortVideoCommentRepliesQuery;
import cn.openaipay.application.shortvideo.query.ListShortVideoCommentsQuery;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短视频评论控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@RestController
@RequestMapping("/api/short-video")
public class ShortVideoCommentController {

    /** 日志。 */
    private static final Logger log = LoggerFactory.getLogger(ShortVideoCommentController.class);

    /** 短视频门面。 */
    private final ShortVideoFacade shortVideoFacade;

    public ShortVideoCommentController(ShortVideoFacade shortVideoFacade) {
        this.shortVideoFacade = shortVideoFacade;
    }

    /**
     * 查询评论列表。
     */
    @GetMapping("/videos/{videoId}/comments")
    public ApiResponse<ShortVideoCommentResponse.CommentPageResponse> listComments(
            @RequestAttribute(UserRequestContext.ATTR_USER_ID) Long currentUserId,
            @PathVariable("videoId") String videoId,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "limit", required = false) Integer limit) {
        ShortVideoCommentResponse.CommentPageResponse response = ShortVideoCommentResponse.CommentPageResponse.from(
                shortVideoFacade.listComments(currentUserId, new ListShortVideoCommentsQuery(videoId, cursor, limit))
        );
        log.info(
                "short video comments served, userId={}, videoId={}, cursor={}, limit={}, itemCount={}, hasMore={}",
                currentUserId,
                videoId,
                cursor,
                limit,
                response.items().size(),
                response.hasMore()
        );
        return ApiResponse.success(response);
    }

    /**
     * 查询评论回复列表。
     */
    @GetMapping("/comments/{commentId}/replies")
    public ApiResponse<ShortVideoCommentResponse.CommentPageResponse> listReplies(
            @RequestAttribute(UserRequestContext.ATTR_USER_ID) Long currentUserId,
            @PathVariable("commentId") String commentId,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "limit", required = false) Integer limit) {
        ShortVideoCommentResponse.CommentPageResponse response = ShortVideoCommentResponse.CommentPageResponse.from(
                shortVideoFacade.listReplies(currentUserId, new ListShortVideoCommentRepliesQuery(commentId, cursor, limit))
        );
        log.info(
                "short video comment replies served, userId={}, commentId={}, cursor={}, limit={}, itemCount={}, hasMore={}",
                currentUserId,
                commentId,
                cursor,
                limit,
                response.items().size(),
                response.hasMore()
        );
        return ApiResponse.success(response);
    }

    /**
     * 发布评论。
     */
    @PostMapping("/videos/{videoId}/comments")
    public ApiResponse<ShortVideoCommentResponse> createComment(
            @RequestAttribute(UserRequestContext.ATTR_USER_ID) Long currentUserId,
            @PathVariable("videoId") String videoId,
            @Valid @RequestBody CreateShortVideoCommentRequest request) {
        ShortVideoCommentResponse response = ShortVideoCommentResponse.from(
                shortVideoFacade.createComment(
                        currentUserId,
                        new CreateShortVideoCommentCommand(
                                videoId,
                                request.parentCommentId(),
                                request.content(),
                                request.imageMediaId()
                        )
                )
        );
        log.info(
                "short video comment created, userId={}, videoId={}, commentId={}, parentCommentId={}, hasImage={}, contentLength={}",
                currentUserId,
                videoId,
                response == null ? null : response.commentId(),
                request.parentCommentId(),
                request.imageMediaId() != null && !request.imageMediaId().trim().isEmpty(),
                request.content() == null ? 0 : request.content().trim().length()
        );
        return ApiResponse.success(response);
    }

    /**
     * 点赞评论。
     */
    @PostMapping("/comments/{commentId}/like")
    public ApiResponse<ShortVideoCommentLikeResponse> likeComment(
            @RequestAttribute(UserRequestContext.ATTR_USER_ID) Long currentUserId,
            @PathVariable("commentId") String commentId) {
        ShortVideoCommentLikeResponse response = ShortVideoCommentLikeResponse.from(
                shortVideoFacade.likeComment(currentUserId, new LikeShortVideoCommentCommand(commentId))
        );
        log.info(
                "short video comment liked, userId={}, commentId={}, liked={}, likeCount={}",
                currentUserId,
                commentId,
                response == null ? null : response.liked(),
                response == null ? null : response.likeCount()
        );
        return ApiResponse.success(response);
    }

    /**
     * 取消点赞评论。
     */
    @DeleteMapping("/comments/{commentId}/like")
    public ApiResponse<ShortVideoCommentLikeResponse> unlikeComment(
            @RequestAttribute(UserRequestContext.ATTR_USER_ID) Long currentUserId,
            @PathVariable("commentId") String commentId) {
        ShortVideoCommentLikeResponse response = ShortVideoCommentLikeResponse.from(
                shortVideoFacade.unlikeComment(currentUserId, new LikeShortVideoCommentCommand(commentId))
        );
        log.info(
                "short video comment unliked, userId={}, commentId={}, liked={}, likeCount={}",
                currentUserId,
                commentId,
                response == null ? null : response.liked(),
                response == null ? null : response.likeCount()
        );
        return ApiResponse.success(response);
    }
}
