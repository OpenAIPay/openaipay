package cn.openaipay.application.media.command;

/**
 * 上传图片命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record UploadImageCommand(
        /** 所属用户ID */
        Long ownerUserId,
        /** 原始名称 */
        String originalName,
        /** 业务类型 */
        String mimeType,
        /** 二进制内容 */
        byte[] bytes
) {
}
