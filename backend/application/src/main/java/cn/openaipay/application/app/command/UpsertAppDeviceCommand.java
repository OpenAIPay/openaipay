package cn.openaipay.application.app.command;

import java.util.List;

/**
 * 上报设备安装/启动信息命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/12
 */
public record UpsertAppDeviceCommand(
        /** 设备ID */
        String deviceId,
        /** 应用编码 */
        String appCode,
        /** 客户端ID列表 */
        List<String> clientIds,
        /** 设备品牌 */
        String deviceBrand,
        /** 系统版本号 */
        String osVersion,
        /** 当前版本编码 */
        String currentVersionCode,
        /** 当前版本号 */
        String currentVersionNo,
        /** 是否已启动标记 */
        Boolean started
) {
}
