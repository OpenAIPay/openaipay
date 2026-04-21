package cn.openaipay.application.shortvideo.command;

/**
 * 发布评论命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public record CreateShortVideoCommentCommand(
        /** 视频标识。 */
        String videoId,
        /** 父评论标识。 */
        String parentCommentId,
        /** 评论内容。 */
        String content,
        /** 图片媒体标识。 */
        String imageMediaId
) {
}
