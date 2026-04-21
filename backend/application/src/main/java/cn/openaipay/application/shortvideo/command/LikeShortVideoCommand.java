package cn.openaipay.application.shortvideo.command;

/**
 * 点赞视频命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public record LikeShortVideoCommand(
        /** 视频标识。 */
        String videoId
) {
}
