package cn.openaipay.application.shortvideo.command;

/**
 * 短视频评论点赞命令。
 *
 * @author: tenggk.ai
 * @date: 2026/04/03
 */
public record LikeShortVideoCommentCommand(
        /** 评论标识。 */
        String commentId
) {
}
