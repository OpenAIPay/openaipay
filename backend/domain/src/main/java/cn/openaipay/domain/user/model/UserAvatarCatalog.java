package cn.openaipay.domain.user.model;

import java.util.Locale;

/**
 * 用户头像目录
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public final class UserAvatarCatalog {

    /** 聊天头像素材数量。 */
    private static final int CHAT_AVATAR_COUNT = 60;

    private UserAvatarCatalog() {
    }

    /**
     * 处理URL信息。
     */
    public static String defaultWechatStyleAvatarUrl(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        int avatarIndex = (int) (Math.floorMod(userId, CHAT_AVATAR_COUNT) + 1);
        return String.format(Locale.ROOT, "/demo-avatar/chat-avatar-%02d.jpg", avatarIndex);
    }
}
