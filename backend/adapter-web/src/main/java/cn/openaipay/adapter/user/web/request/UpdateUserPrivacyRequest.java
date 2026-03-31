package cn.openaipay.adapter.user.web.request;
/**
 * 更新用户隐私请求参数
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */

public record UpdateUserPrivacyRequest(
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
