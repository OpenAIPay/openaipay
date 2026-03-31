package cn.openaipay.application.contact.command;

/**
 * 更新联系人备注命令
 *
 * @author: tenggk.ai
 * @date: 2026/03/04
 */
public record UpdateContactRemarkCommand(
        /** 所属用户ID */
        Long ownerUserId,
        /** 用户ID */
        Long friendUserId,
        /** 备注 */
        String remark
) {
}
