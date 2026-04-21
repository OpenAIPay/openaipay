package cn.openaipay.adapter.shortvideo.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.security.UserRequestContext;
import cn.openaipay.adapter.shortvideo.web.response.ShortVideoEngagementResponse;
import cn.openaipay.application.shortvideo.command.FavoriteShortVideoCommand;
import cn.openaipay.application.shortvideo.command.LikeShortVideoCommand;
import cn.openaipay.application.shortvideo.facade.ShortVideoFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短视频互动控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@RestController
@RequestMapping("/api/short-video")
public class ShortVideoEngagementController {

    /** 日志。 */
    private static final Logger log = LoggerFactory.getLogger(ShortVideoEngagementController.class);

    /** 短视频门面。 */
    private final ShortVideoFacade shortVideoFacade;

    public ShortVideoEngagementController(ShortVideoFacade shortVideoFacade) {
        this.shortVideoFacade = shortVideoFacade;
    }

    /**
     * 点赞视频。
     */
    @PostMapping("/videos/{videoId}/like")
    public ApiResponse<ShortVideoEngagementResponse> like(@RequestAttribute(UserRequestContext.ATTR_USER_ID) Long currentUserId,
                                                          @PathVariable("videoId") String videoId) {
        ShortVideoEngagementResponse response = ShortVideoEngagementResponse.from(
                shortVideoFacade.like(currentUserId, new LikeShortVideoCommand(videoId))
        );
        logEngagementAction("LIKE", currentUserId, videoId, response);
        return ApiResponse.success(response);
    }

    /**
     * 取消点赞。
     */
    @DeleteMapping("/videos/{videoId}/like")
    public ApiResponse<ShortVideoEngagementResponse> unlike(@RequestAttribute(UserRequestContext.ATTR_USER_ID) Long currentUserId,
                                                            @PathVariable("videoId") String videoId) {
        ShortVideoEngagementResponse response = ShortVideoEngagementResponse.from(
                shortVideoFacade.unlike(currentUserId, new LikeShortVideoCommand(videoId))
        );
        logEngagementAction("UNLIKE", currentUserId, videoId, response);
        return ApiResponse.success(response);
    }

    /**
     * 收藏视频。
     */
    @PostMapping("/videos/{videoId}/favorite")
    public ApiResponse<ShortVideoEngagementResponse> favorite(@RequestAttribute(UserRequestContext.ATTR_USER_ID) Long currentUserId,
                                                              @PathVariable("videoId") String videoId) {
        ShortVideoEngagementResponse response = ShortVideoEngagementResponse.from(
                shortVideoFacade.favorite(currentUserId, new FavoriteShortVideoCommand(videoId))
        );
        logEngagementAction("FAVORITE", currentUserId, videoId, response);
        return ApiResponse.success(response);
    }

    /**
     * 取消收藏。
     */
    @DeleteMapping("/videos/{videoId}/favorite")
    public ApiResponse<ShortVideoEngagementResponse> unfavorite(@RequestAttribute(UserRequestContext.ATTR_USER_ID) Long currentUserId,
                                                                @PathVariable("videoId") String videoId) {
        ShortVideoEngagementResponse response = ShortVideoEngagementResponse.from(
                shortVideoFacade.unfavorite(currentUserId, new FavoriteShortVideoCommand(videoId))
        );
        logEngagementAction("UNFAVORITE", currentUserId, videoId, response);
        return ApiResponse.success(response);
    }

    private void logEngagementAction(String action,
                                     Long currentUserId,
                                     String videoId,
                                     ShortVideoEngagementResponse response) {
        log.info(
                "short video engagement updated, action={}, userId={}, videoId={}, liked={}, favorited={}, likeCount={}, favoriteCount={}, commentCount={}",
                action,
                currentUserId,
                videoId,
                response.liked(),
                response.favorited(),
                response.likeCount(),
                response.favoriteCount(),
                response.commentCount()
        );
    }
}
