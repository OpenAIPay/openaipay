package cn.openaipay.application.user.command;
/**
 * 更新用户隐私命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record UpdateUserPrivacyCommand(
        /** 用户ID */
        Long userId,
        /** BY手机号 */
        Boolean allowSearchByMobile,
        /** BYUID */
        Boolean allowSearchByAipayUid,
        /** 业务名称 */
        Boolean hideRealName,
        /** 个性化推荐开关 */
        Boolean personalizedRecommendationEnabled
) {
}
