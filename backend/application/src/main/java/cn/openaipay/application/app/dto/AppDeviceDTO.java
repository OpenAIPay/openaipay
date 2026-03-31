package cn.openaipay.application.app.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * App 设备 DTO。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public record AppDeviceDTO(
        /** 设备ID */
        String deviceId,
        /** 应用编码 */
        String appCode,
        /** 客户端ID列表 */
        List<String> clientIds,
        /** 状态编码 */
        String status,
        /** 业务时间 */
        LocalDateTime installedAt,
        /** 开始时间 */
        LocalDateTime startedAt,
        /** 最近一次时间 */
        LocalDateTime lastOpenedAt,
        /** 应用版本ID */
        Long currentAppVersionId,
        /** 当前版本编码 */
        String currentVersionCode,
        /** 当前版本号 */
        String currentVersionNo,
        /** IOSID */
        Long currentIosPackageId,
        /** IOS编码 */
        String currentIosCode,
        /** 应用更新时间 */
        LocalDateTime appUpdatedAt,
        /** 设备品牌 */
        String deviceBrand,
        /** 系统版本号 */
        String osVersion,
        /** 用户ID */
        Long userId,
        /** 爱支付UID */
        String aipayUid,
        /** 登录账号ID */
        String loginId,
        /** 业务状态 */
        String accountStatus,
        /** KYClevel信息 */
        String kycLevel,
        /** 昵称 */
        String nickname,
        /** 头像地址 */
        String avatarUrl,
        /** 手机号 */
        String mobile,
        /** 业务名称 */
        String maskedRealName,
        /** ID卡单号 */
        String idCardNoMasked,
        /** 业务编码 */
        String countryCode,
        /** 性别 */
        String gender,
        /** 地区 */
        String region,
        /** 最近一次登录时间 */
        LocalDateTime lastLoginAt,
        /** 记录创建时间 */
        LocalDateTime createdAt,
        /** 记录更新时间 */
        LocalDateTime updatedAt
) {
}
