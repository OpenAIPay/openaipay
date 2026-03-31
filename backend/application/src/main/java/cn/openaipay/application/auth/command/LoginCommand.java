package cn.openaipay.application.auth.command;

import java.util.List;

/**
 * 登录命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record LoginCommand(
        /** 登录账号ID */
        String loginId,
        /** 当前稳定设备ID */
        String deviceId,
        /** 旧设备标识列表 */
        List<String> legacyDeviceIds
) {
}
