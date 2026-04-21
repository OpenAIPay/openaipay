package cn.openaipay.application.shortvideo.dto;

/**
 * 短视频作者摘要。
 *
 * @author: tenggk.ai
 * @date: 2026/03/31
 */
public record ShortVideoAuthorDTO(
        /** 用户ID。 */
        Long userId,
        /** 昵称。 */
        String nickname,
        /** 头像地址。 */
        String avatarUrl
) {
}
