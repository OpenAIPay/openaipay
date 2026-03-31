package cn.openaipay.application.adminuser.dto;

/**
 * 用户中心统计
 *
 * @author: tenggk.ai
 * @date: 2026/03/19
 */
public record AdminUserCenterSummaryDTO(
        /** 总用户数 */
        long totalUserCount,
        /** 正常用户数 */
        long activeUserCount,
        /** 冻结用户数 */
        long frozenUserCount,
        /** 注销用户数 */
        long closedUserCount,
        /** 开通钱包用户数 */
        long walletActiveUserCount,
        /** 开通信用账户用户数 */
        long creditNormalUserCount
) {
}
