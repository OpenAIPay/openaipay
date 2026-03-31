package cn.openaipay.application.audience.command;

/**
 * 保存人群定义命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/20
 */
public record UpsertAudienceSegmentCommand(
        /** 人群编码 */
        String segmentCode,
        /** 人群名称 */
        String segmentName,
        /** 人群描述 */
        String description,
        /** 场景编码 */
        String sceneCode,
        /** 人群状态 */
        String status
) {
}
