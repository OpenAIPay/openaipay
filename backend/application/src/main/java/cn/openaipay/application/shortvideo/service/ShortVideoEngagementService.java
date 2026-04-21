package cn.openaipay.application.shortvideo.service;

import cn.openaipay.application.shortvideo.command.FavoriteShortVideoCommand;
import cn.openaipay.application.shortvideo.command.LikeShortVideoCommand;
import cn.openaipay.application.shortvideo.dto.ShortVideoEngagementDTO;

/**
 * 短视频互动应用服务。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public interface ShortVideoEngagementService {

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
}
