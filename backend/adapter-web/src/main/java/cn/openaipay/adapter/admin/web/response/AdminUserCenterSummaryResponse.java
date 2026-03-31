package cn.openaipay.adapter.admin.web.response;

/**
 * 用户中心统计响应模型
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record AdminUserCenterSummaryResponse(
        /** 总用户次数 */
        long totalUserCount,
        /** 用户次数 */
        long activeUserCount,
        /** 用户次数 */
        long frozenUserCount,
        /** 用户次数 */
        long closedUserCount,
        /** 钱包用户次数 */
        long walletActiveUserCount,
        /** 信用用户次数 */
        long creditNormalUserCount
) {
}
