package cn.openaipay.application.shortvideo.command;

/**
 * 收藏视频命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public record FavoriteShortVideoCommand(
        /** 视频标识。 */
        String videoId
) {
}
