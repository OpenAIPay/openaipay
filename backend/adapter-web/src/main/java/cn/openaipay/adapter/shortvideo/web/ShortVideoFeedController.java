package cn.openaipay.adapter.shortvideo.web;

import cn.openaipay.adapter.common.ApiResponse;
import cn.openaipay.adapter.security.UserRequestContext;
import cn.openaipay.adapter.shortvideo.web.response.ShortVideoFeedResponse;
import cn.openaipay.application.shortvideo.facade.ShortVideoFacade;
import cn.openaipay.application.shortvideo.query.ListShortVideoFeedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短视频信息流控制器。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
@RestController
@RequestMapping("/api/short-video")
public class ShortVideoFeedController {

    /** 日志。 */
    private static final Logger log = LoggerFactory.getLogger(ShortVideoFeedController.class);

    /** 短视频门面。 */
    private final ShortVideoFacade shortVideoFacade;

    public ShortVideoFeedController(ShortVideoFacade shortVideoFacade) {
        this.shortVideoFacade = shortVideoFacade;
    }

    /**
     * 拉取短视频信息流。
     */
    @GetMapping("/feed")
    public ApiResponse<ShortVideoFeedResponse> listFeed(
            @RequestAttribute(UserRequestContext.ATTR_USER_ID) Long currentUserId,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "limit", required = false) Integer limit) {
        ShortVideoFeedResponse response = ShortVideoFeedResponse.from(
                shortVideoFacade.listFeed(currentUserId, new ListShortVideoFeedQuery(cursor, limit))
        );
        log.info(
                "short video feed served, userId={}, cursor={}, limit={}, itemCount={}, hasMore={}",
                currentUserId,
                cursor,
                limit,
                response.items().size(),
                response.hasMore()
        );
        return ApiResponse.success(response);
    }
}
