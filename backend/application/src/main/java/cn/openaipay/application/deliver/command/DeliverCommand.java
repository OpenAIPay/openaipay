package cn.openaipay.application.deliver.command;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 投放请求命令。
 *
 * @author: tenggk.ai
 * @date: 2026/03/10
 */
public record DeliverCommand(
        /** 位置编码列表 */
        List<String> positionCodeList,
        /** 业务ID */
        String clientId,
        /** 用户ID */
        Long userId,
        /** 场景编码 */
        String sceneCode,
        /** 渠道信息 */
        String channel,
        /** 用户信息 */
        Set<String> userTags,
        /** 请求时间 */
        LocalDateTime requestTime
) {

    public DeliverCommand {
        positionCodeList = positionCodeList == null ? List.of() : positionCodeList.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(String::trim)
                .toList();
        userTags = userTags == null ? Set.of() : userTags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(String::trim)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }
}
