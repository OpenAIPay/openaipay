package cn.openaipay.application.shortvideo.service;

import cn.openaipay.application.shortvideo.dto.ShortVideoFeedPageDTO;
import cn.openaipay.application.shortvideo.query.ListShortVideoFeedQuery;

/**
 * 短视频信息流应用服务。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public interface ShortVideoFeedService {

    /**
     * 查询当前用户可见的短视频信息流。
     */
    ShortVideoFeedPageDTO listFeed(Long userId, ListShortVideoFeedQuery query);
}
